/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.snmp4j.opaqueadapter;

import java.util.Objects;
import org.opennms.netmgt.snmp.snmp4j.OpaqueValueType;
import org.snmp4j.smi.OctetString;


public class UnsupportedAdapter implements OpaqueTypeAdapter {
    
    final OctetString value;

    public UnsupportedAdapter(OctetString value) {
        this.value = Objects.requireNonNull(value);
    }
    
    @Override
    public Long getLong() {
        return null;
    }

    @Override
    public Double getDouble() {
        return null;
    }

    @Override
    public String getString() {
        return this.value.toString();
    }

    @Override
    public OpaqueValueType getValueType() {
        return OpaqueValueType.UNSUPPORTED;
    }
}
