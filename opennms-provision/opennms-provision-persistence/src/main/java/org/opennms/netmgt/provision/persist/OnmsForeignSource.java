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

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
@XmlRootElement(name="foreign-source")
public class OnmsForeignSource {
    @XmlAttribute(name="name")
    private String m_name;
    
    @XmlElement(name="scan-interval")
    @XmlJavaTypeAdapter(StringIntervalAdapter.class)
    private Long m_scanInterval = Long.valueOf(60 * 60 * 24 * 1000); // 1 day

    @XmlElementWrapper(name="detectors")
    @XmlElement(name="detector")
    private List<PluginConfig> m_detectors = Collections.emptyList();
    
    @XmlElementWrapper(name="policies")
    @XmlElement(name="policy")
    private List<PluginConfig> m_policies = Collections.emptyList();

    public OnmsForeignSource() {
    }
    
    public OnmsForeignSource(String name) {
        setName(name);
    }
    
    /**
     * @return the name
     */
    @XmlTransient
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
     * @return the scanInterval
     */
    @XmlTransient
    public long getScanInterval() {
        return m_scanInterval;
    }
    /**
     * @param scanInterval the scanInterval to set
     */
    public void setScanInterval(long scanInterval) {
        m_scanInterval = scanInterval;
    }
    /**
     * @return the detectors
     */
    @XmlTransient
    public List<PluginConfig> getDetectors() {
        return m_detectors;
    }
    /**
     * @param detectors the detectors to set
     */
    public void setDetectors(List<PluginConfig> detectors) {
        m_detectors = detectors;
    }
    
    @XmlTransient
    public List<PluginConfig> getPolicies() {
        return m_policies;
    }
    
    public void setPolicies(List<PluginConfig> policies) {
        m_policies = policies;
    }
    
    public String toString() {
        if (m_name != null) {
            return m_name;
        } else {
            return super.toString();
        }
    }
}
