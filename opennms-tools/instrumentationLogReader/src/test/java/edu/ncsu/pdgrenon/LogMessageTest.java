package edu.ncsu.pdgrenon;

import static org.junit.Assert.*;

import org.junit.Test;


public class LogMessageTest {
	@Test
	public void testGetService() {
		LogMessage log = new LogMessage ("2010-05-26 12:12:38,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");
		assertEquals("24/216.216.217.254/SNMP",log.getServiceID());
	}
}
