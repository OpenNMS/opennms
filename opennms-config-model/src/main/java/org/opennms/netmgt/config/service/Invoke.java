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
package org.opennms.netmgt.config.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "invoke")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("service-configuration.xsd")
public class Invoke implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "method")
    private String m_method;

    @XmlAttribute(name = "pass")
    private Integer m_pass = 0;

    @XmlAttribute(name = "at")
    private InvokeAtType m_at;

    @XmlElement(name = "argument")
    private List<Argument> m_arguments = new ArrayList<>();

    public Invoke() {
    }

    public Invoke(final InvokeAtType type, final Integer pass,
        final String method, final List<Argument> arguments) {
        setAt(type);
        setPass(pass);
        setMethod(method);
        setArguments(arguments);
    }

    public String getMethod() {
        return m_method;
    }

    public void setMethod(final String method) {
        m_method = ConfigUtils.assertNotEmpty(method, "method");
    }

    public int getPass() {
        return m_pass == null? 0 : m_pass;
    }

    public void setPass(final int pass) {
        m_pass = pass;
    }

    public InvokeAtType getAt() {
        return m_at;
    }

    public void setAt(final InvokeAtType at) {
        m_at = ConfigUtils.assertNotNull(at, "at");
    }

    public List<Argument> getArguments() {
        return m_arguments;
    }

    public void setArguments(final List<Argument> arguments) {
        if (arguments == m_arguments) return;
        m_arguments.clear();
        if (arguments != null) m_arguments.addAll(arguments);
    }

    public void addArgument(final Argument argument) {
        m_arguments.add(argument);
    }

    public boolean removeArgument(final Argument argument) {
        return m_arguments.remove(argument);
    }

    public int hashCode() {
        return Objects.hash(m_method, m_pass, m_at, m_arguments);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Invoke) {
            final Invoke that = (Invoke) obj;
            return Objects.equals(this.m_method, that.m_method) &&
                    Objects.equals(this.m_pass, that.m_pass) &&
                    Objects.equals(this.m_at, that.m_at) &&
                    Objects.equals(this.m_arguments, that.m_arguments);
        }
        return false;
    }
}
