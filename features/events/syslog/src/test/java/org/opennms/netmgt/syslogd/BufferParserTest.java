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

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.syslogd.BufferParser.BufferParserFactory;
import org.opennms.netmgt.syslogd.BufferParser.MatchMonth;
import org.opennms.netmgt.syslogd.BufferParser.ParserStage;
import org.opennms.netmgt.syslogd.BufferParser.ParserState;
import org.opennms.netmgt.xml.event.Event;

public class BufferParserTest {

	private final ExecutorService m_executor = Executors.newSingleThreadExecutor();

	//private final ExecutorService m_executor = new ExecutorFactoryCassandraSEPImpl().newExecutor("StagedParser", "StageExecutor");

	//private final ExecutorService m_executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	@Test
	public void testMe() throws Exception {

		MockLogAppender.setupLogging(true, "INFO");

		String abc = "<190>Mar 11 08:35:17 127.0.0.1 30128311[4]: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet";
		//String abc = "<190>Mar 11 08:35:17 127.0.0.1 30128311: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet";
		ByteBuffer incoming = ByteBuffer.wrap(abc.getBytes());

		AtomicReference<SyslogFacility> facility = new AtomicReference<>();
		AtomicReference<Integer> year = new AtomicReference<>();
		AtomicReference<Integer> month = new AtomicReference<>();
		AtomicReference<Integer> day = new AtomicReference<>();
		AtomicReference<Integer> hour = new AtomicReference<>();
		AtomicReference<Integer> minute = new AtomicReference<>();
		AtomicReference<Integer> second = new AtomicReference<>();

		BufferParserFactory grokFactory = GrokParserFactory.parseGrok("<%{INTEGER:facilityPriority}> %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:month} %{INTEGER:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INTEGER:priority}-%{STRING:mnemonic}: %{STRING:message}");
		//BufferParserFactory grokFactory = parseGrok("<%{INTEGER:facilityPriority}> %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}: %{STRING:month} %{INTEGER:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INTEGER:priority}-%{STRING:mnemonic}: %{STRING:message}");

		// SyslogNG format
		BufferParserFactory factory = new BufferParserFactory()
			.intBetweenDelimiters('<', '>', (s,v) -> { facility.set(SyslogFacility.getFacilityForCode(v)); })
			.whitespace()
			.month((s,v) -> month.set(v))
			.whitespace()
			.integer((s,v) -> day.set(v))
			.whitespace()
			.integer((s,v) -> hour.set(v))
			.character(':')
			.integer((s,v) -> minute.set(v))
			.character(':')
			.integer((s,v) -> second.set(v))
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
			.stringUntilChar('-', (s,v) -> { /* TODO: Set facility */ })
			.character('-')
			.stringUntilChar('-', (s,v) -> { /* TODO: Set severity */ })
			.character('-')
			.stringUntilChar(':', (s,v) -> { /* TODO: Set mnemonic */ })
			.character(':')
			.whitespace()
			.terminal().string((s,v) -> { s.builder.setLogMessage(v); })
			;

		int iterations = 100000;

		CompletableFuture<Event> event = null;
		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			event = factory.parse(incoming.asReadOnlyBuffer(), m_executor);
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

		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			event = grokFactory.parse(incoming.asReadOnlyBuffer(), m_executor);
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
		end = System.currentTimeMillis();
		System.out.println("GROK: " + (end - start) + "ms");

		InputStream stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-syslogng-configuration.xml");
		SyslogdConfig config = new SyslogdConfigFactory(stream);

		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			ConvertToEvent convertToEvent = new ConvertToEvent(
				DistPollerDao.DEFAULT_DIST_POLLER_ID,
				InetAddressUtils.ONE_TWENTY_SEVEN,
				9999,
				abc, 
				config
			);
			Event convertedEvent = convertToEvent.getEvent();
		}
		end = System.currentTimeMillis();
		System.out.println("OLD: " + (end - start) + "ms");

	}

	@Test
	public void testMatchMonth() {
		ParserState state = new ParserState();
		state.buffer = ByteBuffer.wrap("Oct".getBytes());

		ParserStage stage = new MatchMonth((s,v) -> { System.out.println(v); });
		stage.apply(state);
	}

	@Test
	public void testGrokParser() {
		GrokParserFactory.parseGrok("<%{INTEGER:facilityPriority}> %{MONTH:month} %{INTEGER:day} %{INTEGER:hour}:%{INTEGER:minute}:%{INTEGER:second} %{STRING:hostname} %{STRING:processName}[%{INTEGER:processId}]: %{STRING:month} %{INTEGER:day} %{STRING:timestamp} %{STRING:timezone} \\%%{STRING:facility}-%{INTEGER:priority}-%{STRING:mnemonic}: %{STRING:message}");
	}

}
