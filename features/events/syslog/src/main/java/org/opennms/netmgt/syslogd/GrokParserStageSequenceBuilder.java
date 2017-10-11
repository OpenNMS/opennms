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
import java.util.function.BiConsumer;

import org.opennms.core.time.ZonedDateTimeBuilder;
import org.opennms.netmgt.syslogd.ParserStageSequenceBuilder.MatchInteger;
import org.opennms.netmgt.syslogd.ParserStageSequenceBuilder.MatchUntil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This class can parse grok pattern strings to create a parser that can
 * assign parsed values to a syslog message object. It supports the following
 * pattern types:</p>
 * <ul>
 * <li>INT: Positive integer.</li>
 * <li>MONTH: 3-character English month abbreviation.</li>
 * <li>NOSPACE: String that contains no whitespace.</li>
 * <li>STRING: String. Because this matches any character, it must be followed by a delimiter in the pattern string.</li>
 * </ul>
 * 
 * @author Seth
 */
public abstract class GrokParserStageSequenceBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(GrokParserStageSequenceBuilder.class);

	private static enum GrokState {
		TEXT,
		ESCAPE_PATTERN,
		START_PATTERN,
		PATTERN,
		SEMANTIC,
		END_PATTERN
	}

	private static enum GrokPattern {
		INT,
		MONTH,
		NOSPACE,
		STRING
	}

	/**
	 * This enum contains all well-known syslog message fields.
	 */
	public static enum SyslogSemanticType {
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

		/**
		 * Remaining string message.
		 */
		message
	}

	/**
	 * This function maps {@link SyslogSemanticType} values of type int to fields in the parser
	 * state.
	 * 
	 * @param semanticString
	 * @return
	 */
	private static BiConsumer<ParserState,Integer> semanticIntegerToField(String semanticString) {
		SyslogSemanticType semanticType = null;
		try {
			semanticType = SyslogSemanticType.valueOf(semanticString);
		} catch (IllegalArgumentException e) {
			// Leave semanticType == null
		}

		if (semanticType == null) {
			return (s,v) -> {
				s.message.setParam(semanticString, v);
			};
		} else {
			switch(semanticType) {
			case day:
				return (s,v) -> {
					s.message.setDayOfMonth(v);
				};
			case facilityPriority:
				return (s,v) -> {
					SyslogFacility facility = SyslogFacility.getFacilityForCode(v);
					SyslogSeverity priority = SyslogSeverity.getSeverityForCode(v);

					s.message.setFacility(facility);
					s.message.setSeverity(priority);
				};
			case hour:
				return (s,v) -> {
					s.message.setHourOfDay(v);
				};
			case minute:
				return (s,v) -> {
					s.message.setMinute(v);
				};
			case month:
				return (s,v) -> {
					s.message.setMonth(v);
				};
			case processId:
				// processId can be an integer or string
				return (s,v) -> {
					s.message.setProcessId(String.valueOf(v));
				};
			case second:
				return (s,v) -> {
					s.message.setSecond(v);
				};
			// TODO: This should be handled as a string... this is only
			// in here as a stopgap until we create a DIGITS pattern type.
			case secondFraction:
				return (s,v) -> {
					if (v >= 1000) {
						s.message.setMillisecond(Math.round(v / 1000f));
					} else {
						s.message.setMillisecond(v);
					}
				};
			case version:
				return (s,v) -> {
					s.message.setVersion(v);
				};
			case year:
				return (s,v) -> {
					s.message.setYear(v);
				};
			default:
				throw new IllegalArgumentException(String.format("Semantic type %s does not have an integer value", semanticString));
			}
		}

	}

	/**
	 * This function maps {@link SyslogSemanticType} values of type String to fields in the parser
	 * state.
	 * 
	 * @param semanticString
	 * @return
	 */
	private static BiConsumer<ParserState,String> semanticStringToField(String semanticString) {
		SyslogSemanticType semanticType = null;
		try {
			semanticType = SyslogSemanticType.valueOf(semanticString);
		} catch (IllegalArgumentException e) {
			// Leave semanticType == null
		}

		if (semanticType == null) {
			return (s,v) -> {
				s.message.setParam(semanticString, v);
			};
		} else {
			switch(semanticType) {
			case hostname:
				return (s,v) -> {
					s.message.setHostName(v);
				};
			case message:
				return (s,v) -> {
					// Trim the message to match behavior of legacy parsers
					s.message.setMessage(v == null ? null : v.trim());
				};
			case messageId:
				// Unique to this parser
				return (s,v) -> {
					if ("-".equals(v.trim())) {
						// Ignore
					} else {
						s.message.setMessageID(v);
					}
				};
			case processId:
				// processId can be an integer or string
				return (s,v) -> {
					if ("-".equals(v.trim())) {
						// Ignore
					} else {
						s.message.setProcessId(v);
					}
				};
			case processName:
				return (s,v) -> {
					if ("-".equals(v.trim())) {
						// Ignore
					} else {
						s.message.setProcessName(v);
					}
				};
			case secondFraction:
				return (s,v) -> {
					// Convert the fraction value into a millisecond value since that
					// is the best resolution that EventBuilder can handle
					switch(v.length()) {
					case 1:
						s.message.setMillisecond(MatchInteger.trimAndConvert(v) * 100);
						break;
					case 2:
						s.message.setMillisecond(MatchInteger.trimAndConvert(v) * 10);
						break;
					case 3:
						s.message.setMillisecond(MatchInteger.trimAndConvert(v));
						break;
					case 4:
						s.message.setMillisecond(MatchInteger.trimAndConvert(v) / 10);
						break;
					case 5:
						s.message.setMillisecond(MatchInteger.trimAndConvert(v) / 100);
						break;
					case 6:
						s.message.setMillisecond(MatchInteger.trimAndConvert(v) / 1000);
						break;
					}
				};
			case timezone:
				return (s,v) -> {
					s.message.setZoneId(ZonedDateTimeBuilder.parseZoneId(v));
				};
			default:
				throw new IllegalArgumentException(String.format("Semantic type %s does not have a string value", semanticString));
			}
		}
	}

	public static List<ParserStage> parseGrok(String grok) {
		GrokState state = GrokState.TEXT;
		ParserStageSequenceBuilder factory = new ParserStageSequenceBuilder();

		StringBuilder pattern = new StringBuilder();
		StringBuilder semantic = new StringBuilder();

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
					case NOSPACE:
						// TODO: We need to peek forward to the escaped character and then do the same as the default case
						// factory.stringUntil(MatchUntil.WHITESPACE + c, semanticStringToEventBuilder(semanticString));
						// factory.character(c);
						// break;
						throw new UnsupportedOperationException("Cannot support escape sequence directly after a NOSPACE pattern yet");
					case STRING:
						// TODO: We need to peek forward to the escaped character and then do the same as the default case
						// factory.stringUntil(String.valueOf(c), semanticStringToEventBuilder(semanticString));
						// factory.character(c);
						// break;
						throw new UnsupportedOperationException("Cannot support escape sequence directly after a STRING pattern yet");
					case INT:
						factory.integer(semanticIntegerToField(semanticString));
						break;
					case MONTH:
						factory.monthString(semanticIntegerToField(semanticString));
						break;
					}
					pattern = new StringBuilder();
					semantic = new StringBuilder();
					state = GrokState.ESCAPE_PATTERN;
					continue;
				case '%':
					switch(patternType) {
					case NOSPACE:
						// This is probably not an intended behavior
						LOG.warn("NOSPACE pattern followed immediately by another pattern will greedily consume until whitespace is encountered");
						factory.stringUntilWhitespace(semanticStringToField(semanticString));
						factory.whitespace();
						break;
					case STRING:
						// TODO: Can we handle this case?
						throw new IllegalArgumentException(String.format("Invalid pattern: %s:%s does not have a trailing delimiter, cannot determine end of string", patternString, semanticString));
					case INT:
						factory.integer(semanticIntegerToField(semanticString));
						break;
					case MONTH:
						factory.monthString(semanticIntegerToField(semanticString));
						break;
					}
					pattern = new StringBuilder();
					semantic = new StringBuilder();
					state = GrokState.START_PATTERN;
					continue;
				case ' ':
					switch(patternType) {
					case NOSPACE:
					case STRING:
						factory.stringUntilWhitespace(semanticStringToField(semanticString));
						factory.whitespace();
						break;
					case INT:
						factory.intUntilWhitespace(semanticIntegerToField(semanticString));
						factory.whitespace();
						break;
					case MONTH:
						factory.monthString(semanticIntegerToField(semanticString));
						factory.whitespace();
						break;
					}
					break;
				default:
					switch(patternType) {
					case NOSPACE:
						factory.stringUntil(MatchUntil.WHITESPACE + c, semanticStringToField(semanticString));
						factory.character(c);
						break;
					case STRING:
						factory.stringUntil(String.valueOf(c), semanticStringToField(semanticString));
						factory.character(c);
						break;
					case INT:
						factory.integer(semanticIntegerToField(semanticString));
						factory.character(c);
						break;
					case MONTH:
						factory.monthString(semanticIntegerToField(semanticString));
						factory.character(c);
						break;
					}
				}
				pattern = new StringBuilder();
				semantic = new StringBuilder();
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
			case NOSPACE:
			case STRING:
				factory.terminal().string(semanticStringToField(semanticString));
				break;
			case INT:
				factory.terminal().integer(semanticIntegerToField(semanticString));
				break;
			case MONTH:
				factory.terminal().monthString(semanticIntegerToField(semanticString));
				break;
			}
		}

		return factory.getStages();
	}
}
