/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.util.ilr;

import static org.junit.Assert.*;
import org.opennms.util.ilr.Main;
import org.junit.Test;

public class MainTest {
    Main main = new Main();
    public void setup(String testFile, String sortFlag) {
        String [] args = new String[2];
        args[0] = testFile;
        args[1] = sortFlag;
        main.execute(args, System.out);
    }
    @Test
    public void testParseTotalCollectionSortFlag() {
        setup("target/test-classes/TestLogFile.log", "-tc");
        assertEquals("TOTALCOLLECTS", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseAverageCollectionTime() {
        setup("target/test-classes/TestLogFile.log", "-act");
        assertEquals("AVGCOLLECTTIME", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseTotalCollectionTime(){
        setup("target/test-classes/TestLogFile.log", "-tct");
        assertEquals("TOTALCOLLECTTIME", main.getCollector().m_sortColumn.toString());
    }
    @Test
    public void testParseAverageTimeBetweenCollection() {
        setup("target/test-classes/TestLogFile.log","-atbc");
        assertEquals("AVGTIMEBETWEENCOLLECTS", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseAverageSuccessfulCollectionTime() {
        setup("target/test-classes/TestLogFile.log","-asct");
        assertEquals("AVGSUCCESSCOLLECTTIME", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseSuccessfulPercentage() {
        setup("target/test-classes/TestLogFile.log","-sp");
        assertEquals("SUCCESSPERCENTAGE", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseAverageUnsuccessfulCollectionTime() {
        setup("target/test-classes/TestLogFile.log","-auct");
        assertEquals("AVGUNSUCCESSCOLLECTTIME", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseUnsuccessfulPercentage() {
        setup("target/test-classes/TestLogFile.log","-up");
        assertEquals("UNSUCCESSPERCENTAGE", main.getCollector().m_sortColumn.toString());
     }
    @Test
    public void testParseTotalPersistTime() {
        setup("target/test-classes/TestLogFile.log","-tpt");
        assertEquals("TOTALPERSISTTIME", main.getCollector().m_sortColumn.toString());
     }
}
