/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.syslogd.ParserStageSequenceBuilder.MatchChar;
import org.opennms.netmgt.syslogd.ParserStageSequenceBuilder.MatchMonth;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferParserTest {

	final static Logger LOG = LoggerFactory.getLogger(BufferParserTest.class);

//	private final ExecutorService m_executor = Executors.newSingleThreadExecutor();

//	private final ExecutorService m_executor = new ExecutorFactoryCassandraSEPImpl().newExecutor("StagedParser", "StageExecutor");

//	private final ExecutorService m_executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	@Test
	public void testMe() throws Exception {

		MockLogAppender.setupLogging(true, "INFO");

		String abc = "<190>Mar 11 08:35:17 127.0.0.1 30128311[4]: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet";
		//String abc = "<190>Mar 11 08:35:17 127.0.0.1 30128311: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet";
		ByteBuffer incoming = ByteBuffer.wrap(abc.getBytes());

		List<ParserStage> grokStages = GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}> %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{MONTH:month} %{INTEGER:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INTEGER:priority}-%{STRING:mnemonic}: %{STRING:message}");
		//BufferParserFactory grokFactory = parseGrok("<%{INTEGER:facilityPriority}> %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}: %{MONTH:month} %{INTEGER:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INTEGER:priority}-%{STRING:mnemonic}: %{STRING:message}");
		ByteBufferParser<Event> grokParser = new SingleSequenceParser(grokStages);

		// SyslogNG format
		List<ParserStage> parserStages = new ParserStageSequenceBuilder()
			.intBetweenDelimiters('<', '>', (s,v) -> {
				String facilityTxt = SyslogFacility.getFacilityForCode(v).toString();
				String priorityTxt = SyslogSeverity.getSeverityForCode(v).toString();

				s.builder.setParam("service", facilityTxt);
				s.builder.setParam("severity", priorityTxt);
				s.builder.setUei("uei.opennms.org/syslogd/" + facilityTxt + "/" + priorityTxt);
			})
			.whitespace()
			.monthString((s,v) -> { s.builder.setMonth(v); })
			.whitespace()
			.integer((s,v) -> { s.builder.setDayOfMonth(v); })
			.whitespace()
			.integer((s,v) -> { s.builder.setHourOfDay(v); })
			.character(':')
			.integer((s,v) -> { s.builder.setMinute(v); })
			.character(':')
			.integer((s,v) -> { s.builder.setSecond(v); })
			.whitespace()
			.stringUntilWhitespace((s,v) -> { s.builder.setHost(v); })
			.whitespace()
			.stringUntil("\\s[:", (s,v) -> { s.builder.addParam("processName", v); })
			.optional().character('[')
			.optional().integer((s,v) -> { s.builder.addParam("processId", v); })
			.optional().character(']')
			.optional().character(':')
			.whitespace()
			.stringUntilWhitespace(null) // Original month
			.whitespace()
			.integer(null) // Original day
			.whitespace()
			.stringUntilWhitespace(null) // Original timestamp
			.whitespace()
			.stringUntilWhitespace(null) // Original time zone
			.whitespace()
			.character('%')
			.stringUntilChar('-', (s,v) -> { s.builder.addParam("facility", v); })
			.character('-')
			.stringUntilChar('-', (s,v) -> { s.builder.addParam("severity", v); })
			.character('-')
			.stringUntilChar(':', (s,v) -> { s.builder.addParam("mnemonic", v); })
			.character(':')
			.whitespace()
			.terminal().string((s,v) -> { s.builder.setLogMessage(v); })
			.getStages()
			;
		ByteBufferParser<Event> parser = new SingleSequenceParser(parserStages);

		RadixTreeParser radixParser = new RadixTreeParser();
		//radixParser.teach(grokStages.toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}]").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} %{STRING:structureddata} %{STRING:message}").toArray(new ParserStage[0]));

		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} [%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} [%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{STRING:repeatedmonth} %{INTEGER:repeatedday} %{INTEGER:repeatedhour}:%{INTEGER:repeatedminute}:%{INTEGER:repeatedsecond} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}> %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} [%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}> %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}> %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} [%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}> %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));

		int iterations = 100000;

		{
			CompletableFuture<Event> event = null;
			Event lastEvent = null;
			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				event = radixParser.parse(incoming.asReadOnlyBuffer());
				event.whenComplete((e, ex) -> {
					if (ex == null) {
						//System.out.println(e.toString());
					} else {
						ex.printStackTrace();
					}
				});
			}
			// Wait for the last future to complete
			try {
				lastEvent = event.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			long end = System.currentTimeMillis();
			System.out.println("RADIX: " + (end - start) + "ms");
			System.out.println(lastEvent.toString());
		}

		{
			CompletableFuture<Event> event = null;
			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				event = parser.parse(incoming.asReadOnlyBuffer());
				event.whenComplete((e, ex) -> {
					if (ex == null) {
						//System.out.println(e.toString());
					} else {
						ex.printStackTrace();
					}
				});
			}
			// Wait for the last future to complete
			try {
				event.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			long end = System.currentTimeMillis();
			System.out.println("NEW: " + (end - start) + "ms");
		}

		{
			CompletableFuture<Event> event = null;
			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				event = grokParser.parse(incoming.asReadOnlyBuffer());
				event.whenComplete((e, ex) -> {
					if (ex == null) {
						//System.out.println(e.toString());
					} else {
						ex.printStackTrace();
					}
				});
			}
			// Wait for the last future to complete
			try {
				event.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			long end = System.currentTimeMillis();
			System.out.println("GROK: " + (end - start) + "ms");
		}

		{
			InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-syslogng-configuration.xml");
			SyslogdConfig config = new SyslogdConfigFactory(stream);

			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				ConvertToEvent convertToEvent = new ConvertToEvent(
    				DistPollerDao.DEFAULT_DIST_POLLER_ID,
    				MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
    				InetAddressUtils.ONE_TWENTY_SEVEN,
    				9999,
    				abc, 
    				config
				);
				Event convertedEvent = convertToEvent.getEvent();
			}
			long end = System.currentTimeMillis();
			System.out.println("OLD: " + (end - start) + "ms");
		}

	}

	@Test
	public void testMatchMonth() {
		{
			ParserState state = new ParserState(ByteBuffer.wrap("Jan".getBytes()));
			ParserStage stage = new MatchMonth((s,v) -> { assertEquals(1, v.intValue()); });
			assertNotNull(stage.apply(state));
		}
		{
			ParserState state = new ParserState(ByteBuffer.wrap("Jun".getBytes()));
			ParserStage stage = new MatchMonth((s,v) -> { assertEquals(6, v.intValue()); });
			assertNotNull(stage.apply(state));
		}
		{
			ParserState state = new ParserState(ByteBuffer.wrap("dec".getBytes()));
			ParserStage stage = new MatchMonth((s,v) -> { assertEquals(12, v.intValue()); });
			assertNotNull(stage.apply(state));
		}
		{
			ParserState state = new ParserState(ByteBuffer.wrap("Jul".getBytes()));
			ParserStage stage = new MatchMonth((s,v) -> { assertEquals(7, v.intValue()); });
			assertNotNull(stage.apply(state));
		}
		{
			ParserState state = new ParserState(ByteBuffer.wrap("Oct".getBytes()));
			ParserStage stage = new MatchMonth((s,v) -> { assertEquals(10, v.intValue()); });
			assertNotNull(stage.apply(state));
		}
		{
			ParserState state = new ParserState(ByteBuffer.wrap("oct".getBytes()));
			ParserStage stage = new MatchMonth((s,v) -> { assertEquals(10, v.intValue()); });
			assertNotNull(stage.apply(state));
		}
		{
			ParserState state = new ParserState(ByteBuffer.wrap("juf".getBytes()));
			ParserStage stage = new MatchMonth((s,v) -> { assertEquals(Integer.MAX_VALUE, v.intValue()); });
			// Parse failed
			assertNull(stage.apply(state));
		}
		{
			ParserState state = new ParserState(ByteBuffer.wrap("Mai".getBytes()));
			ParserStage stage = new MatchMonth((s,v) -> { assertEquals(Integer.MAX_VALUE, v.intValue()); });
			// Parse failed
			assertNull(stage.apply(state));
		}
		{
			ParserState state = new ParserState(ByteBuffer.wrap("SepPARSE_WILL_STILL_SUCCEED".getBytes()));
			ParserStage stage = new MatchMonth((s,v) -> { assertEquals(9, v.intValue()); });
			assertNotNull(stage.apply(state));
		}
	}

	@Test
	public void testGrokParser() {
		GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}> %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{MONTH:month} %{INTEGER:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INTEGER:priority}-%{STRING:mnemonic}: %{STRING:message}");
	}

	@Test
	public void testParserStages() throws InterruptedException, ExecutionException {
		ParserStage a = new MatchChar('a');
		ParserStage b = new MatchChar('b');
		ParserStage c = new MatchChar('c');
		ParserStage d = new MatchChar('d');

		c.setTerminal(true);
		d.setTerminal(true);

		RadixTreeParser treeParser = new RadixTreeParser();
		treeParser.teach(new ParserStage[] { a });
		treeParser.teach(new ParserStage[] { b, c });
		treeParser.teach(new ParserStage[] { b, a, d });
		treeParser.teach(new ParserStage[] { b, d });
		treeParser.teach(new ParserStage[] { c });

		System.out.println(treeParser.toString());

		CompletableFuture<Event> root = treeParser.parse(ByteBuffer.wrap("bad".getBytes()));
		assertNotNull("One pattern should match", root.join());
		root = treeParser.parse(ByteBuffer.wrap("bbd".getBytes()));
		assertNull("No pattern should match", root.join());
	}

	@Test
	public void testGrokRadixTree() {
		RadixTreeParser radixParser = new RadixTreeParser();
//		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{TIMESTAMP_ISO8601:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
//		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{TIMESTAMP_ISO8601:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}]").toArray(new ParserStage[0]));
//		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{TIMESTAMP_ISO8601:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
//		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{TIMESTAMP_ISO8601:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} %{STRING:structureddata} %{STRING:message}").toArray(new ParserStage[0]));

		// RFC 5424
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}]").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{INTEGER:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} %{STRING:structureddata} %{STRING:message}").toArray(new ParserStage[0]));

		// RFC 3164
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} [%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} [%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{STRING:repeatedmonth} %{INTEGER:repeatedday} %{INTEGER:repeatedhour}:%{INTEGER:repeatedminute}:%{INTEGER:repeatedsecond} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}> %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} [%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}> %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}> %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} [%{INTEGER:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}>%{STRING:messageId}: %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INTEGER:facilityPriority}> %{INTEGER:year}-%{INTEGER:month}-%{INTEGER:day} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));

		System.out.println("Parser tree: " + radixParser.tree.toString());
		System.out.println("Parser tree size: " + radixParser.tree.size());
	}
}
