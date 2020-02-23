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

package org.apache.skywalking.oap.server.receiver.trace.provider.handler.v6.rest.reader;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import org.apache.skywalking.apm.network.common.KeyStringValuePair;

public class KeyStringValuePairJsonReader implements StreamJsonReader<KeyStringValuePair> {

    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Override
    public KeyStringValuePair read(JsonReader reader) throws IOException {
        KeyStringValuePair.Builder builder = KeyStringValuePair.newBuilder();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case KEY:
                    builder.setKey(reader.nextString());
                    break;
                case VALUE:
                    builder.setValue(reader.nextString());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return builder.build();
    }
}
