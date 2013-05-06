/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;

/**
 * <p>DataLinkInterfaceDao interface.</p>
 */
public interface DataLinkInterfaceDao extends OnmsDao<DataLinkInterface, Integer>{
    /**
     * <p>findAll</p>
     *
     * @param offset a {@link java.lang.Integer} object.
     * @param limit a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<DataLinkInterface> findAll(Integer offset, Integer limit);
    /**
     * <p>findById</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.DataLinkInterface} object.
     */
    DataLinkInterface findById(Integer id);
    /**
     * <p>findByNodeId</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<DataLinkInterface> findByNodeId(Integer nodeId);
    /**
     * <p>findByNodeParentId</p>
     *
     * @param nodeParentId a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<DataLinkInterface> findByNodeParentId(Integer nodeParentId);

    void markDeletedIfNodeDeleted();

    DataLinkInterface findByNodeIdAndIfIndex(Integer nodeId, Integer ifindex);

    void deactivateIfOlderThan(Date now, String source);

    void deleteIfOlderThan(Date now, String source);

    void setStatusForNode(Integer nodeid, StatusType action);

    void setStatusForNodeAndIfIndex(Integer nodeid, Integer ifIndex, StatusType action);
    
    void setStatusForNode(Integer nodeid, String source, StatusType action);

    void setStatusForNodeAndIfIndex(Integer nodeid, Integer ifIndex, String source, StatusType action);

}
