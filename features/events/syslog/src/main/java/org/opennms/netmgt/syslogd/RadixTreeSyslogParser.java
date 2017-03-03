/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.nio.ByteBuffer;

import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.model.events.EventBuilder;

/**
 * @author Seth
 */
public class RadixTreeSyslogParser extends SyslogParser {

	private static RadixTreeParser radixParser = new RadixTreeParser();
	
	static {
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
	}

	public RadixTreeSyslogParser(SyslogdConfig config, ByteBuffer syslogString) {
		super(config, syslogString);
	}

	/**
	 * Since this parser does not rely on a regex expression match for its initial
	 * parsing, always return true.
	 */
	@Override
	public boolean find() {
		return true;
	}

	@Override
	public EventBuilder parseToEventBuilder(String systemId, String location) {
		EventBuilder bldr = radixParser.parse(getText()).join();
		if (bldr != null) {
			bldr.setDistPoller(systemId);
			// TODO: Set nodeid based on location
		}
		return bldr;
	}
}
