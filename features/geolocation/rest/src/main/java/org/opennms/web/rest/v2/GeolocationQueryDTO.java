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
package org.opennms.web.rest.v2;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.deser.std.StdScalarDeserializer;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

@XmlRootElement
@Schema(description = "Query for the geolocation endpoint. Every member is optional.")
public class GeolocationQueryDTO {
    @Schema(description = """
            How to calculate each node's status. Matched case-insensitively. `Alarms` and `Outages` are \
            the values the endpoint can act on; `None` is accepted by validation and then rejected with \
            500, so omit the member to skip the calculation.""",
            allowableValues = {"Alarms", "Outages"}, example = "Alarms")
    private String strategy;

    @Schema(description = """
            Lowest calculated severity a node must have to be returned. Read only when `strategy` is \
            also given. Validation accepts any `OnmsSeverity` name, but only `Normal`, `Warning`, \
            `Minor`, `Major` and `Critical` can be converted; `Indeterminate` and `Cleared` are \
            rejected with 500.""",
            allowableValues = {"Normal", "Warning", "Minor", "Major", "Critical"}, example = "Normal")
    private String severityFilter;

    static public class CustomBooleanSerializer extends StdScalarDeserializer<Boolean> {

        public CustomBooleanSerializer() {
            super(Boolean.class);
        }

        public Boolean deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws JsonProcessingException, IOException {
            try {
                return this._parseBoolean(jsonParser, deserializationContext);
            } catch (final JsonMappingException e) {
                throw new JsonMappingException("Error mapping JSON to Boolean value, details omitted.");
            }
        }

        public Boolean deserializeWithType(final JsonParser jsonParser, final DeserializationContext deserializationContext, final TypeDeserializer typeDeserializer) throws JsonProcessingException, IOException {
            try {
                return this._parseBoolean(jsonParser, deserializationContext);
            } catch (final JsonMappingException e) {
                throw new JsonMappingException("Error mapping JSON to Boolean value, details omitted.");
            }
        }
    }

    /**
     * Use of a custom deserializer to prevent the incorrect value from being returned to the requester. See NMS-18052.
     */
    @JsonDeserialize(using = CustomBooleanSerializer.class)
    @Schema(description = """
            Whether acknowledged alarms count towards the status when `strategy` is `Alarms`. Defaults \
            to false. A value that is not a boolean is answered with 500.""",
            example = "false")
    private boolean includeAcknowledgedAlarms;

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getSeverityFilter() {
        return severityFilter;
    }

    public void setSeverityFilter(String severityFilter) {
        this.severityFilter = severityFilter;
    }

    public boolean isIncludeAcknowledgedAlarms() {
        return includeAcknowledgedAlarms;
    }

    public void setIncludeAcknowledgedAlarms(boolean includeAcknowledgedAlarms) {
        this.includeAcknowledgedAlarms = includeAcknowledgedAlarms;
    }
}
