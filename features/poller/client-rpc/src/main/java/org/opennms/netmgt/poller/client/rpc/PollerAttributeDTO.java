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
package org.opennms.netmgt.poller.client.rpc;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.config.poller.PollerClassObjectAdapter;

@XmlRootElement(name = "attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class PollerAttributeDTO {

    @XmlAttribute(name = "key")
    private String key;

    @XmlAttribute(name="value")
    private String value;

    @XmlAnyElement(lax=false)
    @XmlJavaTypeAdapter(PollerClassObjectAdapter.class)
    private Object contents;

    public PollerAttributeDTO() {
        // no-arg constructor for JAXB
    }

    public PollerAttributeDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public PollerAttributeDTO(String key, Object contents) {
        this.key = key;
        if (contents != null && contents instanceof String) {
            this.value = (String)contents;
        } else {
            this.contents = contents;
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object getContents() {
        return contents;
    }

    public void setContents(Object contents) {
        this.contents = contents;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, contents);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final PollerAttributeDTO other = (PollerAttributeDTO) obj;
        return Objects.equals(this.key, other.key)
                && Objects.equals(this.value, other.value)
                && Objects.equals(this.contents, other.contents);
    }

    @Override
    public String toString() {
        return String.format("PollerAttributeDTO[key='%s', value='%s', contents='%s']", key, value, contents);
    }
}
