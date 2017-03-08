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

import java.util.List;
import java.util.TimeZone;
import java.util.function.BiConsumer;

import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.syslogd.ParserStageSequenceBuilder.MatchInteger;

public abstract class GrokParserStageSequenceBuilder {

	private static enum GrokState {
		TEXT,
		ESCAPE_PATTERN,
		START_PATTERN,
		PATTERN,
		SEMANTIC,
		END_PATTERN
	}

	private static enum GrokPattern {
		STRING,
		INTEGER,
		MONTH
	}

	/**
	 * This enum contains all well-known syslog message fields.
	 */
	public static enum SemanticType {
		/**
		 * Facility-priority integer.
		 * 
		 * @see RFC 3164: PRI
		 * @see RFC 5424: PRIVAL
		 */
		facilityPriority,

		/**
		 * Version.
		 * 
		 * @see RFC 5424: VERSION
		 */
		version,

		/**
		 * 4-digit year.
		 * 
		 * @see ISO-8601
		 * @see RFC 3339
		 * @see RFC 5424: DATE-FULLYEAR
		 */
		year,

		/**
		 * 2-digit month (1-12).
		 * 
		 * @see ISO-8601
		 * @see RFC 3164
		 * @see RFC 3339
		 * @see RFC 5424: DATE-MONTH
		 */
		month,

		/**
		 * 2-digit day of month (1-31).
		 * 
		 * @see ISO-8601
		 * @see RFC 3164
		 * @see RFC 3339
		 * @see RFC 5424: DATE-MDAY
		 */
		day,

		/**
		 * 2-digit hour of day (0-23).
		 * 
		 * @see ISO-8601
		 * @see RFC 3164
		 * @see RFC 3339
		 * @see RFC 5424: TIME-HOUR
		 */
		hour,

		/**
		 * 2-digit minute (0-59).
		 * 
		 * @see ISO-8601
		 * @see RFC 3164
		 * @see RFC 3339
		 * @see RFC 5424: TIME-MINUTE
		 */
		minute,

		/**
		 * 2-digit second (0-59).
		 * 
		 * @see ISO-8601
		 * @see RFC 3164
		 * @see RFC 3339
		 * @see RFC 5424: TIME-SECOND
		 */
		second,

		/**
		 * 1- to 6-digit fractional second value as a string.
		 * Note that the maximum resolution of this value is microseconds
		 * but we are storing the value in nanoseconds since nanosecond 
		 * resolution is more prevalent in the Java time APIs.
		 * 
		 * @see RFC 5424: TIME-SECFRAC
		 */
		secondFraction,

		/**
		 * String timezone value.
		 * 
		 * @see ISO-8601
		 * @see RFC 3339
		 * @see RFC 5424: TIME-OFFSET
		 */
		timezone,

		/**
		 * String hostname (unqualified or FQDN), IPv4 address, or IPv6 address.
		 * 
		 * @see RFC 5424: HOSTNAME
		 */
		hostname,

		/**
		 * String process name.
		 * 
		 * @see RFC 5424: APP-NAME
		 */
		processName,

		/**
		 * String process name.
		 * 
		 * @see RFC 5424: PROCID
		 */
		processId,

		/**
		 * String message ID.
		 * 
		 * @see RFC 5424: MSGID
		 */
		messageId,

		/*
		facility, // Cisco
		priority, // Cisco
		mnemonic, // Cisco
		*/

		/*
		structuredData, // RFC5424 STRUCTURED-DATA
		*/

		/**
		 * Remaining string message.
		 */
		message
	}

/*
Relevant EventBuilder fields:
//		public EventBuilder setAlarmData(AlarmData alarmData);
		public EventBuilder setDescription(String descr);
//		public EventBuilder setDistPoller(String distPoller);
		public EventBuilder setHost(String hostname);
//		public EventBuilder setIfIndex(int ifIndex);
		public EventBuilder setInterface(InetAddress ipAddress);
//		public EventBuilder setIpInterface(OnmsIpInterface iface);
		public EventBuilder setLogDest(String dest);
		public EventBuilder setLogMessage(String content);
//		public EventBuilder setMasterStation(String masterStation);
//		public EventBuilder setMonitoredService(OnmsMonitoredService monitoredService);
//		public EventBuilder setNode(OnmsNode node);
//		public EventBuilder setNodeid(long nodeid);
		public EventBuilder setParam(String parmName, String val);
		public EventBuilder setParms(List<Parm> parms);
//		public EventBuilder setService(String serviceName);
		public EventBuilder setSeverity(String severity);
//		public EventBuilder setSource(String source);
		public EventBuilder setTime(Date date);
		public EventBuilder setUei(String uei);
//		public EventBuilder setUuid(String uuid);

SNMP-only:
//		public EventBuilder setCommunity(String community);
//		public EventBuilder setEnterpriseId(String enterprise);
//		public EventBuilder setGeneric(int generic);
//		public EventBuilder setSnmpHost(String snmpHost);
//		public EventBuilder setSnmpTimeStamp(long timeStamp);
//		public EventBuilder setSnmpVersion(String version);
//		public EventBuilder setSpecific(int specific);
*/

	/**
	 * This function maps {@link SemanticType} values of type int to {@link EventBuilder}
	 * calls.
	 * 
	 * @param semanticString
	 * @return
	 */
	private static BiConsumer<ParserState,Integer> semanticIntegerToEventBuilder(String semanticString) {
		SemanticType semanticType = null;
		try {
			semanticType = SemanticType.valueOf(semanticString);
		} catch (IllegalArgumentException e) {
			// Leave semanticType == null
		}

		if (semanticType == null) {
			return (s,v) -> {
				s.builder.setParam(semanticString, v);
			};
		} else {
			switch(semanticType) {
			case day:
				return (s,v) -> {
					s.builder.setDayOfMonth(v);
				};
			case facilityPriority:
				return (s,v) -> {
					String facilityTxt = SyslogFacility.getFacilityForCode(v).toString();
					String priorityTxt = SyslogSeverity.getSeverityForCode(v).toString();

					s.builder.setParam("service", facilityTxt);
					s.builder.setParam("severity", priorityTxt);
					s.builder.setUei("uei.opennms.org/syslogd/" + facilityTxt + "/" + priorityTxt);
				};
			case hour:
				return (s,v) -> {
					s.builder.setHourOfDay(v);
				};
			case minute:
				return (s,v) -> {
					s.builder.setMinute(v);
				};
			case month:
				return (s,v) -> {
					s.builder.setMonth(v);
				};
			case processId:
				// processId can be an integer or string
				return (s,v) -> {
					s.builder.setParam("processid", v);
				};
			case second:
				return (s,v) -> {
					s.builder.setSecond(v);
				};
			// TODO: This should be handled as a string... this is only
			// in here as a stopgap until we create a DIGITS pattern type.
			case secondFraction:
				return (s,v) -> {
					s.builder.setMillisecond(v);
				};
			case version:
				// Unique to this parser
				return (s,v) -> {
					s.builder.setParam("syslogversion", v);
				};
			case year:
				return (s,v) -> {
					s.builder.setYear(v);
				};
			default:
				throw new IllegalArgumentException(String.format("Semantic type %s does not have an integer value", semanticString));
			}
		}

	}

	private static final BiConsumer<ParserState,String> NOOP = (s,v) -> {};

	/**
	 * This function maps {@link SemanticType} values of type String to {@link EventBuilder}
	 * calls.
	 * 
	 * @param semanticString
	 * @return
	 */
	private static BiConsumer<ParserState,String> semanticStringToEventBuilder(String semanticString) {
		SemanticType semanticType = null;
		try {
			semanticType = SemanticType.valueOf(semanticString);
		} catch (IllegalArgumentException e) {
			// Leave semanticType == null
		}

		if (semanticType == null) {
			return (s,v) -> {
				s.builder.setParam(semanticString, v);
			};
		} else {
			switch(semanticType) {
			case hostname:
				// TODO
				return NOOP;
			case message:
				return (s,v) -> {
					s.builder.setLogMessage(v);
					// Using parms provides configurability.
					s.builder.setParam("syslogmessage", v);
				};
			case messageId:
				// Unique to this parser
				return (s,v) -> {
					s.builder.setParam("messageid", v);
				};
			case processId:
				// processId can be an integer or string
				return (s,v) -> {
					s.builder.setParam("processid", v);
				};
			case processName:
				return (s,v) -> {
					s.builder.setParam("process", v);
				};
			case secondFraction:
				return (s,v) -> {
					// Convert the fraction value into a millisecond value since that
					// is the best resolution that EventBuilder can handle
					switch(v.length()) {
					case 1:
						s.builder.setMillisecond(MatchInteger.trimAndConvert(v) * 100);
						break;
					case 2:
						s.builder.setMillisecond(MatchInteger.trimAndConvert(v) * 10);
						break;
					case 3:
						s.builder.setMillisecond(MatchInteger.trimAndConvert(v));
						break;
					case 4:
						s.builder.setMillisecond(MatchInteger.trimAndConvert(v) / 10);
						break;
					case 5:
						s.builder.setMillisecond(MatchInteger.trimAndConvert(v) / 100);
						break;
					case 6:
						s.builder.setMillisecond(MatchInteger.trimAndConvert(v) / 1000);
						break;
					}
				};
			case timezone:
				return (s,v) -> {
					// TODO: Make sure this works for all variants
					s.builder.setTimeZone(TimeZone.getTimeZone(v));
				};
			default:
				throw new IllegalArgumentException(String.format("Semantic type %s does not have a string value", semanticString));
			}
		}
	}

//	TODO: Figure out how to add a timestamp parm to the event
//	bldr.addParam("timestamp", message.getSyslogFormattedDate());

	public static List<ParserStage> parseGrok(String grok) {
		GrokState state = GrokState.TEXT;
		ParserStageSequenceBuilder factory = new ParserStageSequenceBuilder();

		StringBuffer pattern = new StringBuffer();
		StringBuffer semantic = new StringBuffer();

		for (char c : grok.toCharArray()) {
			switch(state) {
			case TEXT:
				switch(c) {
				case '%':
					state = GrokState.START_PATTERN;
					continue;
				case '\\':
					state = GrokState.ESCAPE_PATTERN;
					continue;
				case ' ':
					factory = factory.whitespace();
					continue;
				default:
					factory = factory.character(c);
					continue;
				}
			case ESCAPE_PATTERN:
				switch(c) {
				default:
					factory = factory.character(c);
					state = GrokState.TEXT;
					continue;
				}
			case START_PATTERN:
				switch(c) {
				case '{':
					state = GrokState.PATTERN;
					continue;
				default:
					throw new IllegalStateException("Illegal character to start pattern");
				}
			case PATTERN:
				switch(c) {
				case ':':
					state = GrokState.SEMANTIC;
					continue;
				default:
					pattern.append(c);
					continue;
				}
			case SEMANTIC:
				switch(c) {
				case '}':
					state = GrokState.END_PATTERN;
					continue;
				default:
					semantic.append(c);
					continue;
				}
			case END_PATTERN:
				final String patternString = pattern.toString();
				final String semanticString = semantic.toString();

				GrokPattern patternType = GrokPattern.valueOf(patternString);

				switch(c) {
				case '\\':
					switch(patternType) {
					case STRING:
						// TODO: We need to peek forward to the escaped character and then do the same as the default case
						throw new UnsupportedOperationException("Cannot support escape sequence directly after a pattern yet");
					case INTEGER:
						factory.integer(semanticIntegerToEventBuilder(semanticString));
						break;
					case MONTH:
						factory.monthString(semanticIntegerToEventBuilder(semanticString));
						break;
					}
					pattern = new StringBuffer();
					semantic = new StringBuffer();
					state = GrokState.ESCAPE_PATTERN;
					continue;
				case '%':
					switch(patternType) {
					case STRING:
						// TODO: Can we handle this case?
						throw new IllegalArgumentException(String.format("Invalid pattern: %s:%s does not have a trailing delimiter, cannot determine end of string", patternString, semanticString));
					case INTEGER:
						factory.integer(semanticIntegerToEventBuilder(semanticString));
						break;
					case MONTH:
						factory.monthString(semanticIntegerToEventBuilder(semanticString));
						break;
					}
					pattern = new StringBuffer();
					semantic = new StringBuffer();
					state = GrokState.START_PATTERN;
					continue;
				case ' ':
					switch(patternType) {
					case STRING:
						factory.stringUntilWhitespace(semanticStringToEventBuilder(semanticString));
						factory.whitespace();
						break;
					case INTEGER:
						factory.intUntilWhitespace(semanticIntegerToEventBuilder(semanticString));
						factory.whitespace();
						break;
					case MONTH:
						factory.monthString(semanticIntegerToEventBuilder(semanticString));
						factory.whitespace();
						break;
					}
					break;
				default:
					switch(patternType) {
					case STRING:
						factory.stringUntil(String.valueOf(c), semanticStringToEventBuilder(semanticString));
						factory.character(c);
						break;
					case INTEGER:
						factory.integer(semanticIntegerToEventBuilder(semanticString));
						factory.character(c);
						break;
					case MONTH:
						factory.monthString(semanticIntegerToEventBuilder(semanticString));
						factory.character(c);
						break;
					}
				}
				pattern = new StringBuffer();
				semantic = new StringBuffer();
				state = GrokState.TEXT;
				continue;
			}
		}

		// If we are in the process of ending a pattern, then tie it up with a bow
		if (state == GrokState.END_PATTERN) {
			final String patternString = pattern.toString();
			final String semanticString = semantic.toString();

			GrokPattern patternType = GrokPattern.valueOf(patternString);

			switch(patternType) {
			case STRING:
				factory.terminal().string(semanticStringToEventBuilder(semanticString));
				break;
			case INTEGER:
				factory.terminal().integer(semanticIntegerToEventBuilder(semanticString));
				break;
			case MONTH:
				factory.terminal().monthString(semanticIntegerToEventBuilder(semanticString));
				break;
			}
		}

		return factory.getStages();
	}
}
