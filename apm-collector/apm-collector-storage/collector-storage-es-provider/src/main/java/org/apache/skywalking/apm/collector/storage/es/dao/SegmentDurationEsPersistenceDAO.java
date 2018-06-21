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

package org.apache.skywalking.apm.collector.storage.es.dao;

import com.google.gson.Gson;
import java.util.*;
import org.apache.skywalking.apm.collector.client.elasticsearch.ElasticSearchClient;
import org.apache.skywalking.apm.collector.storage.dao.ISegmentDurationPersistenceDAO;
import org.apache.skywalking.apm.collector.storage.es.base.dao.EsDAO;
import org.apache.skywalking.apm.collector.storage.table.segment.*;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.slf4j.*;

/**
 * @author peng-yongsheng
 */
public class SegmentDurationEsPersistenceDAO extends EsDAO implements ISegmentDurationPersistenceDAO<IndexRequestBuilder, UpdateRequestBuilder, SegmentDuration> {

    private final Logger logger = LoggerFactory.getLogger(SegmentDurationEsPersistenceDAO.class);

    private final Gson gson = new Gson();

    public SegmentDurationEsPersistenceDAO(ElasticSearchClient client) {
        super(client);
    }

    @Override
    public SegmentDuration get(String id) {
        return null;
    }

    @Override
    public UpdateRequestBuilder prepareBatchUpdate(SegmentDuration data) {
        return null;
    }

    @Override
    public IndexRequestBuilder prepareBatchInsert(SegmentDuration data) {
        Map<String, Object> target = new HashMap<>();
        target.put(SegmentDurationTable.SEGMENT_ID.getName(), data.getSegmentId());
        target.put(SegmentDurationTable.APPLICATION_ID.getName(), data.getApplicationId());
        target.put(SegmentDurationTable.SERVICE_NAME.getName(), gson.toJson(data.getServiceName()));
        target.put(SegmentDurationTable.DURATION.getName(), data.getDuration());
        target.put(SegmentDurationTable.START_TIME.getName(), data.getStartTime());
        target.put(SegmentDurationTable.END_TIME.getName(), data.getEndTime());
        target.put(SegmentDurationTable.IS_ERROR.getName(), data.getIsError());
        target.put(SegmentDurationTable.TIME_BUCKET.getName(), data.getTimeBucket());
        return getClient().prepareIndex(SegmentDurationTable.TABLE, data.getId()).setSource(target);
    }

    @Override public void deleteHistory(Long timeBucketBefore) {
        BulkByScrollResponse response = getClient().prepareDelete(
            QueryBuilders.rangeQuery(SegmentDurationTable.TIME_BUCKET.getName()).lte(timeBucketBefore * 100),
            SegmentDurationTable.TABLE)
            .get();

        long deleted = response.getDeleted();
        logger.info("Delete {} rows history from {} index.", deleted, SegmentDurationTable.TABLE);
    }
}
