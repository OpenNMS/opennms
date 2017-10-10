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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.time.ZonedDateTimeBuilder;
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

	@Test
	public void testSizeOfEmptyParser() {
		assertEquals(0, new RadixTreeParser().size());
	}

	@Test
	public void testDifferentImplementations() throws Exception {

		MockLogAppender.setupLogging(true, "INFO");

		final String abc = "<190>Mar 11 08:35:17 127.0.0.1 30128311[4]: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet";
		//String abc = "<190>Mar 11 08:35:17 127.0.0.1 30128311: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet";
		final ByteBuffer incoming = ByteBuffer.wrap(abc.getBytes());

		//final List<ParserStage> grokStages = GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{MONTH:month} %{INT:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INT:priority}-%{STRING:mnemonic}: %{STRING:message}");
		final List<ParserStage> grokStages = GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}");
		//BufferParserFactory grokFactory = parseGrok("<%{INT:facilityPriority}> %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}: %{MONTH:month} %{INT:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INT:priority}-%{STRING:mnemonic}: %{STRING:message}");
		final ByteBufferParser<SyslogMessage> grokParser = new SingleSequenceParser(grokStages);

		// SyslogNG format
		final List<ParserStage> parserStages = new ParserStageSequenceBuilder()
			.intBetweenDelimiters('<', '>', (s,v) -> {
				SyslogFacility facility = SyslogFacility.getFacilityForCode(v);
				SyslogSeverity priority = SyslogSeverity.getSeverityForCode(v);

				s.message.setFacility(facility);
				s.message.setSeverity(priority);
			})
			.whitespace()
			.monthString((s,v) -> { s.message.setMonth(v); })
			.whitespace()
			.integer((s,v) -> { s.message.setDayOfMonth(v); })
			.whitespace()
			.integer((s,v) -> { s.message.setHourOfDay(v); })
			.character(':')
			.integer((s,v) -> { s.message.setMinute(v); })
			.character(':')
			.integer((s,v) -> { s.message.setSecond(v); })
			.whitespace()
			.stringUntilWhitespace((s,v) -> { s.message.setHostName(v); })
			.whitespace()
			.stringUntil("\\s[:", (s,v) -> { s.message.setProcessName(v); })
			.optional().character('[')
			.optional().integer((s,v) -> { s.message.setProcessId(String.valueOf(v)); })
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
//			.stringUntilChar('-', (s,v) -> { s.message.setParam("facility", v); })
			.stringUntilChar('-', null)
			.character('-')
//			.stringUntilChar('-', (s,v) -> { s.message.setParam("severity", v); })
			.stringUntilChar('-', null)
			.character('-')
//			.stringUntilChar(':', (s,v) -> { s.message.setParam("mnemonic", v); })
			.stringUntilChar(':', null)
			.character(':')
			.whitespace()
			.terminal().string((s,v) -> {
				s.message.setMessage(v);
			 })
			.getStages()
			;
		final ByteBufferParser<SyslogMessage> parser = new SingleSequenceParser(parserStages);

		final RadixTreeParser radixParser = new RadixTreeParser();
		//radixParser.teach(grokStages.toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}]").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} %{STRING:structureddata} %{STRING:message}").toArray(new ParserStage[0]));

		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} [%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} [%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{STRING:repeatedmonth} %{INT:repeatedday} %{INT:repeatedhour}:%{INT:repeatedminute}:%{INT:repeatedsecond} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} [%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{NOSPACE:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} [%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{NOSPACE:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));

		final int iterations = 100000;

		{
			CompletableFuture<SyslogMessage> event = null;
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
				event.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			long end = System.currentTimeMillis();
			System.out.println("RADIX: " + (end - start) + "ms");
//			System.out.println(lastEvent.toString());
		}

		{
			CompletableFuture<SyslogMessage> event = null;
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
			CompletableFuture<SyslogMessage> event = null;
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
					incoming, 
					config
				);
				Event convertedEvent = convertToEvent.getEvent();
			}
			long end = System.currentTimeMillis();
			System.out.println("OLD: " + (end - start) + "ms");
		}

		{
			InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-radix-configuration.xml");
			SyslogdConfig config = new SyslogdConfigFactory(stream);

			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				ConvertToEvent convertToEvent = new ConvertToEvent(
					DistPollerDao.DEFAULT_DIST_POLLER_ID,
					MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID,
					InetAddressUtils.ONE_TWENTY_SEVEN,
					9999,
					incoming, 
					config
				);
				Event convertedEvent = convertToEvent.getEvent();
			}
			long end = System.currentTimeMillis();
			System.out.println("RADIX CONVERT: " + (end - start) + "ms");
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
		GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{MONTH:month} %{INT:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INT:priority}-%{STRING:mnemonic}: %{STRING:message}");
	}

	/**
	 * Test adjacent pattern statements. This does not work with STRING patterns
	 * yet because they must be terminated by a delimiter: either a character,
	 * whitespace, or the end of the {@link ByteBuffer}.
	 */
	@Test
	public void testGrokWithAdjacentPatterns() {
		SingleSequenceParser parser = new SingleSequenceParser(GrokParserStageSequenceBuilder.parseGrok("%{INT:year}%{MONTH:month}%{INT:day}%{STRING:messageId} %{INT:hour}"));

		SyslogMessage message = parser.parse(ByteBuffer.wrap("435Jan333fghj 999".getBytes(StandardCharsets.US_ASCII))).join();
		assertEquals(435, message.getYear().intValue());
		// January
		assertEquals(1, message.getMonth().intValue());
		assertEquals(333, message.getDayOfMonth().intValue());
		assertEquals("fghj", message.getMessageID());
		assertEquals(999, message.getHourOfDay().intValue());

		assertNull(parser.parse(ByteBuffer.wrap("Feb12345".getBytes(StandardCharsets.US_ASCII))).join());
	}

	/**
	 * Test a pattern followed immediately by an escape sequence. This does not
	 * work yet with STRING patterns but could if we peek ahead to the escaped
	 * value and use it as a delimiter.
	 */
	@Test
	public void testGrokWithAdjacentEscape() {
		SingleSequenceParser parser = new SingleSequenceParser(GrokParserStageSequenceBuilder.parseGrok("\\5%{INT:day}\\%\\%%{MONTH:month}\\f"));

		SyslogMessage message = parser.parse(ByteBuffer.wrap("56666%%Febf".getBytes(StandardCharsets.US_ASCII))).join();
		assertEquals(6666, message.getDayOfMonth().intValue());
		// February
		assertEquals(2, message.getMonth().intValue());

		assertNull(parser.parse(ByteBuffer.wrap("5abc%%Febf".getBytes(StandardCharsets.US_ASCII))).join());
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

		CompletableFuture<SyslogMessage> root = treeParser.parse(ByteBuffer.wrap("bad".getBytes()));
		assertNotNull("One pattern should match", root.join());
		root = treeParser.parse(ByteBuffer.wrap("bbd".getBytes()));
		assertNull("No pattern should match", root.join());

		treeParser.performEdgeCompression();
		System.out.println(treeParser.toString());

		root = treeParser.parse(ByteBuffer.wrap("bad".getBytes()));
		assertNotNull("One pattern should match", root.join());
		root = treeParser.parse(ByteBuffer.wrap("bbd".getBytes()));
		assertNull("No pattern should match", root.join());
	}

	@Test
	public void testGrokRadixTree() {
		RadixTreeParser radixParser = new RadixTreeParser();
//		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{TIMESTAMP_ISO8601:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
//		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{TIMESTAMP_ISO8601:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}]").toArray(new ParserStage[0]));
//		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{TIMESTAMP_ISO8601:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
//		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{TIMESTAMP_ISO8601:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} %{STRING:structureddata} %{STRING:message}").toArray(new ParserStage[0]));

		// RFC 5424
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}][%{STRING:structureddata}]").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} [%{STRING:structureddata}] %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{INT:version} %{STRING:isotimestamp} %{STRING:hostname} %{STRING:processName} %{STRING:processId} %{STRING:messageId} %{STRING:structureddata} %{STRING:message}").toArray(new ParserStage[0]));

		// RFC 3164
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} [%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} [%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{STRING:repeatedmonth} %{INT:repeatedday} %{INT:repeatedhour}:%{INT:repeatedminute}:%{INT:repeatedsecond} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} [%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{NOSPACE:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} [%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{NOSPACE:processName}: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{STRING:message}").toArray(new ParserStage[0]));

		System.out.println("Parser tree: " + radixParser.tree.toString());
		System.out.println("Parser tree size: " + radixParser.tree.size());

		radixParser.performEdgeCompression();
		System.out.println("Compressed parser tree: " + radixParser.tree.toString());
		System.out.println("Compressed parser tree size: " + radixParser.tree.size());
	}

	@Test
	public void testParseSingleMessage() {
		RadixTreeParser radixParser = new RadixTreeParser();
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}>%{NOSPACE:messageId}: %{INT:year}-%{INT:month}-%{INT:day} %{STRING:hostname} %{NOSPACE:processName}: %{STRING:message}").toArray(new ParserStage[0]));

		SyslogMessage message = radixParser.parse(ByteBuffer.wrap("<31>main: 2010-08-19 localhost foo%d: load test %d on tty1".getBytes(StandardCharsets.US_ASCII))).join();
		assertNotNull(message);
		assertEquals("main", message.getMessageID());
		assertEquals("foo%d", message.getProcessName());
		assertEquals(2010, message.getYear().intValue());
		assertEquals(8, message.getMonth().intValue());
		assertEquals(19, message.getDayOfMonth().intValue());
		assertNull(message.getHourOfDay());
		assertNull(message.getMinute());
		assertNull(message.getSecond());
		assertNull(message.getMillisecond());
		assertNull(message.getZoneId());

		Event event = ConvertToEvent.toEventBuilder(message, DistPollerDao.DEFAULT_DIST_POLLER_ID, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID).getEvent();
		assertEquals("main", event.getParm("messageid").getValue().getContent());
		assertEquals("foo%d", event.getParm("process").getValue().getContent());
	}

	/**
	 * The cause of NMS-9522 was that the parser tree generation
	 * was considering the {@code %{STRING:timezone}} and 
	 * {@code %{STRING:hostname}} clauses as identical because the 
	 * {@code m_resultConsumer} function wasn't
	 * being taken into account in the {@code equals()} method for
	 * the parser stages.
	 * 
	 * @see https://issues.opennms.org/browse/NMS-9522
	 */
	@Test
	public void testHostnameVersusTimezoneNms9522() {
		RadixTreeParser radixParser = new RadixTreeParser();
		// Put the more-precise timezone match first in the parser tree
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:timezone} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));
		radixParser.teach(GrokParserStageSequenceBuilder.parseGrok("<%{INT:facilityPriority}> %{MONTH:month} %{INT:day} %{INT:hour}:%{INT:minute}:%{INT:second} %{STRING:hostname} %{NOSPACE:processName}[%{INT:processId}]: %{STRING:message}").toArray(new ParserStage[0]));

		System.out.println("Parser tree: " + radixParser.tree.toString());
		System.out.println("Parser tree size: " + radixParser.tree.size());

		verifyNms9522Message1(radixParser);
		verifyNms9522Message2(radixParser);

		radixParser.performEdgeCompression();

		System.out.println("Parser tree (after compression): " + radixParser.tree.toString());
		System.out.println("Parser tree size (after compression): " + radixParser.tree.size());

		// Make sure that everything works after edge compression as well
		verifyNms9522Message1(radixParser);
		verifyNms9522Message2(radixParser);
	}

	private static void verifyNms9522Message1(RadixTreeParser radixParser) {
		SyslogMessage message;
		message = radixParser.parse(ByteBuffer.wrap("<14> Nov 16 00:01:25 localhost postfix/smtpd[1713]: connect from www.opennms.org[10.1.1.1]".getBytes(StandardCharsets.US_ASCII))).join();
		assertNotNull(message);
		assertNull(message.getMessageID());
		assertEquals("postfix/smtpd", message.getProcessName());
		assertEquals("1713", message.getProcessId());
		//assertEquals(2010, message.getYear().intValue());
		assertEquals(11, message.getMonth().intValue());
		assertEquals(16, message.getDayOfMonth().intValue());
		assertEquals(0, message.getHourOfDay().intValue());
		assertEquals(1, message.getMinute().intValue());
		assertEquals(25, message.getSecond().intValue());
		assertNull(message.getMillisecond());
		assertNull(message.getZoneId());
		assertEquals("localhost", message.getHostName());
		assertEquals("connect from www.opennms.org[10.1.1.1]", message.getMessage());

		Event event = ConvertToEvent.toEventBuilder(message, DistPollerDao.DEFAULT_DIST_POLLER_ID, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID).getEvent();
		assertEquals("localhost", event.getParm("hostname").getValue().getContent());
		assertEquals("postfix/smtpd", event.getParm("process").getValue().getContent());
	}

	private static void verifyNms9522Message2(RadixTreeParser radixParser) {
		SyslogMessage message;
		message = radixParser.parse(ByteBuffer.wrap("<19> Nov 17 14:28:48 CST %AUTHPRIV-3-SYSTEM_MSG[0]: Authentication failed from 7.40.16.188 - sshd[20189]".getBytes(StandardCharsets.US_ASCII))).join();
		assertNotNull(message);
		assertNull(message.getMessageID());
		assertEquals("%AUTHPRIV-3-SYSTEM_MSG", message.getProcessName());
		assertEquals("0", message.getProcessId());
		//assertEquals(2010, message.getYear().intValue());
		assertEquals(11, message.getMonth().intValue());
		assertEquals(17, message.getDayOfMonth().intValue());
		assertEquals(14, message.getHourOfDay().intValue());
		assertEquals(28, message.getMinute().intValue());
		assertEquals(48, message.getSecond().intValue());
		assertNull(message.getMillisecond());
		assertEquals(ZonedDateTimeBuilder.parseZoneId("CST"), message.getZoneId());
		assertNull(message.getHostName());
		assertEquals("Authentication failed from 7.40.16.188 - sshd[20189]", message.getMessage());

		Event event = ConvertToEvent.toEventBuilder(message, DistPollerDao.DEFAULT_DIST_POLLER_ID, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID).getEvent();
		assertNull(event.getParm("hostname").getValue().getContent());
		assertEquals("%AUTHPRIV-3-SYSTEM_MSG", event.getParm("process").getValue().getContent());
	}
}
