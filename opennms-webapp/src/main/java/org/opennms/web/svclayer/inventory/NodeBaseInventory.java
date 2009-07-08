/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.svclayer.inventory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.opennms.rancid.InventoryElement2;

public class NodeBaseInventory {

    String devicename;
    String groupname;
    String version;
    String status;
    Date creationdate;
    String swconfigurationurl;
    String configurationurl;

    List<InventoryElement2> ie;
    
    public NodeBaseInventory(){
        ie = new ArrayList<InventoryElement2>();
    }

    public String getDevicename() {
        return devicename;
    }

    public void setDevicename(String devicename) {
        this.devicename = devicename;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(Date creationdate) {
        this.creationdate = creationdate;
    }

    public String getSwconfigurationurl() {
        return swconfigurationurl;
    }

    public void setSwconfigurationurl(String swconfigurationurl) {
        this.swconfigurationurl = swconfigurationurl;
    }

    public String getConfigurationurl() {
        return configurationurl;
    }

    public void setConfigurationurl(String configurationurl) {
        this.configurationurl = configurationurl;
    }

    public List<InventoryElement2> getIe() {
        return ie;
    }

    public void setIe(List<InventoryElement2> ie) {
        this.ie = ie;
    }
    
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

