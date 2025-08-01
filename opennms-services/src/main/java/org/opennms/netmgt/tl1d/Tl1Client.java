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
package org.opennms.netmgt.tl1d;

import java.util.concurrent.BlockingQueue;


/**
 * The API for TL1 client connections.
 *
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * @version $Id: $
 */
public interface Tl1Client {

    /**
     * <p>start</p>
     */
    void start();

	/**
	 * <p>stop</p>
	 */
	void stop();

	/**
	 * <p>getHost</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	String getHost();
	/**
	 * <p>setHost</p>
	 *
	 * @param host a {@link java.lang.String} object.
	 */
	void setHost(String host);

	/**
	 * <p>getPort</p>
	 *
	 * @return a int.
	 */
	int getPort();
	/**
	 * <p>setPort</p>
	 *
	 * @param port a int.
	 */
	void setPort(int port);

	/**
	 * <p>getReconnectionDelay</p>
	 *
	 * @return a long.
	 */
	long getReconnectionDelay();
	/**
	 * <p>setReconnectionDelay</p>
	 *
	 * @param reconnectionDelay a long.
	 */
	void setReconnectionDelay(long reconnectionDelay);

	/**
	 * <p>getTl1Queue</p>
	 *
	 * @return a {@link java.util.concurrent.BlockingQueue} object.
	 */
	BlockingQueue<Tl1AutonomousMessage> getTl1Queue();
	/**
	 * <p>setTl1Queue</p>
	 *
	 * @param queue a {@link java.util.concurrent.BlockingQueue} object.
	 */
	void setTl1Queue(BlockingQueue<Tl1AutonomousMessage> queue);
	
    /**
     * <p>getMessageProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.tl1d.Tl1AutonomousMessageProcessor} object.
     */
    public Tl1AutonomousMessageProcessor getMessageProcessor();
    /**
     * <p>setMessageProcessor</p>
     *
     * @param messageProcessor a {@link org.opennms.netmgt.tl1d.Tl1AutonomousMessageProcessor} object.
     */
    public void setMessageProcessor(Tl1AutonomousMessageProcessor messageProcessor);

}
