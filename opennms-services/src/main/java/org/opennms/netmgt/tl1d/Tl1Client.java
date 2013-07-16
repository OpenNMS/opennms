/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
