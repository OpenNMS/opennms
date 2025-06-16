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
