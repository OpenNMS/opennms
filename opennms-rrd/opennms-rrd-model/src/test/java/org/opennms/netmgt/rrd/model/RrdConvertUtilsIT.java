/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

