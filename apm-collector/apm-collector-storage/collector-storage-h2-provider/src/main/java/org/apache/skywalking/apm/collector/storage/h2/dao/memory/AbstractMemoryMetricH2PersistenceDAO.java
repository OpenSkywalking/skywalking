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

package org.apache.skywalking.apm.collector.storage.h2.dao.memory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.skywalking.apm.collector.client.h2.H2Client;
import org.apache.skywalking.apm.collector.storage.h2.base.dao.AbstractPersistenceH2DAO;
import org.apache.skywalking.apm.collector.storage.table.jvm.MemoryMetric;
import org.apache.skywalking.apm.collector.storage.table.jvm.MemoryMetricTable;

/**
 * @author peng-yongsheng
 */
public abstract class AbstractMemoryMetricH2PersistenceDAO extends AbstractPersistenceH2DAO<MemoryMetric> {

    AbstractMemoryMetricH2PersistenceDAO(H2Client client) {
        super(client);
    }

    @Override protected final MemoryMetric h2DataToStreamData(ResultSet resultSet) throws SQLException {
        MemoryMetric memoryMetric = new MemoryMetric();
        memoryMetric.setId(resultSet.getString(MemoryMetricTable.ID.getName()));
        memoryMetric.setMetricId(resultSet.getString(MemoryMetricTable.METRIC_ID.getName()));

        memoryMetric.setInstanceId(resultSet.getInt(MemoryMetricTable.INSTANCE_ID.getName()));
        memoryMetric.setIsHeap(resultSet.getInt(MemoryMetricTable.IS_HEAP.getName()));

        memoryMetric.setInit(resultSet.getLong(MemoryMetricTable.INIT.getName()));
        memoryMetric.setMax(resultSet.getLong(MemoryMetricTable.MAX.getName()));
        memoryMetric.setUsed(resultSet.getLong(MemoryMetricTable.USED.getName()));
        memoryMetric.setCommitted(resultSet.getLong(MemoryMetricTable.COMMITTED.getName()));
        memoryMetric.setTimes(resultSet.getLong(MemoryMetricTable.TIMES.getName()));

        memoryMetric.setTimeBucket(resultSet.getLong(MemoryMetricTable.TIME_BUCKET.getName()));
        return memoryMetric;
    }

    @Override protected final Map<String, Object> streamDataToH2Data(MemoryMetric streamData) {
        Map<String, Object> target = new HashMap<>();
        target.put(MemoryMetricTable.ID.getName(), streamData.getId());
        target.put(MemoryMetricTable.METRIC_ID.getName(), streamData.getMetricId());

        target.put(MemoryMetricTable.INSTANCE_ID.getName(), streamData.getInstanceId());
        target.put(MemoryMetricTable.IS_HEAP.getName(), streamData.getIsHeap());
        target.put(MemoryMetricTable.INIT.getName(), streamData.getInit());
        target.put(MemoryMetricTable.MAX.getName(), streamData.getMax());
        target.put(MemoryMetricTable.USED.getName(), streamData.getUsed());
        target.put(MemoryMetricTable.COMMITTED.getName(), streamData.getCommitted());
        target.put(MemoryMetricTable.TIMES.getName(), streamData.getTimes());
        target.put(MemoryMetricTable.TIME_BUCKET.getName(), streamData.getTimeBucket());

        return target;
    }
}
