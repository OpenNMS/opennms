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

import javax.sql.DataSource;

import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Wrapper class for OssDaoOpenNMSImpl which makes it onto a singleton which can be shared
 * between Qosd and QoSDrx when either one or or both applications are running. This is needed because Qosd and
 * QoSDrx have different local application contexts but need to share access to the OssDao which provides the
 * synchronized alarm cache for both applications.
 * Either Qosd or QoSDrx can initialise the class. The first call to getInstance() causes the class to be created.
 * All subsequent calls return the same instance.
 *
 * Note it is expected that the Spring application context has already set up the opennms DAO's before the first
 * call to getInstance(). This means that you must ensure that the application contexts for Qosd and QosDrx
 * set the same values for these DAO's otherwise there will be unpredictable results
 *
 * @author ranger
 * @version $Id: $
 */
public class OssDaoOpenNMSImplSingleton {
	private static AlarmDao alarmDao;
	private static AssetRecordDao assetRecordDao;
	private static DataSource dataSource;
	private static NodeDao nodeDao;
	@SuppressWarnings("unused")
    private static TransactionTemplate transTemplate;

	private static OssDao instance = null;

	// This will return a single instance of this call (creating one if none exist)
	/**
	 * <p>Getter for the field <code>instance</code>.</p>
	 *
	 * @return a {@link org.openoss.opennms.spring.dao.OssDao} object.
	 */
	public static OssDao getInstance() {
		if (instance == null) // test if an instance exists, if so then return it
		{
			// Create an instance in a synchronized block to avoid access by multiple threads
			// (avoids inefficiency of declaring method body synchronized)
			synchronized(OssDaoOpenNMSImplSingleton.class)
			{
				// check once again to ensure first check didn't let two threads through at the same time
				if (instance == null)
					instance = new OssDaoOpenNMSImpl(); // create the new instance
				instance.setAlarmDao(alarmDao);
				instance.setAssetRecordDao(assetRecordDao);
				instance.setDataSource(dataSource);
				instance.setNodeDao(nodeDao);
				// Seth 2010-05-04: This field was being set on the singleton factory,
				// but not the constructed instances; keeping it that way for now
				// instance.setTransTemplate(transTemplate);
			}
		}
		return instance; // return the single OssDaoOpenNMSImpl object
	}

	/**
	 * <p>Setter for the field <code>alarmDao</code>.</p>
	 *
	 * @param alarmDao a {@link org.opennms.netmgt.dao.api.AlarmDao} object.
	 */
	public void setAlarmDao(AlarmDao alarmDao) {
		OssDaoOpenNMSImplSingleton.alarmDao = alarmDao;
	}

	/**
	 * <p>Setter for the field <code>assetRecordDao</code>.</p>
	 *
	 * @param assetRecordDao a {@link org.opennms.netmgt.dao.api.AssetRecordDao} object.
	 */
	public void setAssetRecordDao(AssetRecordDao assetRecordDao) {
		OssDaoOpenNMSImplSingleton.assetRecordDao = assetRecordDao;
	}

	/**
	 * <p>Setter for the field <code>dataSource</code>.</p>
	 *
	 * @param dataSource a {@link javax.sql.DataSource} object.
	 */
	public void setDataSource(DataSource dataSource) {
		OssDaoOpenNMSImplSingleton.dataSource = dataSource;
	}

	/**
	 * <p>Setter for the field <code>nodeDao</code>.</p>
	 *
	 * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
	 */
	public void setNodeDao(NodeDao nodeDao) {
		OssDaoOpenNMSImplSingleton.nodeDao = nodeDao;
	}
	
	/**
	 * <p>Setter for the field <code>transTemplate</code>.</p>
	 *
	 * @param transTemplate a {@link org.springframework.transaction.support.TransactionTemplate} object.
	 */
	public void setTransTemplate(TransactionTemplate transTemplate) {
		OssDaoOpenNMSImplSingleton.transTemplate = transTemplate;
	}
}
