/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
