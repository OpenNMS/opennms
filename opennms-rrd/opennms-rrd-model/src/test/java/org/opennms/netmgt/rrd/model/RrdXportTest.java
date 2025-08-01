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
package org.opennms.netmgt.rrd.model;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.rrd.model.RrdXport;

/**
 * The Class RRD Export Test.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class RrdXportTest {

    /**
     * Parses the Xport.
     *
     * @throws Exception the exception
     */
    @Test
    public void parseXport() throws Exception {
        RrdXport xport = JaxbUtils.unmarshal(RrdXport.class, new File("src/test/resources/rrd-xport.xml"));
        Assert.assertNotNull(xport);
        Assert.assertEquals(new Long(300), xport.getMeta().getStep());
        Assert.assertEquals(new Long(1206312900), xport.getMeta().getStart());
        Assert.assertEquals(new Long(1206316500), xport.getMeta().getEnd());
        Assert.assertEquals("load average 5min", xport.getMeta().getLegends().get(0));
        Assert.assertEquals(new Long(1206312900), xport.getRows().get(0).getTimestamp());
        Assert.assertEquals(new Double(19.86), xport.getRows().get(0).getValues().get(0));
    }
}
