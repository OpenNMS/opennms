/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.dao;

import java.util.Collection;

import org.opennms.netmgt.provision.persist.requisition.Requisition;

/**
 * <p>ManualProvisioningDao interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public interface ManualProvisioningDao {

    /**
     * <p>getProvisioningGroupNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<String> getProvisioningGroupNames();

    /**
     * <p>get</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    Requisition get(String name);

    /**
     * <p>save</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param group a {@link org.opennms.netmgt.provision.persist.requisition.Requisition} object.
     */
    void save(String groupName, Requisition group);

    /**
     * <p>getUrlForGroup</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getUrlForGroup(String groupName);

    /**
     * <p>delete</p>
     *
     * @param groupName a {@link java.lang.String} object.
     */
    void delete(String groupName);

}
