/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
