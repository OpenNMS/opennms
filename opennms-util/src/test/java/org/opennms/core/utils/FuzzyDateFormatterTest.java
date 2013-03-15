/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.util.Date;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
public class FuzzyDateFormatterTest extends TestCase {
    protected Date now = new Date();

    public void testFuzzy() throws Exception {
        assertEquals("1 second",     FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - 1000L                       ), now));
        assertEquals("30 seconds",   FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - 30000L                      ), now));
        assertEquals("30 seconds",   FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - 30005L                      ), now));
        assertEquals("30 seconds",   FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - 30100L                      ), now));
        assertEquals("1 minute",     FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - 60000L                      ), now));
        assertEquals("2 minutes",    FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 90)                ), now));
        assertEquals("25 minutes",   FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 25)           ), now));
        assertEquals("1 hour",       FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60)           ), now));
        assertEquals("1 hour",       FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 72)           ), now));
        assertEquals("2 hours",      FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 120)          ), now));
        assertEquals("1 day",        FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24)      ), now));
        assertEquals("2 days",       FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 36)      ), now));
        assertEquals("2 weeks",      FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 14) ), now));
        assertEquals("2 weeks",      FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 17) ), now));
        assertEquals("1 month",      FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 30) ), now));
        
        Double realMonth = (1000 * 60 * 60 * 24 * 365.0 / 12.0);
        
        assertEquals("2 months",     FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (realMonth.longValue() * 2) ), now));
        assertEquals("3 months",     FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (Double.valueOf(realMonth * 2.5)).longValue() ), now));
        
        // why is this not 8.0?
        assertEquals("8 months",     FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (Double.valueOf(realMonth * 8)).longValue() ), now));
        
        assertEquals("1 year",       FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 365) ), now));
        assertEquals("2 years",      FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 548) ), now));
        assertEquals("2 years",      FuzzyDateFormatter.calculateDifference(new Date(now.getTime() - (1000L * 60 * 60 * 24 * 730) ), now));
    }
}
