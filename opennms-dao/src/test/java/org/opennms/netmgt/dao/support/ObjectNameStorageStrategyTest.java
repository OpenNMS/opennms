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

    /*
     * This test verifies that if we try to use a sanitized instance string we get an IllegalArgumentException back
     * when we try to get the resource name of our index.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInstance() {
        ResourcePath parentResource = ResourcePath.get("1");
        String instance = "java.lang:type=MemoryPool,name=Survivor Space";
        String expResult = MockCollectionResource.sanitizeInstance(instance);
        CollectionResource resource = new MockCollectionResource(parentResource, instance, expResult, "");
        List<org.opennms.netmgt.collection.api.Parameter> params = new ArrayList<>();
        params.add(new Parameter("index-format", "${ObjectName.toString()}"));
        ObjectNameStorageStrategy storageStrategy = new ObjectNameStorageStrategy();
        storageStrategy.setParameters(params);

        String result = storageStrategy.getResourceNameFromIndex(resource);
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
