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

package org.apache.skywalking.apm.collector.storage.es.http.dao.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.skywalking.apm.collector.client.elasticsearch.http.ElasticSearchHttpClient;
import org.apache.skywalking.apm.collector.core.util.Const;
import org.apache.skywalking.apm.collector.storage.dao.ui.IServiceMetricUIDAO;
import org.apache.skywalking.apm.collector.storage.es.http.base.dao.EsHttpDAO;
import org.apache.skywalking.apm.collector.storage.table.MetricSource;
import org.apache.skywalking.apm.collector.storage.table.service.ServiceMetricTable;
import org.apache.skywalking.apm.collector.storage.ui.common.Node;
import org.apache.skywalking.apm.collector.storage.ui.common.Step;
import org.apache.skywalking.apm.collector.storage.ui.service.ServiceMetric;
import org.apache.skywalking.apm.collector.storage.ui.service.ServiceNode;
import org.apache.skywalking.apm.collector.storage.utils.DurationPoint;
import org.apache.skywalking.apm.collector.storage.utils.TimePyramidTableNameBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregatorBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.searchbox.client.JestResult;
import io.searchbox.core.MultiGet;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.ScriptedMetricAggregation;
import io.searchbox.core.search.aggregation.SumAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;

/**
 * @author cyberdak
 */
public class ServiceMetricEsUIDAO extends EsHttpDAO implements IServiceMetricUIDAO {

    private static final String AVG_DURATION = "avg_duration";

    public ServiceMetricEsUIDAO(ElasticSearchHttpClient client) {
        super(client);
    }

    @Override
    public List<Integer> getServiceResponseTimeTrend(int serviceId, Step step, List<DurationPoint> durationPoints) {
        
        String tableName = TimePyramidTableNameBuilder.build(step, ServiceMetricTable.TABLE);
        MultiGet.Builder.ById multiGet = new MultiGet.Builder.ById(tableName, "type");

        durationPoints.forEach(durationPoint -> {
            String id = durationPoint.getPoint() + Const.ID_SPLIT + serviceId + Const.ID_SPLIT + MetricSource.Callee.getValue();
            multiGet.addId(id);
        });

        List<Integer> trends = new LinkedList<>();
        JestResult result = getClient().execute(multiGet.build());
        JsonArray docs =  result.getJsonObject().getAsJsonArray("docs");
        for (JsonElement response : docs) {
            if (response.getAsJsonObject().get("found").getAsBoolean()) {
                JsonObject source = response.getAsJsonObject().getAsJsonObject("_source");
                long calls = (source.get(ServiceMetricTable.COLUMN_TRANSACTION_CALLS)).getAsLong();
                long errorCalls = (source.get(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_CALLS)).getAsLong();
                long durationSum = (source.get(ServiceMetricTable.COLUMN_TRANSACTION_DURATION_SUM)).getAsLong();
                long errorDurationSum = (source.get(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_DURATION_SUM)).getAsLong();
                trends.add((int)((durationSum - errorDurationSum) / (calls - errorCalls)));
            } else {
                trends.add(0);
            }
        }
        return trends;
    }

    @Override public List<Integer> getServiceSLATrend(int serviceId, Step step, List<DurationPoint> durationPoints) {
        String tableName = TimePyramidTableNameBuilder.build(step, ServiceMetricTable.TABLE);
        MultiGet.Builder.ById multiGet = new MultiGet.Builder.ById(tableName, "type");

        durationPoints.forEach(durationPoint -> {
            String id = durationPoint.getPoint() + Const.ID_SPLIT + serviceId + Const.ID_SPLIT + MetricSource.Callee.getValue();
            multiGet.addId(id);
        });

        List<Integer> trends = new LinkedList<>();
        JestResult result = getClient().execute(multiGet.build());
        JsonArray docs =  result.getJsonObject().getAsJsonArray("docs");
        for (JsonElement response : docs) {
            if (response.getAsJsonObject().get("found").getAsBoolean()) {
                JsonObject source = response.getAsJsonObject().getAsJsonObject("_source");
                long calls = (source.get(ServiceMetricTable.COLUMN_TRANSACTION_CALLS)).getAsLong();
                long errorCalls = (source.get(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_CALLS)).getAsLong();
                trends.add((int)(((calls - errorCalls) / calls)) * 10000);
            } else {
                trends.add(10000);
            }
        }
        return trends;
    }

    @Override
    public List<Node> getServicesMetric(Step step, long startTime, long endTime, MetricSource metricSource,
        Collection<Integer> serviceIds) {
        String tableName = TimePyramidTableNameBuilder.build(step, ServiceMetricTable.TABLE);

//        SearchRequestBuilder searchRequestBuilder = getClient().prepareSearch(tableName);
//        searchRequestBuilder.setTypes(ServiceMetricTable.TABLE_TYPE);
//        searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must().add(QueryBuilders.rangeQuery(ServiceMetricTable.COLUMN_TIME_BUCKET).gte(startTime).lte(endTime));
        boolQuery.must().add(QueryBuilders.termsQuery(ServiceMetricTable.COLUMN_SERVICE_ID, serviceIds));
        boolQuery.must().add(QueryBuilders.termQuery(ServiceMetricTable.COLUMN_SOURCE_VALUE, metricSource.getValue()));

//        searchRequestBuilder.setQuery(boolQuery);
//        searchRequestBuilder.setSize(0);

        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms(ServiceMetricTable.COLUMN_SERVICE_ID).field(ServiceMetricTable.COLUMN_SERVICE_ID).size(100);
        aggregationBuilder.subAggregation(AggregationBuilders.sum(ServiceMetricTable.COLUMN_TRANSACTION_CALLS).field(ServiceMetricTable.COLUMN_TRANSACTION_CALLS));
        aggregationBuilder.subAggregation(AggregationBuilders.sum(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_CALLS).field(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_CALLS));

//        searchRequestBuilder.addAggregation(aggregationBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQuery).size(0).aggregation(aggregationBuilder);
        
        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(tableName).build();
        
        SearchResult searchResponse =   getClient().execute(search);
        
//        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        List<Node> nodes = new LinkedList<>();
        TermsAggregation serviceIdTerms = searchResponse.getAggregations().getTermsAggregation(ServiceMetricTable.COLUMN_SERVICE_ID);
        serviceIdTerms.getBuckets().forEach(serviceIdBucket -> {
            int serviceId =  Integer.parseInt(serviceIdBucket.getKeyAsString());

            SumAggregation callsSum = serviceIdBucket.getSumAggregation(ServiceMetricTable.COLUMN_TRANSACTION_CALLS);
            SumAggregation errorCallsSum = serviceIdBucket.getSumAggregation(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_CALLS);

            ServiceNode serviceNode = new ServiceNode();
            serviceNode.setId(serviceId);
            serviceNode.setCalls((long)callsSum.getSum().intValue());
            serviceNode.setSla((int)(((callsSum.getSum() - errorCallsSum.getSum()) / callsSum.getSum()) * 10000));
            nodes.add(serviceNode);
        });
        return nodes;
    }

    @Override public List<ServiceMetric> getSlowService(int applicationId, Step step, long start, long end, Integer top,
        MetricSource metricSource) {
        String tableName = TimePyramidTableNameBuilder.build(step, ServiceMetricTable.TABLE);


        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must().add(QueryBuilders.rangeQuery(ServiceMetricTable.COLUMN_TIME_BUCKET).gte(start).lte(end));
        if (applicationId != 0) {
            boolQuery.must().add(QueryBuilders.termQuery(ServiceMetricTable.COLUMN_APPLICATION_ID, applicationId));
        }
        boolQuery.must().add(QueryBuilders.termQuery(ServiceMetricTable.COLUMN_SOURCE_VALUE, metricSource.getValue()));


        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms(ServiceMetricTable.COLUMN_SERVICE_ID).field(ServiceMetricTable.COLUMN_SERVICE_ID).size(top);
        aggregationBuilder.subAggregation(AggregationBuilders.sum(ServiceMetricTable.COLUMN_TRANSACTION_CALLS).field(ServiceMetricTable.COLUMN_TRANSACTION_CALLS));
        aggregationBuilder.subAggregation(AggregationBuilders.sum(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_CALLS).field(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_CALLS));
        aggregationBuilder.subAggregation(AggregationBuilders.sum(ServiceMetricTable.COLUMN_TRANSACTION_DURATION_SUM).field(ServiceMetricTable.COLUMN_TRANSACTION_DURATION_SUM));
        aggregationBuilder.subAggregation(AggregationBuilders.sum(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_DURATION_SUM).field(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_DURATION_SUM));

        Map<String, String> bucketsPathsMap = new HashMap<>();
        bucketsPathsMap.put(ServiceMetricTable.COLUMN_TRANSACTION_CALLS, ServiceMetricTable.COLUMN_TRANSACTION_CALLS);
        bucketsPathsMap.put(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_CALLS, ServiceMetricTable.COLUMN_TRANSACTION_ERROR_CALLS);
        bucketsPathsMap.put(ServiceMetricTable.COLUMN_TRANSACTION_DURATION_SUM, ServiceMetricTable.COLUMN_TRANSACTION_DURATION_SUM);
        bucketsPathsMap.put(ServiceMetricTable.COLUMN_TRANSACTION_ERROR_DURATION_SUM, ServiceMetricTable.COLUMN_TRANSACTION_ERROR_DURATION_SUM);

        String idOrCode = "(params." + ServiceMetricTable.COLUMN_TRANSACTION_DURATION_SUM + " - params." + ServiceMetricTable.COLUMN_TRANSACTION_ERROR_DURATION_SUM + ")"
            + " / "
            + "(params." + ServiceMetricTable.COLUMN_TRANSACTION_CALLS + " - params." + ServiceMetricTable.COLUMN_TRANSACTION_ERROR_CALLS + ")";
        Script script = new Script(idOrCode);
        aggregationBuilder.subAggregation(PipelineAggregatorBuilders.bucketScript(AVG_DURATION, bucketsPathsMap, script));

        
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        searchSourceBuilder.query(boolQuery);
        searchSourceBuilder.aggregation(aggregationBuilder);
        
        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(tableName).build();
        
        SearchResult searchResponse = getClient().execute(search);
        
        List<ServiceMetric> serviceMetrics = new LinkedList<>();
        TermsAggregation serviceIdTerms = searchResponse.getAggregations().getTermsAggregation(ServiceMetricTable.COLUMN_SERVICE_ID);
        serviceIdTerms.getBuckets().forEach(serviceIdTerm -> {
            int serviceId =  Integer.parseInt(serviceIdTerm.getKeyAsString());

            ServiceMetric serviceMetric = new ServiceMetric();
            ScriptedMetricAggregation  simpleValue = serviceIdTerm.getScriptedMetricAggregation(AVG_DURATION);

            serviceMetric.setId(serviceId);
            serviceMetric.setAvgResponseTime(simpleValue.getScriptedMetric().intValue());
            serviceMetrics.add(serviceMetric);
        });
        return serviceMetrics;
    }
}
