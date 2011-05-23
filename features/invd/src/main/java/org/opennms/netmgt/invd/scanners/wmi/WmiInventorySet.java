//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
package org.opennms.netmgt.invd.scanners.wmi;

import org.opennms.netmgt.config.invd.wmi.WmiAsset;
import org.opennms.netmgt.invd.InventorySet;
import org.opennms.netmgt.invd.InventoryResource;
import org.opennms.netmgt.model.inventory.OnmsInventoryAsset;
import org.opennms.netmgt.model.inventory.OnmsInventoryCategory;
import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;
import org.opennms.protocols.wmi.WmiException;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;

public class WmiInventorySet implements InventorySet {
    private int m_status;
    private List<InventoryResource> m_inventoryResources = new ArrayList<InventoryResource>();

    public int getStatus() {
        return m_status;
    }

    public void setStatus(int status) {
        m_status = status;
    }

    public List<InventoryResource> getInventoryResources() {
        return m_inventoryResources;
    }
    
    public void setInventoryResources(List<InventoryResource> resources) {
    	this.m_inventoryResources  = resources;
    }

    public void collect(WmiAsset asset, OnmsWbemObject wmiObject) {

    }
}
