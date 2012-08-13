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

/** QosD
 * Adapated from Gavin Willingham's QoSD to make use of the OpenNMS alarm API.
 * Nicholas Dance 30 August 2005
 * 
 * TODO
 * alarm.getlasteventtime needed in openoss- ALARM APP NOT FULLY WORKING 
 * replacement for web.. Alarm.values in OnmsAlarm
 * asset - need 3 new fields naged object type, managed object instance applicationdn
 * idellay also state tag and other fields - comments etc
 *  
 *
 */

package org.openoss.opennms.spring.qosd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.oss.fm.monitor.AlarmKey;
import javax.oss.fm.monitor.AlarmValue;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.openoss.opennms.spring.dao.OnmsAlarmOssjMapper;
import org.openoss.opennms.spring.dao.OssDao;
import org.openoss.ossj.jvt.fm.monitor.OOSSAlarmValue;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * This class is a fiber scheduled by OpenNMS. It's purpose is to
 * collect OpenNMS alarms and transmit them to an OpenOSS QoS bean.
 *
 * The start() method loads the configuration for the QosD daemon and registers for events
 * Configuration is held in 4 files.
 * <p>      QoSD-configuration.xml
 * <p>      qosd.properties
 * <p>      opennms.conf
 * <p>      rmi.policy
 *
 * The Daemon starts in the following sequence;
 * <p>1. When the deamon starts it initialises the <CODE>AlarmListJ2eeConnectionManagerThread</CODE> and registers with the
 * AlarmMonitor bean in the application server.
 *
 * <p>2. It then calls the AlarmListJ2eeConnectionManagerThread.Reset_List in order to cause the interface to send an
 * AlarmListRebuiltEvent.
 * <p>The JNDI naming factory, JMS queues and ejb's conected to by the daemon are specified in the
 * qosd.properties file. The location of qosd.properties file is set by the JRE system variable
 * -DpropertiesFile which should be set when OpenNMS is started up. This is set in /etc/opennms.conf file
 *
 * Contents of opennms.conf:
 * <CODE>
 * <p>ADDITIONAL_MANAGER_OPTIONS='-Djava.security.policy=/opt/OpenNMS/etc/rmi.policy \
 * -DpropertiesFile=/opt/OpenNMS/etc/qosd.properties \
 * -Drx_propertiesFile=/opt/OpenNMS/etc/qosdrx.properties \
 * -Djava.naming.provider.url=jnp://jbossjmsserver1:1099 \
 * -Djava.naming.factory.initial=org.jnp.interfaces.NamingContextFactory \
 * -Djava.naming.factory.url.pkgs=org.jboss.naming '
 * </CODE>
 *
 * rmi.policy sets the security settings to allow the JVM to connect externally
 *
 * Contents of rmi.policy:
 * <CODE>grant{permission java.security.AllPermission;};</CODE>
 *
 * <p>3. The daemon then sends out the full current alarm list to the AlarmMonitor bean and  registers
 * with OpenNMS for events
 *
 * <p>The events used to run the QosD bean are determined by the file /etc/QoSD-configuration.xml
 * By default only the 'uei.opennms.org/vacuumd/alarmListChanged' uei is included in this file. This event
 * is generated when the <code>notifyOSSJnewAlarm</code> automation running in the vacuumd deamon
 * determines that the alarm list has changed. In normal operation there is a short delay between an alarm
 * entering the alarm list and the notifyOSSJnewAlarm automation picking it up. This can be significantly
 * shortend for high priority alarms if their raise uei's are also included in the QoSD-configuration.xml file.
 * However for most alarms this is not worth the effort.
 * <p>
 *
 * @author ranger
 * @version $Id: $
 */
public class QoSDimpl2 extends AbstractServiceDaemon implements EventListener, QoSD {

	/**
	 * <p>Constructor for QoSDimpl2.</p>
	 */
	public QoSDimpl2() {
		super(NAME);
	}

	/**
	 *  Method to get the QosD's logger from OpenNMS
	 *
	 * @return a {@link org.opennms.core.utils.ThreadCategory} object.
	 */
	public static ThreadCategory getLog() {
		return ThreadCategory.getInstance(QoSDimpl2.class);	
	}

	// ---------------SPRING DAO DECLARATIONS----------------

	private OssDao ossDao;

	/**
	 * {@inheritDoc}
	 *
	 * provides an interface to OpenNMS which provides a unified api
	 */
	public void setOssDao(OssDao _ossDao) {
		ossDao = _ossDao;
	}

	private OnmsAlarmOssjMapper onmsAlarmOssjMapper; 

	/**
	 * {@inheritDoc}
	 *
	 * Used by Spring Application context to pass in OnmsAlarmOssjMapper
	 * The OnmsAlarmOssjMapper class maps OpenNMS alarms to OSS/J alarms and events
	 */
	public void setOnmsAlarmOssjMapper(
			OnmsAlarmOssjMapper _onmsAlarmOssjMapper) {
		onmsAlarmOssjMapper = _onmsAlarmOssjMapper;
	}

	/**
	 * Used to obtain opennms asset information for inclusion in alarms
	 * @see org.opennms.netmgt.dao.AssetRecordDao
	 */
	@SuppressWarnings("unused")
	private AssetRecordDao assetRecordDao;

	/**
	 * {@inheritDoc}
	 *
	 * Used by Spring Application context to pass in AssetRecordDao
	 */
	public void setAssetRecordDao(AssetRecordDao ar){
		assetRecordDao = ar;
	}

	/**
	 * Used to obtain opennms node information for inclusion in alarms
	 * @see org.opennms.netmgt.dao.NodeDao 
	 */
	@SuppressWarnings("unused")
	private NodeDao nodeDao;

	/**
	 * {@inheritDoc}
	 *
	 * Used by Spring Application context to pass in NodeDaof
	 */
	public void setNodeDao( NodeDao nodedao){
		nodeDao = nodedao;
	}

	/**
	 * Used to register for opennms events
	 * @see org.opennms.netmgt.model.events.EventIpcManager
	 */
	private EventIpcManager eventIpcManager;

	/**
	 * {@inheritDoc}
	 *
	 * Used by Spring Application context to pass in EventIpcManager
	 */
	public void setEventIpcManager( EventIpcManager evtIpcManager){
		eventIpcManager = evtIpcManager;
	}

	/**
	 * Used to search and update opennms alarm list
	 * @see org.opennms.netmgt.dao.AlarmDao
	 */
	@SuppressWarnings("unused")
	private AlarmDao alarmDao;

	/**
	 * {@inheritDoc}
	 *
	 * Used by Spring Application context to pass in alarmDao
	 */
	public void setAlarmDao( AlarmDao almDao){
		alarmDao = almDao;
	}

	/**
	 * AlarmListConnectionManager connects to the alarm list and allows the QosD to send alarm updates
	 * This is used by spring to provide a proxy for the J2EE AlarmMonitor bean or for the local spring
	 * AlarmMonitor if J2EE is not being used
	 */
	private AlarmListConnectionManager alarmListConnectionManager;

	/**
	 * {@inheritDoc}
	 *
	 * Used by Spring Application context to pass in AlarmListConnectionManager
	 */
	public void setAlarmListConnectionManager( AlarmListConnectionManager alcm) {
		alarmListConnectionManager =alcm;
	}

	/**
	 * used to hold a local reference to the application context from which this bean was started
	 */
	private ClassPathXmlApplicationContext m_context=null; // used to passapplication context to OssBeans

	/**
	 * {@inheritDoc}
	 *
	 * Used by jmx mbean QoSD to pass in Spring Application context
	 */
	public void setApplicationContext(ClassPathXmlApplicationContext m_context){
		this.m_context = m_context;
	}

	/*---------------VARIABLE DECLARATIONS----------------*/

	/* Status is used to inform OpenNMS of what state the fiber is in.
	 * Which could be: PAUSED, PAUSE_PENDING, RESUME_PENDING, START_PENDING,
	 * STARTING, RUNNING, STOP_PENDING, STOPPED
	 */

	private QoSDConfiguration config = null;
	public PropertiesLoader props;
	private Properties env;

	private Hashtable<String,String> triggerUeiList;


	/** Constant <code>NAME="OpenOSS.QoSD"</code> */
	public static final String NAME = "OpenOSS.QoSD";
	private String m_stats=null;  //not used but needed for initialisation	

	// TODO - need to make this a configuration option
	public boolean useUeiList=false; // if true only alarms with event id's in the UEI list are sent
	OpenNMSEventHandlerThread openNMSEventHandlerThread;

	/*----------------START OF PUBLIC METHODS---------------*/

	/**
	 * Method to set up the fiber
	 *  Note - not used in Spring activation
	 */
	protected void onInit(){
		ThreadCategory log = getLog();	
		log.info("Initialising QoSD");
	}

	/**
	 * The start() method loads the configuration for the QosD daemon and registers for events
	 */
	protected void onStart() {
		String jnp_host;
		ThreadCategory log = getLog();		//Get a reference to the QosD logger instance assigned by OpenNMS

		log.info("Qosd.start(): Preparing to load configuration");

		// set application context for AlarmListConnectionManager
		try {
			if (log.isDebugEnabled()) log.debug("Qosd.start():setting application context for alarmListConnectionManager: m.context.toString:"+m_context.toString());                           
			alarmListConnectionManager.setApplicationContext(m_context);
		}catch ( Exception ex){
			throw new IllegalArgumentException("Qosd.start(): Error setting spring application context: "+ex);
		}


		//Load the configuration file QosD-Configuration.xml This file contains
		//all the UEIs that will be sent as alarms if useUeiList = true.
		try {
			config = QoSDConfigFactory.getConfig();
			log.info("QoSD QoSD-configuration.xml - Configuration Loaded Successfully");
			
			// loading list of UEI's which trigger this daemon
			triggerUeiList = new Hashtable<String,String>();
			String[] temp = config.getEventlist().getUei();
			for(int i = 0; i < temp.length; i++)
				triggerUeiList.put(temp[i], "1");
						
		} catch(MarshalException mrshl_ex) {
			//write an error message to the log file
			log.error("Qosd.start(): Marshal Exception thrown whilst getting QoSD configuration\n" +
					"\t\t\t\tEnsure tags have correct names", mrshl_ex);
			throw new UndeclaredThrowableException(mrshl_ex);
		} catch(ValidationException vldtn_ex){
			log.error("Qosd.start(): Validation Exception thrown whilst getting QoSD configuration\n" +
					"\t\t\t\tMake sure all the tags are formatted correctly within QoSD-configuration.xml", vldtn_ex);
			throw new UndeclaredThrowableException(vldtn_ex);
		} catch(IOException io_ex){
			String configFile = System.getProperty("opennms.home");		//Get the OpenNMS home directory
			if(configFile.endsWith(java.io.File.separator))				//if the is '/' at the end
				configFile.substring(0, configFile.length() - 1);		//remove it so that
			configFile += "/etc/QoSD-configuration.xml";			//we can compose a valid filename
			log.error("Qosd.start(): Failed to load configuration file: " + configFile +
					"\n\t\t\t\tMake sure that it exists", io_ex);
			throw new UndeclaredThrowableException(io_ex);
		}

		if (useUeiList) log.info("Qosd.start(): useUeiList = true = using QoSD QoSD-configuration.xml UEI list selects which alarms are sent");
		

		try {
			//Load the properties file containing the JNDI connection address etc.
			props = PropertiesLoader.getInstance();
		}
		catch(FileNotFoundException fnf_ex) {
			//record in log that the properties file could not be found
			String propertiesFilename = System.getProperty("propertiesFile");
			log.error("Qosd.start(): Could not find properties file: " + propertiesFilename, fnf_ex);
			throw new UndeclaredThrowableException(fnf_ex);
		}
		catch(IOException io_ex) {
			//record in log that the properties file could not be read
			String propertiesFilename = System.getProperty("propertiesFile");
			log.error("Qosd.start(): Could not read from properties file: " + propertiesFilename + 
					"\n\t\t\t\tPlease check the file permissions", io_ex);
			throw new UndeclaredThrowableException(io_ex);
		}

		log.info("Qosd.start(): QosD Properties File Loaded");

		if(System.getSecurityManager() == null)
			System.setSecurityManager(new RMISecurityManager());

		/*The following if-statement checks if the naming provider property exists in
		 * the properties file. If it does then it stores it in the jnp_host string. If
		 * it doesn't then it uses a default naming provider string "jbossjmsserver1:1099" and
		 * assigns it to jnp_host, stating this in the log file.
		 */
		if(props.getProperty("org.openoss.opennms.spring.qosd.naming.provider") != null){
			jnp_host = (String)props.getProperty("org.openoss.opennms.spring.qosd.naming.provider");
			log.info("Using JNP: " + jnp_host);
		}
		else {
			log.warn("Qosd.start(): Naming provider property not set, Using default: jnp://jbossjmsserver1:1099");
			jnp_host = "jnp://jbossjmsserver1:1099";
		}

		/* Fill a new properties object with the properties supplied in 
		 * the properties file.
		 */
		env = new Properties();
		env.setProperty("java.naming.provider.url", jnp_host);
		env.setProperty("java.naming.factory.initial", props.getProperty("org.openoss.opennms.spring.qosd.naming.contextfactory"));
		env.setProperty("java.naming.factory.url.pkgs", props.getProperty("org.openoss.opennms.spring.qosd.naming.pkg"));

		// start a new connection manager thread
		try {
			alarmListConnectionManager.init(props, env);
			alarmListConnectionManager.start();
			//wait until the AlarmListConnectionManager has connected to bean		
			log.info("Qosd.start(): Waiting Connection Manager Thread to get JMS connection");
			while(alarmListConnectionManager.getStatus() != AlarmListConnectionManager.CONNECTED);
			log.info("Qosd.start(): Connection Manager Thread JMS connection successfully registered");

			log.info("Qosd.start(): openNMS just restarted - sending alarm list rebuilt event");
			//send alarm list rebuilt event to EJB via the connection manager thread.
			alarmListConnectionManager.reset_list("openNMS just restarted - alarm list rebuilt. Time:"+new Date()); // send an alarm list rebuilt event
		}
		catch(Throwable iae) {
			log.error("Qosd.start(): Exception caught starting alarmListConnectionManager", iae);
			throw new UndeclaredThrowableException(iae);
		}
		
		// setting up ossDao to access the OpenNMS database
		try {
			if (log.isDebugEnabled()) log.debug("Qosd.start(): Using ossDao instance:"+ (ossDao == null ? "IS NULL" : ossDao.toString()));
			log.info("Qosd.start(): Initialising the Node and alarm Caches");
			ossDao.init();
//	TODO REMOVE
//			ossDao.updateNodeCaches();
			log.info("Qosd.start(): Set up ossDao call back interface to QoSD for forwarding changes to alarm list");
			ossDao.setQoSD(this);
		} catch (Throwable ex) {
			log.error("Qosd.start(): Exception caught setting callback interface from ossDao", ex);
			throw new UndeclaredThrowableException(ex);
		}

		// set up thread to handle incoming OpenNMS events
		log.info("Qosd.start(): initialising OpenNMSEventHandlerThread");
		try {
			openNMSEventHandlerThread= new OpenNMSEventHandlerThread();
			openNMSEventHandlerThread.setOssDao(ossDao);
			openNMSEventHandlerThread.init();
			openNMSEventHandlerThread.start();
		} catch (Throwable ex) {
			log.error("Qosd.start(): Exception caught initialising OpenNMSEventHandlerThread", ex);
			throw new UndeclaredThrowableException(ex);
		}

		//send all the alarmList to EJB via the connection manager thread.
		log.info("Qosd.start(): openNMS just restarted - sending all alarms in rebuilt alarm list");
		try {
			//this.sendAlarms(); // interface has just started up. Send all alarms
			openNMSEventHandlerThread.sendAlarmList();
		} catch ( Exception e) {
			log.error("Qosd.start(): problem sending initial alarm list Error:", e);
		}

		// register listener for OpenNMS events
		log.info("Qosd.start(): Starting OpenNMS event listener");
		try {
			registerListener();
		} catch ( Exception e) {
			log.error("Qosd.start(): problem registering event listener Error:", e);
		}
				
		// TODO - replace ack handler code with QoSDrx receiver code

		log.info("QoSD Started");
	}


	/**
	 * Stop method of fiber, called by OpenNMS when fiber execution is to
	 * finish. Its purpose is to clean everything up, e.g. close any JNDI or
	 * database connections, before the fiber's execution is ended.
	 */
	protected void onStop() {
		ThreadCategory log = getLog();		//Get a reference to the QoSD logger

		log.info("Stopping QosD");

		try {
			unregisterListener();	//unregister the OpenNMS event listener
		} catch(Throwable ex) {
			log.error("stop() Error unregistering the OpenNMS event listener. Error:", ex);
		}


		try {
			openNMSEventHandlerThread.kill();
		} catch(Throwable ex) {
			log.error("stop() Error killing openNMSEventHandlerThread. Error:", ex);
		}

		try {
			alarmListConnectionManager.kill();	//kill the connection thread
		} catch(Throwable ex) {
			log.error("stop() Error killing alarmListConnectionManager. Error:", ex);
		}

		log.info("QosD Stopped");
	}


	/**
	 * Resume method of fiber, called by OpenNMS to start the fiber up from
	 * a paused state.
	 */
	protected void onResume() {
		ThreadCategory log = getLog();		//Get a reference to the QosD logger
		//instance assigned by OpenNMS
		log.info("Resuming QosD");
		registerListener();		//start responding to OpenNMS events
		log.info("QosD Resumed");
	}


	/**
	 * Pause method of fiber, called by OpenNMS to put the fiber in a
	 * suspended state until it can be later resumed.
	 */
	protected void onPause() {
		ThreadCategory log = getLog();		//Get a reference to the QosD logger
		//instance assigned by OpenNMS
		log.info("Pausing QosD");
		unregisterListener();		//stop responding to OpenNMS events
		log.info("QosD Paused");
	}

	/**
	 * Registers an OpenNMS event listener with this class.
	 * When an event occurs, OpenNMS will call the onEvent()
	 * method of this object.
	 */
	public void registerListener() 	{
		ThreadCategory log =getLog();
		List<String> ueiList = new ArrayList<String>();
		String[] temp = config.getEventlist().getUei();
		for(int i=0; i<temp.length; i++)
			ueiList.add(temp[i]);

		log.info("QosD Registering for " + temp.length + " types of event");
		eventIpcManager.addEventListener(this, ueiList);

	}

	/**
	 * Stops OpenNMS calling the onEvent method of this object when
	 * an event occurs.
	 */
	public void unregisterListener() {
		ThreadCategory log = getLog();

		log.info("QosD Unregistering for events");
		eventIpcManager.removeEventListener(this);
	}

	/**
	 * {@inheritDoc}
	 *
	 * The OpenNMS event listener runs this routine when a
	 * new event is detected. This can be run on any event but only needs to run on
	 * uei.opennms.org/vacuumd/alarmListChanged
	 */
	public void onEvent(Event event) {

		ThreadCategory log = getLog();
		if (log.isDebugEnabled()) log.debug("Qosd.onEvent: OpenNMS Event Detected by QosD. uei '"+ event.getUei()+"' Dbid(): "+event.getDbid()+"  event.getTime(): " + event.getTime());

		String s = event.getUei();
		if (s==null) return;

		if (    EventConstants.NODE_ADDED_EVENT_UEI.equals(s) ||
				EventConstants.NODE_LABEL_CHANGED_EVENT_UEI.equals(s) ||
				EventConstants.NODE_DELETED_EVENT_UEI.equals(s) ||
				EventConstants.ASSET_INFO_CHANGED_EVENT_UEI.equals(s) ) {
			try {
				if (log.isDebugEnabled()) log.debug("QosD.onEvent Event causing update to node list");
				openNMSEventHandlerThread.updateNodeCache();
				return;
			} catch ( Exception ex){
				log.error("Qosd.onEvent. Problem calling openNMSEventHandlerThread.updateNodeCache(). Error:"+ ex);
				return;
			}
		} 

		if (event.getUei().equals("uei.opennms.org/vacuumd/alarmListChanged")) {
			if (log.isDebugEnabled()) log.debug("QosD.onEvent received 'uei.opennms.org/vacuumd/alarmListChanged' event; Updating alarm list");
		} else { 
			// used code from AlarmWriter.java Check value of <logmsg> attribute 'dest', if set to
			// "donotpersist" then simply return, the uei is not to be persisted to the database		
			// The uei.opennms.org/vacuumd/alarmListChanged event must be set to be reised
			// as auto-action from vacumd-configuration.xml and is called
			// when vacumd updates the current ( alarm not acknowledgd and cleared ) alarm list
			// note alarmListChanged event may be marked as 'donotpersist' so checked first
			try {
				// this section prints out events received which are not uei.opennms.org/vacuumd/alarmListChanged
				// return if a donotpersist event
				if (event.getLogmsg().getDest().equals("donotpersist")) {
					if (log.isDebugEnabled()) log.debug("QosD.onEvent Ignoring event marked as 'doNotPersist'. Event Uei:"+event.getUei());
					return;
				}				
				// AlarmData should not be null if QoSD-configuration.xml is set up only to receive raise 
				// and not clearing alarms
				if (event.getAlarmData().getAlarmType() == 2){
					if (log.isDebugEnabled()) log.debug("Qosd.onEvent: uei '"+ event.getUei()+"' Dbid(): "+event.getDbid()+" alarm type = 2 (clearing alarm) so ignoring.");
					return; 
				}
			} catch (NullPointerException e) {
				log.error("Qosd.onEvent: uei '" + event.getUei() +"' Dbid(): "+event.getDbid()+"' problem dealing with event. Check QoSD-configuration.xml.");
				return;
			} 
		}

		// This forces the ossDao to update it's list on this event and call back to sendAlarms() to send the
		// updated alarm list.
		try {
			if (log.isDebugEnabled()) log.debug("QosD.onEvent calling openNMSEventHandlerThread.sendAlarmList() to update list.");
			//ossDao.updateAlarmCacheAndSendAlarms();
			openNMSEventHandlerThread.sendAlarmList();
		} catch ( Exception ex){
			log.error("Qosd.onEvent. Problem calling openNMSEventHandlerThread.sendAlarmList(). Error:"+ ex);
		}
	}


	/**
	 * A method to request an alarm list from the OpenNMS database using the ossDao,
	 * convert them to OSS/J alarms using the onmsAlarmOssjMapper and send the OSS/J alarms
	 * using the alarm list connection manager (alcm) to  update the the AlarmMonitor bean.
	 * This is called from ossDao every time there is an update to the database.
	 */
	public void sendAlarms() {
		ThreadCategory log = getLog();	

		Hashtable<AlarmKey,AlarmValue> ossjAlarmUpdateList = new Hashtable<AlarmKey,AlarmValue>();
		OnmsAlarm[] onmsAlarmUpdateList = null;
		AlarmValue ossjAlarm;

		try {
			if (log.isDebugEnabled()) log.debug("sendAlarms() using ossDao to get current alarm list");
			onmsAlarmUpdateList=ossDao.getAlarmCache();
		} catch(Throwable ex) {
			//problems contacting the PostgreSQL database
			log.error("sendAlarms() Cannot retrieve alarms from ossDao.getAlarmCache()", ex);
			throw new UndeclaredThrowableException(ex, "sendAlarms() Cannot retrieve alarms from ossDao.getAlarmCache()");
		}

		if (log.isDebugEnabled()) log.debug("sendAlarms() Alarms fetched. Processing each alarm in list.");
		// Convert the OnmsAlarm array alarmBuf to a hashtable using the alarmid as the key.
		try {
			for(int i = 0; i < onmsAlarmUpdateList.length; i++) {
				if (log.isDebugEnabled()) log.debug("sendAlarms() processing an OpenNMS alarm:");

				// if useUeiList is true only the alarms whose UEI's are listed in the 
				// QosD-configuration.xml file will be included in the list. 
				if (useUeiList) {
					if (log.isDebugEnabled()) log.debug("sendAlarms() useUeiList= true: using UeiList to determine alarms to send");
					if( null == triggerUeiList.get(onmsAlarmUpdateList[i].getUei()) ) {
						if (log.isDebugEnabled()) log.debug("sendAlarms() alarm UEI not in QosD-configuration.xml. Not sending. alarmID:"+ onmsAlarmUpdateList[i].getId()+" alarmUEI:"+onmsAlarmUpdateList[i].getUei() );
						continue; // ignore this event and return
					}
					if (log.isDebugEnabled()) log.debug("sendAlarms() alarm UEI is in QosD-configuration.xml. Trying to send alarmID:"+ onmsAlarmUpdateList[i].getId()+" alarmUEI:"+onmsAlarmUpdateList[i].getUei() );
				}

				if (onmsAlarmUpdateList[i].getAlarmType()!= 1)	{
					if (log.isDebugEnabled()) log.debug("sendAlarms() Alarm AlarmType !=1 ( not raise alarm ) Not sending alarmID:"+ onmsAlarmUpdateList[i].getId()+" :alarmBuf[i].getQosAlarmState()=: "+ onmsAlarmUpdateList[i].getQosAlarmState());
					continue;
				} else {
					if (log.isDebugEnabled()) log.debug("sendAlarms() Alarm AlarmType==1 ( raise alarm ) Sending alarmID:"+ onmsAlarmUpdateList[i].getId()+" :alarmBuf[i].getQosAlarmState()=: "+ onmsAlarmUpdateList[i].getQosAlarmState());
					try {

						// Code which creates the OSSJ AlarmValue from the Spring OSS?J AlarmValue Specification
						if (log.isDebugEnabled()) log.debug("sendAlarms(): generating the OSS/J alarm specification:");
						ossjAlarm = alarmListConnectionManager.makeAlarmValueFromSpec();
						if (log.isDebugEnabled()) log.debug("sendAlarms(): OSS/J alarm specification:"+	OOSSAlarmValue.converttoString(ossjAlarm));

						// Code which creates the OSSJ AlarmValue from the Spring OSS/J AlarmValue Specification
						if (log.isDebugEnabled()) log.debug("sendAlarms(): onmsAlarmOssjMapper.populateOssjAlarmFromOpenNMSAlarm:");
						ossjAlarm = onmsAlarmOssjMapper.populateOssjAlarmFromOpenNMSAlarm(ossjAlarm, onmsAlarmUpdateList[i]);
						if (log.isDebugEnabled()) log.debug("buildList(): alarm specifcation:"+	OOSSAlarmValue.converttoString(ossjAlarm));

						// TODO selector on ACKNOWLEDGED and CLEARED - currently always sends all alarms in list
						if (true) try {
							// alarms which are ACKNOWLEDGED and CLEARED are included in this current alarm list
							if (log.isDebugEnabled()) log.debug("sendAlarms() including ACKNOWLEDGED and CLEARED alarms in alarm in list");
							ossjAlarmUpdateList.put(ossjAlarm.getAlarmKey(), ossjAlarm); 
						} catch (Throwable e) {
							log.error("sendAlarms() error putting alarm in alarmList", e);
						}

						//TODO - THIS CODE NEVER RUN
						/*
						else try {
							// no alarms which are ACKNOWLEDGED and CLEARED are included in this current alarm list
							if (ossjAlarm.getAlarmAckState() == javax.oss.fm.monitor.AlarmAckState.UNACKNOWLEDGED ) {
								ossjAlarmUpdateList.put(ossjAlarm.getAlarmKey(), ossjAlarm); // put all unacknowledged alarms in list
							} 
							else if (ossjAlarm.getPerceivedSeverity() != javax.oss.fm.monitor.PerceivedSeverity.CLEARED ){
								ossjAlarmUpdateList.put(ossjAlarm.getAlarmKey(), ossjAlarm);	// put all uncleared acknowledged alarms in list			
							}
						} catch (Throwable e) {
							log.error("sendAlarms() error in alarmACKState or PercievedSeverity - check alarm definitons", e);
						}*/
					} catch (Throwable ex) {
						log.error("sendAlarms() error trying to populate alarm - alarm disguarded - check alarm definitons", ex);
					}
				}
			}
		}
		catch (Throwable ex){
			log.error("Qosd.sendAlarms(): Problem when building alarm list:", ex);
			throw new UndeclaredThrowableException(ex, "Qosd.sendAlarms(): Problem when building alarm list");
		}
		try{
			// debug code prints out alarm list to be sent if enabled
			if (log.isDebugEnabled()) {
				log.debug("QosD sendAlarms() - Alarm list built:");
				log.debug("QosD sendAlarms() - ******* Alarm List to be sent : primary keys");
				for (AlarmKey key : ossjAlarmUpdateList.keySet()) {
					AlarmValue a = ossjAlarmUpdateList.get(key);
					log.debug("QosD sendAlarms() key : " + key.getPrimaryKey() +"  AlarmValue.getAlarmChangedTime: " + a.getAlarmChangedTime()); 
				}
				log.debug("QosD sendAlarms() - ******* END OF LIST");
				log.debug("QosD sendAlarms() Sending alarm list to bean");
			}		
			//send the alarmList to Ossbeans EJB or local runner via the connection manager thread.

			alarmListConnectionManager.send(ossjAlarmUpdateList);
		}
		catch (Throwable ex){
			log.error("Qosd.sendAlarms(): Problem when sending alarm list:", ex);
			throw new UndeclaredThrowableException(ex,"Qosd.sendAlarms(): Problem when sending alarm list");
		}
	}

	
	/**
	 * not used but needed for initialization
	 *
	 * @return stats
	 */
	public String getStats() { 
		return (m_stats == null ? "No Stats Available" : m_stats.toString()); 
	}

}
