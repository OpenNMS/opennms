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
