// This file is part of the OpenNMS(R) QoSD OSS/J interface.
//
// Copyright (C) 2006-2007 Craig Gallen, 
//                         University of Southampton,
//                         School of Electronics and Computer Science
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// See: http://www.fsf.org/copyleft/lesser.html
//

package org.openoss.opennms.spring.qosd.spring;

import java.util.Hashtable;
import java.util.Properties;

import javax.oss.fm.monitor.AlarmKey;
import javax.oss.fm.monitor.AlarmValue;

import org.apache.log4j.Logger;
import org.openoss.opennms.spring.qosd.AlarmListConnectionManager;
import org.openoss.opennms.spring.qosd.PropertiesLoader;
import org.openoss.opennms.spring.qosd.QoSDimpl2;
import org.openoss.ossj.jvt.fm.monitor.OOSSAlarmValue;
import org.openoss.ossj.fm.monitor.spring.AlarmMonitorDao;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * @author openoss
 *
 */
public class AlarmListConnectionManagerSpringImpl implements AlarmListConnectionManager {

	boolean init=false; // set true if init called
	Logger log;
	int status = DISCONNECTED; //  this changes to CONNECTED when the bean is instantiated 
	
	// ************************
    // Spring DAO setters
    // ************************
	
	AlarmMonitorDao alarmMonitorDao; // j2ee alarmMonitorDao - to be encapsulated in spring

	/**
	 * @param alarmMonitorDao the alarmMonitorDao to set
	 */
	public void setAlarmMonitorDao(AlarmMonitorDao alarmMonitorDao) {
		this.alarmMonitorDao = alarmMonitorDao;
	}
	
	/**
	 * used to hold a local reference to the application context from which this bean was started
	 */
	private ClassPathXmlApplicationContext m_context=null; // used to passapplication context to OssBeans

	/**
	 * Used by jmx mbean QoSD to pass in Spring Application context
	 * @param m_context - application conext for this bean to use
	 */
	public  void setApplicationContext(ClassPathXmlApplicationContext m_context){
		this.m_context = m_context;
	}
	
	// ************************
    // Constructor and methods
    // ************************
	
	/**
	 * The AlarmListConnectionManagerSpringImpl instantiates the AlarmMonitorBean within OpenNMS without Jboss
	 * This is an alternative to contacting the bean in jboss. This only deals with XML alarms
	 */
	public AlarmListConnectionManagerSpringImpl() {
	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.AlarmListConnectionManager#getStatus()
	 */
	public int getStatus() {
		
		return status;
	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.AlarmListConnectionManager#init(org.openoss.opennms.spring.qosd.PropertiesLoader, java.util.Properties)
	 */
	public void init(PropertiesLoader props, Properties env) {
		log = (Logger) QoSDimpl2.getLog();	//Get a reference to the QoSD logger

		try {
			if (log.isDebugEnabled()) log.debug("AlarmListConnectionManagerSpringImpl.init() initialising AlarmMonitorDao. Setting alarmMonitorDao.setLogName to:"+ log.getName());
			alarmMonitorDao.setLogName(log.getName());
			alarmMonitorDao.init();
			
		}
		catch (Exception ex) {
			log.error("AlarmListConnectionManagerSpringImpl.init() problem creating AlarmMonitorDao"+ ex);
		}
		init = true;		//inform the thread that it has been initialised 
							//and can execute the run() method.
		status = CONNECTED;
		
	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.AlarmListConnectionManager#kill()
	 */
	public void kill() {
		try {
			//alarmMonitorDao.ejbRemove(); TODO - NEED TO CLOSE BEAN PROPERLY
		}catch (Exception ex) {
			log.error("AlarmListConnectionManagerSpringImpl.init() problem stopping alarmMonitorDao"+ ex);
		}
		status = DISCONNECTED;

	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.AlarmListConnectionManager#reset_list(java.lang.String)
	 */
	public void reset_list(String _rebuilt_message) {
		this.alarmMonitorDao.rebuildAlarmList(_rebuilt_message );
	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.AlarmListConnectionManager#run()
	 */
	public void run() throws IllegalStateException {
		if(!init)
			throw new IllegalStateException("AlarmListSpringConnectionManagerThread - You must call init() before calling run()");
		
	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.AlarmListConnectionManager#send(java.util.Hashtable)
	 */
	public void send(Hashtable<AlarmKey,AlarmValue> alarmList) {
		this.alarmMonitorDao.updateAlarmList(alarmList);
	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.qosd.AlarmListConnectionManager#start()
	 */
	public void start() {
		this.run();

	}

	/**
	 * Makes a new empty alarm value object
	 * NOTE THIS IS A PATCH to proxy for JVTAlarmMonitorSession.makeAlarmValue()
	 */
	public  javax.oss.fm.monitor.AlarmValue makeAlarmValue(){
		return new OOSSAlarmValue();
	}

	/**
	 * Makes a new alarm value object pre-populated with internal objects
	 * which have been made from a local invarient specification. 
	 * NOTE THIS IS A PATCH to proxy for JVTAlarmMonitorSession
	 */
	public javax.oss.fm.monitor.AlarmValue makeAlarmValueFromSpec() {
		javax.oss.fm.monitor.AlarmValue alarmValueSpecification = (javax.oss.fm.monitor.AlarmValue)m_context.getBean("alarmValueSpecification");
			return alarmValueSpecification;
		}

}
