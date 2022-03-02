/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
