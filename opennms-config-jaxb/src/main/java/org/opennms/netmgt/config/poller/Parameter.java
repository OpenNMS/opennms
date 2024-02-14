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
package org.opennms.netmgt.config.poller;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Parameters to be used for polling this service. E.g.: for polling HTTP, the
 * URL to hit is configurable via a parameter. Parameters are specific to the
 * service monitor.
 */

@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.NONE)
public class Parameter implements Serializable {
    private static final long serialVersionUID = 8611979898730492432L;

    /**
     * Field m_key.
     */
    @XmlAttribute(name="key")
    private String m_key;

    /**
     * Field m_value.
     */
    @XmlAttribute(name="value")
    private String m_value;

    /**
     * Field m_contents.
     */
    @XmlAnyElement(lax=false)
    @XmlJavaTypeAdapter(PollerClassObjectAdapter.class)
    private Object m_contents;


    public Parameter() {
        super();
    }

    public Parameter(final String key, final String value) {
        this();
        m_key = key;
        m_value = value;
    }

    public String getKey() {
        return m_key;
    }

    public void setKey(final String key) {
        m_key = key;
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        m_value = value;
    }

    public Object getAnyObject() {
        return m_contents;
    }

    public void setAnyObject(final Object anyObject) {
        m_contents = anyObject;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_contents == null) ? 0 : m_contents.hashCode());
        result = prime * result + ((m_key == null) ? 0 : m_key.hashCode());
        result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Parameter)) {
            return false;
        }
        final Parameter other = (Parameter) obj;
        if (m_contents == null) {
            if (other.m_contents != null) {
                return false;
            }
        } else if (!m_contents.equals(other.m_contents)) {
            return false;
        }
        if (m_key == null) {
            if (other.m_key != null) {
                return false;
            }
        } else if (!m_key.equals(other.m_key)) {
            return false;
        }
        if (m_value == null) {
            if (other.m_value != null) {
                return false;
            }
        } else if (!m_value.equals(other.m_value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Parameter [key=" + m_key + ", value=" + m_value + ", contents=" + m_contents + "]";
    }
}
