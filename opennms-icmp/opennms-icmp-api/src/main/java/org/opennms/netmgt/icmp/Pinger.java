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

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * <p>Pinger class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public interface Pinger {
    /**
     * This method is used to ping a remote host to test for ICMP support.  Calls
     * the callback method upon success or error.
     *
     * @param host The {@link java.net.InetAddress} address to poll.
     * @param timeout The time to wait between each retry.
     * @param retries The number of times to retry.
     * @param packetsize The size in byte of the ICMP packet.
     * @param sequenceId an ID representing the ping
     * 
     * @param cb the {@link org.opennms.netmgt.ping.PingResponseCallback} callback to call upon success or error
     */
    public void ping(InetAddress host, long timeout, int retries, int packetsize, int sequenceId, PingResponseCallback cb) throws Exception;

    /**
     * This method is used to ping a remote host to test for ICMP support.  Calls
     * the callback method upon success or error.
     *
     * @param host The {@link java.net.InetAddress} address to poll.
     * @param timeout The time to wait between each retry.
     * @param retries The number of times to retry.
     * @param sequenceId an ID representing the ping
     * 
     * @param cb the {@link org.opennms.netmgt.ping.PingResponseCallback} callback to call upon success or error
     */
    public void ping(InetAddress host, long timeout, int retries, int sequenceId, PingResponseCallback cb) throws Exception;

    /**
     * This method is used to ping a remote host to test for ICMP support. If
     * the remote host responds within the specified period, defined by retries
     * and timeouts, then the response time is returned.
     *
     * @param host The {@link java.net.InetAddress} address to poll.
     * @param timeout The time to wait between each retry.
     * @param retries The number of times to retry.
     * @param packetsize The size in byte of the ICMP packet.
     * @return The response time in microseconds if the host is reachable and has responded with an echo reply, otherwise a null value.
     */
    public Number ping(InetAddress host, long timeout, int retries, int packetsize) throws Exception;
    

    /**
     * This method is used to ping a remote host to test for ICMP support. If
     * the remote host responds within the specified period, defined by retries
     * and timeouts, then the response time is returned.
     *
     * @param host The {@link java.net.InetAddress} address to poll.
     * @param timeout The time to wait between each retry.
     * @param retries The number of times to retry.
     * @return The response time in microseconds if the host is reachable and has responded with an echo reply, otherwise a null value.
     */
    public Number ping(InetAddress host, long timeout, int retries) throws Exception;
    

	/**
	 * Ping a remote host, using the default number of retries and timeouts.
	 *
     * @param host The {@link java.net.InetAddress} address to poll.
     * @return The response time in microseconds if the host is reachable and has responded with an echo reply, otherwise a null value.
	 * @throws IOException if any.
	 * @throws InterruptedException if any.
	 * @throws java.lang.Exception if any.
	 */
	public Number ping(InetAddress host) throws Exception;

        /**
         * Ping a remote host, sending 1 or more packets at the given interval, and then
         * return the response times as a list.
         *
         * @param host The {@link java.net.InetAddress} address to poll.
         * @param count The number of packets to send.
         * @param timeout The time to wait between each retry.
         * @param pingInterval The interval at which packets will be sent.
         * @param size The size of the packet to send.
         * @return a {@link java.util.List} of response times in microseconds.
         *     If, for a given ping request, the host is reachable and has responded with an
         *     echo reply, it will contain a number, otherwise a null value.
         */
        public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval, int size) throws Exception;
        
        /**
         * Ping a remote host, sending 1 or more packets at the given interval, and then
         * return the response times as a list.
         *
         * @param host The {@link java.net.InetAddress} address to poll.
         * @param count The number of packets to send.
         * @param timeout The time to wait between each retry.
         * @param pingInterval The interval at which packets will be sent.
         * @return a {@link java.util.List} of response times in microseconds.
         *     If, for a given ping request, the host is reachable and has responded with an
         *     echo reply, it will contain a number, otherwise a null value.
         */
        public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval) throws Exception;
        
	public void setAllowFragmentation(final boolean allow) throws Exception;

	public void setTrafficClass(final int tc) throws Exception;

	/**
	 * Initialize IPv4 in this Pinger implementation.  If unable to do so, implementations should throw an exception.
	 * @throws Exception
	 */
	public void initialize4() throws Exception;

	/**
	 * Initialize IPv6 in this Pinger implementation.  If unable to do so, implementations should throw an exception.
	 * @throws Exception
	 */
	public void initialize6() throws Exception;
	
	/**
	 * Whether or not IPv4 is initialized and available for this implementation.
	 */
	public boolean isV4Available();
	
	/**
	 * Whether or not IPv6 is initialized and available for this implementation.
	 * @return
	 */
	public boolean isV6Available();
}
