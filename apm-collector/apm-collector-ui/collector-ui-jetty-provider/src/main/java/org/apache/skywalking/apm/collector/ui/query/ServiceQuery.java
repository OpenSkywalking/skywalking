/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.collector.ui.query;

import java.text.ParseException;
import java.util.List;
import org.apache.skywalking.apm.collector.core.module.ModuleManager;
import org.apache.skywalking.apm.collector.core.util.ObjectUtils;
import org.apache.skywalking.apm.collector.storage.ui.common.Duration;
import org.apache.skywalking.apm.collector.storage.ui.common.ResponseTimeTrend;
import org.apache.skywalking.apm.collector.storage.ui.common.SLATrend;
import org.apache.skywalking.apm.collector.storage.ui.common.ThroughputTrend;
import org.apache.skywalking.apm.collector.storage.ui.common.Topology;
import org.apache.skywalking.apm.collector.storage.ui.service.ServiceInfo;
import org.apache.skywalking.apm.collector.ui.graphql.Query;
import org.apache.skywalking.apm.collector.ui.service.ServiceNameService;

/**
 * @author peng-yongsheng
 */
public class ServiceQuery implements Query {

    private final ModuleManager moduleManager;
    private ServiceNameService serviceNameService;

    public ServiceQuery(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    private ServiceNameService getServiceNameService() {
        if (ObjectUtils.isEmpty(serviceNameService)) {
            this.serviceNameService = new ServiceNameService(moduleManager);
        }
        return serviceNameService;
    }

    public List<ServiceInfo> searchService(String keyword, int topN) throws ParseException {
        return getServiceNameService().searchService(keyword, topN);
    }

    public ResponseTimeTrend getServiceResponseTimeTrend(int serviceId, Duration duration) {
        return null;
    }

    public ThroughputTrend getServiceTPSTrend(int serviceId, Duration duration) {
        return null;
    }

    public SLATrend getServiceSLATrend(int serviceId, Duration duration) {
        return null;
    }

    public Topology getServiceTopology(int serviceId, Duration duration) {
        return null;
    }
}
