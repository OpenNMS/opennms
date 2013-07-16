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

package org.openoss.opennms.spring.qosd;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openoss.opennms.spring.dao.OssDao;

/**
 * This class provides a thread to decouple the OpenNMS event handling from updates to
 * the Node list and the Alarm List in the ossDao. If OpenNMS issues multiple events to the QoSD
 * event handler which cause it to want to update the alarm list or update the node list, this Thread latches
 * the event and will request the ossDao to update it's cache and send any new alarms.
 * If further events occur while the ossDao is updating, these will be latched until the
 * process completes. This prevents every new opennms alarm causing a call to the ossDao while it is running
 * which would otherwise back up a queue of requests against it's synchronized methods.
 *
 * @author ranger
 * @version $Id: $
 */
public class OpenNMSEventHandlerThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSEventHandlerThread.class);

	// ---------------SPRING DAO DECLARATIONS----------------


	private static OssDao ossDao;

	/**
	 * provides an interface to OpenNMS which provides a unified api
	 *
	 * @param _ossDao the ossDao to set
	 */
	public void setOssDao(OssDao _ossDao) {
		ossDao = _ossDao;
	}


	// Business Methods
	private static boolean sendList=false; // true if alarm list is to be updated and sent when the thread wakes up
	private static boolean updateNCache=false; // true if node list is to be updated when the thread wakes up
	private static boolean runThread=false; // true if this thread is to keep running. If false, the thread will exit
	private static boolean init=false;     // true if this thread has been initialised
	//private static OpenNMSEventHandlerThread instance =null; // used to hold instance of this thread

	/**
	 * Run method loops until kill() called.
	 * It wakes up if sendAlarmList() is called and updates the alarmlist
	 * It wakes up if updateNodeCache() is called and updates the node list
	 * init() must be called before the run() method.
	 *
	 * @throws java.lang.IllegalStateException if any.
	 */
        @Override
	public void run() throws IllegalStateException 	{
		//instance=this;
		boolean localupdateNCache=false;
		boolean localsendList=false;

		// if the init variable is false then the thread has not been initialised
		if(!init)
			throw new IllegalStateException("OpenNMSEventHandlerThread.run() - You must call init() before calling run()");

		while (runThread){
			synchronized(this) { 
				try{
					// test to see if there have been more requests to update the list while updating the list
					if ((sendList==false)&&(updateNCache==false)){
						LOG.debug("OpenNMSEventHandlerThread.run() thread waiting for interrupt");
						wait(); 
					}
				} catch ( InterruptedException e){
					LOG.debug("OpenNMSEventHandlerThread.run() thread woken up");
				}
				localupdateNCache=updateNCache;
				localsendList=sendList;
				updateNCache=false;
				sendList=false;
			}
			if (localupdateNCache) try {
				LOG.debug("OpenNMSEventHandlerThread.run() updating node list");
				ossDao.updateNodeCaches();
			} catch (Throwable ex) {
				LOG.error("OpenNMSEventHandlerThread.run() Exception caught in ossDao.updateNodeCaches()", ex);
			}
			if (localsendList) try{
				LOG.debug("OpenNMSEventHandlerThread.run() updating and sending alarm list");
				ossDao.updateAlarmCacheAndSendAlarms();
			}
			catch (Throwable ex) {
				LOG.error("OpenNMSEventHandlerThread.run() Exception caught in ossDao.updateAlarmCacheAndSendAlarms()", ex);
			}

		}
		LOG.debug("OpenNMSEventHandlerThread.run() STOPPED");

	}

	/**
	 * Initialise the Thread. Must be called before a call to run.
	 */
	synchronized public void init()	{
		LOG.debug("OpenNMSEventHandlerThread.init() initialised");
		init = true;	//inform the thread that it has been initialised 
		//and can execute the run() method.
		runThread=true;
	}



	/**
	 * Stop execution of the thread. Will complete any current update before exiting
	 */
	synchronized public void kill() {
		// Thread.stop() is unsafe so ending run method by changing
		// a status variable that tells the run method to return
		// and end execution.
		LOG.debug("OpenNMSEventHandlerThread.kill() request received to kill thread");
		runThread=false;
		//instance.notify();
		notify();
	}

	/**
	 * If called, this method will request that the ossDao Alarm Cache is updated from the OpenNMS database
	 * and sent to the QoSD for processing.
	 * Note that multiple calls while the update process is running will be latched ( i.e. not queued) and result
	 * another update when the previous one completes
	 */
	synchronized public void sendAlarmList(){
		LOG.debug("OpenNMSEventHandlerThread.sendAlarmList() request received to update alarm list");
		sendList=true;
		//instance.notify();
		notify();
	}



	/**
	 * If called, this method will request that the ossDao Node Cache is updated from the OpenNMS database
	 * and sent to the QoSD for processing.
	 * Note that multiple calls while the update process is running will be latched ( i.e. not queued) and result
	 * in only one update when the previous one completes
	 */
	synchronized public void updateNodeCache(){
		LOG.debug("OpenNMSEventHandlerThread.updateNodeCache() request received to update node list");
		updateNCache=true;
		//instance.notify();
		notify();
	}
}
