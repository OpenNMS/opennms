/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/*
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
/**
 * <p>BeanInfo class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class BeanInfo {

    /** The m_mbean name. */
    private String m_mbeanName;

    /** The m_object name. */
    private String m_objectName;

    /** The m_key field. */
    private String m_keyField;

    /** The m_excludes. */
    private String m_excludes;

    /** The m_key alias. */
    private String m_keyAlias;
    
    /** The m_resource type. */
    private String m_resourceType;
    
    /** The m_operations. */
    private ArrayList<Object> m_operations;
    
    /** The m_attributes. */
    private List<String> m_attributes;
    
    /** The m_composite attributes. */
    private List<String> m_compositeAttributes;

    /**
     * <p>getCompositeAttributeNames</p>.
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getCompositeAttributeNames() {
        return m_compositeAttributes;
    }

    /**
     * <p>Setter for the field <code>compositeAttributes</code>.</p>
     *
     * @param compAttr a {@link java.util.List} object.
     */
    public void setCompositeAttributes(List<String> compAttr) {
        m_compositeAttributes = compAttr;
    }

    /**
     * <p>Constructor for BeanInfo.</p>
     */
    public BeanInfo() {
        m_operations = new ArrayList<Object>();
        m_attributes = new ArrayList<String>();
        m_compositeAttributes = new ArrayList<String>();
    }

    /**
     * <p>Setter for the field <code>attributes</code>.</p>
     *
     * @param attr a {@link java.util.List} object.
     */
    public void setAttributes(List<String> attr) {
        m_attributes = attr;
    }

    /**
     * <p>getAttributeNames</p>.
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getAttributeNames() {
        return m_attributes;
    }

    /**
     * <p>addOperations</p>.
     *
     * @param attr a {@link java.lang.Object} object.
     */
    public void addOperations(Object attr) {
        m_operations.add(attr);
    }

    /**
     * <p>Getter for the field <code>operations</code>.</p>
     *
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<Object> getOperations() {
        return m_operations;
    }

    /**
     * <p>Getter for the field <code>mbeanName</code>.</p>
     *
     * @return Returns the mbeanName.
     */
    public String getMbeanName() {
        return m_mbeanName;
    }

    /**
     * <p>Setter for the field <code>mbeanName</code>.</p>
     *
     * @param mbeanName
     *            The mbeanName to set.
     */
    public void setMbeanName(String mbeanName) {
        m_mbeanName = mbeanName;
    }

    /**
     * <p>Getter for the field <code>objectName</code>.</p>
     *
     * @return Returns the objectName.
     */
    public String getObjectName() {
        return m_objectName;
    }

    /**
     * <p>Setter for the field <code>objectName</code>.</p>
     *
     * @param objectName
     *            The objectName to set.
     */
    public void setObjectName(String objectName) {
        m_objectName = objectName;
    }

    /**
     * <p>Getter for the field <code>excludes</code>.</p>
     *
     * @return Returns the excludes.
     */
    public String getExcludes() {
        return m_excludes;
    }

    /**
     * <p>Setter for the field <code>excludes</code>.</p>
     *
     * @param excludes
     *            The excludes to set.
     */
    public void setExcludes(String excludes) {
        m_excludes = excludes;
    }

    /**
     * <p>Getter for the field <code>keyField</code>.</p>
     *
     * @return Returns the keyField.
     */
    public String getKeyField() {
        return m_keyField;
    }

    /**
     * <p>Setter for the field <code>keyField</code>.</p>
     *
     * @param keyField
     *            The keyField to set.
     */
    public void setKeyField(String keyField) {
        m_keyField = keyField;
    }

    /**
     * <p>Getter for the field <code>keyAlias</code>.</p>
     *
     * @return Returns the substitutions.
     */
    public String getKeyAlias() {
        return m_keyAlias;
    }
    
    /**
     * <p>Setter for the field <code>keyAlias</code>.</p>
     *
     * @param substitutions The substitutions to set.
     */
    public void setKeyAlias(String substitutions) {
        m_keyAlias = substitutions;
    }

    /**
     * Gets the resource type.
     *
     * @return the resource type
     */
    public String getResourceType() {
        return m_resourceType;
    }

    /**
     * Sets the resource type.
     *
     * @param resourceType the new resource type
     */
    public void setResourceType(String resourceType) {
        m_resourceType = resourceType;
    }
}
