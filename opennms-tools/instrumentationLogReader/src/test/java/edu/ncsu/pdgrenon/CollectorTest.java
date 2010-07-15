package edu.ncsu.pdgrenon;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;


public class CollectorTest {
	private Date getDate(String dateString) throws ParseException {
		return new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss,S").parse(dateString);
	}
	@Test
	public void testStartNotSetEnd() throws ParseException {
		Collector c = new Collector();
		c.addLog("2010-05-26 12:12:38,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");	
		c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: begin: 24/216.216.217.254/SNMP");
		c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");
		c.addLog("2010-05-26 12:12:50,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: begin: 24/216.216.217.254/SNMP");
		assertEquals(getDate("2010-05-26 12:12:40,883"), c.getStartTime());
		assertEquals(getDate("2010-05-26 12:12:48,027"), c.getEndTime());
		assertEquals(7144, c.getDuration());
	}
	@Test
	public void testStartAndEndTime() throws ParseException {
		Collector c = new Collector();
		c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: begin: 24/216.216.217.254/SNMP");
		c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");	
		
		assertEquals(getDate("2010-05-26 12:12:40,883"), c.getStartTime());
		assertEquals(getDate("2010-05-26 12:12:48,027"), c.getEndTime());
		assertEquals(7144, c.getDuration());
	}
	@Test
	public void testServiceCount(){
		Collector c = new Collector();
		assertEquals(0, c.getServiceCount());
		c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: begin: 24/216.216.217.254/SNMP");
		c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");
		assertEquals(1, c.getServiceCount());
		c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: begin: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:36:28,995 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: end: 19/209.61.128.9/SNMP");
		c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: begin: 24/216.216.217.254/SNMP");
		c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");
		c.addLog("2010-06-01 09:36:02,541 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: end:7/172.20.1.12/SNMP");
		c.addLog("2010-06-01 09:36:02,542 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: begin:27/172.20.1.6/SNMP");
		c.addLog("2010-06-01 09:36:02,544 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: collectData: begin: 27/172.20.1.6/SNMP");
		c.addLog("2010-06-01 09:36:03,508 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: collectData: end: 27/172.20.1.6/SNMP");
		c.addLog("2010-06-01 09:36:02,541 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: error: 7/172.20.1.12/SNMP: org.opennms.netmgt.collectd.CollectionTimedOut: Timeout retrieving SnmpCollectors for 172.20.1.12 for kenny.internal.opennms.com/172.20.1.12: SnmpCollectors for 172.20.1.12: snmpTimeoutError for: kenny.internal.opennms.com/172.20.1.12");
		c.addLog("2010-06-01 09:36:27,644 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: collectData: begin: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: collectData: end: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:33:56,292 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: collectData: begin: 83/172.20.1.15/SNMP");
		c.addLog("2010-06-01 09:33:56,440 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: collectData: end: 83/172.20.1.15/SNMP");
		assertEquals(5, c.getServiceCount());
	}
	
	@Test
	public void testThreadcount(){
		Collector c = new Collector();
		assertEquals(0, c.getThreadCount());
		c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: begin: 24/216.216.217.254/SNMP");
		c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");
		assertEquals(1, c.getThreadCount());
		c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: begin: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:36:28,995 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: end: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:33:31,964 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: collectData: begin: 60/172.20.1.202/SNMP");
		c.addLog("2010-06-01 09:33:32,477 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: collectData: end: 60/172.20.1.202/SNMP");
		c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: begin: 86/172.20.1.25/WMI");
		c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: end: 86/172.20.1.25/WMI");
		c.addLog("2010-06-01 08:39:46,648 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
		c.addLog("2010-06-01 08:39:46,650 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: collectData: begin: 58/172.20.1.201/SNMP");
		assertEquals(5, c.getThreadCount());
	}
	@Test 
	public void testCollectionsPerService() {
		Collector c = new Collector();
		c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: begin: 24/216.216.217.254/SNMP");
		c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");
		c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: begin: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:36:28,995 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: end: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:33:31,964 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: collectData: begin: 60/172.20.1.202/SNMP");
		c.addLog("2010-06-01 09:33:32,477 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: collectData: end: 60/172.20.1.202/SNMP");
		c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: begin: 86/172.20.1.25/WMI");
		c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: end: 86/172.20.1.25/WMI");
		c.addLog("2010-06-01 08:39:46,648 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
		c.addLog("2010-06-01 08:39:46,650 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: collectData: begin: 58/172.20.1.201/SNMP");
		assertEquals(1,c.collectionsPerService("24/216.216.217.254/SNMP"));
		assertEquals(0,c.collectionsPerService("19/209.61.128.9/SNMP"));
		assertEquals(1,c.collectionsPerService("60/172.20.1.202/SNMP"));
		assertEquals(0,c.collectionsPerService("86/172.20.1.25/WMI"));
		assertEquals(1,c.collectionsPerService("58/172.20.1.201/SNMP"));
	}
	@Test
	public void testAverageCollectionTimePerService() {
		Collector c = new Collector();
		c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: begin: 24/216.216.217.254/SNMP");
		c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");
		c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: begin: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:36:28,995 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: end: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:33:31,964 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: collectData: begin: 60/172.20.1.202/SNMP");
		c.addLog("2010-06-01 09:33:32,477 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: collectData: end: 60/172.20.1.202/SNMP");
		c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: begin: 86/172.20.1.25/WMI");
		c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: end: 86/172.20.1.25/WMI");
		c.addLog("2010-06-01 08:39:46,648 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
		c.addLog("2010-06-01 08:39:46,650 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: collectData: begin: 58/172.20.1.201/SNMP");
		assertEquals(7144,c.averageCollectionTimePerService("24/216.216.217.254/SNMP"));
		assertEquals(0,c.averageCollectionTimePerService("19/209.61.128.9/SNMP"));
		assertEquals(513,c.averageCollectionTimePerService("60/172.20.1.202/SNMP"));
		assertEquals(0,c.averageCollectionTimePerService("86/172.20.1.25/WMI"));
		assertEquals(0,c.averageCollectionTimePerService("58/172.20.1.201/SNMP"));
	}
	@Test
	public void testTotalCollectionTimePerService() {
		Collector c = new Collector();
		c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: begin: 24/216.216.217.254/SNMP");
		c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");
		c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: begin: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:36:28,995 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: end: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:33:31,964 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: collectData: begin: 60/172.20.1.202/SNMP");
		c.addLog("2010-06-01 09:33:32,477 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: collectData: end: 60/172.20.1.202/SNMP");
		c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: begin: 86/172.20.1.25/WMI");
		c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: end: 86/172.20.1.25/WMI");
		c.addLog("2010-06-01 08:39:46,648 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
		c.addLog("2010-06-01 08:39:46,650 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: collectData: begin: 58/172.20.1.201/SNMP");
		assertEquals(7144,c.testTotalCollectionTimePerService("24/216.216.217.254/SNMP"));
		assertEquals(0,c.testTotalCollectionTimePerService("19/209.61.128.9/SNMP"));
		assertEquals(513,c.testTotalCollectionTimePerService("60/172.20.1.202/SNMP"));
		assertEquals(0,c.testTotalCollectionTimePerService("86/172.20.1.25/WMI"));
		assertEquals(-1,c.testTotalCollectionTimePerService("58/172.20.1.201/SNMP"));
	}
	@Test
	public void testSortAndPrintServiceCount() {
		Collector c = new Collector();
		c.addLog("2010-05-26 12:12:40,883 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: begin: 24/216.216.217.254/SNMP");
		c.addLog("2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");
		c.addLog("2010-06-01 09:36:28,950 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: begin: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:36:28,995 DEBUG [CollectdScheduler-50 Pool-fiber1] Collectd: collector.collect: persistDataQueueing: end: 19/209.61.128.9/SNMP");
		c.addLog("2010-06-01 09:33:31,964 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: collectData: begin: 60/172.20.1.202/SNMP");
		c.addLog("2010-06-01 09:33:32,477 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: collector.collect: collectData: end: 60/172.20.1.202/SNMP");
		c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: begin: 86/172.20.1.25/WMI");
		c.addLog("2010-06-01 08:45:12,104 DEBUG [CollectdScheduler-50 Pool-fiber2] Collectd: collector.collect: persistDataQueueing: end: 86/172.20.1.25/WMI");
		c.addLog("2010-06-01 08:39:46,648 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: begin:58/172.20.1.201/SNMP");
		c.addLog("2010-06-01 08:39:46,650 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: collectData: begin: 58/172.20.1.201/SNMP");
		assertEquals("Beginning collecting messages during collection: " + 4 + " Ending collecting messages during collection:  " + 2 +
				" Persisting messages during collection: " + 4 + " Error messages during collection: " + 0 + " failures: " + 0,c.sortAndPrintServiceCount());
	}
	@Test 
	public void testReadLogMessagesFromFile () throws IOException {
		Collector c = new Collector();
		c.readLogMessagesFromFile("TestLogFile.log");
		assertEquals(7144,c.testTotalCollectionTimePerService("24/216.216.217.254/SNMP"));
		assertEquals(0,c.testTotalCollectionTimePerService("19/209.61.128.9/SNMP"));
		assertEquals(513,c.testTotalCollectionTimePerService("60/172.20.1.202/SNMP"));
		assertEquals(0,c.testTotalCollectionTimePerService("86/172.20.1.25/WMI"));
		assertEquals(-1,c.testTotalCollectionTimePerService("58/172.20.1.201/SNMP"));
	}
	@Test
	public void testPrintGlobalStats () {
		Collector c = new Collector ();
		assertEquals(null,c.printGlobalStats());
	}
}
