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

package org.apache.skywalking.oap.server.receiver.zipkin.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import javax.servlet.http.HttpServletRequest;
import org.apache.skywalking.oap.server.core.source.SourceReceiver;
import org.apache.skywalking.oap.server.receiver.zipkin.ZipkinReceiverConfig;
import org.apache.skywalking.oap.server.receiver.zipkin.analysis.ZipkinSkyWalkingTransfer;
import org.apache.skywalking.oap.server.receiver.zipkin.trace.SpanForward;
import zipkin2.Span;
import zipkin2.codec.SpanBytesDecoder;

public class SpanProcessor {
    private SourceReceiver receiver;

    public SpanProcessor(SourceReceiver receiver) {
        this.receiver = receiver;
    }

    void convert(ZipkinReceiverConfig config, SpanBytesDecoder decoder, HttpServletRequest request) throws IOException {
        try(InputStream inputStream = getInputStream(request)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int readCntOnce;

            while ((readCntOnce = inputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, readCntOnce);
            }

            List<Span> spanList = decoder.decodeList(out.toByteArray());

            if (config.isNeedAnalysis()) {
                ZipkinSkyWalkingTransfer transfer = new ZipkinSkyWalkingTransfer();
                transfer.doTransfer(config, spanList);
            } else {
                SpanForward forward = new SpanForward(config, receiver);
                forward.send(spanList);
            }
        }
    }

    private InputStream getInputStream(HttpServletRequest request) throws IOException {
        String headEncoding = request.getHeader("accept-encoding");
        if (headEncoding != null && (headEncoding.indexOf("gzip") != -1)) {
            try(InputStream requestInStream = new GZIPInputStream(request.getInputStream());){
                return requestInStream;
            }
        } else {
            return request.getInputStream();
        }
    }

}
