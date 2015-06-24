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

package org.opennms.web.admin.nodeManagement;

/**
 * <p>ManagedService class.</p>
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class ManagedService {
    /**
     * 
     */
    protected String name;

    /**
     * 
     */
    protected String status;

    /**
     * 
     */
    protected int serviceId;

    /**
     * <p>Constructor for ManagedService.</p>
     */
    public ManagedService() {
    }

    /**
     * <p>setId</p>
     *
     * @param id a int.
     */
    public void setId(int id) {
        serviceId = id;
    }

    /**
     * <p>getId</p>
     *
     * @return a int.
     */
    public int getId() {
        return serviceId;
    }

    /**
     * <p>Setter for the field <code>name</code>.</p>
     *
     * @param newName a {@link java.lang.String} object.
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * <p>Getter for the field <code>name</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return name;
    }

    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param newStatus a {@link java.lang.String} object.
     */
    public void setStatus(String newStatus) {
        if (newStatus.equals("A")) {
            status = "managed";
        } else if (newStatus.equals("R")) {
            status = "managed";
        } else {
            status = "unmanaged";
        }
    }

    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatus() {
        return status;
    }
}
