package org.opennms.core.utils;

import java.util.Date;

import junit.framework.TestCase;

public class FuzzyDateFormatterTest extends TestCase {
    protected Date now = new Date();

    public void testFuzzy() throws Exception {
        assertEquals("1 second",     FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - 1000L                       ), now));
        assertEquals("30 seconds",   FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - 30000L                      ), now));
        assertEquals("30 seconds",   FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - 30005L                      ), now));
        assertEquals("30.1 seconds", FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - 30100L                      ), now));
        assertEquals("1 minute",     FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - 60000L                      ), now));
        assertEquals("1.5 minutes",  FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 90)                ), now));
        assertEquals("25 minutes",   FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 25)           ), now));
        assertEquals("1 hour",       FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60)           ), now));
        assertEquals("1.2 hours",    FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 72)           ), now));
        assertEquals("2 hours",      FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 120)          ), now));
        assertEquals("1 day",        FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24)      ), now));
        assertEquals("1.5 days",     FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 36)      ), now));
        assertEquals("2 weeks",      FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 14) ), now));
        assertEquals("2.4 weeks",    FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 17) ), now));
        assertEquals("1 month",      FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 30) ), now));
        
        Double realMonth = (1000 * 60 * 60 * 24 * 365.0 / 12.0);
        
        assertEquals("2 months",     FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (realMonth.longValue() * 2) ), now));
        assertEquals("2.5 months",   FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (new Double(realMonth * 2.5)).longValue() ), now));
        
        // why is this not 8.0?
        assertEquals("8.1 months",   FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (new Double(realMonth * 8)).longValue() ), now));
        
        assertEquals("1 year",       FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 365) ), now));
        assertEquals("1.5 years",    FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 547) ), now));
        assertEquals("2 years",      FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 730) ), now));
    }
}
