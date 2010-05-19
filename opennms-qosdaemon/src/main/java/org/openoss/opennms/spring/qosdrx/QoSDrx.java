// This file is part of the OpenNMS(R) QoSD OSS/J interface.
//
// Modifications:
//
// 2007 Jun 24: Mark unread fields as unused. - dj@opennms.org
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

package org.openoss.opennms.spring.qosdrx;

import org.apache.log4j.Logger;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.openoss.ossj.fm.monitor.spring.OssBeanRunner;
import org.openoss.ossj.fm.monitor.spring.OssBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class QoSDrx extends AbstractServiceDaemon implements PausableFiber {

	public QoSDrx() {
		super(NAME);
	}

	//---------------SPRING DAO DECLARATIONS----------------

	/**
	 * The first ossBeanRunner to start up in this Fiber which subsiquently starts up any other OssBeans.
	 * OpenNMS uses this to pass control into an OssBean configuration.	 */
	private OssBeanRunner initialOssBeanRunner=null;

	/**
	 * @param ossBeanRunner the ossBeanRunner to set
	 */
	public void setInitialOssBeanRunner(OssBeanRunner initialOssBeanRunner) {
		this.initialOssBeanRunner = initialOssBeanRunner;
	}

	/**
	 * Used to obtain opennms asset information for inclusion in alarms
	 * @see org.opennms.netmgt.dao.AssetRecordDao
	 */
	@SuppressWarnings("unused")
	private static AssetRecordDao _assetRecordDao;


	/**
	 * Used by Spring Application context to pass in AssetRecordDao
	 * @param ar 
	 */
	public  void setAssetRecordDao(AssetRecordDao ar){
		_assetRecordDao = ar;
	}

	/**
	 * Used to obtain opennms node information for inclusion in alarms
	 * @see org.opennms.netmgt.dao.NodeDao 
	 */
	@SuppressWarnings("unused")
	private static NodeDao _nodeDao;

	/**
	 * Used by Spring Application context to pass in NodeDaof
	 * @param nodedao 
	 */
	public  void setNodeDao( NodeDao nodedao){
		_nodeDao = nodedao;
	}

	/**
	 * Used to search and update opennms alarm list
	 * @see org.opennms.netmgt.dao.AlarmDao
	 */
	@SuppressWarnings("unused")
	private static AlarmDao _alarmDao;

	/**
	 * Used by Spring Application context to pass in alarmDao
	 * @param alarmDao
	 */
	public  void setAlarmDao( AlarmDao alarmDao){
		_alarmDao = alarmDao;
	}

	/**
	 * used to hold a local reference to the spring application context from which this bean was started
	 */
	private ClassPathXmlApplicationContext m_context=null; // used to passapplication context to OssBeans

	/**
	 * Used by jmx mbean QoSDrx to pass in Spring Application context
	 * @param alarmDao
	 */
	public  void setApplicationContext(ClassPathXmlApplicationContext m_context){
		this.m_context = m_context;
	}



	/*---------------VARIABLE DECLARATIONS----------------*/


	public static final String NAME = "OpenOSS.QoSDrx";
	private static String m_stats=null;  //not used but needed for initialisation	




	/*----------------START OF PUBLIC METHODS---------------*/




	/** Method to set up the fiber. */
	protected void onInit()
	{
		Logger log = getLog();	//Get a reference to the QosDrx logger instance assigned by OpenNMS
		log.info("QoSDrx.init(): Initialising QoSDrx");
		if (log.isDebugEnabled()) log.debug("QoSDrx.init(): Setting initialOssBeanRunner.setLogName(_logName) to:"+ log.getName());
		initialOssBeanRunner.setLogName(log.getName());

		if (initialOssBeanRunner.getStatus()==OssBean.UNINITIALISED){
			initialOssBeanRunner.setParentApplicationContext(m_context);
			initialOssBeanRunner.init();
		}
		log.info("QoSDrx.init(): QoSDrx initialised. Status= START_PENDING");
	}

	/**
	 * The start() method loads the configuration for the QoSDrx daemon and starts the initialOssBeanRunner
	 */
	protected void onStart()
	{
		Logger log = getLog();	//Get a reference to the QoSDrx logger instance assigned by OpenNMS
		log.info("QoSDrx.start(): Starting QoSDrx");

		if (initialOssBeanRunner.getStatus()==OssBean.START_PENDING){
			initialOssBeanRunner.run();  // begins startup of OssBean
			while (initialOssBeanRunner.getStatus()!=OssBean.RUNNING); // wait for bean to start up
			log.info("QoSDrx.start(): OssBean Receiver Configurations: "+initialOssBeanRunner.getOssBeanInstancesStatus());
		} else {
			log.error("QoSDrx.start(): initialOssBeanRunner not initialised status= STOPPED");
			this.setStatus(STOPPED);
			initialOssBeanRunner.stop(); // release any resources held by bean
		}
	}

	/**
	 * Stop method of fiber, called by OpenNMS when fiber execution is to
	 * finish. Its purpose is to clean everything up, e.g. close any JNDI or 
	 * database connections, before the fiber's execution is ended. 
	 */
	protected void onStop()
	{
		Logger log = getLog(); //Get a reference to the QoSDrx logger instance assigned by OpenNMS
		log.info("QoSDrx.stop(): Stopping QoSDrx");
		initialOssBeanRunner.stop();
		while (initialOssBeanRunner.getStatus()!=OssBean.STOPPED); // wait for bean to stop
	}

	/**
	 * Pause method of fiber, called by OpenNMS to put the fiber in a 
	 * suspended state until it can be later resumed.
	 * 
	 * NOTE QoSDrx.pause() NOT IMPLEMENTED - this method does nothing and returns
	 */
	protected void onPause()
	{
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		Logger log = getLog();	
		log.error("QoSDrx.pause(): NOT IMPLEMENTED - this method does nothing and returns");

		//log.info("Pausing QoSDrx");
		//status = PAUSE_PENDING;
		//status = PAUSED;
		//log.info("QoSDrx Paused");
	}

	/**
	 * Resume method of fiber, called by OpenNMS to start the fiber up from 
	 * a paused state.
	 * 
	 * NOTE QoSDrx.resume() NOT IMPLEMENTED - this method does nothing and returns
	 */
	protected void onResume()
	{
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		Logger log = getLog();	
		log.error("QoSDrx.resume(): NOT IMPLEMENTED - this method does nothing and returns");

		//log.info("Resuming QoSDrx");
		//status = RESUME_PENDING;
		//registerListener();		//start responding to OpenNMS events
		//status = RUNNING;
		//log.info("QoSDrx Resumed");
	}

	/**
	 *  Method to get the QoSDrx's logger from OpenNMS
	 */
	private static Logger getLog()
	{
		return ThreadCategory.getInstance(QoSDrx.class);	
	}

	/**
	 * Method to return statistics from the running receivers
	 * @return string representation of the statistics for the running receivers
	 */
	public String getRuntimeStatistics(){
		Logger log = getLog();	
		String runtimeStats="QoSDrx.getRuntimeStatistics(): NOT AVAILABLE";
		try {
			runtimeStats=initialOssBeanRunner.getRuntimeStatistics();
		}
		catch (Exception ex){
			log.error("QoSDrx.getStats() Problem getting statistics:",ex);
		}
		return runtimeStats;
	}
	
	
	
	/**
	 * not used but needed for initialisation 
	 * @return stats
	 */
	public String getStats() { 
		return (m_stats == null ? "QoSDrx.getStats(): No Stats Available" : m_stats.toString());
	}


}
