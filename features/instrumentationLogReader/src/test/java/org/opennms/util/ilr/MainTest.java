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
    @Test
    public void testOnms14() {
        setup("target/test-classes/instrumentation-1.14.log","-tc");
        assertEquals("TOTALCOLLECTS", main.getCollector().m_sortColumn.toString());
     }
}
