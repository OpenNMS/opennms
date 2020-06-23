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
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.rrd.model.Row;
import org.opennms.netmgt.rrd.model.v3.RRDv3;
import org.opennms.netmgt.rrd.model.v3.RRA;

/**
 * The Class RRD Merging Test.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class RrdMergeIT {

    /**
     * Test RRD merge.
     * <p>Both test XML contains data from different range of times, and the value is always increasing.</p>
     * 
     * @throws Exception the exception
     */
    @Test
    public void testRrdMerge() throws Exception {
        RRDv3 tempA = JaxbUtils.unmarshal(RRDv3.class, new File("src/test/resources/rrd-tempA-rrd.xml"));
        RRDv3 tempB = JaxbUtils.unmarshal(RRDv3.class, new File("src/test/resources/rrd-tempB-rrd.xml"));

        // Retrieve a list of the time stamps of the rows with data from tempA.rrd
        // Verify the max value
        Double value = Double.NEGATIVE_INFINITY;
        List<Long> timestampsA = new ArrayList<>();
        for (RRA rra : tempA.getRras()) {
            for (Row row : rra.getRows()) {
                if (!row.isNan()) {
                    timestampsA.add(tempA.findTimestampByRow(rra, row));
                    Double current = row.getValues().get(0);
                    if (current > value) {
                        value = current;
                    }
                }
            }
        }
        Assert.assertEquals(new Double(3.0), value);

        // Retrieve a list of the time stamps of the rows with data from tempB.rrd
        value = Double.NEGATIVE_INFINITY;
        List<Long> timestampsB = new ArrayList<>();
        for (RRA rra : tempB.getRras()) {
            for (Row row : rra.getRows()) {
                if (!row.isNan()) {
                    timestampsB.add(tempB.findTimestampByRow(rra, row));
                    Double current = row.getValues().get(0);
                    if (current > value) {
                        value = current;
                    }
                }
            }
        }
        Assert.assertEquals(new Double(18.0), value);

        // Verify that all the timestamps on timestampsA are different than the timestamps from timestampsB
        for (Long l : timestampsA) {
            if (timestampsB.contains(l)) {
                Assert.fail("The timestampsB should not contain any timestamp from timestampsA");
            }
        }
        for (Long l : timestampsB) {
            if (timestampsA.contains(l)) {
                Assert.fail("The timestampsA should not contain any timestamp from timestampsB");
            }
        }

        // Perform the Merge Operation, merging the data from tempA.rrd to tempB.rrd
        tempB.merge(tempA);

        // Retrieve the list of the non NaN rows from the updated tempB.rrd
        value = Double.NEGATIVE_INFINITY;
        List<Long> timestampsFinal = new ArrayList<>();
        for (RRA rra : tempB.getRras()) {
            for (Row row : rra.getRows()) {
                if (!row.isNan()) {
                    timestampsFinal.add(tempB.findTimestampByRow(rra, row));
                    Double current = row.getValues().get(0);
                    if (current > value) {
                        value = current;
                    }
                }
            }
        }
        Assert.assertEquals(new Double(18.0), value);

        // Verify that timestampsFinal contains timestampsA and timestampsB
        Assert.assertTrue(timestampsFinal.containsAll(timestampsA));
        Assert.assertTrue(timestampsFinal.containsAll(timestampsB));
    }

}
