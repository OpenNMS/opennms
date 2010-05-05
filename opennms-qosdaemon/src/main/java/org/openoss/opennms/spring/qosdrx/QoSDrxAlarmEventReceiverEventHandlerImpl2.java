// This file is part of the OpenNMS(R) QoSD OSS/J interface.
//
// Modifications:
//
// 2008 Oct 04: Use new OnmsSeverity object for OnmsAlarms. - dj@opennms.org
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

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.openoss.opennms.spring.dao.OnmsAlarmOssjMapper;
import org.openoss.opennms.spring.dao.OssDao;
import org.openoss.opennms.spring.dao.OssDaoOpenNMSImpl;
import org.openoss.ossj.fm.monitor.spring.AlarmEventReceiverEventHandler;
import org.openoss.ossj.fm.monitor.spring.OssBeanAlarmEventReceiver;


/**
 * Work in progress - implementing business methods
 */
public class QoSDrxAlarmEventReceiverEventHandlerImpl2 implements AlarmEventReceiverEventHandler{

	private boolean initialised = false; // true if init() has initialised class

	/**
	 *  Method to get the QoSDrx's logger from OpenNMS
	 */
	private static Logger getLog() {
		return (Logger)ThreadCategory.getInstance(QoSDrxAlarmEventReceiverEventHandlerImpl2.class);
	}

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
	 * @param alarmUpdateBehaviour must be  
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
	 * @see org.opennms.netmgt.dao.AssetRecordDao
	 */
	@SuppressWarnings("unused")
	private AssetRecordDao _assetRecordDao;


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
	private NodeDao _nodeDao;

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
	private AlarmDao _alarmDao;

	/**
	 * Used by Spring Application context to pass in alarmDao
	 * @param alarmDao
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
	 */
	public  void setDistPollerDao(DistPollerDao _distPollerDao) {
		 distPollerDao =  _distPollerDao;
	}	

	private OssDao ossDao;

// TODO remove
//	private static boolean ossDaoIsInitialised=false; // TODO - may want this in the spring initialisation

	/**
	 * provides an interface to OpenNMS which provides a unified api 
	 * @param ossDao the ossDao to set
	 */
	public void setOssDao(OssDao _ossDao) {
		ossDao = _ossDao;
	}

	@SuppressWarnings("unused")
	private OnmsAlarmOssjMapper onmsAlarmOssjMapper; 

	/**
	 * Used by Spring Application context to pass in OnmsAlarmOssjMapper
	 * The OnmsAlarmOssjMapper class maps OpenNMS alarms to OSS/J alarms and events
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
	synchronized public void init(){
		if (initialised) return;
		try {
			ossDao.init();  // initialises the node and alarm caches
			initialised=true;
		} catch (Exception ex){
		throw new UndeclaredThrowableException(ex, this.getClass().getSimpleName()+"init() problem initialising class");
	}
		
// TODO remove
//		if (! ossDaoIsInitialised){
//			try {
//				ossDao.updateNodeCaches();
//				ossDao.updateAlarmCache();
//				ossDaoIsInitialised=true;
//			} catch (Exception ex){
//				throw new UndeclaredThrowableException(ex, this.getClass().getSimpleName()+"init() problem initialising class");
//			}
//		}
//		initialised=true;
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

		try{
			OnmsAlarm alarm=null;
			String ossPrimaryKey=nnae.getAlarmKey().getAlarmPrimaryKey();
			String applicationDN=nnae.getAlarmKey().getApplicationDN();
			if (log.isDebugEnabled()) 
				log.debug(logheader+": Received an onNotifyNewAlarmEvent() - AlarmPrimaryKey: "
						+ ossPrimaryKey +" ApplictionDN: " + applicationDN +" eventtime: " + nnae.getEventTime());
			if (log.isDebugEnabled())log.debug(logheader+":Using this OssDao (toString):"+ossDao.toString());
			if ((applicationDN==null)||(applicationDN.equals("")) || (ossPrimaryKey==null)||(ossPrimaryKey.equals(""))) {
				log.error(logheader+" ApplicatioDN or PrimaryKey not set");
			} else {
				if (log.isDebugEnabled()) log.debug(logheader+": Creating new alarm");

//				alarm = ossDao.getCurrentAlarmForUniqueKey(applicationDN, ossPrimaryKey);
//				if (alarm!=null) { // already an alarm with this unique id - log error
//				log.error(logheader+" Alarm Already exists with this Unique ID");
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
					log.error(logheader+" problem setting severity used default:'WARNING'. Exception:"+ iae);
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
					log.error("RX:"+callingAer.getName()+": This receiver's alarmUpdateBehaviour is not set: defaulting to update nodeID:1");
				}
				else if (callingAer.getName()==null) {
					log.error("RX:"+callingAer.getName()+": This receiver has no name: default alarms will update nodeID:1");
				}
				else {
					if (log.isDebugEnabled()) 
						log.debug(logheader+" alarmUpdateBehaviour:"+almUpdateBehaviour+" "+alarmUpdateBehaviour);

					if (almUpdateBehaviour.equals(SPECIFY_OUTSTATION)) {
						// this will look for first match of node label to callingAer.getName()
						// and set node id to this value.

						if (log.isDebugEnabled()) 
							log.debug(logheader+" SPECIFY_OUTSTATION looking for node with nodelabel:"+callingAer.getName());
						try {
							// TODO temp remove ?
							try {
								node =ossDao.findNodeByLabel(callingAer.getName());
							} catch (Exception ex){
								log.error(logheader+" alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE) Problem looking up Node "+ex);
							}

							if (node!=null) {
								if (log.isDebugEnabled()) 
									log.debug(logheader+" alarmUpdateBehaviour.equals(SPECIFY_OUTSTATION):"
											+"NODE FOUND for this RX Name:"+callingAer.getName()+" setting node id to NodeLabel:"+node.getLabel()+" NodeID:"+node.getId());
								alarm.setNode(node); // maps into FIRST instance of node with the same managedObjectInstance and managedObjectType
							} else {
								log.error(logheader+" alarmUpdateBehaviour.equals(SPECIFY_OUTSTATION):"
										+"NODE NOT FOUND for this RX Name:"+callingAer.getName()+" setting node id to default NodeID: 1");
								node=new OnmsNode() ; // TODO remove ossDao.makeExtendedOnmsNode(); 
								node.setId(new Integer(1));  // node id cannot be null
								alarm.setNode(node); // 
							}
						} catch (Exception ex){
							log.error(logheader+" alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE) Problem looking up Node for alarm Set to default nodeID:1"+ex);
						}

					} 
					else if (almUpdateBehaviour.equals(USE_TYPE_INSTANCE)){
						// this will look for first match of node Managed object Instance and Managed Object type
						// and set node id to this value. 
						String managedObjectType=nnae.getManagedObjectClass();
						String managedObjectInstance=nnae.getManagedObjectInstance();

						if (log.isDebugEnabled()) 
							log.debug(logheader+" USE_TYPE_INSTANCE looking for node with managedObjectType:"+managedObjectType+" managedObjectInstance:"+managedObjectInstance);
						try {
							node =ossDao.findNodeByInstanceAndType(managedObjectInstance, managedObjectType);

							if (node!=null) {
								if (log.isDebugEnabled()) 
									log.debug(logheader+" alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE):"
											+"NODE FOUND for this RX Name:"+callingAer.getName()+" setting node id to NodeLabel:"+node.getLabel()+" NodeID:"+node.getId());
								alarm.setNode(node); // maps into FIRST instance of node with the same managedObjectInstance and managedObjectType
							} else {
								log.error(logheader+" alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE):"
										+"NODE NOT FOUND for this managedObjectType:"+managedObjectType+" managedObjectInstance:"+managedObjectInstance+" setting node id to default NodeID: 1");
								node=new OnmsNode() ; // TODO remove ossDao.makeExtendedOnmsNode();
								node.setId(new Integer(1));  // node id cannot be null
								alarm.setNode(node); // 
							}
						} catch (Exception ex){
							log.error(logheader+" alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE) Problem looking up Node for alarm Set to default nodeID:1"+ex);
						}
					}		
					else {
						log.error(logheader+" Invalid value for alarmUpdateBehaviour:"+almUpdateBehaviour+" "+alarmUpdateBehaviour+" defaulting to update nodeID:1");
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

				alarm.setIpAddr("localhost"); // needed?
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
					if (log.isDebugEnabled()) log.debug(logheader+": Creating Alarm: " );
					OnmsAlarm updatedAlarm = ossDao.addCurrentAlarmForUniqueKey(alarm);
					if (log.isDebugEnabled()) {
						log.debug(logheader+": Created alarm:"
								+ OssDaoOpenNMSImpl.alarmToString(updatedAlarm));
					}
				}
				catch ( Exception ex ) {
					log.error(logheader+": problem creating new alarm AlarmPrimaryKey: "
							+ ossPrimaryKey +" ApplictionDN: " + applicationDN+": "+ ex);
				}
			}
			//TODO remove			}
		}
		catch(Exception e){
			log.error(logheader+" Error : ", e);
		}
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
		// BUSINESS LOGIC

		try{

			OnmsAlarm alarm=null;
			String ossPrimaryKey=nclae.getAlarmKey().getAlarmPrimaryKey();
			String applicationDN=nclae.getAlarmKey().getApplicationDN();
			if (log.isDebugEnabled()) 
				log.debug(logheader+": Received an onNotifyClearedAlarmEvent() - AlarmPrimaryKey: "
						+ ossPrimaryKey +" ApplictionDN: " + applicationDN +" eventtime: " + nclae.getEventTime());
			if ((applicationDN==null)||(applicationDN.equals("")) 
					|| (ossPrimaryKey==null)||(ossPrimaryKey.equals(""))) {
				log.error(logheader+" ApplicatioDN or PrimaryKey not set");
			} else {
				if (log.isDebugEnabled()) 
					log.debug(logheader+": trying to find existing alarm using getCurrentAlarmForUniqueKey");

				alarm = ossDao.getCurrentAlarmForUniqueKey(applicationDN, ossPrimaryKey);
				if (alarm==null) { // no alarm with this unique id - log error
					log.info(logheader+"WARNING Alarm does not exist with this Unique ID:- AlarmPrimaryKey: "
							+ ossPrimaryKey +" ApplictionDN: " + applicationDN);
				} else {
					if (log.isDebugEnabled()) 
						log.debug(logheader+": found alarm alarmID:"+alarm.getId());

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
						if (log.isDebugEnabled()) {
							log.debug(logheader+": Alarm before update:"+ OssDaoOpenNMSImpl.alarmToString(alarm));
						}
						if (log.isDebugEnabled()) log.debug(logheader+": Updating Alarm using ossDao.updateCurrentAlarmForUniqueKey" );
						OnmsAlarm updatedAlarm = ossDao.updateCurrentAlarmForUniqueKey(alarm);
						if (log.isDebugEnabled()) {
							log.debug(logheader+": Updated alarm:"
									+ OssDaoOpenNMSImpl.alarmToString(updatedAlarm));
						}
					}
					catch ( Exception ex ) {
						log.error(logheader+": problem clearing new alarm AlarmPrimaryKey: "
								+ ossPrimaryKey +" ApplictionDN: " + applicationDN+": "+ ex);
					}
				}
			}
		}
		catch(Exception e){
			log.error(logheader+" Error : ", e);
		}
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

		/*

		try{

			if (log.isDebugEnabled()) 
				log.debug("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): Received an NotifyAckStateChangedEvent - AlarmPrimaryKey: " + nasce.getAlarmKey().getAlarmPrimaryKey() +" New Ack State: " + nasce.getAlarmAckState());
			OnmsAlarm alarm=null;
			try {
				String ossPrimaryKey=nasce.getAlarmKey().getAlarmPrimaryKey();
				String applicationDN=nasce.getAlarmKey().getApplicationDN();
				alarm = ossDao.getCurrentAlarmForUniqueKey(applicationDN, ossPrimaryKey);
				alarm.setId(null);  // must be done to do update
			}
			catch (IllegalStateException ise) {
				log.error("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent():: nasce alarm key set in illegal state"+ ise);
			}
			catch (java.lang.IllegalArgumentException iae){
				log.error("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): "+ iae);
			}

			if(nasce.getAlarmAckState() == AlarmAckState.ACKNOWLEDGED) {
				if (alarm != null ) { // if opennms has an alarm with this id to update
					try {
						alarm.setAlarmAckTime(nasce.getAckTime());
						alarm.setAlarmAckUser(nasce.getAckUserId());
						ossDao.updateCurrentAlarmForUniqueKey(alarm);
						if (log.isDebugEnabled()) log.debug("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): Acknowledging Alarm: " + nasce.getAlarmKey().getAlarmPrimaryKey() +" New Ack State: " + nasce.getAlarmAckState());
					}
					catch ( Exception ex ) {
						log.error("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent():: problem updating alarm ack state"+ ex);
					}
				}
				else {  //if opennms does not have an alarm with this id to update
					if (log.isDebugEnabled()) log.debug("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): Alarm cannot be acknowledged - not in database: " + nasce.getAlarmKey().getAlarmPrimaryKey());
				}
			} else { // unacknowledge alarm
				if (alarm != null ) { // if opennms has an alarm with this id to update
					try {
						alarm.setAlarmAckTime(null);  // may throw illegal as putting in null
						alarm.setAlarmAckUser(null);
						ossDao.updateCurrentAlarmForUniqueKey(alarm);
						if (log.isDebugEnabled()) log.debug("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): UnAcknowledging Alarm: " + nasce.getAlarmKey().getAlarmPrimaryKey() +" New Ack State: " + nasce.getAlarmAckState());
					}
					catch ( Exception ex ) {
						log.error("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent():: problem updating alarm ack state"+ ex);
					}
				}
				else {  //if opennms does not have an alarm with this id to update
					if (log.isDebugEnabled()) log.debug("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent(): Alarm cannot be Unacknowledged - not in database: " + nasce.getAlarmKey().getAlarmPrimaryKey());
				}
			}
		}
		catch(Throwable e){
			log.error("QoSDrxAlarmEventReceiverEventHandlerImpl().onNotifyAckStateChangedEvent() Error : ", e);
		}

		 */

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

