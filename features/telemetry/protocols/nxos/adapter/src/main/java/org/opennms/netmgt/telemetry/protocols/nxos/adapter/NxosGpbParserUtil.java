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
package org.opennms.netmgt.telemetry.protocols.nxos.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis;
import org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry;


/**
 * Utility to parse any key/value metric from gpb messages.
 *
 * Since there is no easy way to get key/value metrics from repeated data_gpbkv,
 * this utility provides some methods to get values for those metrics without
 * bothering about structure they may follow.
 *
 * @author cgorantla
 */
public class NxosGpbParserUtil {

    /**
     * Given a Telemetry Message and field name, get it's value as double.
     * 
     * @param   msg  {@link org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry} message
     * @param   name  field name
     * @return  value field value
     */
    public static Double getValueAsDouble(TelemetryBis.Telemetry msg, String name) {
        if (Objects.isNull(msg) || Objects.isNull(name)) {
            return Double.NaN;
        }
        for (TelemetryBis.TelemetryField field : msg.getDataGpbkvList()) {
            TelemetryBis.TelemetryField subField = findFieldWithName(field, name);
            if (subField != null) {
                return getDoubleValue(subField);
            }
        }
        return Double.NaN;
    }

    /**
     * Given a Telemetry Message and field name, get it's value as string.
     * 
     * @param   msg  {@link org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry} message
     * @param   name  field name
     * @return  value field value
     */
    public static String getValueAsString(TelemetryBis.Telemetry msg, String name) {
        if (Objects.isNull(msg) || Objects.isNull(name))  {
            return null;
        }
        for (TelemetryBis.TelemetryField field : msg.getDataGpbkvList()) {
            TelemetryBis.TelemetryField subField = findFieldWithName(field, name);
            if (subField != null) {
                return getStringValue(subField);
            }
        }
        return null;
    }

    /**
     * Given a Telemetry Message and row name, get the list of fields
     * 
     * @param   Telemetry message {@link org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry}
     * @param   name of field
     * @return  list of Telemetry fields
     */
    public static List<TelemetryBis.TelemetryField> getRowsFromTable(Telemetry msg, String name) {
        if (Objects.isNull(msg) || Objects.isNull(name)) {
            return Collections.emptyList();
        }
        for (TelemetryBis.TelemetryField field : msg.getDataGpbkvList()) {
            // ROWS are arrays for a set of metrics, Assumption is that they
            // start with ROW_
            TelemetryBis.TelemetryField subField = findFieldWithName(field, "ROW_" + name);
            if (subField != null) {
                return subField.getFieldsList();
            }
        }
        return Collections.emptyList();

    }

    /**
     * Given a Telemetry field row and name of field, get field value as String
     * 
     * @param   row {@link org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField}
     * @param   name  field name
     * @return  value field value
     */
    public static String getValueFromRowAsString(TelemetryBis.TelemetryField row, String name) {
        if (Objects.isNull(row) || Objects.isNull(name)) {
            return null;
        }
        for (TelemetryBis.TelemetryField nestedField : row.getFieldsList()) {
            TelemetryBis.TelemetryField subField = findFieldWithName(nestedField, name);
            if (subField != null) {
                return getStringValue(subField);
            }
        }
        return null;
    }

    /**
     * Given a Telemetry field row and name of field, get field value as Double
     * 
     * @param   row {@link org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.TelemetryField}
     * @param   name  field name
     * @return  value field value
     */
    public static Double getValueFromRowAsDouble(TelemetryBis.TelemetryField row, String name) {
        if (Objects.isNull(row) || Objects.isNull(name)) {
            return Double.NaN;
        }
        for (TelemetryBis.TelemetryField field : row.getFieldsList()) {
            TelemetryBis.TelemetryField subField = findFieldWithName(field, name);
            if (subField != null) {
                return getDoubleValue(subField);
            }
        }
        return Double.NaN;
    }

    /**
     * Given a Telemetry Message, parent field name and field name, get field value as Double
     * 
     * @param   msg {@link org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry} message
     * @param   parentFieldName 
     * @param   name  field name
     * @return  value field value
     */
    public static Double getValueAsDoubleRelativeToField(TelemetryBis.Telemetry msg, String parentFieldName,
            String name) {
        if (Objects.isNull(msg) || Objects.isNull(parentFieldName) || Objects.isNull(name)) {
            return Double.NaN;
        }
        for (TelemetryBis.TelemetryField field : msg.getDataGpbkvList()) {
            TelemetryBis.TelemetryField parentField = findFieldWithName(field, parentFieldName);
            if (parentField != null) {
                TelemetryBis.TelemetryField subField = findFieldWithName(parentField, name);
                if (subField != null) {
                    return getDoubleValue(subField);
                }
            }
        }
        return Double.NaN;
    }

    /**
     * Given a Telemetry Message, parent field name and field name, get field value as Double
     *
     * @param   msg {@link org.opennms.netmgt.telemetry.protocols.nxos.adapter.proto.TelemetryBis.Telemetry} message
     * @param   parentFieldName
     * @param   name  field name
     * @return  value field value
     */
    public static String getValueAsStringRelativeToField(TelemetryBis.Telemetry msg, String parentFieldName,
            String name) {
        if (Objects.isNull(msg) || Objects.isNull(parentFieldName) || Objects.isNull(name)) {
            return null;
        }
        for (TelemetryBis.TelemetryField field : msg.getDataGpbkvList()) {
            TelemetryBis.TelemetryField parentField = findFieldWithName(field, parentFieldName);
            if (parentField != null) {
                TelemetryBis.TelemetryField subField = findFieldWithName(parentField, name);
                if (subField != null) {
                    return getStringValue(subField);
                }
            }
        }
        return null;
    }

    private static TelemetryBis.TelemetryField findFieldWithName(TelemetryBis.TelemetryField field, String name) {
        if (Objects.equals(field.getName(), name)) {
            return field;
        }
        for (TelemetryBis.TelemetryField subField : field.getFieldsList()) {
            TelemetryBis.TelemetryField matchingField = findFieldWithName(subField, name);
            if (matchingField != null) {
                return matchingField;
            }
        }
        return null;
    }

    private static Double getDoubleValue(TelemetryBis.TelemetryField field) {
        TelemetryBis.TelemetryField.ValueByTypeCase typeOfValue = field.getValueByTypeCase();
        if (TelemetryBis.TelemetryField.ValueByTypeCase.STRING_VALUE.equals(typeOfValue)) {
            return Double.parseDouble(field.getStringValue());
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.UINT64_VALUE.equals(typeOfValue)) {
            return Double.valueOf(field.getUint64Value());
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.UINT32_VALUE.equals(typeOfValue)) {
            return Double.valueOf(field.getUint32Value());
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.DOUBLE_VALUE.equals(typeOfValue)) {
            return field.getDoubleValue();
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.FLOAT_VALUE.equals(typeOfValue)) {
            return Double.valueOf(field.getFloatValue());
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.SINT32_VALUE.equals(typeOfValue)) {
            return Double.valueOf(field.getSint32Value());
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.SINT64_VALUE.equals(typeOfValue)) {
            return Double.valueOf(field.getSint64Value());
        }
        return Double.NaN;
    }

    private static String getStringValue(TelemetryBis.TelemetryField field) {
        TelemetryBis.TelemetryField.ValueByTypeCase typeOfValue = field.getValueByTypeCase();
        if (TelemetryBis.TelemetryField.ValueByTypeCase.STRING_VALUE.equals(typeOfValue)) {
            return field.getStringValue();
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.UINT64_VALUE.equals(typeOfValue)) {
            return String.valueOf(field.getUint64Value());
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.UINT32_VALUE.equals(typeOfValue)) {
            return String.valueOf(field.getUint32Value());
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.DOUBLE_VALUE.equals(typeOfValue)) {
            return String.valueOf(field.getDoubleValue());
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.FLOAT_VALUE.equals(typeOfValue)) {
            return String.valueOf(field.getFloatValue());
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.SINT32_VALUE.equals(typeOfValue)) {
            return String.valueOf(field.getSint32Value());
        } else if (TelemetryBis.TelemetryField.ValueByTypeCase.SINT64_VALUE.equals(typeOfValue)) {
            return String.valueOf(field.getSint64Value());
        }
        return null;
    }

}
