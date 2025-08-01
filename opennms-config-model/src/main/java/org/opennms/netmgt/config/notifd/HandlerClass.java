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
package org.opennms.netmgt.config.notifd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "handler-class")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("notifd-configuration.xsd")
public class HandlerClass implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "name", required = true)
    private String m_name;

    @XmlElement(name = "init-params")
    private List<InitParams> m_initParams = new ArrayList<>();

    public HandlerClass() { }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public List<InitParams> getInitParams() {
        return m_initParams;
    }

    public void setInitParams(final List<InitParams> params) {
        if (params == m_initParams) return;
        m_initParams.clear();
        if (params != null) m_initParams.addAll(params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_initParams);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof HandlerClass) {
            final HandlerClass that = (HandlerClass)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_initParams, that.m_initParams);
        }
        return false;
    }

}
