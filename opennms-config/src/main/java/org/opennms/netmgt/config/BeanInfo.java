/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import java.util.*;

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
    private String mbeanName;

    private String objectName;

    private String keyField;

    private String excludes;
    
    private String keyAlias;
    
    private ArrayList<Object> operations;
    
    private List<String> attributes;
    
    private List<String> compositeAttributes;

    /**
     * <p>getCompositeAttributeNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getCompositeAttributeNames() {
        return compositeAttributes;
    }

    /**
     * <p>Setter for the field <code>compositeAttributes</code>.</p>
     *
     * @param compAttr a {@link java.util.List} object.
     */
    public void setCompositeAttributes(List<String> compAttr) {
        compositeAttributes = compAttr;
    }

    /**
     * <p>Constructor for BeanInfo.</p>
     */
    public BeanInfo() {
        operations = new ArrayList<Object>();
        attributes = new ArrayList<String>();
        compositeAttributes = new ArrayList<String>();
    }

    /**
     * <p>Setter for the field <code>attributes</code>.</p>
     *
     * @param attr a {@link java.util.List} object.
     */
    public void setAttributes(List<String> attr) {
        attributes = attr;
    }

    /**
     * <p>getAttributeNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getAttributeNames() {
        return attributes;
    }

    /**
     * <p>addOperations</p>
     *
     * @param attr a {@link java.lang.Object} object.
     */
    public void addOperations(Object attr) {
        operations.add(attr);
    }

    /**
     * <p>Getter for the field <code>operations</code>.</p>
     *
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<Object> getOperations() {
        return operations;
    }

    /**
     * <p>Getter for the field <code>mbeanName</code>.</p>
     *
     * @return Returns the mbeanName.
     */
    public String getMbeanName() {
        return mbeanName;
    }

    /**
     * <p>Setter for the field <code>mbeanName</code>.</p>
     *
     * @param mbeanName
     *            The mbeanName to set.
     */
    public void setMbeanName(String mbeanName) {
        this.mbeanName = mbeanName;
    }

    /**
     * <p>Getter for the field <code>objectName</code>.</p>
     *
     * @return Returns the objectName.
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * <p>Setter for the field <code>objectName</code>.</p>
     *
     * @param objectName
     *            The objectName to set.
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * <p>Getter for the field <code>excludes</code>.</p>
     *
     * @return Returns the excludes.
     */
    public String getExcludes() {
        return excludes;
    }

    /**
     * <p>Setter for the field <code>excludes</code>.</p>
     *
     * @param excludes
     *            The excludes to set.
     */
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    /**
     * <p>Getter for the field <code>keyField</code>.</p>
     *
     * @return Returns the keyField.
     */
    public String getKeyField() {
        return keyField;
    }

    /**
     * <p>Setter for the field <code>keyField</code>.</p>
     *
     * @param keyField
     *            The keyField to set.
     */
    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }

    /**
     * <p>Getter for the field <code>keyAlias</code>.</p>
     *
     * @return Returns the substitutions.
     */
    public String getKeyAlias() {
        return keyAlias;
    }
    
    /**
     * <p>Setter for the field <code>keyAlias</code>.</p>
     *
     * @param substitutions The substitutions to set.
     */
    public void setKeyAlias(String substitutions) {
        this.keyAlias = substitutions;
    }
}
