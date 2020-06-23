/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
