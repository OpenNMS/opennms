/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.osgid;

import java.io.File;

import org.apache.karaf.main.Main;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.DisposableBean;

/**
 * OSGi container that can run inside OpenNMS.
 *
 * @author Seth
 */
@EventListener(name=Osgid.NAME)
public class Osgid implements SpringServiceDaemon, DisposableBean {

	/** Constant <code>NAME="OpenNMS.Osgid"</code> */
	public static final String NAME = "OpenNMS.Osgid";

	private volatile EventForwarder m_eventForwarder;

	private volatile Main m_main;

	private final Object m_mainLock = new Object();

	/**
	 * <p>start</p>
	 * @throws Exception 
	 */
	@Override
	public void start() throws Exception {
		// TODO: Use function to fetch OpenNMS homedir
		String root = new File(System.getProperty("opennms.home"), "karaf").getAbsolutePath();
		System.err.println("Root: " + root);
		System.setProperty("karaf.home", root);
		System.setProperty("karaf.base", root);
		System.setProperty("karaf.data", root + "/data");
		System.setProperty("karaf.history", root + "/data/history.txt");
		System.setProperty("karaf.instances", root + "/instances");
		System.setProperty("karaf.startLocalConsole", "false");
		System.setProperty("karaf.startRemoteShell", "true");
		System.setProperty("karaf.lock", "false");
		synchronized(m_mainLock) {
			m_main = new Main(new String[0]);
			m_main.launch();
		}
	}

	/**
	 * <p>destroy</p>
	 */
	@Override
	public void destroy() throws Exception {
		synchronized(m_mainLock) {
			if (m_main != null)
				m_main.destroy();
		}
	}

	/**
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	@EventHandler(uei=EventConstants.ACKNOWLEDGE_EVENT_UEI)
	public void handleAckEvent(Event event) {
	}

	/**
	 * <p>getEventForwarder</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.events.EventForwarder} object.
	 */
	public EventForwarder getEventForwarder() {
		return m_eventForwarder;
	}

	/**
	 * <p>setEventForwarder</p>
	 *
	 * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
	 */
	public void setEventForwarder(EventForwarder eventForwarder) {
		m_eventForwarder = eventForwarder;
	}

	/**
	 * <p>afterPropertiesSet</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
	}

	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return NAME;
	}

}
