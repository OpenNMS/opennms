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

import javax.oss.fm.monitor.NotifyAckStateChangedEvent;
import javax.oss.fm.monitor.NotifyAlarmCommentsEvent;
import javax.oss.fm.monitor.NotifyAlarmListRebuiltEvent;
import javax.oss.fm.monitor.NotifyChangedAlarmEvent;
import javax.oss.fm.monitor.NotifyClearedAlarmEvent;
import javax.oss.fm.monitor.NotifyNewAlarmEvent;
import javax.oss.util.IRPEvent;
import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.openoss.ossj.fm.monitor.spring.OssBeanAlarmEventReceiver;
import org.openoss.ossj.fm.monitor.spring.AlarmEventReceiverEventHandler;


/**
 * This provides a shell implimentation into which business methods can 
 * be added
 */
public class QoSDrxAlarmEventReceiverEventHandlerImplShell implements AlarmEventReceiverEventHandler{

	private static boolean initialised=false; // true if init() has initialised class

	/**
	 *  Method to get the QoSDrx's logger from OpenNMS
	 */
	private static Logger getLog() {
		return (Logger)ThreadCategory.getInstance(QoSDrx.class);	
	}
	
	// ************************
	// Spring DAO setters
	// ************************

	
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
	 * called to initialise the AlarmEventReceiverEventHandler
	 * must be called before all other classes
	 */
	public void init(){
		initialised=true;
		// TODO add initialisation code if needed
	}
	
	// ************************
	// On Event Methods
	// ************************
	
	public void onNotifyNewAlarmEvent(NotifyNewAlarmEvent nnae, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		Logger log = getLog();	
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyNewAlarmEvent(): ";

		if (log.isDebugEnabled()) log.debug(logheader+"\n    Statistics:" +callingAer.getRuntimeStatistics());
		if (!initialised ){
			log.error(logheader+"event handler not initialised. init() must be called by receiver before handling any events");
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}
	
	public void onNotifyClearedAlarmEvent(NotifyClearedAlarmEvent nclae, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		Logger log = getLog();	
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyClearedAlarmEvent(): ";

		if (log.isDebugEnabled()) log.debug(logheader+"\n    Statistics:" +callingAer.getRuntimeStatistics());
		if (!initialised ){
			log.error(logheader+"event handler not initialised. init() must be called by receiver before handling any events");
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}


	public void onNotifyAckStateChangedEvent(NotifyAckStateChangedEvent nasce, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		Logger log = getLog();	
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyAckStateChangedEvent(): ";

		if (log.isDebugEnabled()) log.debug(logheader+"\n    Statistics:" +callingAer.getRuntimeStatistics());
		if (!initialised ){
			log.error(logheader+"event handler not initialised. init() must be called by receiver before handling any events");
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}

	public void onNotifyAlarmCommentsEvent(NotifyAlarmCommentsEvent nace, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		Logger log = getLog();	
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyAlarmCommentsEvent(): ";

		if (log.isDebugEnabled()) log.debug(logheader+"\n    Statistics:" +callingAer.getRuntimeStatistics());
		if (!initialised ){
			log.error(logheader+"event handler not initialised. init() must be called by receiver before handling any events");
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}

	public void onNotifyAlarmListRebuiltEvent(NotifyAlarmListRebuiltEvent nalre, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		Logger log = getLog();	
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyAlarmListRebuiltEvent(): ";

		if (log.isDebugEnabled()) log.debug(logheader+"\n    Statistics:" +callingAer.getRuntimeStatistics());
		if (!initialised ){
			log.error(logheader+"event handler not initialised. init() must be called by receiver before handling any events");
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}

	public void onNotifyChangedAlarmEvent(NotifyChangedAlarmEvent nchae, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		Logger log = getLog();	
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyChangedAlarmEvent(): ";

		if (log.isDebugEnabled()) log.debug(logheader+"\n    Statistics:" +callingAer.getRuntimeStatistics());
		if (!initialised ){
			log.error(logheader+"event handler not initialised. init() must be called by receiver before handling any events");
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}


	public void onUnknownIRPEvt(IRPEvent irpevt, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		Logger log = getLog();	
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onUnknownIRPEvt(): ";

		if (log.isDebugEnabled()) log.debug(logheader+"\n    Statistics:" +callingAer.getRuntimeStatistics());
		if (!initialised ){
			log.error(logheader+"event handler not initialised. init() must be called by receiver before handling any events");
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}

	public void onunknownObjectMessage(Object objectMessage, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		Logger log = getLog();	
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onunknownObjectMessage(): ";

		if (log.isDebugEnabled()) log.debug(logheader+"\n    Statistics:" +callingAer.getRuntimeStatistics());
		if (!initialised ){
			log.error(logheader+"event handler not initialised. init() must be called by receiver before handling any events");
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}
	
}

