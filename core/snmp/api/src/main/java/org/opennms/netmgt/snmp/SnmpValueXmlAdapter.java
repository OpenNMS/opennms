/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
