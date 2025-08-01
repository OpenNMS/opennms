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

import java.util.Objects;

public class PingSequence {
    private boolean timeout;
    private Throwable error;
    private PingResponse response;
    private int sequenceNumber;

    public PingSequence(int sequenceNumber, PingResponse response) {
        this.response = Objects.requireNonNull(response);
        this.sequenceNumber = sequenceNumber;
        this.timeout = response.getRtt() == Double.POSITIVE_INFINITY;
    }

    public PingSequence(int sequenceNumber, Throwable throwable) {
        this.error = Objects.requireNonNull(throwable);
        this.sequenceNumber = sequenceNumber;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public boolean isError() {
        return error != null;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public Throwable getError() {
        return error;
    }

    public boolean isSuccess() {
        return !isTimeout() && !isError();
    }

    public PingResponse getResponse() {
        return response;
    }
}
