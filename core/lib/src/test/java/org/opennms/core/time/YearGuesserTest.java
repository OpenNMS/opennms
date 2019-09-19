/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.time;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;

import org.junit.Test;

public class YearGuesserTest {

    @Test
    public void shouldGuessTheYearRight() {
        test("0000-02-02T10:10:10", "2019-02-02T10:10:10", 2019);
        test("0000-01-01T01:01:10", "2019-12-31T23:59:10", 2020);
        test("0000-12-31T23:59:10", "2020-01-01T01:01:10", 2019);
        test("0000-07-01T01:01:10", "2019-06-30T23:59:10", 2019);
        test("0000-06-30T23:59:10", "2019-07-01T01:01:10", 2019);
        test("1970-06-30T23:59:10", "2019-07-01T01:01:10", 2019);
    }

    @Test
    public void shouldUseTheYearWhichWasGiven() {
        test("2000-04-04T10:10:10", "2019-04-04T10:10:10", 2000);
        test("2000-01-01T01:01:10", "2019-12-31T23:59:10", 2000);
        test("2000-12-31T23:59:10", "2020-01-01T01:01:10", 2000);
        test("2000-07-01T01:01:10", "2019-06-30T23:59:10", 2000);
        test("2000-06-30T23:59:10", "2019-07-01T01:01:10", 2000);
    }

    private void test(final String syslogDate, final String referenceDate, final int expectedYear) {
        LocalDateTime reference = LocalDateTime.parse(referenceDate);
        LocalDateTime dateWithoutYear = LocalDateTime.parse(syslogDate);
        LocalDateTime dateWithYear = YearGuesser.guessYearForDate(dateWithoutYear, reference);
        assertEquals(expectedYear, dateWithYear.getYear());
    }
}
