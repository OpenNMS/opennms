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
package org.opennms.netmgt.telemetry.distributed.common;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;

public class ConfigurationTest {

    @Test
    public void verifyAdapter() {
        final Map<String, String> properties = new HashMap();
        properties.put("name", "Example");
        properties.put("class-name", "com.example.Adapter");
        properties.put("parameters.test", "success");
        properties.put("parameters.dotted.key", "success");

        final PropertyTree tree = PropertyTree.from(properties);

        final AdapterDefinition adapter = new MapBasedAdapterDef("Test", tree);

        Assert.assertEquals("Example", adapter.getName());
        Assert.assertEquals("com.example.Adapter", adapter.getClassName());
        Assert.assertEquals("success", adapter.getParameterMap().get("test"));
        Assert.assertEquals("success", adapter.getParameterMap().get("dotted.key"));
    }

    /**
     * see NMS-13477
     */
    @Test
    public void testWhitespaces() {
        final Map<String, String> properties = new HashMap();
        properties.put("name", " Example");
        properties.put("class-name", " com.example.Adapter");
        properties.put("parameters.test", "success ");
        properties.put("parameters.dotted.key", "success ");

        final PropertyTree tree = PropertyTree.from(properties);

        final AdapterDefinition adapter = new MapBasedAdapterDef("Test", tree);

        Assert.assertEquals("Example", adapter.getName());
        Assert.assertEquals("com.example.Adapter", adapter.getClassName());
        Assert.assertEquals("success", adapter.getParameterMap().get("test"));
        Assert.assertEquals("success", adapter.getParameterMap().get("dotted.key"));
    }
}
