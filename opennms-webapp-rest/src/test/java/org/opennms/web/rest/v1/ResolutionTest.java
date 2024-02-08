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
package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.opennms.web.rest.v1.support.TimeChunker;


public class ResolutionTest {

    @Test
    public void testResolution() {
        Date startDate = new Date(new Date().getTime() - 300000);
        long endTime = startDate.getTime() + 300000;
        
        TimeChunker resolution = new TimeChunker(TimeChunker.MINUTE, startDate, new Date(endTime));
        
        assertEquals(1, resolution.getSegmentCount());
        Date startDate1 = resolution.getNextSegment().getStartDate();
        while(resolution.hasNext()) {
            System.err.println("startDate segment1: " + startDate1);
            assertEquals(startDate, startDate1);
        }
    }
    
    @Test
    public void testGetTimeIndex() {
        Date startDate = new Date(new Date().getTime() - 300000);
        long endTime = startDate.getTime() + 300000;
        
        TimeChunker resolution = new TimeChunker(60000, startDate, new Date(endTime));
        Date date = new Date(startDate.getTime() + 150000);
        assertEquals(2, resolution.getIndexContaining(date));
        
    }
}
