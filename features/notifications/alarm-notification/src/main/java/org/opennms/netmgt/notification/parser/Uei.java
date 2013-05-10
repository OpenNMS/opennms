/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 */

package org.opennms.netmgt.notification.parser;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "uei")
public class Uei {
	@XmlElement(name = "filter", required = false)
	protected List<Filter> m_filter;

	@XmlAttribute(name = "name", required = true)
	protected String m_name;

	@XmlAttribute(name = "notification_threshold",required=false)
    protected String m_notificationThreshold;
	
	/**
	 * Gets the value of the filter property. Objects of the following type(s)
	 * are allowed in the list {@link Filter }
	 */
	public List<Filter> getFilter() {
		if (m_filter == null) {
			m_filter = new ArrayList<Filter>();
		}
		return this.m_filter;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return return object is {@link String }
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 */
	public void setName(String value) {
		this.m_name = value;
	}

	@Override
	public boolean equals(Object obj) {
		Uei ueiOther = (Uei) obj;
		if (obj instanceof Uei)
			if ((this.getName().equals(ueiOther.getName()))
			// && (this.getFilter().equals(ueiOther.getFilter()))
			)
				return true;
		return false;
	}

	 /**
     * Gets the value of the notificationThreshold property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotificationThreshold() {
        if (m_notificationThreshold == null) {
            return "5";
        } else {
            return m_notificationThreshold;
        }
    }

    /**
     * Sets the value of the notificationThreshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotificationThreshold(String value) {
        this.m_notificationThreshold = value;
    }
    
	@Override
	public String toString() {
		String uei = "UEI Name " + this.getName() + " ,Notification Threshold " + this.getNotificationThreshold() + " ,Applied Filter"
				+ this.getFilter().toString();
		return uei;
	}
}
