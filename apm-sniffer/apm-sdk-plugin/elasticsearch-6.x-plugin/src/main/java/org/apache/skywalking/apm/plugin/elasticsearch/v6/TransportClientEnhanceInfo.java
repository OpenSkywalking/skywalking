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

package org.apache.skywalking.apm.plugin.elasticsearch.v6;


/**
 * @author yi.liang
 * date 2020.02.13 20:50
 */
public class TransportClientEnhanceInfo {
    /**
     * elasticsearch cluster name
     */
    private String clusterName;

    private TransportAddressCache transportAddressCache;

    public String transportAddresses() {
        return transportAddressCache.transportAddress();
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public TransportAddressCache getTransportAddressCache() {
        return transportAddressCache;
    }

    public void setTransportAddressCache(TransportAddressCache transportAddressCache) {
        this.transportAddressCache = transportAddressCache;
    }
}
