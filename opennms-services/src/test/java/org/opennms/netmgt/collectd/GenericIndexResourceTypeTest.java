/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2007 May 14: Created this file. - dj@opennms.org
 * 
 * Copyright (C) 2007 DJ Gregor.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
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
        ps.setClazz("org.opennms.netmgt.collectd.PersistAllSelectorStrategy");
        rt.setPersistenceSelectorStrategy(ps);
        
        StorageStrategy ss = new StorageStrategy();
        ss.setClazz("org.opennms.netmgt.dao.support.IndexStorageStrategy");
        rt.setStorageStrategy(ss);
        
        return new GenericIndexResourceType(null, null, rt);
    }
}
