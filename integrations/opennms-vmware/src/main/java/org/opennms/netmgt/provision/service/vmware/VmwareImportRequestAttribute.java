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
package org.opennms.netmgt.provision.service.vmware;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.Objects;

@XmlRootElement(name = "custom-attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class VmwareImportRequestAttribute {

    @XmlAttribute(name="key")
    private String key;

    @XmlValue
    private String value;

    public VmwareImportRequestAttribute() { }

    public VmwareImportRequestAttribute(String key, String value) {
        this.key = key;
        this.value = value;
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

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VmwareImportRequestAttribute)) {
            return false;
        }
        VmwareImportRequestAttribute castOther = (VmwareImportRequestAttribute) other;
        return Objects.equals(key, castOther.key) && Objects.equals(value, castOther.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

}
