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
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.config.categories.CategoryGroup;
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
		}

		HashMap<String, RTCCategory> retval = new HashMap<String, RTCCategory>();

		cFactory.getReadLock().lock();
		try {
			for (CategoryGroup cg : cFactory.getConfig().getCategoryGroups()) {
				final String commonRule = cg.getCommon().getRule();

				for (final org.opennms.netmgt.config.categories.Category cat : cg.getCategories()) {
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
			// Use a TreeSet to keep the node ids sorted
			Set<Integer> nodeIds = new TreeSet<Integer>(filterDao.getNodeMap(filterRule).keySet());
			LOG.debug("Number of nodes satisfying rule: {}", nodeIds.size());
			return nodeIds;
		} catch (FilterParseException e) {
			LOG.error("Unable to parse filter rule {} ignoring category {}", filterRule, cat.getLabel(), e);
			return Collections.emptySet();
		}
	}
}
