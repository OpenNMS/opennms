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


import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Test;

public class TimezoneHelperTest {
    private static final ZoneId LA = ZoneId.of("America/Los_Angeles");

    @Test
    public void verifyTimezoneAdjustment() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = sdf.parse("2020-11-09 02:00");

        long epoch = TimezoneHelper.getRezonedEpoch(date, LA);
        assertEquals(1604916000000l, epoch);
    }

    @Test
    public void verifyFormatDate() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = sdf.parse("2020-11-09 02:00");

        final String formatted = TimezoneHelper.formatDate(date, LA, "EEE dd MMM yyyy HH:mm:ss");
        assertEquals("Mon 09 Nov 2020 02:00:00", formatted);
    }
}
