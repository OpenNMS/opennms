/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import org.opennms.features.config.service.api.JsonAsString;

/** Converts a configuration properties file into json format to be stored in the cm manger. */
public class PropertiesToJson {
    final private JsonAsString json;

    public PropertiesToJson(final InputStream inputStream) {
        Properties props = new Properties();
        try {
            props.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JSONObject jsonObject = new JSONObject();
        for(Map.Entry<?,?> entry : props.entrySet()) {
            jsonObject.put(entry.getKey().toString(), entry.getValue());
        }
        json = new JsonAsString(jsonObject.toString());
    }

    public JsonAsString getJson() {
        return json;
    }
}
