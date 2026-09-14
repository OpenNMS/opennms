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
package org.opennms.netmgt.config.httpdatacollection;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * One HTTP request header sent on the data collection request. The
 * {@code value} field is interpolated through the metadata DSL the
 * same way every other field on {@link Url} is, so
 * {@code value="Bearer ${token:my-auth}"} resolves into the current
 * dynamic-auth token at request-build time.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "header")
@ValidateUsing("http-datacollection-config.xsd")
public class Header implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "name", required = true)
    protected String m_name;
    @XmlAttribute(name = "value", required = true)
    protected String m_value;

    public Header() {}

    public Header(final String name, final String value) {
        setName(name);
        setValue(value);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String value) {
        m_name = ConfigUtils.assertNotEmpty(value, "name");
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        m_value = ConfigUtils.assertNotEmpty(value, "value");
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Header)) {
            return false;
        }
        final Header that = (Header) other;
        return Objects.equals(this.m_name, that.m_name)
                && Objects.equals(this.m_value, that.m_value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_value);
    }
}
