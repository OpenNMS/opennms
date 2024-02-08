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
import org.opennms.netmgt.rrd.model.RrdConvertUtils;
import org.opennms.netmgt.rrd.model.v1.RRDv1;
import org.opennms.netmgt.rrd.model.v3.RRDv3;

/**
 * The Test Class for RrdConvertUtils.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class RrdConvertUtilsIT {

    /**
     * Test JRobin parse.
     *
     * @throws Exception the exception
     */
    @Test
    public void testJrobinParse() throws Exception {
        RRDv1 jrb = RrdConvertUtils.dumpJrb(new File("src/test/resources/tempA.jrb"));
        Assert.assertNotNull(jrb);

    }

    /**
     * Test JRobin restore.
     *
     * @throws Exception the exception
     */
    @Test
    public void testJrobinRestore() throws Exception {
        RRDv1 jrb = RrdConvertUtils.dumpJrb(new File("src/test/resources/tempA.jrb"));
        File target = new File("target/tempA-converted.jrb");
        RrdConvertUtils.restoreJrb(jrb, target);
        Assert.assertTrue(target.exists());
    }

    /**
     * Test convert JRB into RRD.
     *
     * @throws Exception the exception
     */
    @Test
    public void testConvertJrbIntoRrd() throws Exception {
        RRDv1 jrb = RrdConvertUtils.dumpJrb(new File("src/test/resources/tempA.jrb"));
        RRDv3 rrd = RrdConvertUtils.convert(jrb);
        Assert.assertNotNull(rrd);
    }

    /**
     * Test convert RRD into JRB.
     *
     * @throws Exception the exception
     */
    @Test
    public void testConvertRrdIntoJrb() throws Exception {
        RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, new File("src/test/resources/rrd-dump.xml"));
        RRDv1 jrb = RrdConvertUtils.convert(rrd);
        Assert.assertNotNull(jrb);
    }

    /**
     * Test convert Advanced RRD into JRB (1).
     *
     * @throws Exception the exception
     */
    @Test(expected=IllegalArgumentException.class)
    public void testConvertAdvRrdIntoJrb1() throws Exception {
        RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, new File("src/test/resources/rrd-dump-compute-ds.xml"));
        RRDv1 jrb = RrdConvertUtils.convert(rrd);
        Assert.assertNull(jrb);
    }

    /**
     * Test convert Advanced RRD into JRB (2).
     *
     * @throws Exception the exception
     */
    @Test(expected=IllegalArgumentException.class)
    public void testConvertAdvRrdIntoJrb2() throws Exception {
        RRDv3 rrd = JaxbUtils.unmarshal(RRDv3.class, new File("src/test/resources/rrd-dump-aberrant-behavior-detection.xml"));
        RRDv1 jrb = RrdConvertUtils.convert(rrd);
        Assert.assertNull(jrb);
    }

}

