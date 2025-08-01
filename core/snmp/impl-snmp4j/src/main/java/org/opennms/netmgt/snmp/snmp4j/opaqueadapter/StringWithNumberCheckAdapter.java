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
package org.opennms.netmgt.snmp.snmp4j.opaqueadapter;

import java.util.Objects;
import org.opennms.netmgt.snmp.snmp4j.OpaqueValueType;

/**
 * This adapter is used to collect numeric data which is transfered as string.
 * To improove performance the content of the string is checked only onece at creation.
 */
public class StringWithNumberCheckAdapter implements OpaqueTypeAdapter {
    
    String stringValue;
    Long longValue;
    Double doubleValue;
    OpaqueValueType valueType;

    public StringWithNumberCheckAdapter(String value) {
        this.stringValue = Objects.requireNonNull(value);
        
        //Trying to get an integer value (inl, long, ...) and store it as long. (Long is more perfomant as double)
        try {
            this.longValue = Long.parseLong(value);
            this.doubleValue = (double)this.longValue;
            this.valueType = OpaqueValueType.LONG;
            return;
        } catch (NumberFormatException ex) {
            //do nothing -> thy to convert to double in the next step
        }
        
        //Checking if we have a double value e.g. "+123e-45"
        try {
            this.doubleValue = Double.valueOf(value);
            this.longValue = this.doubleValue.longValue();
            this.valueType = OpaqueValueType.DOUBLE;
            return;
        } catch (NumberFormatException ex) {
            //do nothing
        }

        this.doubleValue = null;
        this.longValue = null;
        this.valueType = OpaqueValueType.STRING;
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
        return this.stringValue;
    }

    @Override
    public OpaqueValueType getValueType() {
        return this.valueType;
    }
    
}
