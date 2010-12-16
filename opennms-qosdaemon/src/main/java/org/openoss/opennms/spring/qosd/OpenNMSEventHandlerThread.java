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


package org.openoss.opennms.spring.qosd;


import org.opennms.core.utils.ThreadCategory;
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
	public void run() throws IllegalStateException 	{
		ThreadCategory log = getLog();	
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
						if (log.isDebugEnabled()) log.debug("OpenNMSEventHandlerThread.run() thread waiting for interrupt");
						wait(); 
					}
				} catch ( InterruptedException e){
					if (log.isDebugEnabled()) log.debug("OpenNMSEventHandlerThread.run() thread woken up");
				}
				localupdateNCache=updateNCache;
				localsendList=sendList;
				updateNCache=false;
				sendList=false;
			}
			if (localupdateNCache) try {
				if (log.isDebugEnabled()) log.debug("OpenNMSEventHandlerThread.run() updating node list");
				ossDao.updateNodeCaches();
			} catch (Exception ex) {
				log.error("OpenNMSEventHandlerThread.run() Exception caught in ossDao.updateNodeCaches():", ex);
			}
			if (localsendList) try{
				if (log.isDebugEnabled()) log.debug("OpenNMSEventHandlerThread.run() updating and sending alarm list");
				ossDao.updateAlarmCacheAndSendAlarms();
			}
			catch (Exception ex) {
				log.error("OpenNMSEventHandlerThread.run() Exception caught in ossDao.updateAlarmCacheAndSendAlarms():", ex);
			}

		}
		if (log.isDebugEnabled()) log.debug("OpenNMSEventHandlerThread.run() STOPPED");

	}

	/**
	 * Initialise the Thread. Must be called before a call to run.
	 */
	synchronized public void init()	{
		ThreadCategory log = getLog();	
		if (log.isDebugEnabled()) log.debug("OpenNMSEventHandlerThread.init() initialised");
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
		ThreadCategory log = getLog();	
		if (log.isDebugEnabled()) log.debug("OpenNMSEventHandlerThread.kill() request received to kill thread");
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
		ThreadCategory log = getLog();	
		if (log.isDebugEnabled()) log.debug("OpenNMSEventHandlerThread.sendAlarmList() request received to update alarm list");
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
		ThreadCategory log = getLog();	
		if (log.isDebugEnabled()) log.debug("OpenNMSEventHandlerThread.updateNodeCache() request received to update node list");
		updateNCache=true;
		//instance.notify();
		notify();
	}

	private static ThreadCategory getLog() {
		return ThreadCategory.getInstance(OpenNMSEventHandlerThread.class);	
	}

}
