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


public class StringWithNumberCheckAdapter implements OpaqueTypeAdapter {
    
    String stringValue;
    Long longValue = null;
    Double doubleValue = null;
    OpaqueValueType valueType = OpaqueValueType.STRING;

    public StringWithNumberCheckAdapter(String value) {
        this.stringValue = Objects.requireNonNull(value);
        
        //Try to extract a long value
        try {
            this.longValue = Long.parseLong(value);
            this.doubleValue = (double)this.longValue;
            this.valueType = OpaqueValueType.LONG;
            return;
        } catch (NumberFormatException ex) {
            //do nothing
        }
        
        //Try to extract a double value
        try {
            this.doubleValue = Double.valueOf(value);
            this.longValue = this.doubleValue.longValue();
            this.valueType = OpaqueValueType.DOUBLE;
        } catch (NumberFormatException ex) {
            //do nothing
        }
    }
    
    @Override
    public Long getLong() {
        return this.longValue;
    }

    @Override
    public Double getDouble() {
        return this.doubleValue;
    }

    @Override
    public String getString() {
        return stringValue;
    }

    @Override
    public OpaqueValueType getValueType() {
        return valueType;
    }
    
}
