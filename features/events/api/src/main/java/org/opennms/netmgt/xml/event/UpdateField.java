/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Object used to identify which alarm fields should be updated during Alarm reduction.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@XmlRootElement(name="update-field")
@XmlAccessorType(XmlAccessType.FIELD)
//@ValidateUsing("event.xsd")
public class UpdateField {
    
    @XmlAttribute(name="field-name", required=true)
    private java.lang.String m_fieldName;
    
    @XmlAttribute(name="update-on-reduction", required=false)
    private java.lang.Boolean m_updateOnReduction = Boolean.TRUE;
    
    public String getFieldName() {
        return m_fieldName;
    }

    public void setFieldName(String fieldName) {
        m_fieldName = fieldName;
    }
    
    public Boolean isUpdateOnReduction() {
        return m_updateOnReduction;
    }
    
    public void setUpdateOnReduction(Boolean update) {
        m_updateOnReduction = update;
    }
}
