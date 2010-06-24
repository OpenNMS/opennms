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
// Modifications:
//
// 2007 Apr 23: Add non-null tests to constructor. - dj@opennms.org
// 2007 Apr 10: Add an optional OnmsEntity object and a String link.
//              - dj@opennms.org
// 2007 Apr 05: Deduplicate constructors, set the resource reference for
//              attributes, and add get*Attributes methods for different
//              attribute types. - dj@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;

public class OnmsResource implements Comparable<OnmsResource> {

    private String m_name;
    private Set<OnmsAttribute> m_attributes;
    private String m_label;
    private String m_link;
    private OnmsResourceType m_resourceType;
    private OnmsEntity m_entity;
    private List<OnmsResource> m_resources;
    private OnmsResource m_parent = null;
    
    public OnmsResource(String name, String label,
            OnmsResourceType resourceType, Set<OnmsAttribute> attributes) {
        this(name, label, resourceType, attributes, new ArrayList<OnmsResource>(0));
    }
    
    public OnmsResource(String name, String label,
            OnmsResourceType resourceType, Set<OnmsAttribute> attributes,
            List<OnmsResource> resources) {
        Assert.notNull(name, "name argument must not be null");
        Assert.notNull(label, "label argument must not be null");
        Assert.notNull(resourceType, "resourceType argument must not be null");
        Assert.notNull(attributes, "attributes argument must not be null");
        Assert.notNull(resources, "resources argument must not be null");
        
        m_name = name;
        m_label = label;
        m_resourceType = resourceType;
        m_attributes = attributes;
        m_resources = resources;
        
        for (OnmsAttribute attribute : m_attributes) {
            attribute.setResource(this);
        }
    }

    public String getName() {
        return m_name;
    }

    public String getLabel() {
        return m_label;
    }

    public OnmsResourceType getResourceType() {
        return m_resourceType;
    }

    public Set<OnmsAttribute> getAttributes() {
        return m_attributes;
    }
    
    public List<OnmsResource> getChildResources() {
        return m_resources;
    }

    public int compareTo(OnmsResource o) {
        return getLabel().compareTo(o.getLabel());
    }
    
    /**
     * Sorts the List of Resources and returns a new List of the
     * generic type Resource.
     * 
     * @param resources list of Resource objects.  This will be
     *          sorted using Collections.sort, and note that this will modify
     *          the provided list.
     * @return a sorted list
     */
    public static List<OnmsResource> sortIntoResourceList(List<OnmsResource> resources) {
        Collections.sort(resources);
        
        ArrayList<OnmsResource> outputResources =
            new ArrayList<OnmsResource>(resources.size());
        for (OnmsResource resource : resources) {
            outputResources.add(resource);
        }

        return outputResources;
    }

    public void setParent(OnmsResource parent) {
        m_parent = parent;
    }
    
    public OnmsResource getParent() {
        return m_parent;
    }
    
    public String getId() {
        String thisId = encode(m_resourceType.getName()) + "[" + encode(m_name) + "]";
        if (getParent() != null) {
            return getParent().getId() + "." + thisId;
        } else {
            return thisId;
        }
    }

    public String getLink() {
        if (m_link != null) {
            return m_link;
        } else {
            return m_resourceType.getLinkForResource(this);
        }
    }

    public void setLink(String link) {
        m_link = link;
    }

    
    public static String createResourceId(String... resources) {
        if ((resources.length % 2) != 0) {
            throw new IllegalArgumentException("Values passed as resources parameter must be in pairs");
        }
        
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < (resources.length / 2); i++) {
            if (buf.length() > 0) {
                buf.append(".");
            }
            
            buf.append(resources[i * 2]);
            buf.append("[");
            buf.append(encode(resources[(i * 2) + 1]));
            buf.append("]");
        }
        return buf.toString();
    }

    /**
     * Get the RRD graph attributes for this resource, if any.
     */
    public Map<String, RrdGraphAttribute> getRrdGraphAttributes() {
        Map<String, RrdGraphAttribute> attributes = new HashMap<String, RrdGraphAttribute>();
        for (OnmsAttribute attribute : getAttributes()) {
            if (RrdGraphAttribute.class.isAssignableFrom(attribute.getClass())) {
                RrdGraphAttribute graphAttribute = (RrdGraphAttribute) attribute;
                attributes.put(graphAttribute.getName(), graphAttribute);
            }
        }
        
        return attributes;
    }

    /**
     * Get the string property attributes for this resource, if any.
     */
    public Map<String, String> getStringPropertyAttributes() {
        Map<String, String> properties = new HashMap<String, String>();
        for (OnmsAttribute attribute : getAttributes()) {
            if (StringPropertyAttribute.class.isAssignableFrom(attribute.getClass())) {
                StringPropertyAttribute stringAttribute = (StringPropertyAttribute) attribute;
                properties.put(stringAttribute.getName(), stringAttribute.getValue());
            }
        }
        
        return properties;
    }
    
    /**
     * Get the external value attributes for this resource, if any.
     */
    public Map<String, String> getExternalValueAttributes() {
        Map<String, String> properties = new HashMap<String, String>();
        for (OnmsAttribute attribute : getAttributes()) {
            if (ExternalValueAttribute.class.isAssignableFrom(attribute.getClass())) {
                ExternalValueAttribute externalValueAttribute = (ExternalValueAttribute) attribute;
                properties.put(externalValueAttribute.getName(), externalValueAttribute.getValue());
            }
        }
        
        return properties;
    }
    
    private static String encode(String string) {
        if (string == null) return null;
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 should *never* throw this
            throw new UndeclaredThrowableException(e);
        }
    }
    
    public String toString() {
        return getId();
    }

    public OnmsEntity getEntity() {
        return m_entity;
    }

    public void setEntity(OnmsEntity entity) {
        m_entity = entity;
    }

}
