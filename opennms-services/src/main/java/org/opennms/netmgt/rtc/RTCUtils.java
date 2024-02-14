/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
