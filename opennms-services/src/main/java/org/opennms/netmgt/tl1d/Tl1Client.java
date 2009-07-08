/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.tl1d;

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Category;

/**
 * The API for TL1 client connections.
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 */
public interface Tl1Client {

    void start();

	void stop();

	String getHost();
	void setHost(String host);

	int getPort();
	void setPort(int port);

	long getReconnectionDelay();
	void setReconnectionDelay(long reconnectionDelay);

	BlockingQueue<Tl1AutonomousMessage> getTl1Queue();
	void setTl1Queue(BlockingQueue<Tl1AutonomousMessage> queue);
	
    public Tl1AutonomousMessageProcessor getMessageProcessor();
    public void setMessageProcessor(Tl1AutonomousMessageProcessor messageProcessor);

    void setLog(Category log);	
}
