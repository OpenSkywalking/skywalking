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

package org.apache.skywalking.apm.plugin.jdbc.mysql.parser;

import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.plugin.jdbc.connectionurl.parser.AbstractURLParser;
import org.apache.skywalking.apm.plugin.jdbc.connectionurl.parser.URLLocation;
import org.apache.skywalking.apm.plugin.jdbc.trace.ConnectionInfo;

/**
 * {@link MysqlURLParser} parse connection url of mysql.
 * <p>
 * The {@link ConnectionInfo#host} be set the string between charset "//" and the first
 * charset "/" after the charset "//", and {@link ConnectionInfo#databaseName} be set the
 * string between the last index of "/" and the first charset "?". but one more thing, the
 * {@link ConnectionInfo#hosts} be set if the host container multiple host.
 *
 * @author zhangxin
 */
public class MysqlURLParser extends AbstractURLParser {

    private static final int DEFAULT_PORT = 3306;
    private static final String DB_TYPE = "Mysql";

    public MysqlURLParser() {
        this("");
    }

    public MysqlURLParser(String url) {
        super(url);
    }

    @Override
    protected URLLocation fetchDatabaseHostsIndexRange() {
        int hostLabelStartIndex = url.indexOf("//");
        int hostLabelEndIndex = url.indexOf("/", hostLabelStartIndex + 2);
        return new URLLocation(hostLabelStartIndex + 2, hostLabelEndIndex);
    }

    @Override
    protected URLLocation fetchDatabaseNameIndexRange() {
        int databaseStartTag = url.lastIndexOf("/");
        int databaseEndTag = url.indexOf("?", databaseStartTag);
        if (databaseEndTag == -1) {
            databaseEndTag = url.length();
        }
        return new URLLocation(databaseStartTag + 1, databaseEndTag);
    }

    @Override
    public ConnectionInfo parse() {
        URLLocation location = fetchDatabaseHostsIndexRange();
        String hosts = url.substring(location.startIndex(), location.endIndex());
        String[] hostSegment = hosts.split(",");
        if (hostSegment.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (String host : hostSegment) {
                if (host.split(":").length == 1) {
                    sb.append(host + ":" + DEFAULT_PORT + ",");
                } else {
                    sb.append(host + ",");
                }
            }
            return new ConnectionInfo(ComponentsDefine.MYSQL, DB_TYPE, sb.toString(), fetchDatabaseNameFromURL());
        } else {
            String[] hostAndPort = hostSegment[0].split(":");
            if (hostAndPort.length != 1) {
                return new ConnectionInfo(ComponentsDefine.MYSQL, DB_TYPE, hostAndPort[0], Integer.valueOf(hostAndPort[1]), fetchDatabaseNameFromURL());
            } else {
                return new ConnectionInfo(ComponentsDefine.MYSQL, DB_TYPE, hostAndPort[0], DEFAULT_PORT, fetchDatabaseNameFromURL());
            }
        }
    }

    @Override public String getJDBCURLPrefix() {
        return "jdbc:mysql";
    }

}
