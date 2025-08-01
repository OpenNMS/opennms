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
package liquibase.ext2.cm.change.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.service.api.JsonAsString;

/** Converts a configuration properties file into json format to be stored in the cm manger. */
public class PropertiesToJson {
    final private JsonAsString json;
    final private ConfigItem schema;

    public PropertiesToJson(final InputStream inputStream, ConfigItem schema) {
        this.schema = schema;
        Properties props = new Properties();
        try {
            props.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JSONObject jsonObject = new JSONObject();
        for(Map.Entry<?,?> entry : props.entrySet()) {
            jsonObject.put(entry.getKey().toString(), cast(entry));
        }
        json = new JsonAsString(jsonObject.toString());
    }

    private Object cast(Map.Entry<?,?> entry) {
        if (null == entry.getValue()) {
            return null;
        }
        ConfigItem.Type type = schema
                .getChild(entry.getKey().toString())
                .map(ConfigItem::getType)
                .orElse(ConfigItem.Type.STRING);
        switch (type) {
            case STRING:
                return entry.getValue().toString();
            case NUMBER:
                return Double.parseDouble(entry.getValue().toString());
            case LONG:
                return Long.parseLong(entry.getValue().toString());
            case BOOLEAN:
                return Boolean.parseBoolean(entry.getValue().toString());
            // TODO: Patrick case DATE:
            // TODO: Patrick case DATE_TIME:
            case INTEGER:
            case POSITIVE_INTEGER:
            case NON_NEGATIVE_INTEGER:
            case NEGATIVE_INTEGER:
                return Integer.parseInt(entry.getValue().toString());
        }
        return entry.getValue().toString();
    }

    public JsonAsString getJson() {
        return json;
    }
}
