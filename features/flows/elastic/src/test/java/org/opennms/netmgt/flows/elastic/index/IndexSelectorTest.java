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

package org.opennms.netmgt.flows.elastic.index;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;

public class IndexSelectorTest {


    @Test
    public void indexesForSingleTimeRange() throws ParseException {

        Check.strategy(IndexStrategy.HOURLY)
                .from("2018-02-03 11:03").to("2018-02-03 13:04")
                .expected("prefix-2018-02-03-11"
                        , "prefix-2018-02-03-12"
                        , "prefix-2018-02-03-13")
                .check();

        Check.strategy(IndexStrategy.DAILY)
                .from("2018-02-02 12:00").to("2018-02-04 15:00")
                .expected("prefix-2018-02-02"
                        , "prefix-2018-02-03"
                        , "prefix-2018-02-04")
                .check();

        Check.strategy(IndexStrategy.MONTHLY)
                .from("2018-02-03 12:00").to("2018-02-28 15:00")
                .expected("prefix-2018-02")
                .check();

        Check.strategy(IndexStrategy.YEARLY)
                .from("2017-02-03 12:00").to("2019-02-28 15:00")
                .expected("prefix-2017"
                        , "prefix-2018"
                        , "prefix-2019")
                .check();
    }

    @Test
    public void indexesForMultipleTimeRange() throws ParseException {

        Check.strategy(IndexStrategy.HOURLY)
                .from("2018-02-03 11:03").to("2018-02-03 16:01")
                .expected("prefix-2018-02-03-11"
                        , "prefix-2018-02-03-12"
                        , "prefix-2018-02-03-13"
                        , "prefix-2018-02-03-14"
                        , "prefix-2018-02-03-15"
                        , "prefix-2018-02-03-16")
                .check();

        Check.strategy(IndexStrategy.DAILY)
                .from("2018-02-02 12:00").to("2018-02-05 15:00")
                .expected("prefix-2018-02-02"
                        , "prefix-2018-02-03"
                        , "prefix-2018-02-04"
                        , "prefix-2018-02-05")
                .check();

        Check.strategy(IndexStrategy.MONTHLY)
                .from("2018-01-03 12:00").to("2018-04-04 15:00")
                .expected("prefix-2018-01"
                        , "prefix-2018-02"
                        , "prefix-2018-03"
                        , "prefix-2018-04")
                .check();

        Check.strategy(IndexStrategy.YEARLY)
                .from("2015-02-03 12:00").to("2019-02-04 15:00")
                .expected("prefix-2015"
                        , "prefix-2016"
                        , "prefix-2017"
                        , "prefix-2018"
                        , "prefix-2019")
                .check();
    }

    @Test
    public void shouldGiveMultipleIndexesForTimeRangeWithCollapseInMiddle() throws ParseException {

        Check.strategy(IndexStrategy.HOURLY)
                .from("2018-02-02 22:03").to("2018-02-04 02:03")
                .expected("prefix-2018-02-02-22"
                        , "prefix-2018-02-02-23"
                        , "prefix-2018-02-03-*"
                        , "prefix-2018-02-04-00"
                        , "prefix-2018-02-04-01"
                        , "prefix-2018-02-04-02")
                .check();

        Check.strategy(IndexStrategy.DAILY)
                .from("2018-01-31 12:00").to("2018-03-02 15:00")
                .expected("prefix-2018-01-31"
                        , "prefix-2018-02-*"
                        , "prefix-2018-03-01"
                        , "prefix-2018-03-02")
                .check();

        Check.strategy(IndexStrategy.MONTHLY)
                .from("2016-12-03 12:00").to("2018-02-03 15:00")
                .expected("prefix-2016-12"
                        , "prefix-2017-*"
                        , "prefix-2018-01"
                        , "prefix-2018-02")
                .check();

        Check.strategy(IndexStrategy.YEARLY)
                .from("2016-02-03 12:00").to("2018-02-04 15:00")
                .expected("prefix-2016"
                        , "prefix-2017"
                        , "prefix-2018")
                .check();
    }

    @Test
    public void indexesForTimeRangeThatBordersYears() throws ParseException {

        Check.strategy(IndexStrategy.HOURLY)
                .from("2018-01-01 00:03").to("2018-01-01 02:03")
                .expected("prefix-2018-01-01-00"
                        , "prefix-2018-01-01-01"
                        , "prefix-2018-01-01-02")
                .check();

        Check.strategy(IndexStrategy.HOURLY)
                .from("2018-12-31 20:03").to("2018-12-31 23:03")
                .expected("prefix-2018-12-31-20"
                        , "prefix-2018-12-31-21"
                        , "prefix-2018-12-31-22"
                        , "prefix-2018-12-31-23")
                .check();

        Check.strategy(IndexStrategy.DAILY)
                .from("2018-01-01 01:00").to("2018-01-03 02:03")
                .expected("prefix-2018-01-01"
                        , "prefix-2018-01-02"
                        , "prefix-2018-01-03")
                .check();

        Check.strategy(IndexStrategy.DAILY)
                .from("2018-12-30 21:00").to("2018-12-31 22:03")
                .expected("prefix-2018-12-30"
                        , "prefix-2018-12-31")
                .check();

        Check.strategy(IndexStrategy.MONTHLY)
                .from("2018-01-01 01:00").to("2018-03-03 02:03")
                .expected("prefix-2018-01"
                        , "prefix-2018-02"
                        , "prefix-2018-03")
                .check();

        Check.strategy(IndexStrategy.MONTHLY)
                .from("2018-11-29 21:00").to("2018-12-30 22:03")
                .expected("prefix-2018-11"
                        , "prefix-2018-12")
                .check();

        Check.strategy(IndexStrategy.YEARLY)
                .from("2016-02-03 12:00").to("2018-02-04 15:00")
                .expected("prefix-2016"
                        , "prefix-2017"
                        , "prefix-2018")
                .check();
    }

    @Test
    public void shouldGiveCollapsedIndexesForFullTimeRange() throws ParseException {

        Check.strategy(IndexStrategy.HOURLY)
                .from("2018-02-02 01:00").to("2018-02-04 23:03")
                .expected("prefix-2018-02-02-*"
                        , "prefix-2018-02-03-*"
                        , "prefix-2018-02-04-*")
                .check();

        Check.strategy(IndexStrategy.DAILY)
                .from("2018-01-01 12:00").to("2018-03-31 12:01")
                .expected("prefix-2018-01-*"
                        , "prefix-2018-02-*"
                        , "prefix-2018-03-*")
                .check();

        Check.strategy(IndexStrategy.MONTHLY)
                .from("2016-01-01 12:00").to("2018-12-04 15:00")
                .expected("prefix-2016-*"
                        , "prefix-2017-*"
                        , "prefix-2018-*")
                .check();

        Check.strategy(IndexStrategy.YEARLY)
                .from("2014-02-03 12:00").to("2018-02-04 15:00")
                .expected("prefix-2014"
                        , "prefix-2015"
                        , "prefix-2016"
                        , "prefix-2017"
                        , "prefix-2018")
                .check();
    }

    @Test
    public void shouldGiveCollapsedIndexesForLayeredTimeRanges() throws ParseException {

        Check.strategy(IndexStrategy.HOURLY)
                .from("2015-11-29 23:05").to("2018-02-02 02:10")
                .expected("prefix-2015-11-29-23"
                        , "prefix-2015-11-30-*"
                        , "prefix-2015-12-*"
                        , "prefix-2016-*"
                        , "prefix-2017-*"
                        , "prefix-2018-01-*"
                        , "prefix-2018-02-01-*"
                        , "prefix-2018-02-02-00"
                        , "prefix-2018-02-02-01"
                        , "prefix-2018-02-02-02")
                .check();

        Check.strategy(IndexStrategy.DAILY)
                .from("2015-11-28 23:05").to("2018-02-01 22:59")
                .expected("prefix-2015-11-28"
                        , "prefix-2015-11-29"
                        , "prefix-2015-11-30"
                        , "prefix-2015-12-*"
                        , "prefix-2016-*"
                        , "prefix-2017-*"
                        , "prefix-2018-01-*"
                        , "prefix-2018-02-01")
                .check();

        Check.strategy(IndexStrategy.MONTHLY)
                .from("2015-12-29 22:05").to("2018-01-02 03:10")
                .expected("prefix-2015-12"
                        , "prefix-2016-*"
                        , "prefix-2017-*"
                        , "prefix-2018-01")
                .check();

        Check.strategy(IndexStrategy.YEARLY)
                .from("2016-11-29 22:05").to("2018-02-02 03:10")
                .expected("prefix-2016"
                        , "prefix-2017"
                        , "prefix-2018")
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
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date start = format.parse(from);
            Date end = format.parse(to);

            TimeRangeFilter filter = new TimeRangeFilter(start.getTime(), end.getTime());

            List<String> expectedList = Arrays.asList(expected);
            long expandTimeRangeInMs = 2 * 60 * 1000; // 2 min
            assertEquals(String.format("Test failed for strategy %s from %s to %s", this.strategy.name(), this.from, this.to)
                    , expectedList, new IndexSelector("prefix", strategy, expandTimeRangeInMs).getIndexNames(filter));
        }
    }
}