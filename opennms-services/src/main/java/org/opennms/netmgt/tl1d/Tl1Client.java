/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 1, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.tl1d;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Category;

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

    /**
     * <p>setLog</p>
     *
     * @param log a {@link org.apache.log4j.Category} object.
     */
    void setLog(Category log);	
}
