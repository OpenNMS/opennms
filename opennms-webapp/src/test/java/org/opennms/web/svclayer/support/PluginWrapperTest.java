/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy;
import org.opennms.netmgt.provision.persist.policies.NodeCategorySettingPolicy;
import org.opennms.netmgt.provision.support.PluginWrapper;


public class PluginWrapperTest {
    
    @Test
    public void testChoices() throws Exception {
        PluginWrapper wrapper = new PluginWrapper(MatchingSnmpInterfacePolicy.class);
        assertTrue("required keys must contain matchBehavior", wrapper.getRequiredItems().containsKey("matchBehavior"));
        assertTrue("action must contain DISABLE_COLLECTION", wrapper.getRequiredItems().get("action").contains("DISABLE_COLLECTION"));
    }

    @Test
    public void testRequired() throws Exception {
        PluginWrapper wrapper = new PluginWrapper(NodeCategorySettingPolicy.class);
        assertTrue("category should be required", wrapper.getRequired().get("category"));
        assertFalse("type should not be required", wrapper.getRequired().get("type"));
    }
}
