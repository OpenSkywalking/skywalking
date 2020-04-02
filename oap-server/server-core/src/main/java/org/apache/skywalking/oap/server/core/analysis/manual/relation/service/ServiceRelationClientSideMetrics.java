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

package org.apache.skywalking.oap.server.core.analysis.manual.relation.service;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.analysis.Stream;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.analysis.worker.MetricsStreamProcessor;
import org.apache.skywalking.oap.server.core.remote.grpc.proto.RemoteData;
import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.annotation.Column;

@Stream(name = ServiceRelationClientSideMetrics.INDEX_NAME, scopeId = DefaultScopeDefine.SERVICE_RELATION, builder = ServiceRelationClientSideMetrics.Builder.class, processor = MetricsStreamProcessor.class)
public class ServiceRelationClientSideMetrics extends Metrics {

    public static final String INDEX_NAME = "service_relation_client_side";
    public static final String SOURCE_SERVICE_ID = "source_service_id";
    public static final String DEST_SERVICE_ID = "dest_service_id";
    public static final String COMPONENT_ID = "component_id";

    @Setter
    @Getter
    @Column(columnName = SOURCE_SERVICE_ID)
    private String sourceServiceId;
    @Setter
    @Getter
    @Column(columnName = DEST_SERVICE_ID)
    private String destServiceId;
    @Setter
    @Getter
    @Column(columnName = COMPONENT_ID, storageOnly = true)
    private int componentId;
    @Setter
    @Getter
    @Column(columnName = ENTITY_ID)
    private String entityId;

    @Override
    public String id() {
        return getTimeBucket() + Const.ID_SPLIT + entityId;
    }

    @Override
    public void combine(Metrics metrics) {

    }

    @Override
    public void calculate() {

    }

    @Override
    public Metrics toHour() {
        ServiceRelationClientSideMetrics metrics = new ServiceRelationClientSideMetrics();
        metrics.setEntityId(getEntityId());
        metrics.setTimeBucket(toTimeBucketInHour());
        metrics.setSourceServiceId(getSourceServiceId());
        metrics.setDestServiceId(getDestServiceId());
        metrics.setComponentId(getComponentId());
        return metrics;
    }

    @Override
    public Metrics toDay() {
        ServiceRelationClientSideMetrics metrics = new ServiceRelationClientSideMetrics();
        metrics.setEntityId(getEntityId());
        metrics.setTimeBucket(toTimeBucketInDay());
        metrics.setSourceServiceId(getSourceServiceId());
        metrics.setDestServiceId(getDestServiceId());
        metrics.setComponentId(getComponentId());
        return metrics;
    }

    @Override
    public int remoteHashCode() {
        return this.hashCode();
    }

    @Override
    public void deserialize(RemoteData remoteData) {
        setEntityId(remoteData.getDataStrings(0));
        setSourceServiceId(remoteData.getDataStrings(1));
        setDestServiceId(remoteData.getDataStrings(2));

        setComponentId(remoteData.getDataIntegers(0));

        setTimeBucket(remoteData.getDataLongs(0));
    }

    @Override
    public RemoteData.Builder serialize() {
        RemoteData.Builder remoteBuilder = RemoteData.newBuilder();
        remoteBuilder.addDataStrings(getEntityId());
        remoteBuilder.addDataStrings(getSourceServiceId());
        remoteBuilder.addDataStrings(getDestServiceId());

        remoteBuilder.addDataIntegers(getComponentId());

        remoteBuilder.addDataLongs(getTimeBucket());
        return remoteBuilder;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + entityId.hashCode();
        result = (int) (31 * result + getTimeBucket());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        ServiceRelationClientSideMetrics metrics = (ServiceRelationClientSideMetrics) obj;
        if (sourceServiceId != metrics.sourceServiceId)
            return false;
        if (destServiceId != metrics.destServiceId)
            return false;
        if (componentId != metrics.componentId)
            return false;

        return getTimeBucket() == metrics.getTimeBucket();
    }

    public static class Builder implements StorageBuilder<ServiceRelationClientSideMetrics> {

        @Override
        public ServiceRelationClientSideMetrics map2Data(Map<String, Object> dbMap) {
            ServiceRelationClientSideMetrics metrics = new ServiceRelationClientSideMetrics();
            metrics.setSourceServiceId((String) dbMap.get(SOURCE_SERVICE_ID));
            metrics.setDestServiceId((String) dbMap.get(DEST_SERVICE_ID));
            metrics.setComponentId(((Number) dbMap.get(COMPONENT_ID)).intValue());
            metrics.setTimeBucket(((Number) dbMap.get(TIME_BUCKET)).longValue());
            metrics.setEntityId((String) dbMap.get(ENTITY_ID));
            return metrics;
        }

        @Override
        public Map<String, Object> data2Map(ServiceRelationClientSideMetrics storageData) {
            Map<String, Object> map = new HashMap<>();
            map.put(TIME_BUCKET, storageData.getTimeBucket());
            map.put(SOURCE_SERVICE_ID, storageData.getSourceServiceId());
            map.put(DEST_SERVICE_ID, storageData.getDestServiceId());
            map.put(COMPONENT_ID, storageData.getComponentId());
            map.put(ENTITY_ID, storageData.getEntityId());
            return map;
        }
    }
}
