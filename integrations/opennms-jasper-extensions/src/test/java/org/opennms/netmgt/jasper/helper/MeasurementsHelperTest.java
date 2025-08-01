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
package org.opennms.netmgt.jasper.helper;


import org.junit.Assert;
import org.junit.Test;

public class MeasurementsHelperTest {

    @Test
    public void verifyGetNodeOrNodeSourceDescriptor() {
        Assert.assertEquals("node[null]", MeasurementsHelper.getNodeOrNodeSourceDescriptor(null, null, null));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", "", ""));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", null, null));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", "opennms.local", ""));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", "", "201508240000"));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", "opennms.local", null));
        Assert.assertEquals("node[11]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", null, "201508240000"));
        Assert.assertEquals("nodeSource[opennms.local:20150824000000]", MeasurementsHelper.getNodeOrNodeSourceDescriptor("11", "opennms.local", "20150824000000"));
    }
}
