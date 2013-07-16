/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vmmgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.ServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaemonManagerDefault implements DaemonManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(DaemonManagerDefault.class);
	
	private List<ServiceDaemon> m_serviceDaemons;
	
	/**
	 * <p>setServiceDaemons</p>
	 *
	 * @param serviceDaemons a {@link java.util.List} object.
	 */
	public void setServiceDaemons(List<ServiceDaemon> serviceDaemons) {
		m_serviceDaemons = serviceDaemons;
	}

	/**
	 * <p>pause</p>
	 */
        @Override
	public void pause() {
        for(ServiceDaemon serviceDaemon : m_serviceDaemons) {
			serviceDaemon.pause();
		}
	}

	/**
	 * <p>resume</p>
	 */
        @Override
	public void resume() {
        for(ServiceDaemon serviceDaemon : m_serviceDaemons) {
			serviceDaemon.resume();
		}
	}

	/**
	 * <p>start</p>
	 */
        @Override
	public void start() {
        for(ServiceDaemon serviceDaemon : m_serviceDaemons) {
			serviceDaemon.start();
		}
	}

	/**
	 * <p>status</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
        @Override
	public Map<String, String> status() {
        Map<String, String> stati = new HashMap<String, String>();
        for(ServiceDaemon serviceDaemon : m_serviceDaemons) {
			stati.put(serviceDaemon.getName(), serviceDaemon.getStatusText());
		}
		return Collections.unmodifiableMap(stati);
	}

	/**
	 * <p>stop</p>
	 */
        @Override
	public void stop() {
        for(ServiceDaemon serviceDaemon : m_serviceDaemons) {
			stopService(serviceDaemon);
		}
		System.exit(0);
	}

	
	private void stopService(ServiceDaemon serviceDaemon) {
		try {
			serviceDaemon.stop();
		} catch (final Exception e) {
			LOG.warn("an error occurred while stopping service: {}", serviceDaemon.getName(), e);
		}
	}

}
