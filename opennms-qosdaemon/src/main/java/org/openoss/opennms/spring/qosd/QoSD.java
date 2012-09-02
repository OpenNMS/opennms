/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.openoss.opennms.spring.qosd;

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;
import org.openoss.opennms.spring.dao.OnmsAlarmOssjMapper;
import org.openoss.opennms.spring.dao.OssDao;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>QoSD interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface QoSD {

	/**
	 * provides an interface to OpenNMS which provides a unified api
	 *
	 * @param _ossDao the ossDao to set
	 */
	public abstract void setOssDao(OssDao _ossDao);

	/**
	 * Used by Spring Application context to pass in OnmsAlarmOssjMapper
	 * The OnmsAlarmOssjMapper class maps OpenNMS alarms to OSS/J alarms and events
	 *
	 * @param _onmsAlarmOssjMapper the onmsAlarmOssjMapper to set
	 */
	public abstract void setOnmsAlarmOssjMapper(
			OnmsAlarmOssjMapper _onmsAlarmOssjMapper);

	/**
	 * Used by Spring Application context to pass in AssetRecordDao
	 *
	 * @param ar a {@link org.opennms.netmgt.dao.AssetRecordDao} object.
	 */
	public abstract void setAssetRecordDao(AssetRecordDao ar);

	/**
	 * Used by Spring Application context to pass in NodeDaof
	 *
	 * @param nodedao a {@link org.opennms.netmgt.dao.NodeDao} object.
	 */
	public abstract void setNodeDao(NodeDao nodedao);

	/**
	 * Used by Spring Application context to pass in EventIpcManager
	 *
	 * @param eventIpcManager a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
	 */
	public abstract void setEventIpcManager(EventIpcManager eventIpcManager);

	/**
	 * Used by Spring Application context to pass in alarmDao
	 *
	 * @param alarmDao a {@link org.opennms.netmgt.dao.AlarmDao} object.
	 */
	public abstract void setAlarmDao(AlarmDao alarmDao);

	/**
	 * Used by Spring Application context to pass in AlarmListConnectionManager
	 *
	 * @param alcm a {@link org.openoss.opennms.spring.qosd.AlarmListConnectionManager} object.
	 */
	public abstract void setAlarmListConnectionManager(
			AlarmListConnectionManager alcm);
	
	/**
	 * Used by jmx mbean QoSD to pass in Spring Application context
	 *
	 * @param m_context - application context for this bean to use
	 */
	public abstract void setApplicationContext(ClassPathXmlApplicationContext m_context);

	/** Constant <code>NAME="OpenOSS.QoSD"</code> */
	public static final String NAME = "OpenOSS.QoSD";

	/**
	 * Method to set up the fiber
	 *  Note - not used in Spring activation
	 */
	public abstract void init();

	/**
	 * The start() method loads the configuration for the QosD daemon and registers for events
	 */
	public abstract void start();

	/**
	 * Stop method of fiber, called by OpenNMS when fiber execution is to
	 * finish. Its purpose is to clean everything up, e.g. close any JNDI or
	 * database connections, before the fiber's execution is ended.
	 */
	public abstract void stop();

	/**
	 * Resume method of fiber, called by OpenNMS to start the fiber up from
	 * a paused state.
	 */
	public abstract void resume();

	/**
	 * Pause method of fiber, called by OpenNMS to put the fiber in a
	 * suspended state until it can be later resumed.
	 */
	public abstract void pause();

	/**
	 *  Returns the Log category name
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public abstract String getName();

	/**
	 *  lets OpenNMS know what state the daemon is in
	 *
	 * @return a int.
	 */
	public abstract int getStatus();

	/**
	 * The OpenNMS event listener runs this routine when a
	 * new event is detected. This can be run on any event but only needs to run on
	 * uei.opennms.org/vacuumd/alarmListChanged
	 *
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	public abstract void onEvent(Event event);

	/**
	 * Registers an OpenNMS event listener with this class.
	 * When an event occurs, OpenNMS will call the onEvent()
	 * method of this object.
	 */
	public abstract void registerListener();

	/**
	 * Stops OpenNMS calling the onEvent method of this object when
	 * an event occurs.
	 */
	public abstract void unregisterListener();

	/**
	 * not used but needed for initialisation
	 *
	 * @return stats
	 */
	public abstract String getStats();
	
	/**
	 * A method to request an alarm list from the OpenNMS database
	 * and send the "unacked" alarms to the remote AlarmMonitor bean
	 */
	public abstract void sendAlarms();

}
