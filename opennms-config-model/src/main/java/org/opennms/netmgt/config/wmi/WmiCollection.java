/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.wmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/wmi-datacollection}rrd"/&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/wmi-datacollection}wpms"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "", propOrder = {
        "m_rrd",
        "m_wpms"
})
@XmlRootElement(name = "wmi-collection")
@ValidateUsing("wmi-datacollection.xsd")
public class WmiCollection implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name="rrd", required = true)
    protected Rrd m_rrd;

    @XmlElementWrapper(name="wpms", required = true)
    @XmlElement(name="wpm", required = true)
    protected List<Wpm> m_wpms = new ArrayList<>();

    @XmlAttribute(name = "name", required = true)
    protected String m_name;

    public Rrd getRrd() {
        return m_rrd;
    }

    public void setRrd(final Rrd rrd) {
        m_rrd = ConfigUtils.assertNotNull(rrd, "rrd");
    }

    public List<Wpm> getWpms() {
        return m_wpms;
    }

    public void setWpms(final List<Wpm> wpms) {
        m_wpms = ConfigUtils.assertMinimumSize(ConfigUtils.assertNotNull(wpms, "wpms"), 1, "wpms");
    }

    public void addWpm(final Wpm wpm) {
        m_wpms.add(wpm);
    }

    public boolean removeWpm(final Wpm wpm) {
        return m_wpms.remove(wpm);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof WmiCollection)) {
            return false;
        }
        final WmiCollection that = (WmiCollection) obj;
        return Objects.equals(this.m_rrd, that.m_rrd) &&
                Objects.equals(this.m_wpms, that.m_wpms) &&
                Objects.equals(this.m_name, that.m_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_rrd, m_wpms, m_name);
    }

}
