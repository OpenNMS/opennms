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
package org.opennms.netmgt.snmp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SnmpValueXmlAdapter extends XmlAdapter<SnmpValueXmlAdapter.JaxbSnmpValue, SnmpValue> {

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "value")
    public static class JaxbSnmpValue {
        @XmlAttribute(name = "type", required = true)
        private int type;

        @XmlValue
        private byte[] bytes;

        public JaxbSnmpValue() {
            // No-arg constructor for JAXB
        }

        public JaxbSnmpValue(SnmpValue value) {
            type = value.getType();
            bytes = value.getBytes();
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }

    @Override
    public JaxbSnmpValue marshal(SnmpValue value) throws Exception {
        return new JaxbSnmpValue(value);
    }

    @Override
    public SnmpValue unmarshal(JaxbSnmpValue value) throws Exception {
        final SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();
        return valueFactory.getValue(value.getType(), value.getBytes());
    }
}
