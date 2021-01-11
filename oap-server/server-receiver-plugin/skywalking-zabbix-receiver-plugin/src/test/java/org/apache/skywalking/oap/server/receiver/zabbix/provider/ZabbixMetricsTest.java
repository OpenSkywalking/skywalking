package org.apache.skywalking.oap.server.receiver.zabbix.provider;

import com.google.common.collect.Maps;
import org.apache.skywalking.oap.server.core.CoreModule;
import org.apache.skywalking.oap.server.core.CoreModuleProvider;
import org.apache.skywalking.oap.server.core.analysis.DisableRegister;
import org.apache.skywalking.oap.server.core.analysis.IDManager;
import org.apache.skywalking.oap.server.core.analysis.meter.MeterSystem;
import org.apache.skywalking.oap.server.core.analysis.meter.function.AcceptableValue;
import org.apache.skywalking.oap.server.core.analysis.meter.function.avg.AvgFunction;
import org.apache.skywalking.oap.server.core.analysis.meter.function.avg.AvgHistogramFunction;
import org.apache.skywalking.oap.server.core.analysis.meter.function.avg.AvgHistogramPercentileFunction;
import org.apache.skywalking.oap.server.core.analysis.meter.function.avg.AvgLabeledFunction;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.receiver.zabbix.provider.config.ZabbixConfig;
import org.apache.skywalking.oap.server.receiver.zabbix.provider.config.ZabbixConfigs;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ZabbixMetricsTest extends ZabbixBaseTest {

    protected CoreModuleProvider moduleProvider;
    protected ModuleManager moduleManager;
    protected MeterSystem meterSystem;

    private List<AcceptableValue> values = new ArrayList<>();

    @Override
    public void setupService() throws Throwable {
        moduleProvider = Mockito.mock(CoreModuleProvider.class);
        moduleManager = Mockito.mock(ModuleManager.class);

        // prepare the context
        meterSystem = Mockito.spy(new MeterSystem(moduleManager));
        CoreModule coreModule = Mockito.spy(CoreModule.class);

        // disable meter register
        DisableRegister.INSTANCE.add("meter_agent_system_cpu_load");

        Whitebox.setInternalState(coreModule, "loadedProvider", moduleProvider);
        when(moduleManager.find(CoreModule.NAME)).thenReturn(coreModule);
        when(moduleProvider.getService(MeterSystem.class)).thenReturn(meterSystem);

        // prepare the meter functions
        final HashMap<String, Class> map = Maps.newHashMap();
        map.put("avg", AvgFunction.class);
        map.put("avgLabeled", AvgLabeledFunction.class);
        map.put("avgHistogram", AvgHistogramFunction.class);
        map.put("avgHistogramPercentile", AvgHistogramPercentileFunction.class);
        Whitebox.setInternalState(meterSystem, "functionRegister", map);
        super.setupService();
    }

    @Override
    protected ZabbixMetrics buildZabbixMetrics() throws Exception {
        // Notifies meter system received metric
        doAnswer(invocationOnMock -> {
            values.add(invocationOnMock.getArgument(0, AcceptableValue.class));
            return null;
        }).when(meterSystem).doStreamingCalculation(any());

        // load context
        List<ZabbixConfig> zabbixConfigs = ZabbixConfigs.loadConfigs(ZabbixModuleConfig.CONFIG_PATH, Arrays.asList("agent.yaml"));
        return new ZabbixMetrics(zabbixConfigs, meterSystem);
    }

    @Test
    public void testReceiveMetrics() throws Throwable {
        startupSocketClient();
        // Verify Active Checks
        socketClient.writeZabbixMessage("{\"request\":\"active checks\",\"host\":\"test-01\"}");
        String activeChecksRespData = socketClient.waitAndGetResponsePayload();
        assertZabbixActiveChecksRequest(0, "test-01");
        assertZabbixActiveChecksResponse(activeChecksRespData, "system.cpu.load[all,avg1]", "system.cpu.load[all,avg5]", "system.cpu.load[all,avg15]");

        // Verify Agent data
        socketClient.writeZabbixMessage("{\"request\":\"agent data\",\"session\":\"f32425dc61971760bf791f731931a92e\",\"data\":[" +
            "{\"host\":\"test-01\",\"key\":\"system.cpu.load[all,avg1]\",\"value\":\"1.123\",\"id\":2,\"clock\":1609588563,\"ns\":87682907}," +
            "{\"host\":\"test-01\",\"key\":\"system.cpu.load[all,avg5]\",\"value\":\"2.123\",\"id\":2,\"clock\":1609588563,\"ns\":87682907}," +
            "{\"host\":\"test-01\",\"key\":\"system.cpu.load[all,avg15]\",\"value\":\"3.123\",\"id\":2,\"clock\":1609588563,\"ns\":87682907}" +
            "],\"clock\":1609588568,\"ns\":102244476}");
        String agentDataRespData = socketClient.waitAndGetResponsePayload();
        assertZabbixAgentDataRequest(1, "test-01", "system.cpu.load[all,avg1]", "system.cpu.load[all,avg5]", "system.cpu.load[all,avg15]");
        assertZabbixAgentDataResponse(agentDataRespData);

        // Verify meter system received data
        Assert.assertEquals(1, values.size());
        AvgLabeledFunction avgLabeledFunction = ((AvgLabeledFunction) values.get(0));
        String serviceId = IDManager.ServiceID.buildId("zabbix::all", true);
        Assert.assertEquals(IDManager.ServiceInstanceID.buildId(serviceId, "test-01"), avgLabeledFunction.getEntityId());
        Assert.assertEquals(serviceId, avgLabeledFunction.getServiceId());
        Assert.assertEquals(1, avgLabeledFunction.getSummation().get("avg1"), 0.0);
        Assert.assertEquals(2, avgLabeledFunction.getSummation().get("avg5"), 0.0);
        Assert.assertEquals(3, avgLabeledFunction.getSummation().get("avg15"), 0.0);
        Assert.assertEquals(1, avgLabeledFunction.getCount().get("avg1"), 0.0);
        Assert.assertEquals(1, avgLabeledFunction.getCount().get("avg5"), 0.0);
        Assert.assertEquals(1, avgLabeledFunction.getCount().get("avg15"), 0.0);
        stopSocketClient();
    }
}