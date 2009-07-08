/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.svclayer.support;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ChooseResourceModel {
    private OnmsResource m_resource;
    private Map<OnmsResourceType, List<OnmsResource>> m_resourceTypes;
    private String m_endUrl;
    
    public void setResource(OnmsResource resource) {
        m_resource = resource;
    }
    
    public OnmsResource getResource() {
        return m_resource;
    }
    
    public Map<OnmsResourceType, List<OnmsResource>> getResourceTypes() {
        return m_resourceTypes;
    }

    public void setResourceTypes(Map<OnmsResourceType, List<OnmsResource>> resourceTypes) {
        m_resourceTypes = resourceTypes;
    }

    public String getEndUrl() {
        return m_endUrl;
    }

    public void setEndUrl(String endUrl) {
        m_endUrl = endUrl;
    }

}
