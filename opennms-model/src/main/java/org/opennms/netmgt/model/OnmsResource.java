/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;

/**
 * <p>OnmsResource class.</p>
 */
public class OnmsResource implements Comparable<OnmsResource> {

    private final String m_name;
    private final Set<OnmsAttribute> m_attributes;
    private final OnmsResourceType m_resourceType;
    private final List<OnmsResource> m_resources;
    private final ResourcePath m_path;

    private String m_label;
    private String m_link;
    private OnmsEntity m_entity;
    private OnmsResource m_parent = null;
    private boolean m_attributesUpdatedWithResource = false;

    /**
     * <p>Constructor for OnmsResource.</p>
     *
     * @param name         a {@link java.lang.String} object.
     * @param label        a {@link java.lang.String} object.
     * @param resourceType a {@link org.opennms.netmgt.model.OnmsResourceType} object.
     * @param attributes   a {@link java.util.Set} object.
     */
    public OnmsResource(String name, String label,
                        OnmsResourceType resourceType, Set<OnmsAttribute> attributes, ResourcePath path) {
        this(name, label, resourceType, attributes, new ArrayList<OnmsResource>(0), path);
    }

    /**
     * <p>Constructor for OnmsResource.</p>
     *
     * @param name         a {@link java.lang.String} object.
     * @param label        a {@link java.lang.String} object.
     * @param resourceType a {@link org.opennms.netmgt.model.OnmsResourceType} object.
     * @param attributes   a {@link java.util.Set} object.
     * @param resources    a {@link java.util.List} object.
     */
    public OnmsResource(String name, String label,
                        OnmsResourceType resourceType, Set<OnmsAttribute> attributes,
                        List<OnmsResource> resources, ResourcePath path) {
        Assert.notNull(name, "name argument must not be null");
        Assert.notNull(label, "label argument must not be null");
        Assert.notNull(resourceType, "resourceType argument must not be null");
        Assert.notNull(attributes, "attributes argument must not be null");
        Assert.notNull(resources, "resources argument must not be null");
        Assert.notNull(path, "path argument must not be null");

        m_name = name;
        m_label = label;
        m_resourceType = resourceType;
        m_attributes = attributes;
        m_resources = resources;
        m_path = path;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * <p>setLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     */
    public void setLabel(String label) {
        m_label = label;
    }

    /**
     * <p>getResourceType</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsResourceType} object.
     */
    public OnmsResourceType getResourceType() {
        return m_resourceType;
    }

    /**
     * <p>getAttributes</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<OnmsAttribute> getAttributes() {
        // Only update the attribute with the resource on the first get
        // In some cases the attributes will be stored in a lazy set
        // so we don't want to preemptively load it
        if (!m_attributesUpdatedWithResource) {
            for (OnmsAttribute attribute : m_attributes) {
                attribute.setResource(this);
            }
            m_attributesUpdatedWithResource = true;
        }
        return m_attributes;
    }

    /**
     * <p>getChildResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> getChildResources() {
        return m_resources;
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @return a int.
     */
    @Override
    // FIXME This doesn't seem to be correct, for example Fa0_1 could exist on multiple devices, so it should include the nodeId or the parent resource
    public int compareTo(OnmsResource o) {
        return getLabel().compareTo(o.getLabel());
    }

    /**
     * Sorts the List of Resources and returns a new List of the
     * generic type Resource.
     *
     * @param resources list of Resource objects.  This will be
     *                  sorted using Collections.sort, and note that this will modify
     *                  the provided list.
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

    /**
     * <p>setParent</p>
     *
     * @param parent a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public void setParent(OnmsResource parent) {
        m_parent = parent;
    }

    /**
     * <p>getParent</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource getParent() {
        return m_parent;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public ResourceId getId() {
        if (this.getParent() != null) {
            return getParent().getId().resolve(getResourceType().getName(), getName());
        } else {
            return ResourceId.get(getResourceType().getName(), getName());
        }
    }

    /**
     * <p>getLink</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLink() {
        if (m_link != null) {
            return m_link;
        } else {
            return m_resourceType.getLinkForResource(this);
        }
    }

    /**
     * <p>setLink</p>
     *
     * @param link a {@link java.lang.String} object.
     */
    public void setLink(String link) {
        m_link = link;
    }

    /**
     * Get the RRD graph attributes for this resource, if any.
     *
     * @return a {@link java.util.Map} object.
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
     *
     * @return a {@link java.util.Map} object.
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
     *
     * @return a {@link java.util.Map} object.
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

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getId().toString();
    }

    /**
     * <p>getEntity</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsEntity} object.
     */
    public OnmsEntity getEntity() {
        return m_entity;
    }

    /**
     * <p>setEntity</p>
     *
     * @param entity a {@link org.opennms.netmgt.model.OnmsEntity} object.
     */
    public void setEntity(OnmsEntity entity) {
        m_entity = entity;
    }

    public ResourcePath getPath() {
        return m_path;
    }

}
