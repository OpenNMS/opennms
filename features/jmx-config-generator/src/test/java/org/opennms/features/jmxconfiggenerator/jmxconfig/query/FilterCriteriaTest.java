/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.jmxconfiggenerator.jmxconfig.query;

import org.junit.Assert;
import org.junit.Test;

public class FilterCriteriaTest {

    @Test
    public void testParsing() {
        validateAttributeNameIsNull("org.eclipse.jetty.server.session:type=sessionhandler,context=opennms-remoting,id=0");
        validateAttributeNameIsNull("org.eclipse.jetty.servlet:type=filtermapping,name=springSecurityFilterChain,id=1");
        validateAttributeNameIsNull("org.eclipse.jetty.servlet:type=servletmapping,name=__org.eclipse.jetty.servlet.JspPropertyGroupServlet__,id=0");
        validateAttributeNameIsNull("org.eclipse.jetty.servlet:type=servletmapping,name=dispatcher,id=8");

        validateFullParsing("org.eclipse.jetty.servlet:type=filtermapping,name=springSecurityFilterChain,id=1", "abc");
        validateFullParsing("org.eclipse.jetty.server.session:type=sessionhandler,context=opennms-remoting,id=0", "running");
        validateFullParsing("org.eclipse.jetty.servlet:type=servletmapping,name=__org.eclipse.jetty.servlet.JspPropertyGroupServlet__,id=0","PendingState");
    }

    @Test
    public void testParsing2() {
        FilterCriteria filterCriteria = FilterCriteria.parse("java.lang:type=GarbageCollector,name=PS Scavenge:CollectionTime");
        Assert.assertEquals("java.lang:type=GarbageCollector,name=PS Scavenge", filterCriteria.objectName);
        Assert.assertEquals("CollectionTime", filterCriteria.attributeName);
    }

    private void validateFullParsing(String objectname, String attributeName) {
        FilterCriteria filterCriteria = FilterCriteria.parse(attributeName != null ?  objectname + ":" + attributeName : objectname);
        Assert.assertEquals(attributeName, filterCriteria.attributeName);
        Assert.assertEquals(objectname, filterCriteria.objectName);
    }

    private void validateAttributeNameIsNull(String input) {
        validateFullParsing(input, null);
    }
}
