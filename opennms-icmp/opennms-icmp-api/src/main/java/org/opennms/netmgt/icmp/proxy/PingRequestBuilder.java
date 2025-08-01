/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.icmp.proxy;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public interface PingRequestBuilder {

    /**
     * Can be used to show progress.
     */
    interface Callback {
        /**
         * Is invoked on a ping progress updated.
         * Is only used if {@link #withNumberOfRequests(int)} is > 1.
         * @param summary
         */
        void onUpdate(PingSequence newSequence, PingSummary summary);
    }

    PingRequestBuilder withTimeout(long timeout, TimeUnit unit);

    PingRequestBuilder withPacketSize(int packageSize);

    PingRequestBuilder withRetries(int retries);

    PingRequestBuilder withInetAddress(InetAddress inetAddress);

    PingRequestBuilder withLocation(String location);

    PingRequestBuilder withSystemId(String systemId);

    PingRequestBuilder withNumberOfRequests(int numberOfRequests);

    PingRequestBuilder withProgressCallback(Callback callback);

    CompletableFuture<PingSummary> execute();

}
