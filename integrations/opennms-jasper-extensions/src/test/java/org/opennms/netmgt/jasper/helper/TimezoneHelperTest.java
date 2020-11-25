/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
