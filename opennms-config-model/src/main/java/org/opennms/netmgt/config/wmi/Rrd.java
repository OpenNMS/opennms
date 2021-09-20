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
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="rra" maxOccurs="unbounded"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;pattern value="RRA:(AVERAGE|MIN|MAX|LAST):.*"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="step" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "", propOrder = {
        "m_rras"
})
@XmlRootElement(name = "rrd")
@ValidateUsing("wmi-datacollection.xsd")
public class Rrd implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name="rra", required = true)
    protected List<String> m_rras = new ArrayList<>();

    @XmlAttribute(name = "step", required = true)
    protected Integer m_step;

    public Rrd() {}

    public Rrd(final Integer step, final String... rras) {
        setStep(step);
        for (final String rra : rras) {
            addRra(rra);
        }
    }

    public List<String> getRra() {
        return m_rras;
    }

    public void setRra(final List<String> rras) {
        if (rras == m_rras) return;
        m_rras.clear();
        if (rras != null) {
            for (final String rra : rras) {
                addRra(rra);
            }
        }
    }

    public void addRra(final String rra) {
        final Pattern pattern = Pattern.compile("^RRA:(AVERAGE|MIN|MAX|LAST):.*$");
        m_rras.add(ConfigUtils.assertMatches(ConfigUtils.assertNotNull(rra, "rra"), pattern, "rra"));
    }

    public boolean removeRra(final String rra) {
        return m_rras.remove(rra);
    }

    public Integer getStep() {
        return m_step;
    }

    public void setStep(final Integer step) {
        m_step = ConfigUtils.assertMinimumInclusive(ConfigUtils.assertNotNull(step, "step"), 1, "step");
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Rrd)) {
            return false;
        }
        final Rrd that = (Rrd) obj;
        return Objects.equals(this.m_rras, that.m_rras) &&
                Objects.equals(this.m_step, that.m_step);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_rras, m_step);
    }

}
