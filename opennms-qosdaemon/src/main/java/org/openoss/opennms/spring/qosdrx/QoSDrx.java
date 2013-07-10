/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.openoss.opennms.spring.qosdrx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.openoss.ossj.fm.monitor.spring.OssBeanRunner;
import org.openoss.ossj.fm.monitor.spring.OssBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * <p>QoSDrx class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class QoSDrx extends AbstractServiceDaemon {
    private static final Logger LOG = LoggerFactory.getLogger(QoSDrx.class);
    
	/**
	 * <p>Constructor for QoSDrx.</p>
	 */
	public QoSDrx() {
		super(NAME);
	}

	//---------------SPRING DAO DECLARATIONS----------------

	/**
	 * The first ossBeanRunner to start up in this Fiber which subsiquently starts up any other OssBeans.
	 * OpenNMS uses this to pass control into an OssBean configuration.	 */
	private OssBeanRunner initialOssBeanRunner=null;

	/**
	 * <p>Setter for the field <code>initialOssBeanRunner</code>.</p>
	 *
	 * @param initialOssBeanRunner the ossBeanRunner to set
	 */
	public void setInitialOssBeanRunner(OssBeanRunner initialOssBeanRunner) {
		this.initialOssBeanRunner = initialOssBeanRunner;
	}

	/**
	 * Used to obtain opennms asset information for inclusion in alarms
	 * @see org.opennms.netmgt.dao.api.AssetRecordDao
	 */
	@SuppressWarnings("unused")
	private static AssetRecordDao _assetRecordDao;


	/**
	 * Used by Spring Application context to pass in AssetRecordDao
	 *
	 * @param ar a {@link org.opennms.netmgt.dao.api.AssetRecordDao} object.
	 */
	public  void setAssetRecordDao(AssetRecordDao ar){
		_assetRecordDao = ar;
	}

	/**
	 * Used to obtain opennms node information for inclusion in alarms
	 * @see org.opennms.netmgt.dao.api.NodeDao 
	 */
	@SuppressWarnings("unused")
	private static NodeDao _nodeDao;

	/**
	 * Used by Spring Application context to pass in NodeDaof
	 *
	 * @param nodedao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
	 */
	public  void setNodeDao( NodeDao nodedao){
		_nodeDao = nodedao;
	}

	/**
	 * Used to search and update opennms alarm list
	 * @see org.opennms.netmgt.dao.api.AlarmDao
	 */
	@SuppressWarnings("unused")
	private static AlarmDao _alarmDao;

	/**
	 * Used by Spring Application context to pass in alarmDao
	 *
	 * @param alarmDao a {@link org.opennms.netmgt.dao.api.AlarmDao} object.
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
	 *
	 * @param context the application context
	 */
	public  void setApplicationContext(ClassPathXmlApplicationContext context){
		this.m_context = context;
	}



	/*---------------VARIABLE DECLARATIONS----------------*/


	public static final String NAME = "oss-qosdrx";
	private static String m_stats=null;  //not used but needed for initialisation	




	/*----------------START OF PUBLIC METHODS---------------*/




	/**
	 * Method to set up the fiber.
	 */
        @Override
	protected void onInit()
	{
		//Get a reference to the QosDrx logger instance assigned by OpenNMS
		LOG.info("QoSDrx.init(): Initialising QoSDrx");
		LOG.debug("QoSDrx.init(): Setting initialOssBeanRunner.setLogName(_logName) to:{}", LOG.getName());
		initialOssBeanRunner.setLogName(LOG.getName());

		if (initialOssBeanRunner.getStatus()==OssBean.UNINITIALISED){
			initialOssBeanRunner.setParentApplicationContext(m_context);
			initialOssBeanRunner.init();
		}
		LOG.info("QoSDrx.init(): QoSDrx initialised. Status= START_PENDING");
	}

	/**
	 * The start() method loads the configuration for the QoSDrx daemon and starts the initialOssBeanRunner
	 */
        @Override
	protected void onStart()
	{
		//Get a reference to the QoSDrx logger instance assigned by OpenNMS
		LOG.info("QoSDrx.start(): Starting QoSDrx");

		if (initialOssBeanRunner.getStatus()==OssBean.START_PENDING){
			initialOssBeanRunner.run();  // begins startup of OssBean
			while (initialOssBeanRunner.getStatus()!=OssBean.RUNNING); // wait for bean to start up
			LOG.info("QoSDrx.start(): OssBean Receiver Configurations: {}", initialOssBeanRunner.getOssBeanInstancesStatus());
		} else {
			LOG.error("QoSDrx.start(): initialOssBeanRunner not initialised status= STOPPED");
			this.setStatus(STOPPED);
			initialOssBeanRunner.stop(); // release any resources held by bean
		}
	}

	/**
	 * Stop method of fiber, called by OpenNMS when fiber execution is to
	 * finish. Its purpose is to clean everything up, e.g. close any JNDI or
	 * database connections, before the fiber's execution is ended.
	 */
        @Override
	protected void onStop()
	{
		//Get a reference to the QoSDrx logger instance assigned by OpenNMS
		LOG.info("QoSDrx.stop(): Stopping QoSDrx");
		initialOssBeanRunner.stop();
		while (initialOssBeanRunner.getStatus()!=OssBean.STOPPED); // wait for bean to stop
	}

	/**
	 * Pause method of fiber, called by OpenNMS to put the fiber in a
	 * suspended state until it can be later resumed.
	 *
	 * NOTE QoSDrx.pause() NOT IMPLEMENTED - this method does nothing and returns
	 */
        @Override
	protected void onPause()
	{
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		LOG.error("QoSDrx.pause(): NOT IMPLEMENTED - this method does nothing and returns");

		//LOG.info("Pausing QoSDrx");
		//status = PAUSE_PENDING;
		//status = PAUSED;
		//LOG.info("QoSDrx Paused");
	}

	/**
	 * Resume method of fiber, called by OpenNMS to start the fiber up from
	 * a paused state.
	 *
	 * NOTE QoSDrx.resume() NOT IMPLEMENTED - this method does nothing and returns
	 */
        @Override
	protected void onResume()
	{
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		LOG.error("QoSDrx.resume(): NOT IMPLEMENTED - this method does nothing and returns");

		//LOG.info("Resuming QoSDrx");
		//status = RESUME_PENDING;
		//registerListener();		//start responding to OpenNMS events
		//status = RUNNING;
		//LOG.info("QoSDrx Resumed");
	}

	/**
	 * Method to return statistics from the running receivers
	 *
	 * @return string representation of the statistics for the running receivers
	 */
	public String getRuntimeStatistics(){
		String runtimeStats="QoSDrx.getRuntimeStatistics(): NOT AVAILABLE";
		try {
			runtimeStats=initialOssBeanRunner.getRuntimeStatistics();
		}
		catch (Throwable ex){
			LOG.error("QoSDrx.getStats() Problem getting statistics:",ex);
		}
		return runtimeStats;
	}
	
	
	
	/**
	 * not used but needed for initialisation
	 *
	 * @return stats
	 */
	public String getStats() { 
		return (m_stats == null ? "QoSDrx.getStats(): No Stats Available" : m_stats.toString());
	}


}
