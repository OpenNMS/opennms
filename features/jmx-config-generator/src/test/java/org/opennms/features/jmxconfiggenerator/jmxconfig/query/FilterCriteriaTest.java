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
