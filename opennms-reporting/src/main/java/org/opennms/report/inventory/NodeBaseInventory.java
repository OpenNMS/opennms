/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.report.inventory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.opennms.rancid.InventoryElement2;

/**
 * <p>NodeBaseInventory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NodeBaseInventory {

    String devicename;
    String groupname;
    String version;
    String status;
    Date creationdate;
    String swconfigurationurl;
    String configurationurl;

    List<InventoryElement2> ie;
    
    /**
     * <p>Constructor for NodeBaseInventory.</p>
     */
    public NodeBaseInventory(){
        ie = new ArrayList<>();
    }

    /**
     * <p>Getter for the field <code>devicename</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDevicename() {
        return devicename;
    }

    /**
     * <p>Setter for the field <code>devicename</code>.</p>
     *
     * @param devicename a {@link java.lang.String} object.
     */
    public void setDevicename(String devicename) {
        this.devicename = devicename;
    }

    /**
     * <p>Getter for the field <code>groupname</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGroupname() {
        return groupname;
    }

    /**
     * <p>Setter for the field <code>groupname</code>.</p>
     *
     * @param groupname a {@link java.lang.String} object.
     */
    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    /**
     * <p>Getter for the field <code>version</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVersion() {
        return version;
    }

    /**
     * <p>Setter for the field <code>version</code>.</p>
     *
     * @param version a {@link java.lang.String} object.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatus() {
        return status;
    }

    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a {@link java.lang.String} object.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * <p>Getter for the field <code>creationdate</code>.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getCreationdate() {
        return creationdate;
    }

    /**
     * <p>Setter for the field <code>creationdate</code>.</p>
     *
     * @param creationdate a {@link java.util.Date} object.
     */
    public void setCreationdate(Date creationdate) {
        this.creationdate = creationdate;
    }

    /**
     * <p>Getter for the field <code>swconfigurationurl</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSwconfigurationurl() {
        return swconfigurationurl;
    }

    /**
     * <p>Setter for the field <code>swconfigurationurl</code>.</p>
     *
     * @param swconfigurationurl a {@link java.lang.String} object.
     */
    public void setSwconfigurationurl(String swconfigurationurl) {
        this.swconfigurationurl = swconfigurationurl;
    }

    /**
     * <p>Getter for the field <code>configurationurl</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getConfigurationurl() {
        return configurationurl;
    }

    /**
     * <p>Setter for the field <code>configurationurl</code>.</p>
     *
     * @param configurationurl a {@link java.lang.String} object.
     */
    public void setConfigurationurl(String configurationurl) {
        this.configurationurl = configurationurl;
    }

    /**
     * <p>Getter for the field <code>ie</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<InventoryElement2> getIe() {
        return ie;
    }

    /**
     * <p>Setter for the field <code>ie</code>.</p>
     *
     * @param ie a {@link java.util.List} object.
     */
    public void setIe(List<InventoryElement2> ie) {
        this.ie = ie;
    }
    
    /**
     * <p>expand</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String expand () {
        
        Iterator<InventoryElement2> iter1 = ie.iterator();

        String tot="";
        while (iter1.hasNext()){
            InventoryElement2 tmp = iter1.next();
            tot = tot + "<" + tmp.expand()+ ">\n";
        }
        return tot;

    }

    
}

