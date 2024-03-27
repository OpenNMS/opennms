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
package org.opennms.netmgt.icmp;

import java.net.InetAddress;

/**
 * <p>PingResponseCallback interface.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @version $Id: $
 */
public interface PingResponseCallback {

	/**
	 * <p>handleResponse</p>
	 *
	 * @param address a {@link java.net.InetAddress} object.
	 * @param response a {@link org.opennms.netmgt.icmp.EchoPacket} object.
	 */
	public void handleResponse(InetAddress address, EchoPacket response);
	/**
	 * <p>handleTimeout</p>
	 *
	 * @param address a {@link java.net.InetAddress} object.
	 * @param request TODO
	 */
	public void handleTimeout(InetAddress address, EchoPacket request);
    /**
     * <p>handleError</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param request a {@link org.opennms.netmgt.icmp.EchoPacket} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public void handleError(InetAddress address, EchoPacket request, Throwable t);

}
