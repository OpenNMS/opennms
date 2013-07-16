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

import javax.oss.fm.monitor.NotifyAckStateChangedEvent;
import javax.oss.fm.monitor.NotifyAlarmCommentsEvent;
import javax.oss.fm.monitor.NotifyAlarmListRebuiltEvent;
import javax.oss.fm.monitor.NotifyChangedAlarmEvent;
import javax.oss.fm.monitor.NotifyClearedAlarmEvent;
import javax.oss.fm.monitor.NotifyNewAlarmEvent;
import javax.oss.util.IRPEvent;

import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.openoss.ossj.fm.monitor.spring.AlarmEventReceiverEventHandler;
import org.openoss.ossj.fm.monitor.spring.OssBeanAlarmEventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This provides a shell implimentation into which business methods can
 * be added
 *
 * @author ranger
 * @version $Id: $
 */
public class QoSDrxAlarmEventReceiverEventHandlerImplShell implements AlarmEventReceiverEventHandler{
    private static final Logger LOG = LoggerFactory.getLogger(QoSDrxAlarmEventReceiverEventHandlerImplShell.class);

	private static boolean initialised=false; // true if init() has initialised class

	// ************************
	// Spring DAO setters
	// ************************

	
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
	 * called to initialise the AlarmEventReceiverEventHandler
	 * must be called before all other classes
	 */
        @Override
	public void init(){
		initialised=true;
		// TODO add initialisation code if needed
	}
	
	// ************************
	// On Event Methods
	// ************************
	
	/** {@inheritDoc} */
        @Override
	public void onNotifyNewAlarmEvent(NotifyNewAlarmEvent nnae, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyNewAlarmEvent():";

		LOG.debug("{} Statistics: {}", logheader, callingAer.getRuntimeStatistics());
		if (!initialised ){
			LOG.error("{} event handler not initialised. init() must be called by receiver before handling any events", logheader);
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}
	
	/** {@inheritDoc} */
        @Override
	public void onNotifyClearedAlarmEvent(NotifyClearedAlarmEvent nclae, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyClearedAlarmEvent():";

		LOG.debug("{} Statistics: {}", logheader, callingAer.getRuntimeStatistics());
		if (!initialised ){
		        LOG.error("{} event handler not initialised. init() must be called by receiver before handling any events", logheader);
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}


	/** {@inheritDoc} */
        @Override
	public void onNotifyAckStateChangedEvent(NotifyAckStateChangedEvent nasce, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyAckStateChangedEvent(): ";

		LOG.debug("{} Statistics: {}", logheader, callingAer.getRuntimeStatistics());
		if (!initialised ){
		        LOG.error("{} event handler not initialised. init() must be called by receiver before handling any events", logheader);
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}

	/** {@inheritDoc} */
        @Override
	public void onNotifyAlarmCommentsEvent(NotifyAlarmCommentsEvent nace, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyAlarmCommentsEvent():";

		LOG.debug("{} Statistics: {}", logheader, callingAer.getRuntimeStatistics());
		if (!initialised ){
		        LOG.error("{} event handler not initialised. init() must be called by receiver before handling any events", logheader);
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}

	/** {@inheritDoc} */
        @Override
	public void onNotifyAlarmListRebuiltEvent(NotifyAlarmListRebuiltEvent nalre, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyAlarmListRebuiltEvent(): ";

		LOG.debug("{} Statistics: {}", logheader, callingAer.getRuntimeStatistics());
		if (!initialised ){
		    LOG.error("{} event handler not initialised. init() must be called by receiver before handling any events", logheader);
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}

	/** {@inheritDoc} */
        @Override
	public void onNotifyChangedAlarmEvent(NotifyChangedAlarmEvent nchae, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyChangedAlarmEvent():";

		LOG.debug("{} Statistics: {}", logheader, callingAer.getRuntimeStatistics());
		if (!initialised ){
		    LOG.error("{} event handler not initialised. init() must be called by receiver before handling any events", logheader);
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}


	/** {@inheritDoc} */
        @Override
	public void onUnknownIRPEvt(IRPEvent irpevt, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onUnknownIRPEvt():";

		LOG.debug("{} Statistics: {}", logheader, callingAer.getRuntimeStatistics());
		if (!initialised ){
		    LOG.error("{} event handler not initialised. init() must be called by receiver before handling any events", logheader);
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}

	/** {@inheritDoc} */
        @Override
	public void onunknownObjectMessage(Object objectMessage, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onunknownObjectMessage():";

		LOG.debug("{} Statistics: {}", logheader, callingAer.getRuntimeStatistics());
		if (!initialised ){
		    LOG.error("{} event handler not initialised. init() must be called by receiver before handling any events", logheader);
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}
	
}

