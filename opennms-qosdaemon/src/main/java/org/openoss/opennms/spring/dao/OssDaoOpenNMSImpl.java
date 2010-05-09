// This file is part of the OpenNMS(R) QoSD OSS/J interface.
//
// Modifications:
//
// 2008 Oct 04: Use new OnmsSeverity object on OnmsAlarms. - dj@opennms.org
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

package org.openoss.opennms.spring.dao;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.openoss.opennms.spring.qosdrx.QoSDrx;
import org.openoss.opennms.spring.qosd.QoSD;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Hashtable;
import java.util.Enumeration;

public class OssDaoOpenNMSImpl implements OssDao {

	private static final String LOG4J_CATEGORY = "OpenOSS.QoSDrx";

	/** 
	 * local store for OpenNMS alarm list indexed by OpenNMS AlarmID as Integer
	 */
	private final Hashtable<Integer,OnmsAlarm> alarmCacheByID = new Hashtable<Integer,OnmsAlarm>(); 

	/** 
	 * local store for OpenNMS alarm list indexed by ApplicationDN+OssPrimaryKey() as string
	 */
	private final Hashtable<String,OnmsAlarm> alarmCacheByUniqueKey = new Hashtable<String,OnmsAlarm>(); 

	/** 
	 * local store for OpenNMS node list indexed by OpenNMS NodeID as Integer
	 */
	private final Hashtable<Integer,OnmsNode> nodeCacheByID = new Hashtable<Integer,OnmsNode>();
	/** 
	 * local store for OpenNMS node list indexed by OpenNMS NodeLabel as String
	 */
	private final Hashtable<String,OnmsNode> nodeCacheByLabel = new Hashtable<String,OnmsNode>();
	/** 
	 * local store for OpenNMS node list indexed by ManagedObjectInstance+ManagedObjectType as string
	 * */
	private final Hashtable<String,OnmsNode> nodeCacheByUniqueID = new Hashtable<String,OnmsNode>();


	/**
	 *  Method to get the QoSDrx's logger from OpenNMS
	 */
	private static Logger getLog() {
		return (Logger)ThreadCategory.getInstance(OssDaoOpenNMSImpl.class);	
	}

	// ****************
	// Spring DAO setters
	// ****************

	/**
	 * Used to create new Lazy objects
	 * 
	 */
	protected DataSource _dataSource;

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#setDataSource(javax.sql.DataSource)
	 */
	public void setDataSource(DataSource dataSource) {
		_dataSource = dataSource;
	}

	/**
	 * Used to obtain opennms asset information for inclusion in alarms
	 * @see org.opennms.netmgt.dao.AssetRecordDao
	 */
	protected AssetRecordDao _assetRecordDao;


	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#setAssetRecordDao(org.opennms.netmgt.dao.AssetRecordDao)
	 */
	public void setAssetRecordDao(AssetRecordDao ar){
		_assetRecordDao = ar;
	}

	/**
	 * Used to obtain opennms node information for inclusion in alarms
	 * @see org.opennms.netmgt.dao.NodeDao 
	 */
	protected NodeDao _nodeDao;

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#setNodeDao(org.opennms.netmgt.dao.NodeDao)
	 */
	public  void setNodeDao( NodeDao nodedao){
		_nodeDao = nodedao;
	}

	/**
	 * Used to search and update opennms alarm list
	 * @see org.opennms.netmgt.dao.AlarmDao
	 */
	protected AlarmDao _alarmDao;

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#setAlarmDao(org.opennms.netmgt.dao.AlarmDao)
	 */
	public  void setAlarmDao( AlarmDao alarmDao){
		_alarmDao = alarmDao;
	}

	/**
	 * Used to ensure that objects retreived from OpenNMS through Hibernate
	 * contain all of their internal objects populated with values
	 */
	protected TransactionTemplate transTemplate;

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#setTransTemplate(org.springframework.transaction.support.TransactionTemplate)
	 */
	public void setTransTemplate(TransactionTemplate _transTemplate) {
		transTemplate = _transTemplate;
	}

	// ************************
	// Qosd callback interface
	// ************************

	/**
	 * Used to provide a call back interface to QoSD for forwarding changes to alarm list
	 * @param alarmDao
	 */
	private QoSD qoSD = null; 

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#setQoSD(org.openoss.opennms.spring.qosd.QoSD)
	 */
	public void setQoSD(QoSD _qoSD){
		qoSD=_qoSD;
	}
	
	// ******************
	// initialise method
	// ******************
	
	private boolean initialised=false; // true if init() has initialised class

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#init()
	 */
	synchronized public void init(){
		if (initialised) return;

		try {
			localUpdateNodeCaches();
			localUpdateAlarmCache();
			initialised=true;
		} catch (Exception ex){
			throw new UndeclaredThrowableException(ex, this.getClass().getSimpleName()+"init() problem initialising class");
		}
	}



	// ************************
	// Business Methods
	// ************************


	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#addCurrentAlarmForUniqueKey(org.opennms.netmgt.model.OnmsAlarm)
	 */


	public synchronized OnmsAlarm addCurrentAlarmForUniqueKey(final OnmsAlarm alarm){
		Logger log = getLog();	

		if ((alarm==null)||(alarm.getId()!=null)) 
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().addCurrentAlarmForUniqueKey(): Illegal value: alarm==null or alarmID!=null");
		if ((alarm.getAlarmType()!=1)) 
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().addCurrentAlarmForUniqueKey(): Illegal value: alarm.getAlarmType() not 'raise' alarm type '1'");
		if ((alarm.getApplicationDN()==null)||(alarm.getApplicationDN().equals(""))) 
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().addCurrentAlarmForUniqueKey(): Illegal value: alarm ApplicationDN null or empty ");
		if ((alarm.getOssPrimaryKey()==null)||(alarm.getOssPrimaryKey().equals(""))) 
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().addCurrentAlarmForUniqueKey(): Illegal value: alarm OssPrimaryKey null or empty");


		OnmsAlarm checkAlarm=getCurrentAlarmForUniqueKey(alarm.getApplicationDN() , alarm.getOssPrimaryKey());
		if (checkAlarm!=null){ // if not a unique alarm throw error
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().addCurrentAlarmForUniqueKey(): Illegal value: alarm not unique in Current Alarm list: ApplicationDN:"+alarm.getApplicationDN()+" OssPrimaryKey:"+alarm.getOssPrimaryKey());
		} 
		else {
			try { // add new alarm then save alarm in local alarm list
				if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().addCurrentAlarmForUniqueKey(): ALARM TO SAVE:\n"+alarmToString(alarm));

				String uniqueKey=alarm.getApplicationDN()+alarm.getOssPrimaryKey();

				//_alarmDao.save(alarm); // - replaced by;
				transTemplate.execute(new TransactionCallback<Object>() {
					public Object doInTransaction(TransactionStatus status) {
						_alarmDao.save(alarm);
						return null;
					}
				});				

				alarmCacheByID.put(new Integer (alarm.getId()), alarm); // update local cache
				alarmCacheByUniqueKey.put(uniqueKey, alarm);			

			} catch (Exception ex){
				log.error("OssDaoOpenNMSImpl().addCurrentAlarmForUniqueKey():Error creating alarm in database:",ex);
				return null;
			}
//			try { // add new alarm then update alarm in local alarm list 
//			alarm = getCurrentAlarmForUniqueKey(alarm.getApplicationDN() , alarm.getOssPrimaryKey());
//			} catch (Exception ex){
//			log.error("OssDaoOpenNMSImpl().addCurrentAlarmForUniqueKey():Error updating alarm in local list:"+ex);
//			return null;
//			}
		}

		if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().addCurrentAlarmForUniqueKey(): ALARM SAVED"+ alarmToStringBrief(alarm));
		try{
			if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().addCurrentAlarmForUniqueKey(): Sending Updated alarm list to QoSD");
			sendAlarms();
		} 	catch ( Exception e ){
			// ignore this exception as Qosd may not be running
			if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().addCurrentAlarmForUniqueKey(): problem sending alarm to QoSD ( QoSD may not be running ):" + e);
		}
		return alarm;
	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#updateCurrentAlarmForUniqueKey(org.opennms.netmgt.model.OnmsAlarm)
	 */
	public synchronized OnmsAlarm updateCurrentAlarmForUniqueKey(final OnmsAlarm alarm){
		Logger log = getLog();	

		if ((alarm==null)||(alarm.getId()==null)) 
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().updateCurrentAlarmForUniqueKey(): Illegal value: alarm==null or alarmID==null");
		if ((alarm.getAlarmType()!=1)) 
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().updateCurrentAlarmForUniqueKey(): Illegal value: alarm.getAlarmType() not 'raise' alarm type '1'");
		if ((alarm.getApplicationDN()==null)||(alarm.getApplicationDN().equals(""))) 
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().updateCurrentAlarmForUniqueKey(): Illegal value: alarm ApplicationDN null or empty ");
		if ((alarm.getOssPrimaryKey()==null)||(alarm.getOssPrimaryKey().equals(""))) 
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().updateCurrentAlarmForUniqueKey(): Illegal value: alarm OssPrimaryKey null or empty");

		String uniqueKey=alarm.getApplicationDN()+alarm.getOssPrimaryKey();

		OnmsAlarm updateAlarm=getCurrentAlarmForUniqueKey(alarm.getApplicationDN() , alarm.getOssPrimaryKey());
		if (updateAlarm!=null){ // if alarm in database then update alarm in OpenNMS

			alarm.setId(updateAlarm.getId());
			try { // if alarm in database then update alarm in OpenNMS
				if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().updateCurrentAlarmForUniqueKey: alarm to update in database"+ alarmToStringBrief(alarm));

				transTemplate.execute(new TransactionCallback<Object>() {
					public Object doInTransaction(TransactionStatus status) {
						_alarmDao.update(alarm);
						return null;
					}
				});	
			
				alarmCacheByID.put(new Integer (alarm.getId()), alarm); // update local cache
				alarmCacheByUniqueKey.put(uniqueKey, alarm);

			} catch (Exception ex){
				log.error("OssDaoOpenNMSImpl().updateCurrentAlarmForUniqueKey():Error updating alarm in database:",ex);
				return null;
			}		
		} 
		else { // if alarm not in database throw error
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().updateCurrentAlarmForUniqueKey(): Illegal value: alarm not found in Current Alarm list: ApplicationDN:"+alarm.getApplicationDN()+" OssPrimaryKey:"+alarm.getOssPrimaryKey());
		}

		if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().updateCurrentAlarmForUniqueKey: Sending Updated alarm list Updated alarm"+ alarmToStringBrief(alarm));

		// force send of updated alarm list
		try{
			if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().updateCurrentAlarmForUniqueKey: Sending Updated alarm list to QoSD");
			sendAlarms();
		} 	catch ( Exception e ){
			// ignore this exception as Qosd may not be running
			if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().updateCurrentAlarmForUniqueKey: problem sending alarm to  QoSD:" + e);
		}

		return alarm;
	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#getCurrentAlarmForUniqueKey(java.lang.String, java.lang.String)
	 */
	public synchronized OnmsAlarm getCurrentAlarmForUniqueKey(String applicationDN , String ossPrimaryKey){
		Logger log = getLog();	

		if ((applicationDN==null)||(applicationDN.equals(""))) 
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().getCurrentAlarmForUniqueKey(): Illegal value: alarm ApplicationDN null or empty ");
		if ((ossPrimaryKey==null)||(ossPrimaryKey.equals(""))) 
			throw new IllegalArgumentException("OssDaoOpenNMSImpl().getCurrentAlarmForUniqueKey(): Illegal value: alarm OssPrimaryKey null or empty");

		OnmsAlarm alarm=null;

		try {
			// check if alarm is in local alarm cache alarmCacheByID
			if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().getCurrentAlarmForUniqueKey: checking if alarm is in local alarm cache alarmCacheByID");
			alarm=searchAlarmCacheForUniqueKey(applicationDN, ossPrimaryKey);

		} catch ( Exception ex) {
			log.error("\tOssDaoOpenNMSImpl().getCurrentAlarmForUniqueKey ERROR : ", ex);
		}
		if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().getCurrentAlarmForUniqueKey: alarm result ="+ alarmToStringBrief(alarm));
		return alarm; // null if not found
	}

	/**
	 * forces an update of the alarm cache from the OpenNMS database
	 * Not Thread Safe - only to be called from within the synchronised methods
	 */
	private void localUpdateAlarmCache(){
		transTemplate.execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus status) {
				localUpdateAlarmCacheTransaction();
				return null;
			}
		});
	}

	/**
	 * method to run in transaction to update from database
	 */
	private void localUpdateAlarmCacheTransaction(){
		Logger log = getLog();	


		Collection<OnmsAlarm> c = _alarmDao.findAll();

		alarmCacheByID.clear(); // clear previous hashtable
		alarmCacheByUniqueKey.clear();
		OnmsAlarm[] alarms= (OnmsAlarm[]) c.toArray(new OnmsAlarm[c.size()]);
		//TODO - ISSUE if too many alarms?
		for (int i=0; i<alarms.length; i++){
			OnmsAlarm newalarm=alarms[i];
			// retrieve inner contents of alarm node if there is a node associated with the alarm
			if (newalarm.getNode()!=null) {
				newalarm.getNode().getLabel();
			}

			alarmCacheByID.put(new Integer (newalarm.getId()), newalarm);

			// only update alarmCacheByUniqueKey if key is not null or empty
			if ( ! (
					(newalarm.getApplicationDN()==null)||
					(newalarm.getOssPrimaryKey()==null)||
					(newalarm.getApplicationDN().equals("")) ||
					(newalarm.getOssPrimaryKey().equals("")) )
			) {
				String uniqueKey=newalarm.getApplicationDN()+newalarm.getOssPrimaryKey();
				if (alarmCacheByUniqueKey.get(uniqueKey)==null) {
					alarmCacheByUniqueKey.put(uniqueKey, newalarm);
				} else {
					log.error("\tOssDaoOpenNMSImpl().localUpdateAlarmCache(): ERROR - duplicate alarm uniqueKey in database ="+ uniqueKey +" AlarmID:"+newalarm.getId());
				}
			}

		}
	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#updateAlarmCache()
	 */
	synchronized public void updateAlarmCache() throws IllegalStateException{
		localUpdateAlarmCache();
	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#updateAlarmCacheAndSendAlarms()
	 */
	synchronized public void updateAlarmCacheAndSendAlarms() throws IllegalStateException{
		localUpdateAlarmCache();
		sendAlarms();
	}

	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#getAlarmCache()
	 */
	public OnmsAlarm[] getAlarmCache(){
		OnmsAlarm[] returnAlarmCache= new OnmsAlarm[alarmCacheByID.size()];
		int i=0;
		Enumeration<Integer> alarmIDS = alarmCacheByID.keys();
		while(alarmIDS.hasMoreElements()) {
			Integer alarmID = alarmIDS.nextElement();
			returnAlarmCache[i] = alarmCacheByID.get(alarmID);
			i++;
		}
		return returnAlarmCache;
	}

	/**
	 * Tries to call back to QoSD to send the latest alarm list after an update.
	 * If QoSD not running. Logs a debug message and returns
	 * Note this is NOT synchronized as it is always called from within a synchronized method in this class
	 * @throws an IllegalStateException if qoSD not running.
	 */
	private void sendAlarms(){
		Logger log = getLog();	
		if ( qoSD!=null){
			try{
				qoSD.sendAlarms();
			} catch ( Exception ex){
				log.error("\tOssDaoOpenNMSImpl().sendAlarms() Problem calling back to qoSD:",ex );
				throw new IllegalStateException("OssDaoOpenNMSImpl().sendAlarms() Problem calling back to qoSD:",ex);
			}
		} else {
			if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().sendAlarms(): QoSD not running - not calling back QoSD to send alarms");
		}
	}

	/**
	 * search the current alarm cache for alarm with unique ApplicationDN and Unique PrimaryKey
	 * @param applicationDN - unique ApplicationDN to search for
	 * @param ossPrimaryKey - unique OssPrimaryKey to search for
	 * @return matching alarm if found or Null if match not found
	 * Not Thread Safe - only to be called from within the synchronised methods
	 */
	private OnmsAlarm searchAlarmCacheForUniqueKey(String applicationDN , String ossPrimaryKey) {
		Logger log = getLog();	

		String uniqueKey=applicationDN+ossPrimaryKey;
		OnmsAlarm alarm=(OnmsAlarm)alarmCacheByUniqueKey.get(uniqueKey);
		if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().searchAlarmBufForUniqueKey alarmCacheByID search result:"+alarmToStringBrief(alarm));

		return alarm;
	}

	/**
	 * Debug method to print out opennms alarms (brief summary)
	 * @param alarm
	 * @return string to print out
	 */
	private static String alarmToStringBrief(OnmsAlarm alarm){
		String s;
		if (alarm==null) {
			s="\n\t\tOnmsAlarm is Null";
		} 
		else {
			s=      
				"\n\t\tapplicationDN \t"+ alarm.getApplicationDN() //applicationDN
				+"\t\tossPrimaryKey \t" + alarm.getOssPrimaryKey() //ossPrimaryKey
				+"\t\talarmID " 		+ alarm.getId() //alarmID
				+"\t\tSeverity():"		+ alarm.getSeverity().getId()
				+"\t\tAlarmAckUser():"	+ alarm.getAlarmAckUser()
				+"\t\tAlarmAckTime():"	+ alarm.getAlarmAckTime();
		}
		return s;
	}



	/**
	 * Debug method to print out opennms alarms
	 * @param alarm
	 * @return string to print out
	 */
	public static String alarmToString(OnmsAlarm alarm){
		String s;
		if (alarm==null) {
			s="\n\t\tOnmsAlarm is Null";
		} 
		else {
			s=
				"\n\t\teventUei \t" + 	alarm.getUei()+"\n"+ //eventUei
				"\t\tdpName \t" + 	(alarm.getDistPoller() == null ? null : alarm.getDistPoller().getName())+"\n"+ //dpName
				"\t\tnodeID \t" + 	(alarm.getNode() == null ? null : alarm.getNode().getId())+"\n"+ //nodeID
				"\t\tipaddr \t" + 	alarm.getIpAddr()+"\n"+ //ipaddr
				"\t\tserviceID \t" + 	(alarm.getServiceType() == null ? null : alarm.getServiceType().getId())+"\n"+ //serviceID
				"\t\treductionKey \t" + 	alarm.getReductionKey()+"\n"+ //reductionKey
				"\t\talarmType \t" + 	alarm.getAlarmType()+"\n"+ //alarmType
				"\t\tcounter \t" + 	alarm.getCounter()+"\n"+ //counter
				"\t\tseverity \t" + 	alarm.getSeverity().getId()+"\n"+ //severity
				"\t\tlastEventID \t" + 	(alarm.getLastEvent() == null ? null : alarm.getLastEvent().getId())+"\n"+ //lastEventID
				"\t\tfirstEventTime \t" + 	alarm.getFirstEventTime()+"\n"+ //firstEventTime
				"\t\tlastEventTime \t" + 	alarm.getLastEventTime()+"\n"+ //lastEventTime
				"\t\tdescription \t" + 	alarm.getDescription()+"\n"+ //description
				"\t\tlogMsg \t" + 	alarm.getLogMsg()+"\n"+ //logMsg
				"\t\toperInstruct \t" + 	alarm.getOperInstruct()+"\n"+ //operInstruct
				"\t\ttticketID \t" + 	alarm.getTTicketId()+"\n"+ //tticketID
				"\t\ttticketState \t" + 	alarm.getTTicketState()+"\n"+ //tticketState
				"\t\tmouseOverText \t" + 	alarm.getMouseOverText()+"\n"+ //mouseOverText
				"\t\tsuppressedUntil \t" + 	alarm.getSuppressedUntil()+"\n"+ //suppressedUntil
				"\t\tsuppressedUser \t" + 	alarm.getSuppressedUser()+"\n"+ //suppressedUser
				"\t\tsuppressedTime \t" + 	alarm.getSuppressedTime()+"\n"+ //suppressedTime
				"\t\talarmAckUser \t" + 	alarm.getAlarmAckUser()+"\n"+ //alarmAckUser
				"\t\talarmAckTime \t" + 	alarm.getAlarmAckTime()+"\n"+ //alarmAckTime
				"\t\tclearKey   \t" + 	alarm.getClearKey()+"\n"+ //clearKey
				"\t\tmanagedObjectInstance \t" + 	alarm.getManagedObjectInstance()+"\n"+ //managedObjectInstance        
				"\t\tmanagedObjectType \t" + 	alarm.getManagedObjectType()+"\n"+ //managedObjectType
				"\t\tapplicationDN \t" + 	alarm.getApplicationDN()+"\n"+ //applicationDN
				"\t\tossPrimaryKey \t" + 	alarm.getOssPrimaryKey()+"\n"+ //ossPrimaryKey
				"\t\talarmID \t" + 	alarm.getId()+"\n"+ //alarmID
			    "\t\tqosAlarmState \t" + 	alarm.getQosAlarmState(); //qosAlarmState
		}
		return s;
	}

	// ***********************************************
	// NODE METHODS
	// ***********************************************


	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#findNodeByLabel(java.lang.String)
	 */
	public OnmsNode findNodeByLabel(String label) {
		Logger log = getLog();	

		if (label==null) throw new IllegalArgumentException("OssDaoOpenNMSImpl().findNodeByLabel: Illegal value: label null or empty ");
		OnmsNode node=null;
		try {
			node = (OnmsNode)nodeCacheByLabel.get(label);
		} catch (Exception ex){
			log.error("\tOssDaoOpenNMSImpl().findNodeByLabel ERROR : ", ex);
		}
		return node; //null if not found
	}


	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#findNodeByInstanceAndType(java.lang.String, java.lang.String)
	 */
	public OnmsNode findNodeByInstanceAndType(String managedObjectInstance, String managedObjectType) throws IllegalArgumentException{
		Logger log = getLog();	
		if (managedObjectInstance==null) throw new IllegalArgumentException("OssDaoOpenNMSImpl().findNodeByInstanceType: Illegal value: managedObjectInstance null");
		if (managedObjectType==null) throw new IllegalArgumentException("OssDaoOpenNMSImpl().findNodeByInstanceType: Illegal value: label managedObjectType null");

		String uniqueid=managedObjectInstance+managedObjectType;
		OnmsNode node=null;
		try {
			node = (OnmsNode)nodeCacheByUniqueID.get(uniqueid);
		} catch (Exception ex){
			log.error("\tOssDaoOpenNMSImpl().findNodeByInstanceAndType ERROR : ", ex);
		}
		return node; //null if not found
	}




	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#findNodeByID(java.lang.Integer)
	 */
	public OnmsNode findNodeByID(Integer nodeid){
		Logger log = getLog();	

		if (nodeid==null) throw new IllegalArgumentException("OssDaoOpenNMSImpl().findNodeByLabel: Illegal value: nodeid null or empty ");
		OnmsNode node=null;
		try {
			node = (OnmsNode)this.nodeCacheByID.get(nodeid);
		} catch (Exception ex){
			log.error("\tOssDaoOpenNMSImpl().findNodeByLabel ERROR : ", ex);
		}
		return node; //null if not found
	}


	/* (non-Javadoc)
	 * @see org.openoss.opennms.spring.dao.OssDao#updateNodeCaches()
	 */
	public synchronized void updateNodeCaches(){
		localUpdateNodeCaches();
	}

	/**
	 * Update the node cache from the OpenNMS database
	 * This must be run at least once to ensure that node data is available
	 * Not Thread Safe - only to be called from within the synchronised methods
	 */
	private void localUpdateNodeCaches(){
		Logger log = getLog();	
		try{
			nodeCacheByID.clear();
			nodeCacheByLabel.clear();
			nodeCacheByUniqueID.clear();

			if (log.isDebugEnabled()) log.debug("\tOssDaoOpenNMSImpl().updateNodeCaches - Updating Node Caches :");

			Collection<OnmsNode> c = _nodeDao.findAll();
			OnmsNode[] nodelist= (OnmsNode[]) c.toArray(new OnmsNode[c.size()]);
			for (int i=0; i<nodelist.length; i++){
				OnmsNode node=(OnmsNode)nodelist[i];
				try {
					// update node by ID cache
					this.nodeCacheByID.put((Integer)node.getId(), node);

					// update node by Label cache
					if (node.getLabel()!=null) {
						if (nodeCacheByLabel.get((String)node.getLabel())!=null){
							log.info("\tOssDaoOpenNMSImpl().updateNodeCaches WARNING node.getId():"+node.getId()+" Node Label:"+node.getId()+" is duplicated");
						} else {
							nodeCacheByLabel.put((String)node.getLabel(), node);
						}
					} else {
						log.info("\tOssDaoOpenNMSImpl().updateNodeCaches WARNING node.getId():"+node.getId()+" Node Label is NULL. Not putting node in nodeCacheByLabel");
					}

					// update node by Unique ID -managedObjectInstance+ManagedObjectType 
					final OnmsAssetRecord assetRecord = node.getAssetRecord();
					if (assetRecord==null) {
						log.info("\tOssDaoOpenNMSImpl().updateNodeCaches WARNING node.getId():"+node.getId()+" assetRecord is NULL. Not putting node in nodeCacheByUniqueID");
						continue;
					} else {
						
						// Note that the node asset record data for instance and type are only filled 
						// given default values once - subsequently changes must be explicitly set directly
						// in the database
						String moi=assetRecord.getManagedObjectInstance();
						if ((moi==null)||("".equals(moi))){
							String fid  = (node.getForeignId()==null) ? "" : node.getForeignId();
							String fsrc = (node.getForeignSource()==null) ? "" : node.getForeignSource() ;
							String label= (node.getLabel()==null) ? "" : node.getLabel();
							moi  = "Label:"+label+":ForeignSource:"+fsrc+":ForeignId():"+fid;
							log.info("\tOssDaoOpenNMSImpl().updateNodeCaches WARNING node.getId():"+node.getId()
									+" ManagedObjectInstance is NULL. Setting  ManagedObjectInstance to: "+moi);
							assetRecord.setManagedObjectInstance(moi);
						}
						String mot=assetRecord.getManagedObjectType();
						if ((mot==null)||("".equals(mot))){
							mot="UNSPECIFIED_TYPE";
							log.info("\tOssDaoOpenNMSImpl().updateNodeCaches WARNING node.getId():"
									+node.getId()+"ManagedObjectType was NULL. Setting ManagedObjectType to: "+mot);
							assetRecord.setManagedObjectType(mot);
						}
						
						// save asset data back with new node information 
						// (Note - data may not have changed)
						transTemplate.execute(new TransactionCallback<Object>() {
							public Object doInTransaction(TransactionStatus status) {
								_assetRecordDao.update(assetRecord);
								return null;
							}
						});
						
						// update nodeCacheByUniqueID
						String uniqueid=assetRecord.getManagedObjectInstance()+assetRecord.getManagedObjectType();
						if (nodeCacheByUniqueID.get((String)uniqueid)!=null){
							log.info("\tOssDaoOpenNMSImpl().updateNodeCaches WARNING node.getId():"+node.getId()+
										" Unique ID is duplicated. Unique ID = ManagedObjectInstance:"+assetRecord.getManagedObjectInstance()+"+ ManagedObjectType:"+assetRecord.getManagedObjectType());
						} else {
							nodeCacheByUniqueID.put(uniqueid, node);
						}
						
					}
				} catch (Exception ex){
					log.error("\tOssDaoOpenNMSImpl().updateNodeCaches Error updating node caches: ERROR : ", ex);
				}	
			}
			if (log.isDebugEnabled()) {
				log.debug("\tOssDaoOpenNMSImpl().updateNodeCaches - Updated nodeCacheByID : contents :");
				try {
				    for (Integer id : nodeCacheByID.keySet()) {
						OnmsNode node =(OnmsNode)nodeCacheByID.get(id);
						log.debug("\t\tKey: Node ID:"+id+"\tNodeLabel:"+node.getLabel());
					}
				}catch ( Exception e){
					log.error("\tOssDaoOpenNMSImpl().updateNodeCaches: Problem listing nodeCacheByLabel contents Error:",e);
				}
				log.debug("\tOssDaoOpenNMSImpl().updateNodeCaches - Updated nodeCacheByLabel : contents :");
				try {
				    for (String label : nodeCacheByLabel.keySet()) {
						OnmsNode node =(OnmsNode)nodeCacheByLabel.get(label);
						log.debug("\t\tKey: Label:"+label+"\tNodeID:"+node.getId());
					}
				}catch ( Exception e){
					log.error("\tOssDaoOpenNMSImpl().updateNodeCaches: Problem listing nodeCacheByLabel contents. Error:",e);
				}
				try {
					log.debug("\tOssDaoOpenNMSImpl().updateNodeCaches - Updated nodeCacheByUniqueID : contents :");
					for (String uniqueID : nodeCacheByUniqueID.keySet()) {
						OnmsNode node =(OnmsNode)nodeCacheByUniqueID.get(uniqueID);
						log.debug("\t\tKey: uniqueID:"+uniqueID+"\tNodeID:"+node.getId());
					}
				}catch ( Exception e){
					log.error("\tOssDaoOpenNMSImpl().updateNodeCaches: Problem listing nodeCacheByUniqueID contents Error:",e);
				}
			}
		} catch (Exception ex){
			log.error("\tOssDaoOpenNMSImpl().updateNodeCaches ERROR : ", ex);
		}
	}

}
