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
