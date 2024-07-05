/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package io.github.http.benchmark;

import io.github.http.HttpException;
import io.github.http.HttpRequest;
import io.github.http.HttpResponse;
import io.github.http.HttpStatus;
import io.github.http.entity.ContentType;
import io.github.http.entity.StringEntity;
import io.github.http.protocol.HttpContext;
import io.github.http.protocol.HttpRequestHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class SmokeTest {

    private HttpServer server;

    @Before
    public void setup() throws Exception {
        server = new HttpServer();
        server.registerHandler("/", new HttpRequestHandler() {
            @Override
            public void handle(
                    final HttpRequest request,
                    final HttpResponse response,
                    final HttpContext context) throws HttpException, IOException {
                response.setStatusCode(HttpStatus.SC_OK);
                response.setEntity(new StringEntity("0123456789ABCDEF", ContentType.TEXT_PLAIN));
            }
        });
        server.start();
    }

    @After
    public void shutdown() throws Exception {
        server.shutdown();
    }

    @Test
    public void testBasics() throws Exception {
        final Config config = new Config();
        config.setKeepAlive(true);
        config.setMethod("GET");
        config.setUrl(new URL("http://localhost:" + server.getPort() + "/"));
        config.setThreads(3);
        config.setRequests(100);
        final HttpBenchmark httpBenchmark = new HttpBenchmark(config);
        final Results results = httpBenchmark.doExecute();
        Assert.assertNotNull(results);
        Assert.assertEquals(16, results.getContentLength());
        Assert.assertEquals(3, results.getConcurrencyLevel());
        Assert.assertEquals(300, results.getKeepAliveCount());
        Assert.assertEquals(300, results.getSuccessCount());
        Assert.assertEquals(0, results.getFailureCount());
        Assert.assertEquals(0, results.getWriteErrors());
        Assert.assertEquals(300 * 16, results.getTotalBytes());
        Assert.assertEquals(300 * 16, results.getTotalBytesRcvd());
    }

}