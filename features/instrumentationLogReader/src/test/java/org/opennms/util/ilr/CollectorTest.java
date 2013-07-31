/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;




import org.junit.Ignore;
import org.junit.Test;
import org.opennms.util.ilr.Collector;
import org.opennms.util.ilr.Collector.SortColumn;
import org.opennms.util.ilr.Collector.SortOrder;


public class CollectorTest {
    private Date getDate(String dateString) throws ParseException {
        return new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss,S").parse(dateString);
    }
    @Test
    public void testStartNotSetEnd() throws ParseException {
        Collector c = new Collector();
        c.addLog("2010-05-26 12:12:38,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: end:24/216.216.217.254/SNMP");	
        c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: begin:24/216.216.217.254/SNMP");
        c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: end:24/216.216.217.254/SNMP");
        c.addLog("2010-05-26 12:12:50,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: begin:24/216.216.217.254/SNMP");
        assertEquals(getDate("2010-05-26 12:12:40,883"), c.getStartTime());
        assertEquals(getDate("2010-05-26 12:12:48,027"), c.getEndTime());
        assertEquals(7144, c.getDuration());
    }
    @Test
    public void testStartAndEndTime() throws ParseException {
        Collector c = new Collector();
        c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: begin:24/216.216.217.254/SNMP");
        c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: end:24/216.216.217.254/SNMP");	

        assertEquals(getDate("2010-05-26 12:12:40,883"), c.getStartTime());
        assertEquals(getDate("2010-05-26 12:12:48,027"), c.getEndTime());
        assertEquals(7144, c.getDuration());
    }
    @Test
    public void testServiceCount(){
        Collector c = new Collector();
        assertEquals(0, c.getServiceCount());
        c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: begin:24/216.216.217.254/SNMP");
        c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: end:24/216.216.217.254/SNMP");
        assertEquals(1, c.getServiceCount());
        c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: begin: 19/209.61.128.9/SNMP");
        c.addLog("2010-06-01 09:36:28,995 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: end: 19/209.61.128.9/SNMP");
        c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: begin:24/216.216.217.254/SNMP");
        c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: end:24/216.216.217.254/SNMP");
        c.addLog("2010-06-01 09:36:02,541 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: end:7/172.20.1.12/SNMP");
        c.addLog("2010-06-01 09:36:02,542 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: begin:27/172.20.1.6/SNMP");
        c.addLog("2010-06-01 09:36:02,544 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: begin:27/172.20.1.6/SNMP");
        c.addLog("2010-06-01 09:36:03,508 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: end:27/172.20.1.6/SNMP");
        c.addLog("2010-06-01 09:36:02,541 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: error: 7/172.20.1.12/SNMP: org.opennms.netmgt.collectd.CollectionTimedOut: Timeout retrieving SnmpCollectors for 172.20.1.12 for kenny.internal.opennms.com/172.20.1.12: SnmpCollectors for 172.20.1.12: snmpTimeoutError for: kenny.internal.opennms.com/172.20.1.12");
        c.addLog("2010-06-01 09:36:27,644 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: begin:19/209.61.128.9/SNMP");
        c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: end:19/209.61.128.9/SNMP");
        c.addLog("2010-06-01 09:33:56,292 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: begin:83/172.20.1.15/SNMP");
        c.addLog("2010-06-01 09:33:56,440 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: end:83/172.20.1.15/SNMP");
        assertEquals(5, c.getServiceCount());
    }

    @Test
    public void testThreadcount(){
        Collector c = new Collector();
        assertEquals(0, c.getThreadCount());
        c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: begin:24/216.216.217.254/SNMP");
        c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: end:24/216.216.217.254/SNMP");
        assertEquals(1, c.getThreadCount());
        c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: begin: 19/209.61.128.9/SNMP");
        c.addLog("2010-06-01 09:36:28,995 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: end: 19/209.61.128.9/SNMP");
        c.addLog("2010-06-01 09:33:31,964 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: begin:60/172.20.1.202/SNMP");
        c.addLog("2010-06-01 09:33:32,477 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: end:60/172.20.1.202/SNMP");
        c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: begin: 86/172.20.1.25/WMI");
        c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: end: 86/172.20.1.25/WMI");
        c.addLog("2010-06-01 08:39:46,648 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
        c.addLog("2010-06-01 08:39:46,650 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
        assertEquals(5, c.getThreadCount());
    }

    @Test 
    public void testCollectionsPerService() {
        Collector c = new Collector();
        c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: begin:24/216.216.217.254/SNMP");
        c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: end:24/216.216.217.254/SNMP");
        c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: begin: 19/209.61.128.9/SNMP");
        c.addLog("2010-06-01 09:36:28,995 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: end: 19/209.61.128.9/SNMP");
        c.addLog("2010-06-01 09:33:31,964 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: begin:60/172.20.1.202/SNMP");
        c.addLog("2010-06-01 09:33:32,477 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: end:60/172.20.1.202/SNMP");
        c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: begin: 86/172.20.1.25/WMI");
        c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: end: 86/172.20.1.25/WMI");
        c.addLog("2010-06-01 08:39:46,648 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
        c.addLog("2010-06-01 08:39:46,650 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
        assertEquals(1,c.getCollectionsPerService("24/216.216.217.254/SNMP"));
        assertEquals(0,c.getCollectionsPerService("19/209.61.128.9/SNMP"));
        assertEquals(1,c.getCollectionsPerService("60/172.20.1.202/SNMP"));
        assertEquals(0,c.getCollectionsPerService("86/172.20.1.25/WMI"));
        assertEquals(0,c.getCollectionsPerService("58/172.20.1.201/SNMP"));
    }
    @Test
    public void testAverageCollectionTimePerService() {
        Collector c = new Collector();
        c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: begin:24/216.216.217.254/SNMP");
        c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: end:24/216.216.217.254/SNMP");
        c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: begin: 19/209.61.128.9/SNMP");
        c.addLog("2010-06-01 09:36:28,995 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: end: 19/209.61.128.9/SNMP");
        c.addLog("2010-06-01 09:33:31,964 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: begin:60/172.20.1.202/SNMP");
        c.addLog("2010-06-01 09:33:32,477 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: end:60/172.20.1.202/SNMP");
        c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: begin: 86/172.20.1.25/WMI");
        c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: end: 86/172.20.1.25/WMI");
        c.addLog("2010-06-01 08:39:46,648 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
        c.addLog("2010-06-01 08:39:46,650 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
        assertEquals(7144,c.getAverageCollectionTimePerService("24/216.216.217.254/SNMP"));
        assertEquals(0,c.getAverageCollectionTimePerService("19/209.61.128.9/SNMP"));
        assertEquals(513,c.getAverageCollectionTimePerService("60/172.20.1.202/SNMP"));
        assertEquals(0,c.getAverageCollectionTimePerService("86/172.20.1.25/WMI"));
        assertEquals(0,c.getAverageCollectionTimePerService("58/172.20.1.201/SNMP"));
    }
    @Test
    public void testTotalCollectionTimePerService() {
        Collector c = new Collector();
        c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: begin:24/216.216.217.254/SNMP");
        c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: end:24/216.216.217.254/SNMP");
        c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: begin: 19/209.61.128.9/SNMP");
        c.addLog("2010-06-01 09:36:28,995 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: end: 19/209.61.128.9/SNMP");
        c.addLog("2010-06-01 09:33:31,964 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: begin:60/172.20.1.202/SNMP");
        c.addLog("2010-06-01 09:33:32,477 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: end:60/172.20.1.202/SNMP");
        c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: begin: 86/172.20.1.25/WMI");
        c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: end: 86/172.20.1.25/WMI");
        c.addLog("2010-06-01 08:39:46,648 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
        c.addLog("2010-06-01 08:39:46,650 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
        assertEquals(7144,c.getAverageCollectionTimePerService("24/216.216.217.254/SNMP"));
        assertEquals(0,c.getAverageCollectionTimePerService("19/209.61.128.9/SNMP"));
        assertEquals(513,c.getAverageCollectionTimePerService("60/172.20.1.202/SNMP"));
        assertEquals(0,c.getAverageCollectionTimePerService("86/172.20.1.25/WMI"));
        assertEquals(0,c.getAverageCollectionTimePerService("58/172.20.1.201/SNMP"));
    }

    @Test
    public void testTotalCollectionTime() {
        Collector c = new Collector();
        c.addLog("2010-03-13 02:20:30,525 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:32028/209.219.9.78/SNMP");
        c.addLog("2010-03-13 02:21:09,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:32028/209.219.9.78/SNMP");
        c.addLog("2010-03-13 02:32:42,641 DEBUG [CollectdScheduler-400 Pool-fiber25] Collectd: collector.collect: begin:32028/209.219.9.78/SNMP");
        c.addLog("2010-03-13 02:33:01,973 DEBUG [CollectdScheduler-400 Pool-fiber25] Collectd: collector.collect: end:32028/209.219.9.78/SNMP");
        c.addLog("2010-03-13 02:43:27,749 DEBUG [CollectdScheduler-400 Pool-fiber112] Collectd: collector.collect: begin:32028/209.219.9.78/SNMP");
        c.addLog("2010-03-13 02:43:31,517 DEBUG [CollectdScheduler-400 Pool-fiber112] Collectd: collector.collect: end:32028/209.219.9.78/SNMP");
        c.addLog("2010-03-13 02:54:13,334 DEBUG [CollectdScheduler-400 Pool-fiber166] Collectd: collector.collect: begin:32028/209.219.9.78/SNMP");
        c.addLog("2010-03-13 02:54:35,223 DEBUG [CollectdScheduler-400 Pool-fiber166] Collectd: collector.collect: end:32028/209.219.9.78/SNMP");
        c.addLog("2010-03-13 03:05:47,554 DEBUG [CollectdScheduler-400 Pool-fiber307] Collectd: collector.collect: begin:32028/209.219.9.78/SNMP");
        c.addLog("2010-03-13 03:06:28,926 DEBUG [CollectdScheduler-400 Pool-fiber307] Collectd: collector.collect: end:32028/209.219.9.78/SNMP");
        c.addLog("2010-03-13 03:18:27,559 DEBUG [CollectdScheduler-400 Pool-fiber264] Collectd: collector.collect: begin:32028/209.219.9.78/SNMP");
        c.addLog("2010-03-13 03:19:06,934 DEBUG [CollectdScheduler-400 Pool-fiber264] Collectd: collector.collect: end:32028/209.219.9.78/SNMP");
        assertEquals(39451+19332+3768+21889+41372+39375,c.getTotalCollectionTimePerService("32028/209.219.9.78/SNMP"));
    }

    @Test 
    public void testReadLogMessagesFromFile() throws IOException {
        Collector c = new Collector();
        c.readLogMessagesFromFile("target/test-classes/TestLogFile.log");
        assertEquals(7144,c.getAverageCollectionTimePerService("24/216.216.217.254/SNMP"));
        assertEquals(0,c.getAverageCollectionTimePerService("19/209.61.128.9/SNMP"));
        assertEquals(513,c.getAverageCollectionTimePerService("60/172.20.1.202/SNMP"));
        assertEquals(0,c.getAverageCollectionTimePerService("86/172.20.1.25/WMI"));
        assertEquals(0,c.getAverageCollectionTimePerService("58/172.20.1.201/SNMP"));
    }

    @Test
    public void testReadLogMessagesFromPostLog4jRefactorFile() throws Exception {
        final Collector c = new Collector();
        c.readLogMessagesFromFile("target/test-classes/instrumentation.log");
        final LogMessage logMessage = c.getFirstValidLogMessage();
        assertNotNull(logMessage);
        assertEquals("380/10.151.117.34/SNMP", logMessage.getServiceID());
        assertEquals(timestamp("2013-07-23 11:39:22,287"), logMessage.getDate());
        assertEquals("LegacyScheduler-Thread-20-of-50", logMessage.getThread());
    }

    @Test
    public void testPrintGlobalStats() throws IOException {
        Collector c = new Collector ();
        c.readLogMessagesFromFile("target/test-classes/TestLogFile.log");
        StringWriter out = new StringWriter();
        c.printGlobalStats(new PrintWriter(out, true));
        String actualOutput = out.toString(); 
        assertTrue(actualOutput.contains("Start Time: 2010-05-26 12:12:40,883"));
        assertTrue(actualOutput.contains("End Time: 2010-06-01 08:45:12,104"));
        assertTrue(actualOutput.contains("Duration: 5d20h32m31.221s"));
        assertTrue(actualOutput.contains("Total Services: 5"));
        assertTrue(actualOutput.contains("Threads Used: 5"));
    }
    @Test 
    public void testPrintServiceStats () throws IOException {
        Collector c = new Collector ();
        c.readLogMessagesFromFile("target/test-classes/TestLogFile.log");
        String expectedOutput = String.format(Collector.SERVICE_DATA_FORMAT, 
                                              "24/216.216.217.254/SNMP",
                                              "7.144s",
                                              1,
                                              "7.144s",
                                              100.0,
                                              "0s",
                                              0.0,
                                              "0s",
                                              "7.144s",
                                              "0s",
                                              "0s"
                );
        StringWriter out = new StringWriter();
        c.printServiceStats("24/216.216.217.254/SNMP", new PrintWriter(out, true));
        String actualOutput = out.toString();
        assertEquals(expectedOutput,actualOutput);
    }

    @Test
    public void testFormatDuration () {
        assertEquals("0s",Collector.formatDuration(0));
        assertEquals("3s",Collector.formatDuration(3000));
        assertEquals("7s",Collector.formatDuration(7000));
        assertEquals("2.345s",Collector.formatDuration(2345));
        assertEquals("1m",Collector.formatDuration(60000));
        assertEquals("2m",Collector.formatDuration(120000));
        assertEquals("2m3.456s",Collector.formatDuration(123456));
        assertEquals("2m0.456s",Collector.formatDuration(120456));
        assertEquals("1h",Collector.formatDuration(3600*1000));
        assertEquals("2h",Collector.formatDuration(2*3600*1000));
        assertEquals("1h1m1s",Collector.formatDuration(3600*1000+60000+1000));
        assertEquals("1h0m1s",Collector.formatDuration(3600*1000+1000));
        assertEquals("1d",Collector.formatDuration(3600*1000*24));
        assertEquals("1d0h0m0.001s",Collector.formatDuration(3600*1000*24+1));
    }
    @Ignore
    @Test
    public void testPrintReport() throws IOException{
        Collector c = new Collector();
        c.readLogMessagesFromFile("target/test-classes/TestLogFile.log");
        StringWriter out = new StringWriter ();
        c.printReport(new PrintWriter(out,true));
        String expectedOutput = fromFile("target/test-classes/TestLogFile.out");
        String actualOutput = out.toString();
        assertEquals(expectedOutput,actualOutput);
    }

    @Test
    public void testSortByAverageCollectionTime() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.AVGCOLLECTTIME);
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:40,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:20,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:50,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:50,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(new Duration(30000), collectors.get(0).getAverageCollectionDuration());
        assertEquals(new Duration(20000), collectors.get(1).getAverageCollectionDuration());
        assertEquals(new Duration(10000), collectors.get(2).getAverageCollectionDuration());


    }
    @Test
    public void testSortByTotalCollectionTime() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.AVGCOLLECTTIME);
        c.addLog("2010-03-13 02:21:10,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:20,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:50,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:20,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:25,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:20,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(new Duration(30000), collectors.get(0).getTotalCollectionDuration());
        assertEquals(new Duration(10000), collectors.get(1).getTotalCollectionDuration());
        assertEquals(new Duration(5000), collectors.get(2).getTotalCollectionDuration());


    }

    @Test
    public void testSortByTotalCollections() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.TOTALCOLLECTS);
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:31,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:32,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:33,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:33,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:34,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:34,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:35,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:35,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:36,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:37,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:38,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(3, collectors.get(0).getCollectionCount());
        assertEquals(2, collectors.get(1).getCollectionCount());
        assertEquals(1, collectors.get(2).getCollectionCount());


    }

    @Test
    public void testSortByAverageTimeBetweenCollections() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.AVGTIMEBETWEENCOLLECTS);
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:32,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:35,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:36,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:20,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:22,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:23,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:25,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:32,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:37,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:38,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(5000, collectors.get(0).getAverageTimeBetweenCollections());
        assertEquals(3000, collectors.get(1).getAverageTimeBetweenCollections());
        assertEquals(1000, collectors.get(2).getAverageTimeBetweenCollections());


    }

    @Test
    public void testSortBySuccessPercentage() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.SUCCESSPERCENTAGE);
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: error:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:40,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:20,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:30,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:31,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:35,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: error:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:41,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:50,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(100.0, collectors.get(0).getSuccessPercentage(),0);
        assertEquals(50.0, collectors.get(1).getSuccessPercentage(),0);
        assertEquals(0.0, collectors.get(2).getSuccessPercentage(),0);


    }
    @Test
    public void testSortBySuccessfulCollections() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.TOTALSUCCESSCOLLECTS);
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:40,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:41,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:42,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:20,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:50,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:31,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:32,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:33,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:34,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:35,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(3, collectors.get(0).getSuccessfulCollectionCount());
        assertEquals(2, collectors.get(1).getSuccessfulCollectionCount());
        assertEquals(1, collectors.get(2).getSuccessfulCollectionCount());

    }

    @Test
    public void testSortByAverageSuccessfulCollectionTime() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.AVGSUCCESSCOLLECTTIME);
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:40,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:20,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:50,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:50,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(new Duration(30000), collectors.get(0).getAverageCollectionDuration());
        assertEquals(new Duration(20000), collectors.get(1).getAverageCollectionDuration());
        assertEquals(new Duration(10000), collectors.get(2).getAverageCollectionDuration());


    }
    @Test
    public void testSortByUnsuccessfulCollections() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.TOTALUNSUCCESSCOLLECTS);
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: error:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:40,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:20,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:25,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: error:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:30,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:31,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:35,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: error:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:41,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:50,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(2, collectors.get(0).getErrorCollectionCount());
        assertEquals(1, collectors.get(1).getErrorCollectionCount());
        assertEquals(0, collectors.get(2).getErrorCollectionCount());


    }
    @Test
    public void testSortByTotalPersistTime() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.TOTALPERSISTTIME);
        c.addLog("2011-02-19 13:09:45,000 DEBUG [CollectdScheduler-200 Pool-fiber115] Collectd: collector.collect: persistDataQueueing: begin:0/1.1.1.1/SNMP");
        c.addLog("2011-02-19 13:09:46,000 DEBUG [CollectdScheduler-200 Pool-fiber175] Collectd: collector.collect: persistDataQueueing: end:0/1.1.1.1/SNMP");
        c.addLog("2011-02-19 13:09:47,000 DEBUG [CollectdScheduler-200 Pool-fiber115] Collectd: collector.collect: persistDataQueueing: begin:0/2.2.2.2/SNMP");
        c.addLog("2011-02-19 13:09:56,000 DEBUG [CollectdScheduler-200 Pool-fiber175] Collectd: collector.collect: persistDataQueueing: end:0/2.2.2.2/SNMP");
        c.addLog("2011-02-19 13:10:01,000 DEBUG [CollectdScheduler-200 Pool-fiber115] Collectd: collector.collect: persistDataQueueing: begin:0/3.3.3.3/SNMP");
        c.addLog("2011-02-19 13:10:03,000 DEBUG [CollectdScheduler-200 Pool-fiber175] Collectd: collector.collect: persistDataQueueing: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(9000, collectors.get(0).getTotalPersistTime());
        assertEquals(2000, collectors.get(1).getTotalPersistTime());
        assertEquals(1000, collectors.get(2).getTotalPersistTime());


    }


    @Test
    public void testSortByUnsuccessfulPercentage() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.UNSUCCESSPERCENTAGE);
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: error:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:40,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:20,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:30,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:31,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:35,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: error:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:41,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:50,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(100.0, collectors.get(0).getErrorPercentage(),0);
        assertEquals(50.0, collectors.get(1).getErrorPercentage(),0);
        assertEquals(0.0, collectors.get(2).getErrorPercentage(),0);


    }
    @Test
    public void testSortByAverageUnsuccessfulCollectionTime() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.AVGUNSUCCESSCOLLECTTIME);
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:35,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: error:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:40,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:20,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:25,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: error:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:40,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:31,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:35,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: error:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:46,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:24:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:24:50,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(17500, collectors.get(0).getAverageErrorCollectionTime());
        assertEquals(10000, collectors.get(1).getAverageErrorCollectionTime());
        assertEquals(0,  collectors.get(2).getAverageErrorCollectionTime());

    }

    @Test
    public void testSortByAverageCollectionTimeReversed() {

        Collector c = new Collector();
        c.setSortColumn(SortColumn.AVGCOLLECTTIME);
        c.setSortOrder(SortOrder.ASCENDING);
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:40,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:20,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:50,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:50,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
        List<ServiceCollector> collectors = c.getServiceCollectors();
        assertEquals(3, collectors.size());
        assertEquals(new Duration(10000), collectors.get(0).getAverageCollectionDuration());
        assertEquals(new Duration(20000), collectors.get(1).getAverageCollectionDuration());
        assertEquals(new Duration(30000), collectors.get(2).getAverageCollectionDuration());

    }

    static Date timestamp(final String dateString) throws ParseException {
        return BaseLogMessage.parseTimestamp(dateString);
    }

    private String fromFile(String fileName) throws IOException {
        StringBuilder buf = new StringBuilder();
        File logFile = new File(fileName);
        BufferedReader r = new BufferedReader(new FileReader(logFile));	
        String line = r.readLine();
        while(line != null){
            buf.append(line);
            buf.append("\n");
            line = r.readLine();
        }
        r.close();
        return buf.toString();
    }
}
