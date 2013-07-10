/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The Class Content.
 * 
 * <p>Post a Form:</p>
 * <pre>
 *   &lt;content type='application/x-www-form-urlencoded'&gt;&lt;![CDATA[
 *     &lt;form-fields&gt;
 *       &lt;form-field name='firstName'&gt;Alejandro&lt;/form-field&gt;
 *       &lt;form-field name='lastName'&gt;Galue&lt;/form-field&gt;
 *     &lt;/form-fields&gt;
 *   ]]&gt;&lt;/content&gt;
 * </pre>
 * 
 * <p>Post a JSON Object:</p>
 * <pre>
 *   &lt;content type='application/json'&gt;&lt;![CDATA[
 *     {
 *       person: {
 *         firstName: 'Alejandro',
 *         lastName: 'Galue'
 *       }
 *     }
 *   ]]&gt;&lt;/content&gt;
 * </pre>
 * 
 * <p>Post a XML:</p>
 * <pre>
 *   &lt;content type='application/xml'&gt;&lt;![CDATA[
 *     &lt;person&gt;
 *       &lt;firstName&gt;Alejandro&lt;/firstName&gt;
 *       &lt;lastName&gt;Galue&lt;/lastName&gt;
 *     &lt;/person&gt;
 *   ]]&gt;&lt;/content&gt;
 * </pre>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="content")
@XmlAccessorType(XmlAccessType.FIELD)
public class Content {

    /** The type. */
    @XmlAttribute(required=true)
    private String type;

    /** The data.
     *  <p>In order to put any arbitrary XML content, CDATA is required.</p>
     */
    @XmlValue
    private String data;

    /**
     * Instantiates a new content.
     */
    public Content() {}

    /**
     * Instantiates a new content.
     *
     * @param type the type
     * @param data the data
     */
    public Content(String type, String data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * Sets the data.
     *
     * @param data the new data
     */
    public void setData(String data) {
        this.data = data;
    }
}