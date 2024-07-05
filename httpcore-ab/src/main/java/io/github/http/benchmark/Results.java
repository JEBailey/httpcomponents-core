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

/**
 * Benchmark results
 *
 * @since 4.3
 */
public final class Results {

    String serverName;
    String hostName;
    int hostPort;
    String documentPath;
    long contentLength;
    int concurrencyLevel;
    long totalTimeNano;
    long successCount;
    long failureCount;
    long writeErrors;
    long keepAliveCount;
    long totalBytesRcvd;
    long totalBytesSent;
    long totalBytes;

    Results() {
        super();
        this.contentLength = -1;
    }

    public String getServerName() {
        return serverName;
    }

    public String getHostName() {
        return hostName;
    }

    public int getHostPort() {
        return hostPort;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public long getContentLength() {
        return contentLength;
    }

    public int getConcurrencyLevel() {
        return concurrencyLevel;
    }

    public long getTotalTimeNano() {
        return totalTimeNano;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public long getWriteErrors() {
        return writeErrors;
    }

    public long getKeepAliveCount() {
        return keepAliveCount;
    }

    public long getTotalBytesRcvd() {
        return totalBytesRcvd;
    }

    public long getTotalBytesSent() {
        return totalBytesSent;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    @Override
    public String toString() {
        return "[serverName=" + serverName +
                ", hostName=" + hostName +
                ", hostPort=" + hostPort +
                ", documentPath=" + documentPath +
                ", contentLength=" + contentLength +
                ", concurrencyLevel=" + concurrencyLevel +
                ", totalTimeNano=" + totalTimeNano +
                ", successCount=" + successCount +
                ", failureCount=" + failureCount +
                ", writeErrors=" + writeErrors +
                ", keepAliveCount=" + keepAliveCount +
                ", totalBytesRcvd=" + totalBytesRcvd +
                ", totalBytesSent=" + totalBytesSent +
                ", totalBytes=" + totalBytes +
                "]";
    }

}
