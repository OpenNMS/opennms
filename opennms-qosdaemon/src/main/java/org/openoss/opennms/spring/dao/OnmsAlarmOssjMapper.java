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

package org.openoss.opennms.spring.dao;

import java.net.InetAddress;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.oss.UnsupportedAttributeException;
import javax.oss.fm.monitor.AlarmAckState;
import javax.oss.fm.monitor.AlarmKey;
import javax.oss.fm.monitor.AlarmType;
import javax.oss.fm.monitor.AlarmValue;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.openoss.ossj.jvt.fm.monitor.OOSSProbableCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>OnmsAlarmOssjMapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsAlarmOssjMapper {
    private static final Logger LOG = LoggerFactory.getLogger(OnmsAlarmOssjMapper.class);

	// pattern for recognising simple <HTML> tags ; 
	// used to strip HTML characters from log messages etc
	private static Pattern p= Pattern.compile("<[^>]*>"); 
	// p= Pattern.compile("\\Q&lt;\\E[^\\Q&gt;\\E]*\\Q&gt;\\E");  
	// NOT USED Alternative pattern for matching
	// <HTML> tags in openNMS logs and eui data



	//---------------SPRING DAO DECLARATIONS----------------


	/**
	 * Used by Spring Application context to pass in distPollerDao;
	 */
	private DistPollerDao distPollerDao;

	/**
	 * Used by Spring Application context to pass in distPollerDao;
	 *
	 * @param _distPollerDao a {@link org.opennms.netmgt.dao.api.DistPollerDao} object.
	 */
	public void setDistPollerDao(DistPollerDao _distPollerDao) {
		distPollerDao =  _distPollerDao;
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
	public void setAssetRecordDao(AssetRecordDao ar){
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
	public void setNodeDao( NodeDao nodedao){
		_nodeDao = nodedao;
	}

	private OssDao ossDao;

	/**
	 * provides an interface to OpenNMS which provides a unified api
	 *
	 * @param _ossDao the ossDao to set
	 */
	public void setOssDao(OssDao _ossDao) {
		ossDao = _ossDao;
	}

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

	private static String alarmUpdateBehaviourStr[] = {"SPECIFY_OUTSTATION", "USE_TYPE_INSTANCE"};

	/**
	 * REturns string value of alarmUpdateBehaviour
	 * @param aub valid value for <code>alarmUpdateBehaviour</code>
	 * SPECIFY_OUTSTATION, or USE_TYPE_INSTANCE
	 * @return
	 */
	private static String getAlarmUpdateBehaviourForInt(Integer aub) {
		try {
			return alarmUpdateBehaviourStr[aub];
		}
		catch (Throwable ex){
			return "getAlarmUpdateBehaviourForInt INVALID_VALUE:"+aub;
		}
	}


	// ****************
	// Business methods
	// ****************

	/**
	 * This method maps an OSS/J AlarmValue to OpenNMS alarm
	 *
	 * @param onmsAlarm OnmsAlarm object to be populated
	 * @param alarmValue OSS/J AlarmValue data to use to populate OnmsAlarm
	 * @param almUpdateBehaviour - determines how to treat the node name of the new alarm must be of value;
	 * <code>USE_TYPE_INSTANCE</code> - populate nodeID with node having same asset type and instance data as alarm
	 * or <code>SPECIFY_OUTSTATION</code> -  populate nodeID with node having same nodeLabel as defaultUpdateNodeLabel
	 * @param defaultUpdateNodeLabel name of node to be updated if almUpdateBehaviour==SPECIFY_OUTSTATION
	 * @return the OnmsAlarm populated with OSS/J NotifyNewAlarmEvent data
	 * @throws java.lang.IllegalArgumentException if any.
	 * @throws javax.oss.UnsupportedAttributeException if any.
	 */
	public OnmsAlarm populateOnmsAlarmFromOssjAlarm(OnmsAlarm onmsAlarm, AlarmValue alarmValue, Integer almUpdateBehaviour, String defaultUpdateNodeLabel  )throws IllegalArgumentException, UnsupportedAttributeException {
		String logheader=this.getClass().getSimpleName()+"populateOnmsAlarmFromOssjAlarm():";

		try{
			String ossPrimaryKey=alarmValue.getAlarmKey().getAlarmPrimaryKey();
			String applicationDN=alarmValue.getAlarmKey().getApplicationDN();
			LOG.debug("{} AlarmPrimaryKey: {} ApplictionDN: {} alarmRaisedTime: {}", logheader, ossPrimaryKey, applicationDN, alarmValue.getAlarmRaisedTime());
			if ((applicationDN==null)||(applicationDN.equals("")) 
					|| (ossPrimaryKey==null)||(ossPrimaryKey.equals(""))) {
				LOG.error("{} ApplicatioDN or PrimaryKey not set", logheader);
			} else {
				LOG.debug("{} trying to find existing alarm using getCurrentAlarmForUniqueKey", logheader);

				onmsAlarm = ossDao.getCurrentAlarmForUniqueKey(applicationDN, ossPrimaryKey);
				if (onmsAlarm!=null) { // already an alarm with this unique id - log error
					LOG.error("{} Alarm Already exists with this Unique ID", logheader);
				} else {
					onmsAlarm=new OnmsAlarm();

					onmsAlarm.setUei(ossjAlarmTypeToUei(alarmValue.getAlarmType()));
					onmsAlarm.setX733AlarmType((alarmValue.getAlarmType()==null) ? "" : alarmValue.getAlarmType());
					onmsAlarm.setX733ProbableCause(alarmValue.getProbableCause());

					onmsAlarm.setTTicketState(null); // needed?
					onmsAlarm.setTTicketId(""); // needed?
					onmsAlarm.setQosAlarmState("");
					onmsAlarm.setSuppressedUser(""); // needed?
					onmsAlarm.setSuppressedUntil(new Date()); // needed?
					onmsAlarm.setSuppressedTime(new Date()); // needed?

					OnmsSeverity onmsseverity;
					try{
						onmsseverity= ossjSeveritytoOnmsSeverity(alarmValue.getPerceivedSeverity());
					} catch (IllegalArgumentException iae){
						LOG.error("{} problem setting severity used default:'WARNING'.", logheader, iae);
						onmsseverity=OnmsSeverity.WARNING;
					}
					onmsAlarm.setSeverity(onmsseverity); 

					OnmsServiceType service= new OnmsServiceType();
					service.setId(new Integer(-1));
					onmsAlarm.setServiceType(new OnmsServiceType()); // needed?


					onmsAlarm.setReductionKey(":managedObjectInstance:"+alarmValue.getManagedObjectInstance()+
							":managedObjectType:"+alarmValue.getManagedObjectClass()+
							":ossPrimaryKey:-"+ossPrimaryKey+
							":applicationDN:-"+applicationDN); // must be unique because of alarm_reductionkey_idx

					onmsAlarm.setOssPrimaryKey(ossPrimaryKey);
					onmsAlarm.setOperInstruct(alarmValue.getProposedRepairActions()); 

					// defaultvalue if search fails - will update node with ID 1
					OnmsNode node = new OnmsNode() ; // TODO remove ossDao.makeExtendedOnmsNode(); 
					node.setId(new Integer(1));  // node id cannot be null
					onmsAlarm.setNode(node); // 

					if (almUpdateBehaviour==null) {
						LOG.error("{} This receiver's alarmUpdateBehaviour is not set: defaulting to update nodeID:1", logheader);
					}
					else {
						LOG.debug("{} alarmUpdateBehaviour:{} {}", logheader, almUpdateBehaviour, getAlarmUpdateBehaviourForInt(almUpdateBehaviour));

						if (almUpdateBehaviour.equals(SPECIFY_OUTSTATION)) {
							// this will look for first match of node label to callingAer.getName()
							// and set node id to this value.

							LOG.debug("{} SPECIFY_OUTSTATION looking for node with nodelabel:{}", logheader, defaultUpdateNodeLabel);
							try {
								// TODO temp remove ?
								try {
									node =ossDao.findNodeByLabel(defaultUpdateNodeLabel);
								} catch (Throwable ex){
									LOG.error("{} alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE) Problem looking up Node", logheader, ex);
								}

								if (node!=null) {
									LOG.debug("{} alarmUpdateBehaviour.equals(SPECIFY_OUTSTATION): NODE FOUND for this name:{} setting node id to NodeLabel:{} NodeID:{}", logheader, defaultUpdateNodeLabel, node.getLabel(), node.getId());
									onmsAlarm.setNode(node); // maps into FIRST instance of node with the same managedObjectInstance and managedObjectType
								} else {
									LOG.error("{} alarmUpdateBehaviour.equals(SPECIFY_OUTSTATION): NODE NOT FOUND for this name:{} setting node id to default NodeID: 1", logheader, defaultUpdateNodeLabel);
									node=new OnmsNode() ; // TODO remove ossDao.makeExtendedOnmsNode(); 
									node.setId(new Integer(1));  // node id cannot be null
									onmsAlarm.setNode(node); // 
								}
							} catch (Throwable ex){
								LOG.error("{} alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE) Problem looking up Node for alarm Set to default nodeID:1", logheader, ex);
							}

						} 
						else if (almUpdateBehaviour.equals(USE_TYPE_INSTANCE)){
							// this will look for first match of node Managed object Instance and Managed Object type
							// and set node id to this value. 
							String managedObjectType=alarmValue.getManagedObjectClass();
							String managedObjectInstance=alarmValue.getManagedObjectInstance();

							LOG.debug("{} USE_TYPE_INSTANCE looking for node with managedObjectType:{} managedObjectInstance:{}", logheader, managedObjectType, managedObjectInstance);
							try {
								node =ossDao.findNodeByInstanceAndType(managedObjectInstance, managedObjectType);

								if (node!=null) {
									LOG.debug("{} alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE): NODE FOUND for this RX Name:{} setting node id to NodeLabel:{} NodeID:{}", logheader, defaultUpdateNodeLabel, node.getLabel(), node.getId());
									onmsAlarm.setNode(node); // maps into FIRST instance of node with the same managedObjectInstance and managedObjectType
								} else {
									LOG.error("{} alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE): NODE NOT FOUND for this managedObjectType:{} managedObjectInstance:{} setting node id to default NodeID: 1", logheader, managedObjectType, managedObjectInstance);
									node=new OnmsNode() ; // TODO remove ossDao.makeExtendedOnmsNode();
									node.setId(new Integer(1));  // node id cannot be null
									onmsAlarm.setNode(node); // 
								}
							} catch (Throwable ex){
								LOG.error("{} alarmUpdateBehaviour.equals(USE_TYPE_INSTANCE) Problem looking up Node for alarm Set to default nodeID:1", logheader, ex);
							}
						}		
						else {
							LOG.error("{} Invalid value for alarmUpdateBehaviour:{} {} defaulting to update nodeID:1", logheader, almUpdateBehaviour, getAlarmUpdateBehaviourForInt(almUpdateBehaviour));
						}
					}


					onmsAlarm.setMouseOverText(""); // needed?
					onmsAlarm.setManagedObjectType(alarmValue.getManagedObjectClass());
					onmsAlarm.setManagedObjectInstance(alarmValue.getManagedObjectInstance());
					onmsAlarm.setLogMsg(alarmValue.getSpecificProblem());

					// NOTE - this has no effect here as .setLastEvent nulls value
					// alarm.setLastEventTime(nnae.getEventTime());

//					TODO REMOVED - DO NOT CREATE EVENT WITH HIBERNATE AlarmDAo
//					OnmsEvent event= new OnmsEvent();
//					//event.setId(new Integer(1));  // This is NOT set since unique constraint in alarms table on Events table
//					onmsAlarm.setLastEvent(event); 

					onmsAlarm.setIpAddr(InetAddressUtils.getLocalHostAddress()); // needed?
					onmsAlarm.setId(null); // set null as updating alarm
					onmsAlarm.setFirstEventTime(alarmValue.getAlarmRaisedTime());
					onmsAlarm.setLastEventTime(alarmValue.getAlarmChangedTime());

//					TODO removed - do create distpoller with hibernate dao	
//					onmsAlarm.setDistPoller(new OnmsDistPoller("undefined","localhost")); //simple constructor
					onmsAlarm.setDistPoller(distPollerDao.get("localhost"));


					onmsAlarm.setDescription(alarmValue.getAdditionalText()); //TODO need Qosd Not to generate this if remote
					onmsAlarm.setCounter(new Integer(1));
					onmsAlarm.setApplicationDN(applicationDN);
					onmsAlarm.setAlarmType(new Integer(1)); // set to raise alarm
					//alarm.setAlarmAckUser(arg0);
					//alarm.setAlarmAckTime(arg0);

					LOG.debug("{} Creating Alarm", logheader);
				}
			}
		}
		catch(Throwable e){
			LOG.error("{} Error : ", logheader, e);
		}
		return onmsAlarm;

	}


	/**
	 * This method maps OpenNMS alarm to an OSS/J alarms and adds additional information
	 *
	 * @param _openNMSalarm data to use to populate the OSS/J alarm
	 * @param alarmValueSpecification AlarmValue object to be populated - Invariant (Specifcation) values should be already populated
	 * @return the _av OSS/J AlarmValue populated with opennms data
	 * @throws java.lang.IllegalArgumentException if any.
	 * @throws javax.oss.UnsupportedAttributeException if any.
	 */
	public AlarmValue populateOssjAlarmFromOpenNMSAlarm(AlarmValue alarmValueSpecification, OnmsAlarm _openNMSalarm) throws IllegalArgumentException, UnsupportedAttributeException {
		String logheader=this.getClass().getSimpleName()+"populateOssjAlarmFromOpenNMSAlarm():";

		//Node node = null;
		OnmsNode node = null;

		// Asset asset = null;
		OnmsAssetRecord asset = null;

		boolean isQoSDrxAlarm=false; // true if alarm is received from Qosdrx


		LOG.debug("{} Populating alarm", logheader);

		// test to see if opennms alarm already has type and instance information set. If yes then it has most likely
		// come from Qosdrx. 
		if ((_openNMSalarm.getManagedObjectInstance()!=null) && (_openNMSalarm.getManagedObjectType()!=null)
				&& (!_openNMSalarm.getManagedObjectInstance().equals("")) && (!_openNMSalarm.getManagedObjectType().equals(""))){
			isQoSDrxAlarm=true;			
			LOG.debug("{} isQoSDrxAlarm TRUE - because OpenNMS alarm has ManagedObjectInstance and ManagedObjectType", logheader);
		} else {
			isQoSDrxAlarm=false;
			LOG.debug("{} isQoSDrxAlarm FALSE - because OpenNMS alarm NOT POPULATED ManagedObjectInstance and ManagedObjectType", logheader);
		}


		try {
			// If the alarm has both an acknowledge time and an acknowledge user
			// then the alarm has been acknowledged. Set the corrsponding parameters
			// in the OSS/J alarm
			if((null != _openNMSalarm.getAlarmAckTime() ) && ( null!= _openNMSalarm.getAlarmAckUser() ) )
			{
				alarmValueSpecification.setAckUserId(_openNMSalarm.getAlarmAckUser());
				// OnmsAlarm can contain java.sql.Timestamp - convert to Date
				alarmValueSpecification.setAckTime(new Date(_openNMSalarm.getAlarmAckTime().getTime()));
				alarmValueSpecification.setAlarmAckState(AlarmAckState.ACKNOWLEDGED);
			}
			else
			{
				alarmValueSpecification.setAlarmAckState(AlarmAckState.UNACKNOWLEDGED);
			}

			// if the alarm is cleared, then set the alarm cleared time
			// to that of the lasteventtime as this must be the time
			// the clear occured.
			if(_openNMSalarm.getSeverity() == OnmsSeverity.CLEARED) {
				// OnmsAlarm can contain java.sql.Timestamp - convert to Date
				alarmValueSpecification.setAlarmClearedTime(new Date(_openNMSalarm.getLastEventTime().getTime()));
			}
			else {
				alarmValueSpecification.setAlarmClearedTime(null);
			}

			// Set the alarmRaisedTime to the FirstEventTime of the OpenNMS
			// alarm. Set the alarm changed time to the last event time.
			// OnmsAlarm can contain java.sql.Timestamp - convert to Date
			if(null != _openNMSalarm.getFirstEventTime() ){
				alarmValueSpecification.setAlarmRaisedTime(new Date(_openNMSalarm.getFirstEventTime().getTime()));
			}
			if(null != _openNMSalarm.getLastEventTime() ) {
				alarmValueSpecification.setAlarmChangedTime(new Date(_openNMSalarm.getLastEventTime().getTime()));
			}


		} catch (Throwable e ){
			LOG.error("{} Problem getting ACK time information", logheader, e);
		}


		Matcher matcher = null;
		String _uei_no_html ="NOT_SET";
		try{
			String uei = _openNMSalarm.getUei();
			if (null != uei) {
				matcher = p.matcher(uei);
				_uei_no_html = matcher.replaceAll(" "); // remove any HTML tags from uei
			}
			alarmValueSpecification.setAlarmType((_openNMSalarm.getX733AlarmType()==null) ? javax.oss.fm.monitor.AlarmType.EQUIPMENT_ALARM :  _openNMSalarm.getX733AlarmType());
		} catch (Throwable e) {
			LOG.error("{} Problem getting  X733AlarmType or Uei", logheader, e);
		}

		// Get some local node information as to where the alarm came from
		// This includes, what type of managed element the node is
		// and what its node id and label are.*/
//		String mftr = "NOT_SET"; // FIXME: Not read
//		String modelNo = "NOT_SET"; // FIXME: Not read
//		String assetserno = "NOT_SET"; // FIXME: Not read
//		String nodelabel = "NOT_SET"; // FIXME: Not read
//		String alarmIP = "NOT_SET"; // FIXME: Not read
		String managedObjectType = "NOT_SET";
		String managedObjectInstance =  "NOT_SET"; 
		String assetManagedObjectType = "NOT_SET";
		String assetManagedObjectInstance =  "NOT_SET"; 

		String assetDescription =  "NOT_SET";
		String assetAddress2 =  "NOT_SET";

		if (!isQoSDrxAlarm ) { // if is locally generated alarm
			try
			{
				// some opennms alarms don't have node information
				// set default values if no node information present
				if (_openNMSalarm.getNode()!=null) {
					node=ossDao.findNodeByID(_openNMSalarm.getNode().getId());

					asset =node.getAssetRecord();

//					alarmIP = _openNMSalarm.getIpAddr(); // Not read
//					if (node != null) {
//					nodelabel = node.getLabel(); // Not read
//					}
					if (asset != null) {
//						if (asset.getManufacturer()!= null) mftr = asset.getManufacturer(); // Not read
//						if (asset.getModelNumber()!= null) modelNo = asset.getModelNumber(); // Not read
//						if (asset.getSerialNumber()!= null) assetserno = asset.getSerialNumber(); // Not read
						if (asset.getDescription()!= null) assetDescription = asset.getDescription();  // TODO was used for managed object class as is 128 char long
						if (asset.getAddress2()!= null) assetAddress2 = asset.getAddress2();        // TODO was used for managed object instance - as is 256 char long string
						if (asset.getManagedObjectInstance()!= null) assetManagedObjectInstance = asset.getManagedObjectInstance();
						if (asset.getManagedObjectType()!= null) assetManagedObjectType = asset.getManagedObjectType();
					}

					managedObjectInstance= assetManagedObjectInstance;
					managedObjectType = assetManagedObjectType;

					LOG.debug("{} isQoSDrxAlarm=FALSE  OpenNMS type and instance not set. Using from Node Asset record: ManagedObjectInstance: {} ManagedObjectType:{}", logheader, managedObjectInstance, managedObjectType);
				}
			}
			catch(Throwable ex) {
				LOG.error("{} Problem getting node and asset information", logheader, ex);
			}
		} else { // is a received alarm
			try {
				managedObjectInstance= _openNMSalarm.getManagedObjectInstance();
				managedObjectType =_openNMSalarm.getManagedObjectType();

				LOG.debug("{} isQoSDrxAlarm=FALSE  OpenNMS type and instance not set. Using from Node Asset record: ManagedObjectInstance: {} ManagedObjectType:{}", logheader, managedObjectInstance, managedObjectType);
			} 
			catch(Throwable ex)	{
				LOG.error("{} Problem managedObjectInstance or managedObjectType", logheader, ex);
			}

		}

		alarmValueSpecification.setManagedObjectClass(managedObjectType);
		LOG.debug("{} _av.setManagedObjectClass set to: ", logheader, managedObjectType);

		alarmValueSpecification.setManagedObjectInstance(managedObjectInstance);
		LOG.debug("{} _av.setManagedObjectInstance set to: ", logheader, managedObjectInstance);

		// set severity and probable cause
		try {			
			alarmValueSpecification.setPerceivedSeverity(onmsSeverityToOssjSeverity(_openNMSalarm.getSeverity()));

//			alarmValueSpecification.setProbableCause((short)-1); // OSS/J set to -1  then text contains description
			alarmValueSpecification.setProbableCause((short)_openNMSalarm.getX733ProbableCause());

		}
		catch (Throwable e) {
			LOG.error("{} Problem getting severity or probable cause: ", logheader, e);
		}

		if (!isQoSDrxAlarm ) { // if is a locally generated alarm

			try {		
				String _opinstr = _openNMSalarm.getOperInstruct();
				if (null != _opinstr) {
					matcher = p.matcher(_opinstr);
					_opinstr = matcher.replaceAll(" "); // remove all HTML tags from operator instructions
				}
				else _opinstr = "NOT_SET";
				alarmValueSpecification.setProposedRepairActions(_opinstr);

				String _logmsg = _openNMSalarm.getLogMsg();
				if (null != _logmsg ) {
					matcher = p.matcher(_logmsg );
					_logmsg  = matcher.replaceAll(" "); // remove all HTML tags from operator instructions
				}
				else _logmsg = "NOT_SET";

				String _description = _openNMSalarm.getDescription();
				if (null != _description ) {
					matcher = p.matcher(_description );
					_description  = matcher.replaceAll(" "); // remove all HTML tags from description
				}
				else _description = "NOT_SET";

				// using manufacturers own definition of specific problem here ( OSS/J )
				alarmValueSpecification.setSpecificProblem( _logmsg );
				Integer alarmid= _openNMSalarm.getId();
				Integer counter= _openNMSalarm.getCounter();
				String reductionkey= _openNMSalarm.getReductionKey();
				
				// note some OnmsAlarms can have null nodes - we use a default value of 0 for ID
				Integer nodeid=0;
				String onmsnodelabel="";
				if (_openNMSalarm.getNode()!= null) {
					nodeid= _openNMSalarm.getNode().getId();
					onmsnodelabel= _openNMSalarm.getNode().getLabel();
				}
				InetAddress ipaddress= _openNMSalarm.getIpAddr();
				String x733AlarmType= _openNMSalarm.getX733AlarmType();
				String x733ProbableCause;
				try {
					x733ProbableCause= OOSSProbableCause.getStringforEnum((short) _openNMSalarm.getX733ProbableCause());
				}catch (Throwable e){
					x733ProbableCause="X733 Probable Cause Incorrectly Defined";
				}

				alarmValueSpecification.setAdditionalText(
						"<alarmid>"  + alarmid + "</alarmid>" + "\n            " +
						"<logmsg>"+ _logmsg +"</logmsg>"+   "\n            " +
						"<uei>" + 	_uei_no_html + "<uei>" +  "\n            " +
						"<x733AlarmType>"  + x733AlarmType + "</x733AlarmType>" + "\n            " +
						"<x733ProbableCause>"  + x733ProbableCause + "</x733ProbableCause>" + "\n            " +
						"<counter>" + counter + "</counter>" +  "\n            " +
						"<reductionkey>" + reductionkey + "</reductionkey>" +  "\n            " +
						"<nodeid>" + nodeid + "</nodeid>" +  "\n            " +
						"<nodelabel>" + onmsnodelabel + "</nodelabel>" +  "\n            " +
						"<ipaddress>" + InetAddressUtils.toIpAddrString(ipaddress) + "</ipaddress>" +   "\n            " +
						"<description>"+ _description +"</description>" +  "\n            " +
						"<opinstr>" + _opinstr + "</opinstr>" + "\n            " +
						"<asset.managedobjectinstance>" + assetManagedObjectInstance + "</asset.managedobjectinstance>" + "\n            "+              //TODO - was used for object instance
						"<asset.managedobjecttype>" + assetManagedObjectType + "</asset.managedobjecttype>" + "\n            "+ 
						"<asset.address2>" + assetAddress2 + "</asset.address2>" + "\n            "+  //TODO - was used for object instance
						"<asset.description>" + assetDescription + "</asset.description>" + "\n");    //TODO - was used for object instancetype

			} catch (Throwable e){
				LOG.error("{} Problem setting description, logmessage or operator instrctions: ", logheader, e);
			}

		} else { // is a received alarm 
			try {		
				String _opinstr = _openNMSalarm.getOperInstruct();
				if (null == _opinstr) _opinstr = "NOT_SET";
				alarmValueSpecification.setProposedRepairActions(_opinstr);

				String _logmsg = _openNMSalarm.getLogMsg();
				if (null == _logmsg ) _logmsg = "NOT_SET";
				// using manufacturers own definition of specific problem here ( OSS/J )
				alarmValueSpecification.setSpecificProblem( _logmsg );

				String _description = _openNMSalarm.getDescription();
				if (null == _description ) _description = "NOT_SET";
				alarmValueSpecification.setAdditionalText(_description);

			} catch (Throwable e){
				LOG.error("{} Problem setting description, logmessage or operator instrctions: ", logheader, e);
			}
		}

		// TODO replacement method to populate the alarm key
		try {
			//populate alarm key
			//TODO was AlarmKey ak = new OOSSAlarmKey(Integer.toString(_openNMSalarm.getId()));
			AlarmKey ak= alarmValueSpecification.getAlarmKey();
			ak.setAlarmPrimaryKey(Integer.toString(_openNMSalarm.getId()));
			ak.setPrimaryKey(ak.getAlarmPrimaryKey());
		}
		catch (Throwable e) {
			LOG.error("{} Problem setting AlarmKey: ", logheader, e);
		}

		LOG.debug("{} Alarm Populated", logheader);

		return alarmValueSpecification;
	} // end populateAlarm()





	/**
	 * convenience method to map OSS/J to OpenNMS severities
	 * A switch statement converts the OSS/J severity qualifier
	 * over to one compatible with OpenNMS
	 * From OpenNMS code;
	 * public static final int INDETERMINATE_SEVERITY = 1;
	 * public static final int CLEARED_SEVERITY = 2;
	 * public static final int NORMAL_SEVERITY = 3;
	 * public static final int WARNING_SEVERITY = 4;
	 * public static final int MINOR_SEVERITY = 5;
	 * public static final int MAJOR_SEVERITY = 6;
	 * public static final int CRITICAL_SEVERITY = 7;
	 *
	 * NOTE  org.opennms.web.alarm.Alarm.NORMAL_SEVERITY has no equivilent in OSS/J X733
	 *
	 * @param ossjseverity the severity value according to ossj / X733
	 * @return the severity value according to opennms
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public static OnmsSeverity ossjSeveritytoOnmsSeverity(short ossjseverity) throws IllegalArgumentException{

		OnmsSeverity onmsseverity;

		switch(ossjseverity)
		{
		case javax.oss.fm.monitor.PerceivedSeverity.INDETERMINATE:
			onmsseverity = OnmsSeverity.INDETERMINATE;
			break;
		case javax.oss.fm.monitor.PerceivedSeverity.CLEARED:
			onmsseverity = OnmsSeverity.CLEARED;
			break;
		case javax.oss.fm.monitor.PerceivedSeverity.WARNING:
			onmsseverity = OnmsSeverity.WARNING;
			break;
		case javax.oss.fm.monitor.PerceivedSeverity.MINOR:
			onmsseverity = OnmsSeverity.MINOR;
			break;
		case javax.oss.fm.monitor.PerceivedSeverity.MAJOR:
			onmsseverity = OnmsSeverity.MAJOR;
			break;
		case javax.oss.fm.monitor.PerceivedSeverity.CRITICAL:
			onmsseverity = OnmsSeverity.CRITICAL;
			break;
		default: throw new IllegalArgumentException("invalid OSS/J severity value:"+ossjseverity);
		}
		return onmsseverity;
	}

	/**
	 * convenience method to map OpenNMS to OSS/J  severities			 
	 * From OpenNMS code;
	 * public static final int INDETERMINATE_SEVERITY = 1;
	 * public static final int CLEARED_SEVERITY = 2;
	 * public static final int NORMAL_SEVERITY = 3;
	 * public static final int WARNING_SEVERITY = 4;
	 * public static final int MINOR_SEVERITY = 5;
	 * public static final int MAJOR_SEVERITY = 6;
	 * public static final int CRITICAL_SEVERITY = 7;
	 * 
	 * NOTE  org.opennms.web.alarm.Alarm.NORMAL_SEVERITY has no equivilent in OSS/J X733
	 * 
	 * @param onmsSeverity the severity value according to opennms
	 * @return  the severity value according to ossj / X733
	 * 
	 */
	private static short onmsSeverityToOssjSeverity(OnmsSeverity onmsSeverity ) throws IllegalArgumentException{

		short ossjseverity=0;

		if (onmsSeverity==null) throw new IllegalArgumentException("onmsSeverityToOssjSeverity: onmsSeverity is Null");

		switch(onmsSeverity)
		{
		case INDETERMINATE:
			ossjseverity=javax.oss.fm.monitor.PerceivedSeverity.INDETERMINATE; 
			break;
		case CLEARED:
			ossjseverity=javax.oss.fm.monitor.PerceivedSeverity.CLEARED;
			break;
		case NORMAL:
			ossjseverity=javax.oss.fm.monitor.PerceivedSeverity.WARNING;
			break;
		case WARNING:
			ossjseverity=javax.oss.fm.monitor.PerceivedSeverity.WARNING;
			break;
		case MINOR:
			ossjseverity=javax.oss.fm.monitor.PerceivedSeverity.MINOR;
			break;
		case MAJOR:
			ossjseverity=javax.oss.fm.monitor.PerceivedSeverity.MAJOR;
			break;
		case CRITICAL:
			ossjseverity=javax.oss.fm.monitor.PerceivedSeverity.CRITICAL;
			break;
		default: throw new IllegalArgumentException("invalid OpenNMS severity value:"+onmsSeverity);
		}
		return ossjseverity;
	}

	/**
	 * Maps OSS/J alarm types to OpenNMS uei types
	 *
	 * @param alarmType String representing OSS/J alarm Type
	 * @return string representing equivilent OpenNMS uei
	 */
	public static String ossjAlarmTypeToUei(String alarmType){

		if (alarmType==null) {
			return "uei.openoss.org.alarm/unknown";
		}
		else if (alarmType.equals(AlarmType.COMMUNICATIONS_ALARM)){
			return "uei.openoss.org.alarm/CommunicationsAlarm";
		}
		else if (alarmType.equals(AlarmType.ENVIRONMENTAL_ALARM)){
			return "uei.openoss.org.alarm/EnvironmentalAlarm";
		}
		else if (alarmType.equals(AlarmType.EQUIPMENT_ALARM)){
			return "uei.openoss.org.alarm/EquipmentAlarm";
		}
		else if (alarmType.equals(AlarmType.INTEGRITY_VIOLATION)){
			return "uei.openoss.org.alarm/IntegrityViolation";
		}
		else if (alarmType.equals(AlarmType.OPERATIONAL_VIOLATION)){
			return "uei.openoss.org.alarm/OperationalViolation";
		}
		else if (alarmType.equals(AlarmType.PHYSICAL_VIOLATION)){
			return "uei.openoss.org.alarm/PhysicalViolation";
		}
		else if (alarmType.equals(AlarmType.PROCESSING_ERROR_ALARM)){
			return "uei.openoss.org.alarm/ProcessingErrorAlarm";
		}
		else if (alarmType.equals(AlarmType.QUALITY_OF_SERVICE_ALARM)){
			return "uei.openoss.org.alarm/QualityOfServiceAlarm";
		}
		else if (alarmType.equals(AlarmType.SECURITY_VIOLATION)){
			return "uei.openoss.org.alarm/SecurityViolation";
		}
		else if (alarmType.equals(AlarmType.TIME_DOMAIN_VIOLATION)){
			return "uei.openoss.org.alarm/TimeDomainViolation";
		}
		else return "uei.openoss.org.alarm/unknown";
	}

}

