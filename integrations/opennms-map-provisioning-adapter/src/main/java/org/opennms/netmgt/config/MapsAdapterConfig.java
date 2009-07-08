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

package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;


import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.map.adapter.Celement;
import org.opennms.netmgt.config.map.adapter.Cmap;
import org.opennms.netmgt.config.map.adapter.Csubmap;

public interface MapsAdapterConfig {

    public int getMapElementDimension();
    public List<Cmap> getAllMaps();    
    public List<Csubmap> getSubMaps(String mapName);
    public Map<String,Csubmap> getContainerMaps(String submapName);
    public Map<String, List<Csubmap>> getsubMaps();
    public Map<String, Celement> getElementByAddress(String ipaddr);
    public void rebuildPackageIpListMap();
    public void update() throws IOException, MarshalException, ValidationException;
}
