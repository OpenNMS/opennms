/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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

package org.opennms.web.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

public class RestUtilsTest {

    private static boolean isProtected(final String key) {
        return RestUtils.isProtectedProperty(key);
    }

    @Test
    public void protectsExactPropertyNames() {
        assertTrue(isProtected("id"));
        assertTrue(isProtected("nodeId"));
        assertTrue(isProtected("authorizedGroups"));
        assertTrue(isProtected("foreignSource"));
        assertTrue(isProtected("foreignId"));
        assertTrue(isProtected("type"));
    }

    /** Request keys are normalized before binding, so separator forms must be covered. */
    @Test
    public void protectsSeparatorAndCaseVariants() {
        assertTrue(isProtected("foreign_source"));
        assertTrue(isProtected("foreign-source"));
        assertTrue(isProtected("Foreign_Source"));
        assertTrue(isProtected("FOREIGNSOURCE"));
        assertTrue(isProtected("Type"));
        assertTrue(isProtected("authorized_groups"));
        assertTrue(isProtected("node_id"));
    }

    /** Spring's BeanWrapper resolves nested and indexed paths, which must not reach a protected property. */
    @Test
    public void protectsNestedAndIndexedPaths() {
        assertTrue(isProtected("assetRecord.node.foreignSource"));
        assertTrue(isProtected("asset_record.node.foreign_source"));
        assertTrue(isProtected("node.foreignId"));
        assertTrue(isProtected("assetRecord.id"));
        assertTrue(isProtected("categories[0].authorizedGroups"));
        assertTrue(isProtected("metaData[foreignSource]"));
        assertTrue(isProtected("metaData[foreign_source]"));
    }

    /** Endpoints that never wire a guard are still covered, e.g. via a node back-reference. */
    @Test
    public void nodePropertiesAreProtectedByDefault() {
        assertTrue(RestUtils.isProtectedProperty("node.foreign_source"));
        assertTrue(RestUtils.isProtectedProperty("ipInterface.node.foreignId"));
        assertTrue(RestUtils.isProtectedProperty("foreignSource"));
    }

    @Test
    public void allowsOrdinaryProperties() {
        assertFalse(isProtected("sysContact"));
        assertFalse(isProtected("sys_contact"));
        assertFalse(isProtected("label"));
        assertFalse(isProtected("assetRecord.manufacturer"));
        assertFalse(isProtected("asset_record.operating_system"));
        assertFalse(isProtected("description"));
    }

    /** End-to-end: a protected property is refused while an ordinary one is written. */
    @Test
    public void setBeanPropertiesRefusesProtectedWrites() {
        final Bean bean = new Bean();
        final MultivaluedMap<String,String> params = new MultivaluedHashMap<>();
        params.putSingle("label", "legit");
        params.putSingle("foreign_source", "AttackerReq");
        params.putSingle("id", "999");
        RestUtils.setBeanProperties(bean, params);

        assertEquals("legit", bean.getLabel());
        assertNull("foreignSource must not be written", bean.getForeignSource());
        assertNull("id must not be written", bean.getId());
    }

    /** Provisioning legitimately sets foreignSource/foreignId, so it opts out of those. */
    @Test
    public void requisitionPropertiesAllowForeignSourceButNotPrimaryKeys() {
        final Bean bean = new Bean();
        final MultivaluedMap<String,String> params = new MultivaluedHashMap<>();
        params.putSingle("foreign_source", "MyRequisition");
        params.putSingle("id", "999");
        RestUtils.setRequisitionProperties(bean, params);

        assertEquals("MyRequisition", bean.getForeignSource());
        assertNull("id is immutable even for requisitions", bean.getId());
    }

    public static class Bean {
        private String label;
        private String foreignSource;
        private Integer id;
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getForeignSource() { return foreignSource; }
        public void setForeignSource(String foreignSource) { this.foreignSource = foreignSource; }
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
    }

    @Test
    public void containsPropertyMatchesVariantsAndPaths() {
        assertTrue(RestUtils.containsProperty(params("ifIndex"), "ifIndex"));
        assertTrue(RestUtils.containsProperty(params("if_index"), "ifIndex"));
        assertTrue(RestUtils.containsProperty(params("IfIndex"), "ifIndex"));
        assertTrue(RestUtils.containsProperty(params("node.ifIndex"), "ifIndex"));
        assertTrue(RestUtils.containsProperty(params("Name"), "name"));
        assertTrue(RestUtils.containsProperty(params("ip_address"), "ipAddress"));

        assertFalse(RestUtils.containsProperty(params("ifDescr"), "ifIndex"));
        assertFalse(RestUtils.containsProperty(params("description"), "name"));
    }

    private static MultivaluedMap<String,String> params(final String key) {
        final MultivaluedMap<String,String> params = new MultivaluedHashMap<>();
        params.putSingle(key, "value");
        return params;
    }
}
