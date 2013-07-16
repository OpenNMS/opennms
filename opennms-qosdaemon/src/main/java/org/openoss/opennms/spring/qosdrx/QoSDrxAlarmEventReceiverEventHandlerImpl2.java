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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.HashMap;

import javax.oss.fm.monitor.NotifyAckStateChangedEvent;
import javax.oss.fm.monitor.NotifyAlarmCommentsEvent;
import javax.oss.fm.monitor.NotifyAlarmListRebuiltEvent;
import javax.oss.fm.monitor.NotifyChangedAlarmEvent;
import javax.oss.fm.monitor.NotifyClearedAlarmEvent;
import javax.oss.fm.monitor.NotifyNewAlarmEvent;
import javax.oss.util.IRPEvent;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.openoss.opennms.spring.dao.OnmsAlarmOssjMapper;
import org.openoss.opennms.spring.dao.OssDao;
import org.openoss.opennms.spring.dao.OssDaoOpenNMSImpl;
import org.openoss.ossj.fm.monitor.spring.AlarmEventReceiverEventHandler;
import org.openoss.ossj.fm.monitor.spring.OssBeanAlarmEventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Work in progress - implementing business methods
 *
 * @author ranger
 * @version $Id: $
 */
public class QoSDrxAlarmEventReceiverEventHandlerImpl2 implements AlarmEventReceiverEventHandler{
    private static final Logger LOG = LoggerFactory.getLogger(QoSDrxAlarmEventReceiverEventHandlerImpl2.class);

	private boolean initialised = false; // true if init() has initialised class

	// ************************
	// Spring DAO setters
	// ************************

	/**
	 * if <code>alarmUpdateBehaviour</code> is set to SPECIFY_OUTSTATION
	 * the receiver name will be used as the node name which will be updated with
	 * alarms from this receiver. Usually this is set to the name of the node
	 * associated with the outstation but it can be set to a node which is a 
	 * catch all for received alarms ( i.e. the local host perhaps )
	 */
	public static int SPECIFY_OUTSTATION=1;

	/**
	 * if <code>alarmUpdateBehaviour</code> is set to USE_TYPE_INSTANCE
	 * the alarm will be created with the node name corrsponding to a concatenation 
	 * of the ManagedObjectID and ManagedObjectType. If these cannot be found
	 * then the alarm will default to  the outstation node
	 */
	public static int USE_TYPE_INSTANCE=2;

	/*
	 * determines the alarm update behaviour. Must be set to <code>SPECIFY_OUTSTATION</code> or
	 * <code>USE_TYPE_INSTANCE</code>
	 */
	private Integer almUpdateBehaviour= null;

	/*
	 * string value of almUpdateBehaviour. Must be set to 
	 * <code>"USE_TYPE_INSTANCE"</code>
	 * or
	 * <code>"SPECIFY_OUTSTATION"</code>
	 */
	private String alarmUpdateBehaviour=null;

	/**
	 * Used by Spring Application context to pass in alarmUpdateBehaviour as a string
	 *
	 * @param _alarmUpdateBehaviour must be
	 * <code>"USE_TYPE_INSTANCE"</code>
	 * or
	 * <code>"SPECIFY_OUTSTATION"</code>
	 */
	public  void setalarmUpdateBehaviour(String _alarmUpdateBehaviour){
		if (_alarmUpdateBehaviour==null) throw new IllegalArgumentException("QoSDrxAlarmEventReceiverEventHandlerImpl().setalarmUpdateBehaviour(): Null value for alarmUpdateBehaviour");
		alarmUpdateBehaviour=_alarmUpdateBehaviour;
		if (_alarmUpdateBehaviour.equals("USE_TYPE_INSTANCE")) {
			almUpdateBehaviour = USE_TYPE_INSTANCE;
			return;
		}
		else if (_alarmUpdateBehaviour.equals("SPECIFY_OUTSTATION")) {
			almUpdateBehaviour = SPECIFY_OUTSTATION;
			return;
		}
		else throw new IllegalArgumentException("QoSDrxAlarmEventReceiverEventHandlerImpl().setalarmUpdateBehaviour(): Unknown value for alarmUpdateBehaviour:"+_alarmUpdateBehaviour);
	}


	/**
	 * Used to obtain opennms asset information for inclusion in alarms
	 * @see org.opennms.netmgt.dao.api.AssetRecordDao
	 */
	@SuppressWarnings("unused")
	private AssetRecordDao _assetRecordDao;


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
	private NodeDao _nodeDao;

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
	private AlarmDao _alarmDao;

	/**
	 * Used by Spring Application context to pass in alarmDao
	 *
	 * @param alarmDao a {@link org.opennms.netmgt.dao.api.AlarmDao} object.
	 */
	public  void setAlarmDao( AlarmDao alarmDao){
		_alarmDao = alarmDao;
	}
	
	/**
	 * Used by Spring Application context to pass in distPollerDao;
	 */
	private DistPollerDao distPollerDao;
	
	/**
	 * Used by Spring Application context to pass in distPollerDao;
	 *
	 * @param _distPollerDao a {@link org.opennms.netmgt.dao.api.DistPollerDao} object.
	 */
	public  void setDistPollerDao(DistPollerDao _distPollerDao) {
		 distPollerDao =  _distPollerDao;
	}	

	private OssDao ossDao;

// TODO remove
//	private static boolean ossDaoIsInitialised=false; // TODO - may want this in the spring initialisation

	/**
	 * provides an interface to OpenNMS which provides a unified api
	 *
	 * @param _ossDao the ossDao to set
	 */
	public void setOssDao(OssDao _ossDao) {
		ossDao = _ossDao;
	}

	@SuppressWarnings("unused")
	private OnmsAlarmOssjMapper onmsAlarmOssjMapper; 

	/**
	 * Used by Spring Application context to pass in OnmsAlarmOssjMapper
	 * The OnmsAlarmOssjMapper class maps OpenNMS alarms to OSS/J alarms and events
	 *
	 * @param onmsAlarmOssjMapper the onmsAlarmOssjMapper to set
	 */
	public void setOnmsAlarmOssjMapper(
			OnmsAlarmOssjMapper onmsAlarmOssjMapper) {
		this.onmsAlarmOssjMapper = onmsAlarmOssjMapper;
	}

	/**
	 * Used by receiver to initialise this class
	 * Must be called before any other methods to ensure that ossDao is initialised
	 */
        @Override
	synchronized public void init(){
		if (initialised) return;
		try {
			ossDao.init();  // initialises the node and alarm caches
			initialised=true;
		} catch (Throwable ex){
		throw new UndeclaredThrowableException(ex, this.getClass().getSimpleName()+"init() problem initialising class");
	}
		
// TODO remove
//		if (! ossDaoIsInitialised){
//			try {
//				ossDao.updateNodeCaches();
//				ossDao.updateAlarmCache();
//				ossDaoIsInitialised=true;
//			} catch (Throwable ex){
//				throw new UndeclaredThrowableException(ex, this.getClass().getSimpleName()+"init() problem initialising class");
//			}
//		}
//		initialised=true;
	}
	

	// ************************
	// On Event Methods
	// ************************


	/** {@inheritDoc} */
        @Override
	public void onNotifyNewAlarmEvent(NotifyNewAlarmEvent nnae, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyNewAlarmEvent(): ";

		LOG.debug("{} Statistics: {}", logheader, callingAer.getRuntimeStatistics());
		if (!initialised ){
			LOG.error("{} event handler not initialised. init() must be called by receiver before handling any events", logheader);
			return;
		}
		
		
		//TODO ADD IN BUSINESS LOGIC

		try{
			OnmsAlarm alarm=null;
			String ossPrimaryKey=nnae.getAlarmKey().getAlarmPrimaryKey();
			String applicationDN=nnae.getAlarmKey().getApplicationDN();
			LOG.debug("{} Received an onNotifyNewAlarmEvent() - AlarmPrimaryKey: {} ApplictionDN: {} eventtime: {}", logheader, ossPrimaryKey, applicationDN, nnae.getEventTime());
			LOG.debug("{} Using this OssDao (toString):{}", logheader, ossDao.toString());
			if ((applicationDN==null)||(applicationDN.equals("")) || (ossPrimaryKey==null)||(ossPrimaryKey.equals(""))) {
				LOG.error("{} ApplicatioDN or PrimaryKey not set", logheader);
			} else {
				LOG.debug("{} Creating new alarm", logheader);

//				alarm = ossDao.getCurrentAlarmForUniqueKey(applicationDN, ossPrimaryKey);
//				if (alarm!=null) { // already an alarm with this unique id - LOG.error
//				LOG.error("{} Alarm Already exists with this Unique ID", logheader);
//				} else {
				alarm=new OnmsAlarm();
				
				alarm.setUei(OnmsAlarmOssjMapper.ossjAlarmTypeToUei(nnae.getAlarmType()));
				
				alarm.setX733AlarmType((nnae.getAlarmType()==null) ? "" : nnae.getAlarmType());
				alarm.setX733ProbableCause(nnae.getProbableCause());

				alarm.setTTicketState(null); // needed?
				alarm.setTTicketId(""); // TODO changed
				alarm.setQosAlarmState("external_type"); // TODO changed				
				alarm.setSuppressedUser(""); // needed?
				alarm.setSuppressedUntil(new Date()); // needed?
				alarm.setSuppressedTime(new Date()); // needed?

				OnmsSeverity onmsseverity;
				try{
					onmsseverity= OnmsAlarmOssjMapper.ossjSeveritytoOnmsSeverity(nnae.getPerceivedSeverity());
				} catch (IllegalArgumentException iae){
					LOG.error("{} problem setting severity used default:'WARNING'. Exception:", logheader, iae);
					onmsseverity=OnmsSeverity.WARNING;
				}
				alarm.setSeverity(onmsseverity); 

//TODO not needed
//				OnmsServiceType service= new OnmsServiceType();
//				service.setId(new Integer(-1));
//				alarm.setServiceType(new OnmsServiceType()); // needed?


				alarm.setReductionKey(":managedObjectInstance:"+nnae.getManagedObjectInstance()+
						":managedObjectType:"+nnae.getManagedObjectClass()+
						":ossPrimaryKey:-"+ossPrimaryKey+
						":applicationDN:-"+applicationDN); // must be unique because of alarm_reductionkey_idx

				alarm.setOssPrimaryKey(ossPrimaryKey);
				alarm.setOperInstruct(nnae.getProposedRepairActions()); 

				// defaultvalue if search fails - will update node with ID 1
				OnmsNode node = new OnmsNode() ; // TODO remove ossDao.makeExtendedOnmsNode(); 
				node.setId(new Integer(1));  // node id cannot be null
				alarm.setNode(node); // 

				if (almUpdateBehaviour==null) {
					LOG.error("RX:{}: This receiver's alarmUpdateBehaviour is not set: defaulting to update nodeID:1", callingAer.getName());
				}
				else if (callingAer.getName()==null) {
					LOG.error("RX:{}: This receiver has no name: default alarms will update nodeID:1", callingAer.getName());
				}
				else {
					LOG.debug("{} alarmUpdateBehaviour:{} {}", logheader, almUpdateBehaviour, alarmUpdateBehaviour);

					if (almUpdateBehaviour.equals(SPECIFY_OUTSTATION)) {
						// this will look for first match of node label to callingAer.getName()
						// and set node id to this value.

						LOG.debug("{} SPECIFY_OUTSTATION looking for node with nodelabel:{}", logheader, callingAer.getName());
						try {
							// TODO temp remove ?
							try {
								node =ossDao.findNodeByLabel(callingAer.getName());
							} catch (Throwable ex){
								LOG.error("{} alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE) Problem looking up Node ", logheader, ex);
							}

							if (node!=null) {
								LOG.debug("{} alarmUpdateBehaviour.equals(SPECIFY_OUTSTATION):NODE FOUND for this RX Name:{} setting node id to NodeLabel:{} NodeID:{}", logheader, callingAer.getName(), node.getLabel(), node.getId());
								alarm.setNode(node); // maps into FIRST instance of node with the same managedObjectInstance and managedObjectType
							} else {
								LOG.error("{} alarmUpdateBehaviour.equals(SPECIFY_OUTSTATION):NODE NOT FOUND for this RX Name:{} setting node id to default NodeID: 1", logheader, callingAer.getName());
								node=new OnmsNode() ; // TODO remove ossDao.makeExtendedOnmsNode(); 
								node.setId(new Integer(1));  // node id cannot be null
								alarm.setNode(node); // 
							}
						} catch (Throwable ex){
							LOG.error("{} alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE) Problem looking up Node for alarm Set to default nodeID:1", logheader, ex);
						}

					} 
					else if (almUpdateBehaviour.equals(USE_TYPE_INSTANCE)){
						// this will look for first match of node Managed object Instance and Managed Object type
						// and set node id to this value. 
						String managedObjectType=nnae.getManagedObjectClass();
						String managedObjectInstance=nnae.getManagedObjectInstance();

						LOG.debug("{} USE_TYPE_INSTANCE looking for node with managedObjectType:{} managedObjectInstance:{}", logheader, managedObjectType, managedObjectInstance);
						try {
							node =ossDao.findNodeByInstanceAndType(managedObjectInstance, managedObjectType);

							if (node!=null) {
								LOG.debug("{} alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE):NODE FOUND for this RX Name:{} setting node id to NodeLabel:{} NodeID:{}", logheader, callingAer.getName(), node.getLabel(), node.getId());
								alarm.setNode(node); // maps into FIRST instance of node with the same managedObjectInstance and managedObjectType
							} else {
								LOG.error("{} alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE):NODE NOT FOUND for this managedObjectType:{} managedObjectInstance:{} setting node id to default NodeID: 1", logheader, managedObjectType, managedObjectInstance);
								node=new OnmsNode() ; // TODO remove ossDao.makeExtendedOnmsNode();
								node.setId(new Integer(1));  // node id cannot be null
								alarm.setNode(node); // 
							}
						} catch (Throwable ex){
							LOG.error("{} alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE) Problem looking up Node for alarm Set to default nodeID:1", logheader, ex);
						}
					}		
					else {
						LOG.error("{} Invalid value for alarmUpdateBehaviour:{} {} defaulting to update nodeID:1", logheader, almUpdateBehaviour, alarmUpdateBehaviour);
					}
				}


				alarm.setMouseOverText(""); // needed?
				alarm.setManagedObjectType(nnae.getManagedObjectClass());
				alarm.setManagedObjectInstance(nnae.getManagedObjectInstance());
				alarm.setLogMsg(nnae.getSpecificProblem());

				// NOTE - this has no effect here as .setLastEvent nulls value
				// alarm.setLastEventTime(nnae.getEventTime());
//TODO REMOVED - DO NOT CREATE EVENT WITH HIBERNATE AlarmDAo
//				OnmsEvent event= new OnmsEvent();
//				//event.setId(new Integer(1));  // This is NOT set since unique constraint in alarms table on Events table
//				alarm.setLastEvent(event); 

				alarm.setIpAddr(InetAddressUtils.getLocalHostAddress()); // needed?
				alarm.setId(null); // set null as updating alarm
				alarm.setFirstEventTime(nnae.getEventTime());
				alarm.setLastEventTime(nnae.getEventTime());
				
// TODO removed - do create distpoller with hibernate dao				
//				alarm.setDistPoller(new OnmsDistPoller("undefined","localhost")); //simple constructor
				alarm.setDistPoller(distPollerDao.get("localhost"));
				
				alarm.setDescription(nnae.getAdditionalText()); //TODO need Qosd Not to generate this if remote
				alarm.setCounter(new Integer(1));
				alarm.setApplicationDN(applicationDN);
				alarm.setAlarmType(new Integer(1)); // set to raise alarm
				//alarm.setAlarmAckUser(arg0);
				//alarm.setAlarmAckTime(arg0);
				
				//TODO added for new alarm field
				HashMap<String, String> m_details = new HashMap<String, String>();
				alarm.setDetails(m_details);

				try {
					LOG.debug("{} Creating Alarm: ", logheader);
					OnmsAlarm updatedAlarm = ossDao.addCurrentAlarmForUniqueKey(alarm);
					LOG.debug("{} Created alarm: {}", logheader, OssDaoOpenNMSImpl.alarmToString(updatedAlarm));
				}
				catch ( Exception ex ) {
					LOG.error("{} problem creating new alarm AlarmPrimaryKey: {} ApplictionDN: {}", logheader, ossPrimaryKey, applicationDN, ex);
				}
			}
			//TODO remove			}
		}
		catch(Throwable e){
			LOG.error("{} Error", logheader, e);
		}
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
		// BUSINESS LOGIC

		try{

			OnmsAlarm alarm=null;
			String ossPrimaryKey=nclae.getAlarmKey().getAlarmPrimaryKey();
			String applicationDN=nclae.getAlarmKey().getApplicationDN();
			LOG.debug("{} Received an onNotifyClearedAlarmEvent() - AlarmPrimaryKey: {} ApplictionDN: {} eventtime: {}", logheader, ossPrimaryKey, applicationDN, nclae.getEventTime());
			if ((applicationDN==null)||(applicationDN.equals("")) 
					|| (ossPrimaryKey==null)||(ossPrimaryKey.equals(""))) {
				LOG.error("{} ApplicatioDN or PrimaryKey not set", logheader);
			} else {
				LOG.debug("{} trying to find existing alarm using getCurrentAlarmForUniqueKey", logheader);

				alarm = ossDao.getCurrentAlarmForUniqueKey(applicationDN, ossPrimaryKey);
				if (alarm==null) { // no alarm with this unique id - LOG.error
					LOG.info("{} WARNING Alarm does not exist with this Unique ID:- AlarmPrimaryKey: {} ApplictionDN: {}", logheader, ossPrimaryKey, applicationDN);
				} else {
					LOG.debug("{} found alarm alarmID:{}", logheader, alarm.getId());

					//alarm.setUei(arg0);
					//alarm.setTTicketState(arg0);
					//alarm.setTTicketId(arg0);
					//alarm.setSuppressedUser(arg0);
					//alarm.setSuppressedUntil(arg0);
					//alarm.setSuppressedTime(arg0);
					alarm.setSeverity(OnmsSeverity.CLEARED);  //TODO need mapping for severity
					//alarm.setServiceType(arg0);
					//alarm.setReductionKey(arg0);
					//alarm.setOssPrimaryKey(arg0);
					//alarm.setOperInstruct(arg0);
					//alarm.setNode(arg0); // TODO mapping to node if in database
					//alarm.setMouseOverText(arg0);
					//alarm.setManagedObjectType(nclae.getManagedObjectClass()); // TODO check if changed
					//alarm.setManagedObjectInstance(nclae.getManagedObjectInstance()); //TODO check if changed
					//alarm.setLogMsg(arg0);
					
//TODO REMOVED - DO NOT CREATE EVENT WITH HIBERNATE AlarmDAo
//					OnmsEvent event= new OnmsEvent();
//					//event.setId(new Integer(1));  // This is NOT set since unique constraint in alarms table on Events table
//					alarm.setLastEvent(event);

					//alarm.setIpAddr(arg0);
					//alarm.setId(arg0); // do not change as updating alarm
					//alarm.setFirstEventTime(arg0);
					alarm.setLastEventTime(nclae.getEventTime()); // must be after .setLastEvent!!!
					//alarm.setDistPoller(arg0);
					//alarm.setDescription(arg0); 
					//alarm.setCounter(arg0);
					//alarm.setApplicationDN(arg0);
					//alarm.setAlarmType(arg0); 
					alarm.setAlarmAckUser("ossjclearevent"); //TODO CLEARING ALARMS ON RECEIPT OF CLEAR - NOT WAITING FOR ACK
					alarm.setAlarmAckTime(new Date());

					try {
						LOG.debug("{} Alarm before update:{}", logheader, OssDaoOpenNMSImpl.alarmToString(alarm));
						LOG.debug("{} Updating Alarm using ossDao.updateCurrentAlarmForUniqueKey", logheader);
						OnmsAlarm updatedAlarm = ossDao.updateCurrentAlarmForUniqueKey(alarm);
						LOG.debug("{} Updated alarm:{}", logheader, OssDaoOpenNMSImpl.alarmToString(updatedAlarm));
					}
					catch ( Exception ex ) {
						LOG.error("{} problem clearing new alarm AlarmPrimaryKey: {} ApplictionDN: {}", logheader, ossPrimaryKey, applicationDN, ex);
					}
				}
			}
		}
		catch(Throwable e){
			LOG.error("{} Error : ", logheader, e);
		}
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

		/*

		try{

			LOG.debug("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): Received an NotifyAckStateChangedEvent - AlarmPrimaryKey: {} New Ack State: {}", nasce.getAlarmKey().getAlarmPrimaryKey(), nasce.getAlarmAckState());
			OnmsAlarm alarm=null;
			try {
				String ossPrimaryKey=nasce.getAlarmKey().getAlarmPrimaryKey();
				String applicationDN=nasce.getAlarmKey().getApplicationDN();
				alarm = ossDao.getCurrentAlarmForUniqueKey(applicationDN, ossPrimaryKey);
				alarm.setId(null);  // must be done to do update
			}
			catch (IllegalStateException ise) {
				LOG.error("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent():: nasce alarm key set in illegal state", ise);
			}
			catch (java.lang.IllegalArgumentException iae){
				LOG.error("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): {}", iae);
			}

			if(nasce.getAlarmAckState() == AlarmAckState.ACKNOWLEDGED) {
				if (alarm != null ) { // if opennms has an alarm with this id to update
					try {
						alarm.setAlarmAckTime(nasce.getAckTime());
						alarm.setAlarmAckUser(nasce.getAckUserId());
						ossDao.updateCurrentAlarmForUniqueKey(alarm);
						LOG.debug("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): Acknowledging Alarm: {} New Ack State: {}", nasce.getAlarmKey().getAlarmPrimaryKey(), nasce.getAlarmAckState());
					}
					catch ( Exception ex ) {
						LOG.error("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent():: problem updating alarm ack state", ex);
					}
				}
				else {  //if opennms does not have an alarm with this id to update
					LOG.debug("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): Alarm cannot be acknowledged - not in database: {}", nasce.getAlarmKey().getAlarmPrimaryKey());
				}
			} else { // unacknowledge alarm
				if (alarm != null ) { // if opennms has an alarm with this id to update
					try {
						alarm.setAlarmAckTime(null);  // may throw illegal as putting in null
						alarm.setAlarmAckUser(null);
						ossDao.updateCurrentAlarmForUniqueKey(alarm);
						LOG.debug("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): UnAcknowledging Alarm: {} New Ack State: {}", nasce.getAlarmKey().getAlarmPrimaryKey(), nasce.getAlarmAckState());
					}
					catch ( Exception ex ) {
						LOG.error("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent():: problem updating alarm ack state", ex);
					}
				}
				else {  //if opennms does not have an alarm with this id to update
					LOG.debug("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): Alarm cannot be Unacknowledged - not in database: {}", nasce.getAlarmKey().getAlarmPrimaryKey());
				}
			}
		}
		catch(Throwable e){
			LOG.error("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent() Error : ", e);
		}

		 */

	}

	/** {@inheritDoc} */
        @Override
	public void onNotifyAlarmCommentsEvent(NotifyAlarmCommentsEvent nace, OssBeanAlarmEventReceiver callingAer) {
		//	Get a reference to the QoSD logger instance assigned by OpenNMS
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyAlarmCommentsEvent(): ";

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
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onNotifyChangedAlarmEvent(): ";

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
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onUnknownIRPEvt(): ";

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
		String logheader="RX:"+callingAer.getName()+":"+this.getClass().getSimpleName()+".onunknownObjectMessage(): ";

		LOG.debug("{} Statistics: {}", logheader, callingAer.getRuntimeStatistics());
		if (!initialised ){
			LOG.error("{} event handler not initialised. init() must be called by receiver before handling any events", logheader);
			return;
		}
		//TODO ADD IN BUSINESS LOGIC
	}



}

/* TODO - clean up
 * 					alarm.setUei(arg0);
					alarm.setTTicketState(arg0);
					alarm.setTTicketId(arg0);
					alarm.setSuppressedUser(arg0);
					alarm.setSuppressedUntil(arg0);
					alarm.setSuppressedTime(arg0);
					alarm.setSeverity(arg0);
					alarm.setServiceType(arg0);
					alarm.setReductionKey(arg0);
					alarm.setOssPrimaryKey(arg0);
					alarm.setOperInstruct(arg0);
					alarm.setNode(arg0);
					alarm.setMouseOverText(arg0);
					alarm.setManagedObjectType(arg0);
					alarm.setManagedObjectInstance(arg0);
					alarm.setLogMsg(arg0);
					alarm.setLastEventTime(arg0);
					alarm.setLastEvent(arg0);
					alarm.setIpAddr(arg0);
					alarm.setId(arg0);
					alarm.setFirstEventTime(arg0);
					alarm.setDistPoller(arg0);
					alarm.setDescription(arg0);
					alarm.setCounter(arg0);
					alarm.setApplicationDN(arg0);
					alarm.setAlarmType(arg0);
					alarm.setAlarmAckUser(arg0);
					alarm.setAlarmAckTime(arg0);
 */

