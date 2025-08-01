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
package org.opennms.netmgt.collectd;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.test.ThrowableAnticipator;

/**
 * Tests for GenericIndexResourceType.
 *
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 * @see GenericIndexResourceType
 */
public class GenericIndexResourceTypeTest {

    @Test
    public void testNullResourceType() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("resourceType argument must not be null"));
        try {
            new GenericIndexResourceType(null, null, null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    public void testInstantiate() {
        instantiate();
    }

    @Test
    public void testGetStorageStrategy() {
        GenericIndexResourceType g = instantiate();
        Assert.assertNotNull("storageStrategy should not be null", g.getStorageStrategy());
        Assert.assertNotNull("persistenceSelectorStrategy should not be null", g.getPersistenceSelectorStrategy());
    }

    private GenericIndexResourceType instantiate() {
        org.opennms.netmgt.config.datacollection.ResourceType rt = new org.opennms.netmgt.config.datacollection.ResourceType();
        
        PersistenceSelectorStrategy ps = new PersistenceSelectorStrategy();
        ps.setClazz("org.opennms.netmgt.collection.support.PersistAllSelectorStrategy");
        rt.setPersistenceSelectorStrategy(ps);
        
        StorageStrategy ss = new StorageStrategy();
        ss.setClazz("org.opennms.netmgt.collection.support.IndexStorageStrategy");
        rt.setStorageStrategy(ss);
        
        return new GenericIndexResourceType(null, null, rt);
    }
}
