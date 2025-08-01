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