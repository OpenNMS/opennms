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

package org.opennms.features.config.osgi.cm;

import static org.junit.Assert.*;
import static org.opennms.features.config.osgi.cm.DictionaryUtil.createFromJson;
import static org.opennms.features.config.osgi.cm.DictionaryUtil.writeToJson;
import static org.osgi.framework.Constants.SERVICE_PID;

import java.util.Dictionary;
import java.util.Properties;

import org.junit.Test;
import org.opennms.features.config.service.api.JsonAsString;

public class DictionaryUtilTest {

    @Test
    public void shouldDoRoundTrip() {
        Properties props = new Properties();
        props.put("attribute1", "value1");
        props.put("attribute2", Boolean.TRUE);
        props.put("attribute3", 42);
        JsonAsString json = writeToJson(props);
        Dictionary propsConverted = createFromJson(json);
        assertEquals(props, propsConverted);
    }

    @Test
    public void shouldBeNullValueTolerant() {
        // A Dictionary doesn't allow null values. They might come from CM => Lets make sure we can handle that
        Dictionary propsConverted = createFromJson(new JsonAsString("{\"a1\":null, \"a2\":\"\"}"));
        assertEquals(1, propsConverted.size());
        assertEquals("", propsConverted.get("a2"));
    }


    @Test
    public void shouldIgnoreServicePid() {
        Properties props = new Properties();
        props.put(SERVICE_PID, "value1");
        props.put("attribute2", Boolean.TRUE);
        props.put("attribute3", 42);
        JsonAsString json = writeToJson(props);
        Dictionary propsConverted = createFromJson(json);
        assertNull(propsConverted.get(SERVICE_PID));
        assertEquals(Boolean.TRUE, propsConverted.get("attribute2"));
        assertEquals(42, propsConverted.get("attribute3"));
    }

}