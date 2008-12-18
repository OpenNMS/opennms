/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 *
 */

package org.opennms.netmgt.provision.persist;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="plugin")
public class PluginConfig {
    @XmlAttribute(name="name")
    private String m_name;

    @XmlAttribute(name="class")
    private String m_pluginClass;

    @XmlJavaTypeAdapter(ParameterMapAdaptor.class)
    @XmlElement(name="parameters")
    private Map<String,String> m_parameters = new HashMap<String,String>();
    
    public PluginConfig() {
    }
    
    public PluginConfig(String name, String clazz) {
        setName(name);
        setPluginClass(clazz);
    }

    /**
     * @return the name
     */
    public String getName() {
        return m_name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        m_name = name;
    }
    /**
     * @return the pluginClass
     */
    public String getPluginClass() {
        return m_pluginClass;
    }
    /**
     * @param pluginClass the plugin class to set
     */
    public void setPluginClass(String pluginClass) {
        m_pluginClass = pluginClass;
    }
    /**
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return m_parameters;
    }
    
    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Map<String, String> parameters) {
        m_parameters = parameters;
    }
    public void addParameter(String key, String value) {
        m_parameters.put(key, value);
    }
}
