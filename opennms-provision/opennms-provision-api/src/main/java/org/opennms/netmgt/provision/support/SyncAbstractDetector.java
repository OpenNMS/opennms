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
package org.opennms.netmgt.provision.support;

import java.net.InetAddress;

import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.SyncServiceDetector;

/**
 * <p>SyncAbstractDetector class.</p>
 *
 * @author ranger
 */
public abstract class SyncAbstractDetector extends AbstractDetector implements SyncServiceDetector {
    
    /**
     * <p>Constructor for SyncAbstractDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     * @param timeout a int.
     * @param retries a int.
     */
    protected SyncAbstractDetector(final String serviceName, final int port, final int timeout, final int retries) {
        super(serviceName, port, timeout, retries);
    }

    /**
     * <p>Constructor for SyncAbstractDetector.</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @param port a int.
     */
    protected SyncAbstractDetector(final String serviceName, final int port) {
        super(serviceName, port);
    }

    /**
     * <p>isServiceDetected</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @return a boolean.
     */
    public abstract boolean isServiceDetected(InetAddress address);

    @Override
    public DetectResults detect(DetectRequest request) {
        return new DetectResultsImpl(isServiceDetected(request.getAddress()));
    }
}
