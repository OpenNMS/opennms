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
package org.opennms.netmgt.config.threshd;


import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Threshold definition
 */
@XmlRootElement(name = "threshold")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("thresholding.xsd")
public class Threshold extends Basethresholddef implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * RRD datasource name. Mutually exclusive with expression,
     *  but one of them must be specified
     */
    @XmlAttribute(name = "ds-name", required = true)
    private String m_dsName;

    public Threshold() { }

    public String getDsName() {
        return m_dsName;
    }

    public void setDsName(final String dsName) {
        m_dsName = ConfigUtils.assertNotEmpty(dsName, "ds-name");
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(m_dsName);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (super.equals(obj)==false) {
            return false;
        }

        if (obj instanceof Threshold) {
            final Threshold that = (Threshold)obj;
            return Objects.equals(this.m_dsName, that.m_dsName);
        }
        return false;
    }

}
