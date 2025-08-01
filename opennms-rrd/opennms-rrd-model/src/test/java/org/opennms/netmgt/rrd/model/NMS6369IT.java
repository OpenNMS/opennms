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
import org.opennms.netmgt.rrd.model.v1.RRDv1;

/**
 * The Test Class for NMS6369.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class NMS6369IT {

    /**
     * Test JRobin parse.
     *
     * @throws Exception the exception
     */
    @Test
    public void testJrobinParse() throws Exception {
        RRDv1 rrd = RrdConvertUtils.dumpJrb(new File("src/test/resources/mib2-interfaces.jrb"));
        Assert.assertNotNull(rrd);
        File target = new File("target/mib2-interfaces.jrb");
        RrdConvertUtils.restoreJrb(rrd, target);
        Assert.assertTrue(target.exists());
    }

}

