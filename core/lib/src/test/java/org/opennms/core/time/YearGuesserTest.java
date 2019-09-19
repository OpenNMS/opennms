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
