//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//  
//For more information contact: 
// OpenNMS Licensing       <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.netmgt.config;

import java.util.*;

/*
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
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

    public List<String> getCompositeAttributeNames() {
        return compositeAttributes;
    }

    public void setCompositeAttributes(List<String> compAttr) {
        compositeAttributes = compAttr;
    }

    public BeanInfo() {
        operations = new ArrayList<Object>();
        attributes = new ArrayList<String>();
        compositeAttributes = new ArrayList<String>();
    }

    public void setAttributes(List<String> attr) {
        attributes = attr;
    }

    public List<String> getAttributeNames() {
        return attributes;
    }

    public void addOperations(Object attr) {
        operations.add(attr);
    }

    public ArrayList<Object> getOperations() {
        return operations;
    }

    /**
     * @return Returns the mbeanName.
     */
    public String getMbeanName() {
        return mbeanName;
    }

    /**
     * @param mbeanName
     *            The mbeanName to set.
     */
    public void setMbeanName(String mbeanName) {
        this.mbeanName = mbeanName;
    }

    /**
     * @return Returns the objectName.
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * @param objectName
     *            The objectName to set.
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * @return Returns the excludes.
     */
    public String getExcludes() {
        return excludes;
    }

    /**
     * @param excludes
     *            The excludes to set.
     */
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    /**
     * @return Returns the keyField.
     */
    public String getKeyField() {
        return keyField;
    }

    /**
     * @param keyField
     *            The keyField to set.
     */
    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }

    /**
     * @return Returns the substitutions.
     */
    public String getKeyAlias() {
        return keyAlias;
    }
    
    /**
     * @param substitutions The substitutions to set.
     */
    public void setKeyAlias(String substitutions) {
        this.keyAlias = substitutions;
    }
}