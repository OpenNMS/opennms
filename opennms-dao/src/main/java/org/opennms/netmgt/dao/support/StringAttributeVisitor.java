/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.dao.support;

import org.opennms.netmgt.config.collector.AttributeGroup;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;

/**
 * StringAttributeVisitor
 * 
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class StringAttributeVisitor implements CollectionSetVisitor {

    private String attributeName;
    private String attributeValue;

    public StringAttributeVisitor(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getValue() {
        return attributeValue;
    }

    public void visitAttribute(CollectionAttribute attribute) {
        if (attribute.getType().toLowerCase().startsWith("string") && attributeName.equals(attribute.getName()))
            attributeValue = attribute.getStringValue();
    }

    public void visitCollectionSet(CollectionSet set) {}
    public void visitResource(CollectionResource resource) {}
    public void visitGroup(AttributeGroup group) {}
    public void completeAttribute(CollectionAttribute attribute) {}
    public void completeGroup(AttributeGroup group) {}
    public void completeResource(CollectionResource resource) {}
    public void completeCollectionSet(CollectionSet set) {}
}
