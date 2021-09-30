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

import java.util.ArrayList;
import java.util.List;

/**
 * A servlet that stores interface information
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class ManagedInterface {
    /**
     * 
     */
    protected String address;

    /**
     * 
     */
    protected List<ManagedService> services;

    /**
     * 
     */
    protected String status;

    /**
     * 
     */
    protected int nodeid;

    /**
     * <p>Constructor for ManagedInterface.</p>
     */
    public ManagedInterface() {
        services = new ArrayList<>();
    }

    /**
     * <p>addService</p>
     *
     * @param newService a {@link org.opennms.web.admin.nodeManagement.ManagedService} object.
     */
    public void addService(ManagedService newService) {
        services.add(newService);
    }

    /**
     * <p>Setter for the field <code>address</code>.</p>
     *
     * @param newAddress a {@link java.lang.String} object.
     */
    public void setAddress(String newAddress) {
        address = newAddress;
    }

    /**
     * <p>Getter for the field <code>address</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAddress() {
        return address;
    }

    /**
     * <p>Getter for the field <code>services</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<ManagedService> getServices() {
        return services;
    }

    /**
     * <p>getServiceCount</p>
     *
     * @return a int.
     */
    public int getServiceCount() {
        return services.size();
    }

    /**
     * <p>Setter for the field <code>nodeid</code>.</p>
     *
     * @param id a int.
     */
    public void setNodeid(int id) {
        nodeid = id;
    }

    /**
     * <p>Getter for the field <code>nodeid</code>.</p>
     *
     * @return a int.
     */
    public int getNodeid() {
        return nodeid;
    }

    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param newStatus a {@link java.lang.String} object.
     */
    public void setStatus(String newStatus) {
        if (newStatus.equals("M")) {
            status = "managed";
        } else if (newStatus.equals("A")) {
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
