/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
