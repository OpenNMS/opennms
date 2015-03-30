/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rtc;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RTCUtils {

	private static final Logger LOG = LoggerFactory.getLogger(RTCUtils.class);

	/**
	 * Calculate the uptime percentage for an outage window.
	 * 
	 * @param totalOutageTime Total outage time over the window in milliseconds
	 * @param outageWindow Length of the outage window in milliseconds
	 * @param numberOfServices Number of services that were managed over this time period
	 * @return An outage percentage between 0.0 and 100.0.
	 */
	public static double getOutagePercentage(double totalOutageTime, long outageWindow, long numberOfServices) {
		if (numberOfServices > 0) {
			return 100.0 * (1.0 - (totalOutageTime / ((double)outageWindow * (double)numberOfServices)));
		} else {
			return 100.0;
		}
	}

	/**
	 * Creates the categories map. Reads the categories from the categories.xml
	 * and creates the 'RTCCategory's map
	 */
	public static HashMap<String, RTCCategory> createCategoriesMap() {
		CatFactory cFactory = null;
		try {
			CategoryFactory.init();
			cFactory = CategoryFactory.getInstance();

		} catch (IOException ex) {
			LOG.error("Failed to load categories information", ex);
			throw new UndeclaredThrowableException(ex);
		} catch (MarshalException ex) {
			LOG.error("Failed to load categories information", ex);
			throw new UndeclaredThrowableException(ex);
		} catch (ValidationException ex) {
			LOG.error("Failed to load categories information", ex);
			throw new UndeclaredThrowableException(ex);
		}

		HashMap<String, RTCCategory> retval = new HashMap<String, RTCCategory>();

		cFactory.getReadLock().lock();
		try {
			for (Categorygroup cg : cFactory.getConfig().getCategorygroupCollection()) {
				final String commonRule = cg.getCommon().getRule();

				for (final org.opennms.netmgt.config.categories.Category cat : cg.getCategories().getCategoryCollection()) {
					RTCCategory rtcCat = new RTCCategory(cat, commonRule);
					retval.put(rtcCat.getLabel(), rtcCat);
				}
			}
		} finally {
			cFactory.getReadLock().unlock();
		}

		return retval;
	}

	public static Set<Integer> getNodeIdsForCategory(FilterDao filterDao, RTCCategory cat) {
		String filterRule = cat.getEffectiveRule();
		try {
			LOG.debug("Category: {}\t{}", cat.getLabel(), filterRule);
			Set<Integer> nodeIds = filterDao.getNodeMap(filterRule).keySet();
			LOG.debug("Number of nodes satisfying rule: {}", nodeIds.size());
			return nodeIds;
		} catch (FilterParseException e) {
			LOG.error("Unable to parse filter rule {} ignoring category {}", filterRule, cat.getLabel(), e);
			return Collections.emptySet();
		}
	}

	/**
	 * TODO: Consolidate this function with similar {@link CategoryModel#getServiceAvailability(int, String, int, Date, Date)}
	 *
	 * @param nodeId a int.
	 * @param ipAddr a {@link java.lang.String} object.
	 * @param serviceId a int.
	 * @param start a {@link java.util.Date} object.
	 * @param end a {@link java.util.Date} object.
	 * @return a double.
	 * @throws java.sql.SQLException if any.
	 */
	public static double getOutageTimeInWindow(int nodeId, String ipAddr, int serviceId, Date start, Date end) throws SQLException {
		if (ipAddr == null || start == null || end == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		if (end.before(start)) {
			throw new IllegalArgumentException("Cannot have an end time before the start time.");
		}

		if (end.equals(start)) {
			throw new IllegalArgumentException("Cannot have an end time equal to the start time.");
		}

		final DBUtils d = new DBUtils(RTCUtils.class);
		try {
			Connection conn = DataSourceFactory.getInstance().getConnection();
			d.watch(conn);

			PreparedStatement stmt = conn.prepareStatement("select getOutageTimeInWindow(?, ?, ?, ?, ?) as avail from ifservices, ipinterface, node where ifservices.ipaddr = ipinterface.ipaddr and ifservices.nodeid = ipinterface.nodeid and ifservices.status='A' and ipinterface.ismanaged='M' and ifservices.nodeid = node.nodeid and node.nodetype='A' and ifservices.nodeid=? and ifservices.ipaddr=? and serviceid=?");
			d.watch(stmt);

			stmt.setInt(1, nodeId);
			stmt.setString(2, ipAddr);
			stmt.setInt(3, serviceId);
			// yes, these are supposed to be backwards, the end time first
			stmt.setTimestamp(4, new Timestamp(end.getTime()));
			stmt.setTimestamp(5, new Timestamp(start.getTime()));
			stmt.setInt(6, nodeId);
			stmt.setString(7, ipAddr);
			stmt.setInt(8, serviceId);

			ResultSet rs = stmt.executeQuery();
			d.watch(rs);

			if (rs.next()) {
				return rs.getDouble("avail");
			}
		} catch (final SQLException e) {
			LOG.warn("Failed to get service availability for nodeId {}, interface {}, serviceId {}", nodeId, ipAddr, serviceId, e);
		} finally {
			d.cleanUp();
		}

		return -1.0;
	}
}
