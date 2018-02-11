/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.support.ObjectNameStorageStrategy;
import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.model.ResourcePath;

/**
 */
public class ObjectNameStorageStrategyTest {

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullParameters() {
        List<org.opennms.netmgt.collection.api.Parameter> params = null;
        ObjectNameStorageStrategy instance = new ObjectNameStorageStrategy();
        instance.setParameters(params);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyParameters() {
        List<org.opennms.netmgt.collection.api.Parameter> params = new ArrayList<>();
        ObjectNameStorageStrategy instance = new ObjectNameStorageStrategy();
        instance.setParameters(params);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidParameters() {
        List<org.opennms.netmgt.collection.api.Parameter> params = new ArrayList<>();
        params.add(new Parameter("SERVICE", "svc"));
        ObjectNameStorageStrategy instance = new ObjectNameStorageStrategy();
        instance.setParameters(params);
    }

    @Test()
    public void testSetValidParameters() {
        List<org.opennms.netmgt.collection.api.Parameter> params = new ArrayList<>();
        params.add(new Parameter("index-format", "${ObjectName.toString()}"));
        ObjectNameStorageStrategy instance = new ObjectNameStorageStrategy();
        instance.setParameters(params);
    }

    @Test
    public void testGetResourceNameFromIndex() {
        ResourcePath parentResource = ResourcePath.get("1");
        CollectionResource resource = new MockCollectionResource(parentResource, "java.lang:type=MemoryPool,name=Survivor Space", "");
        List<org.opennms.netmgt.collection.api.Parameter> params = new ArrayList<>();
        params.add(new Parameter("index-format", "${ObjectName.toString()}"));
        ObjectNameStorageStrategy instance = new ObjectNameStorageStrategy();
        instance.setParameters(params);

        String expResult = "java.lang:type=MemoryPool,name=Survivor Space";
        String result = instance.getResourceNameFromIndex(resource);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetResourceNameFromIndexCleanOutput() {
        ResourcePath parentResource = ResourcePath.get("1");
        CollectionResource resource = new MockCollectionResource(parentResource, "java.lang:type=MemoryPool,name=Survivor Space", "");
        List<org.opennms.netmgt.collection.api.Parameter> params = new ArrayList<>();
        params.add(new Parameter("index-format", "${ObjectName.toString()}"));
        params.add(new Parameter("clean-output", "true"));
        ObjectNameStorageStrategy instance = new ObjectNameStorageStrategy();
        instance.setParameters(params);

        String expResult = "java.lang_typeMemoryPool,nameSurvivor_Space";
        String result = instance.getResourceNameFromIndex(resource);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetResourceNameFromIndex2() {
        ResourcePath parentResource = ResourcePath.get("1");
        CollectionResource resource = new MockCollectionResource(parentResource, "java.lang:type=MemoryPool,name=Survivor Space", "");
        List<org.opennms.netmgt.collection.api.Parameter> params = new ArrayList<>();
        Parameter p = new Parameter("index-format", "${domain}");
        params.add(p);
        ObjectNameStorageStrategy instance = new ObjectNameStorageStrategy();
        instance.setParameters(params);

        String expResult = "java.lang";
        String result = instance.getResourceNameFromIndex(resource);
        assertEquals(expResult, result);

        params.clear();
        p.setValue("${type}");
        params.add(p);
        instance.setParameters(params);
        expResult = "MemoryPool";
        result = instance.getResourceNameFromIndex(resource);
        assertEquals(expResult, result);

        params.clear();
        p.setValue("${name}");
        params.add(p);
        instance.setParameters(params);
        expResult = "Survivor Space";
        result = instance.getResourceNameFromIndex(resource);
        assertEquals(expResult, result);

        params.clear();
        p.setValue("${domain}:type=${type},name=${name}");
        params.add(p);
        instance.setParameters(params);
        expResult = "java.lang:type=MemoryPool,name=Survivor Space";
        result = instance.getResourceNameFromIndex(resource);
        assertEquals(expResult, result);
    }

    @Test
    public void testQuotedKeyValues() {
        ResourcePath parentResource = ResourcePath.get("1");
        CollectionResource resource = new MockCollectionResource(parentResource, "d:k1=\"ab\",k2=\"cd\",k3=\"v3\"", "");
        List<org.opennms.netmgt.collection.api.Parameter> params = new ArrayList<>();
        Parameter p = new Parameter("index-format", "${domain}-${k1}-${k2}-${k3}");
        params.add(p);
        ObjectNameStorageStrategy instance = new ObjectNameStorageStrategy();
        instance.setParameters(params);

        String expResult = "d-ab-cd-v3";
        String result = instance.getResourceNameFromIndex(resource);
        assertEquals(expResult, result);
    }

}
