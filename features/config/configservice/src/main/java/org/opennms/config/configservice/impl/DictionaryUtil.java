/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

package org.opennms.config.configservice.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

public class DictionaryUtil {
    public static boolean equalsWithoutRevision(Dictionary<String, String> a, Dictionary<String, String> b) {
        if (a == null && b == null) {
            return true;
        } else if (a == null || b == null) {
            return false;
        } else if (a.size() != b.size()) {
            return false;
        }

        return Collections.list(a.keys())
                .stream()
                .filter(key -> !":org.apache.felix.configadmin.revision:".equals(key))
                .allMatch(key -> a.get(key).equals(b.get(key)));
    }

    public static Dictionary createFromRawString(String propertiesString) {
        Objects.requireNonNull(propertiesString);
        try {
            Properties props = new Properties();
            props.load(new StringReader(propertiesString));
            return props;
        } catch (IOException e) {
            // should not happen since we have a StringReader...
            throw new RuntimeException(e);
        }
    }

    public static String writeToRawString(final Dictionary dictionary) {
        Objects.requireNonNull(dictionary);

        Properties props;
        if(dictionary instanceof Properties) {
            props = (Properties) dictionary;
        } else {
            props = new Properties();
            Enumeration keys = dictionary.keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = dictionary.get(key);
                props.setProperty(key.toString(), value.toString());
            }
        }

        StringWriter writer = new StringWriter();
        try {
            props.store(writer, "Created by " + DictionaryUtil.class.getName());
            return writer.toString();
        } catch (IOException e) {
            // should not happen since we have a StringWriter...
            throw new RuntimeException(e);
        }

    }
}
