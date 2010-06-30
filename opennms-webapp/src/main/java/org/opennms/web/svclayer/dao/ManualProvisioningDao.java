/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: November 3, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.dao;

import java.util.Collection;

import org.opennms.netmgt.config.modelimport.ModelImport;

/**
 * <p>ManualProvisioningDao interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.6.12
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
     * @return a {@link org.opennms.netmgt.config.modelimport.ModelImport} object.
     */
    ModelImport get(String name);

    /**
     * <p>save</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param group a {@link org.opennms.netmgt.config.modelimport.ModelImport} object.
     */
    void save(String groupName, ModelImport group);

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
