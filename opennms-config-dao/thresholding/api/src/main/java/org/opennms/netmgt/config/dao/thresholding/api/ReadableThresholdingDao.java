/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.dao.thresholding.api;

import java.util.Collection;

import org.opennms.netmgt.config.dao.common.api.ReadableDao;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;

/**
 * This class is the main repository for thresholding configuration information
 * used by the thresholding daemon.. When this class is loaded it reads the
 * thresholding configuration into memory.
 */
public interface ReadableThresholdingDao extends ReadableDao<ThresholdingConfig> {
    /**
     * Retrieves the configured path to the RRD file repository for the
     * specified thresholding group.
     *
     * @param groupName
     *            Group name to lookup
     * @return RRD repository path.
     * @throws java.lang.IllegalArgumentException
     *             if group name does not exist in the group map.
     */
    String getRrdRepository(String groupName);

    Group getGroup(String groupName);

    /**
     * Retrieves a Collection object consisting of all the
     * org.opennms.netmgt.config.Threshold objects which make up the specified
     * thresholding group.
     *
     * @param groupName
     *            Group name to lookup
     * @return Collection consisting of all the Threshold objects for the
     *         specified group..
     * @throws java.lang.IllegalArgumentException
     *             if group name does not exist in the group map.
     */
    Collection<Basethresholddef> getThresholds(String groupName);

    Collection<String> getGroupNames();
}
