/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.jrobin;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jrobin.core.FetchData;
import org.jrobin.core.RrdDb;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class JRB Merging Test.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class JrbMergeTest {

    /** The source JRB. */
    private File source;

    /** The destination JRB. */
    private File dest;

    /**
     * Sets up the test
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        File tempSource = new File("src/test/resources/tempA.jrb");
        File tempDest   = new File("src/test/resources/tempB.jrb");

        source = new File("target/tempA.jrb");
        dest   = new File("target/tempB.jrb");

        FileUtils.copyFile(tempSource, source);
        FileUtils.copyFile(tempDest, dest);

        Assert.assertTrue(source.exists());
        Assert.assertTrue(dest.exists());
    }

    /**
     * Test JRB merge.
     * <p>Both test XML contains data from different range of times, and the value is always increasing.</p>
     * 
     * @throws Exception the exception
     */
    @Test
    public void testRrdMerge() throws Exception {
        RrdMerge merge = new RrdMerge();
        File outputFile = merge.mergeJrbs(source, dest);
        Assert.assertTrue(outputFile.exists());

        long start = 1381488900l;
        long end   = 1381514100;

        RrdDb rrd = new RrdDb(source, true);
        FetchData data = rrd.createFetchRequest("AVERAGE", start, end).fetchData();
        Double min = data.getAggregate("temp", "MIN");
        Double max = data.getAggregate("temp", "MAX");
        rrd.close();
        Assert.assertEquals(new Double(-10.0), min);
        Assert.assertEquals(new Double(3.0), max);

        rrd = new RrdDb(dest, true);
        data = rrd.createFetchRequest("AVERAGE", start, end).fetchData();
        min = data.getAggregate("temp", "MIN");
        max = data.getAggregate("temp", "MAX");
        rrd.close();
        Assert.assertEquals(new Double(5.0), min);
        Assert.assertEquals(new Double(18.0), max);

        rrd = new RrdDb(outputFile, true);
        data = rrd.createFetchRequest("AVERAGE", start, end).fetchData();
        min = data.getAggregate("temp", "MIN");
        max = data.getAggregate("temp", "MAX");
        rrd.close();
        Assert.assertEquals(new Double(-10.0), min);
        Assert.assertEquals(new Double(18.0), max);
    }

}
