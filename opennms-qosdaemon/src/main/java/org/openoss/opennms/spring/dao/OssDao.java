package org.openoss.opennms.spring.dao;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.openoss.opennms.spring.qosd.QoSD;
import org.springframework.transaction.support.TransactionTemplate;

public interface OssDao {

	/**
	 * @param dataSource The dataSource to set
	 */
	void setDataSource(DataSource dataSource);

	/**
	 * Used by Spring Application context to pass in {@link AssetRecordDao}.
	 * @param ar 
	 */
	void setAssetRecordDao(AssetRecordDao ar);

	/**
	 * Used by Spring Application context to pass in {@link NodeDao}.
	 * @param nodedao 
	 */
	void setNodeDao(NodeDao nodedao);

	/**
	 * Used by Spring Application context to pass in {@link AlarmDao}.
	 * @param alarmDao
	 */
	void setAlarmDao(AlarmDao alarmDao);

	/**
	 * Used by Spring Application context to pass in a Spring transaction manager
	 * @param transTemplate
	 */
	void setTransTemplate(TransactionTemplate _transTemplate);

	/**
	 * Used by running QoSD to set up OssDao to call back alarm list updates
	 * @param alarmDao
	 */
	void setQoSD(QoSD _qoSD);

	/**
	 * Initialises the Node and Alarm caches
	 * Must be called before any other methods to ensure that ossDao is initialised
	 */
	void init();

	/**
	 * Adds Current alarm to OpenNMS database with a new alarmID as an AlarmType= 'raise' ( type 1 ) alarm.
	 * Adds the alarm to the local Current Alarm Alarm list alarmCacheByID with the new alarmID only if
	 * the alarm is NOT (Acknowledged AND Cleared).  
	 * @param alarm - alarm to add. 
	 * 
	 * @return added alarm with new alarmID
	 * 
	 * @throws If alarm AlarmID not null throws <code>IllegalArgumentException</code>.
	 * If ApplicationDN() and OssPrimaryKey() not unique in Current Alarm list throws <code>IllegalArgumentException</code>
	 * If alarm type not type 1  throws <code>IllegalArgumentException</code>.
	 * If ApplicationDN()==null or "" or OssPrimaryKey()==null or "", throws <code>IllegalArgumentException</code>.
	 * Note any new locally generated OpenNMS alarms will have ApplictionDN or OssPrimaryKey ==null or "" and so are ignored
	 */

	OnmsAlarm addCurrentAlarmForUniqueKey(final OnmsAlarm alarm);

	/**
	 * Updates Current alarm in OpenNMS database with a new alarmID as an AlarmType= 'raise' ( type 1 ) alarm.
	 * Adds the alarm to the local Current Alarm Alarm list alarmCacheByID with the new alarmID only if
	 * the alarm is NOT (Acknowledged AND Cleared).  
	 * @param alarm - alarm to add. 
	 * 
	 * @return added alarm with new alarmID from OpenNMS Database
	 * 
	 * @throws If alarm AlarmID not null throws <code>IllegalArgumentException</code>.
	 * If alarm type not type 1  throws <code>IllegalArgumentException</code>.
	 * If ApplicationDN()==null or "" or OssPrimaryKey()==null or "", throws <code>IllegalArgumentException</code>.
	 * 
	 * Note any new locally generated OpenNMS alarms will have ApplictionDN or OssPrimaryKey ==null or "" and so are ignored
	 */
	OnmsAlarm updateCurrentAlarmForUniqueKey(final OnmsAlarm alarm);

	/**
	 * @return the first found alarm from current alarm list with matching parameters. 
	 * Returns Null if no such alarm.
	 * @param applicationDN
	 * @param OssPrimaryKey
	 */
	OnmsAlarm getCurrentAlarmForUniqueKey(String applicationDN,
			String ossPrimaryKey);

	/**
	 * Used to force an update to the local cache from latest alarm list in database
	 */
	void updateAlarmCache() throws IllegalStateException;

	/**
	 * Used By QoSD to force an update to the local cache from latest alarm list in database
	 * Tries to call back to QoSD to send the latest alarm list.
	 * The reason for this is to enforce synchronisation between QoSD and QoSDrx so that the 
	 * current alarm list is always sent by QoSD 
	 * If QoSD not running. Logs a debug message and returns
	 */
	void updateAlarmCacheAndSendAlarms() throws IllegalStateException;

	/**
	 * Used By QoSD to retreive a copy of the current view of the alarm cache.
	 * Note NOT Synchronized - but OK if called by QoSD through QoSD.sendAlarms()
	 */
	OnmsAlarm[] getAlarmCache();

	/** 
	 * This will return the first node in nodes table with nodeLable entry matching label
	 * Note for this to work, the configuration of OpenNMS must ensure that the node label is unique 
	 * otherwise only the first instance will be returned
	 * @param label NodeLabel of node to look for
	 * @return will look for first match of node label. <code>null</code> if not found
	 * Note: Accesses the Node Cache
	 */
	OnmsNode findNodeByLabel(String label);

	/**
	 * This will return the first node with entry in Assets table having matching managedObjectInstance and
	 * managedObjectType. 
	 * Note for this to work, the configuration of OpenNMS must ensure that the concatenation of 
	 * these fields is unique in the system otherwise only the first instance will be returned
	 * @param managedObjectInstance
	 * @param managedObjectType
	 * @return
	 * @throws IllegalArgumentException
	 * Note: Accesses the Node Cache
	 */
	OnmsNode findNodeByInstanceAndType(String managedObjectInstance,
			String managedObjectType) throws IllegalArgumentException;

	/**
	 * Returns the OnmsNode for the supplied node id
	 * @param nodeid
	 * @return
	 * Note: Accesses the Node Cache
	 */
	OnmsNode findNodeByID(Integer nodeid);

	/**
	 * Synchronized method to Update the node cache from the OpenNMS database
	 * May be called from Qosd on receipt of an asset register update event
	 */
	void updateNodeCaches();

}