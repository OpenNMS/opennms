/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.opennms.core.utils.TimeSeries;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;

public class IndexSelectorTest {


    @Test
    public void shouldGiveNoIndexesForTimeRange0() throws ParseException {

        Check.strategy(IndexStrategy.HOURLY)
                .from("2018-02-03 12:00").to("2018-02-03 12:00")
                .expected()
                .check();

        Check.strategy(IndexStrategy.DAILY)
                .from("2018-02-03 12:00").to("2018-02-03 12:00")
                .expected()
                .check();

        Check.strategy(IndexStrategy.MONTHLY)
                .from("2018-02-03 12:00").to("2018-02-03 12:00")
                .expected()
                .check();

        Check.strategy(IndexStrategy.YEARLY)
                .from("2018-02-03 12:00").to("2018-02-03 12:00")
                .expected()
                .check();
    }


    @Test
    public void shouldGiveOneIndexesForTimeRange() throws ParseException {

        Check.strategy(IndexStrategy.HOURLY)
                .from("2018-02-03 12:03").to("2018-02-03 12:18")
                .expected("prefix-2018-02-03-12")
                .check();

        Check.strategy(IndexStrategy.DAILY)
                .from("2018-02-03 12:00").to("2018-02-03 15:00")
                .expected("prefix-2018-02-03")
                .check();

        Check.strategy(IndexStrategy.MONTHLY)
                .from("2018-02-03 12:00").to("2018-02-28 15:00")
                .expected("prefix-2018-02")
                .check();

        Check.strategy(IndexStrategy.YEARLY)
                .from("2018-02-03 12:00").to("2018-02-28 15:00")
                .expected("prefix-2018")
                .check();

    }

    @Test
    public void shouldGiveMultipleIndexesForTimeRange() throws ParseException {

        Check.strategy(IndexStrategy.HOURLY)
                .from("2018-02-03 12:00").to("2018-02-03 15:03")
                .expected("prefix-2018-02-03-12", "prefix-2018-02-03-13", "prefix-2018-02-03-14", "prefix-2018-02-03-15")
                .check();

        Check.strategy(IndexStrategy.DAILY)
                .from("2018-02-03 12:00").to("2018-02-04 15:00")
                .expected("prefix-2018-02-03", "prefix-2018-02-04")
                .check();

        Check.strategy(IndexStrategy.MONTHLY)
                .from("2018-02-03 12:00").to("2018-03-04 15:00")
                .expected("prefix-2018-02", "prefix-2018-03")
                .check();

        Check.strategy(IndexStrategy.YEARLY)
                .from("2016-02-03 12:00").to("2018-02-04 15:00")
                .expected("prefix-2016", "prefix-2017", "prefix-2018")
                .check();
    }

    private static class Check {
        IndexStrategy strategy;
        String from;
        String to;
        String[] expected;

        private Check(IndexStrategy strategy){
            this.strategy = strategy;
        }

        static Check strategy(IndexStrategy strategy){
            return new Check(strategy);
        }

        Check from(String from){
            this.from = from;
            return this;
        }

        Check to(String to) {
            this.to = to;
            return this;
        }

        Check expected(String ... expected){
            this.expected = expected;
            return this;
        }
        void check() throws ParseException {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date start = format.parse(from);
            Date end = format.parse(to);

            TimeRangeFilter filter = new TimeRangeFilter(start.getTime(), end.getTime());

            List<String> expectedList = Arrays.asList(expected);
            assertEquals(String.format("Test failed for strategy %s from %s to %s", this.strategy.name(), this.from, this.to)
                    , expectedList, new IndexSelector("prefix", strategy).getIndexNames(filter));
        }
    }
}