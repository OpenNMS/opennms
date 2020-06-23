/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import static org.opennms.netmgt.xml.eventconf.Maskelement.TAG_HOST;
import static org.opennms.netmgt.xml.eventconf.Maskelement.TAG_INTERFACE;
import static org.opennms.netmgt.xml.eventconf.Maskelement.TAG_NODEID;
import static org.opennms.netmgt.xml.eventconf.Maskelement.TAG_SERVICE;
import static org.opennms.netmgt.xml.eventconf.Maskelement.TAG_SNMPHOST;
import static org.opennms.netmgt.xml.eventconf.Maskelement.TAG_SNMP_COMMUNITY;
import static org.opennms.netmgt.xml.eventconf.Maskelement.TAG_SNMP_EID;
import static org.opennms.netmgt.xml.eventconf.Maskelement.TAG_SNMP_GENERIC;
import static org.opennms.netmgt.xml.eventconf.Maskelement.TAG_SNMP_SPECIFIC;
import static org.opennms.netmgt.xml.eventconf.Maskelement.TAG_SOURCE;
import static org.opennms.netmgt.xml.eventconf.Maskelement.TAG_UEI;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.RegexUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

public abstract class EventMatchers  {
	public static EventMatcher falseMatcher() {
		return new EventMatcher() {

			@Override
			public MatchResult matches(Event matchingEvent) {
				return MatchResult.FALSE;
			}

			@Override
			public String toString() {
				return "false";
			}

		};
	}

	public static EventMatcher trueMatcher() {
		return new EventMatcher() {

			@Override
			public MatchResult matches(Event matchingEvent) {
				return MatchResult.TRUE;
			}

			@Override
			public String toString() {
				return "true";
			}

		};
	}

	public static EventMatcher ueiMatcher(final String uei) {
		return new EventMatcher() {
			public MatchResult matches(org.opennms.netmgt.xml.event.Event matchingEvent) {
				final String matchingUei = matchingEvent.getUei();
				return MatchResult.of(uei.equals(matchingUei));
			}
			@Override
			public String toString() {
				return "event.uei=="+uei;
			}
		};

	}


	public static EventMatcher and(final EventMatcher... matchers) {
		return new EventMatcher() {

			@Override
			public MatchResult matches(Event matchingEvent) {
				MatchResult match = MatchResult.TRUE;
				for(EventMatcher matcher : matchers) {
					match = match.and(matcher.matches(matchingEvent));
					if (!match.matched()) {
						return match;
					}
				}
				return match;
			}

			@Override
			public String toString() {
				final StringBuilder buf = new StringBuilder();
				boolean first = true;
				for(EventMatcher matcher : matchers) {
					if (first) {
						first = false;
					} else {
						buf.append("&&");
					}
					buf.append("(").append(matcher).append(")");
				}

				return buf.toString();
			}
		};
	}

	public static EventMatcher or(final EventMatcher... matchers) {
		return new EventMatcher() {

			@Override
			public MatchResult matches(Event matchingEvent) {
				for(EventMatcher matcher : matchers) {
					final MatchResult match = matcher.matches(matchingEvent);
					if (match.matched()) {
						return match;
					}
				}
				return MatchResult.FALSE;
			}

			@Override
			public String toString() {
				final StringBuilder buf = new StringBuilder();
				boolean first = true;
				for(EventMatcher matcher : matchers) {
					if (first) {
						first = false;
					} else {
						buf.append("||");
					}
					buf.append("(").append(matcher).append(")");
				}

				return buf.toString();
			}

		};
	}

	public static Field varbind(final int vbnumber) {
		if (vbnumber <= 0) {
			throw new IllegalArgumentException("Invalid varbind index " + vbnumber + " must be 1 or greater.");
		}
		return new Field() {
			public String get(Event event) {
				List<Parm> parms = event.getParmCollection();
				return vbnumber > parms.size() ? null : EventConstants.getValueAsString(parms.get(vbnumber-1).getValue());
			}
			@Override
			public String toString() {
				return "event.varbind#"+vbnumber;
			}
		};
	}

	private static abstract class EventField implements Field {
		private String m_name;

		public EventField(String name) {
			m_name = name;
		}

		@Override
		public String toString() {
			return "event."+m_name;
		}

		public abstract String get(org.opennms.netmgt.xml.event.Event matchingEvent);
	}

	public static Field field(String name) {

        if (name.startsWith("parm[") && name.endsWith("]")) {
            String parmName = name.substring(5, name.length() - 1);
            return new EventField(name) {
                public String get(Event event) {
                    if (event != null && event.getParm(parmName) != null && event.getParm(parmName).getValue() != null) {
                        return event.getParm(parmName).getValue().getContent();
                    }
                    return null;
                }
            };
        }

		if (name.equals(TAG_UEI)) {
			return new EventField(name) { public String get(Event event) { return event.getUei(); } };
		} else if (name.equals(TAG_SOURCE)) {
			return new EventField(name) { public String get(Event event) { return event.getSource(); } };
		} else if (name.equals(TAG_NODEID)) {
			return new EventField(name) { public String get(Event event) { return Long.toString(event.getNodeid()); } };
		} else if (name.equals(TAG_HOST)) {
			return new EventField(name) { public String get(Event event) { return event.getHost(); } };
		} else if (name.equals(TAG_INTERFACE)) {
			return new EventField(name) { public String get(Event event) { return event.getInterface(); } };
		} else if (name.equals(TAG_SNMPHOST)) {
			return new EventField(name) { public String get(Event event) { return event.getSnmphost(); } };
		} else if (name.equals(TAG_SERVICE)) {
			return new EventField(name) { public String get(Event event) { return event.getService(); } };
		} else if (name.equals(TAG_SNMP_EID)) {
			return new EventField(name) { public String get(Event event) { return event.getSnmp() == null ? null : event.getSnmp().getId(); } };
		} else if (name.equals(TAG_SNMP_COMMUNITY)) {
			return new EventField(name) { public String get(Event event) { return event.getSnmp() == null ? null : event.getSnmp().getCommunity(); } };
		} else if (name.equals(TAG_SNMP_SPECIFIC)) {
			return new EventField(name) {
				public String get(Event event) {
					return event.getSnmp() == null || !event.getSnmp().hasSpecific()
							? null
							: Integer.toString(event.getSnmp().getSpecific());
				}
			};
		} else if (name.equals(TAG_SNMP_GENERIC)) {
			return new EventField(name) {
				public String get(Event event) {
					return event.getSnmp() == null || !event.getSnmp().hasGeneric()
							? null
							: Integer.toString(event.getSnmp().getGeneric());
				}
			};
		}
		else {
			throw new IllegalStateException("Field " + name + " is not understood!");
		}
	}

	public static EventMatcher valueStartsWithMatcher(final Field field, final String value) {
		final String prefix = value.substring(0, value.length()-1);

		return new EventMatcher() {

			@Override
			public MatchResult matches(Event matchingEvent) {
				String eventValue = field.get(matchingEvent);
				// we have to do equals check for compatibility with the old code
				return MatchResult.of(eventValue != null && (eventValue.startsWith(prefix) || eventValue.equals(value)));
			}

			@Override
			public String toString() {
				return field + ".startsWith(" + prefix + ")";
			}
		};
	}

	public static EventMatcher valueMatchesRegexMatcher(final Field field,	final String value) {
		final Pattern eventValueRegex = Pattern.compile(value.startsWith("~") ? value.substring(1) : value);
		final Set<String> namedCaptureGroupsFromRegex = RegexUtils.getNamedCaptureGroupsFromPattern(eventValueRegex.pattern());

        return new EventMatcher() {
            @Override
            public MatchResult matches(Event matchingEvent) {
		        final String eventValue = field.get(matchingEvent);
		        if (eventValue == null) {
		            return MatchResult.FALSE;
		        }
		        // we have to do equals check for compatibility with the old code
                if (eventValue.equals(value)) {
		            return MatchResult.TRUE;
		        }

                final Matcher eventValueMatcher = eventValueRegex.matcher(eventValue);
                if (!eventValueMatcher.matches()) {
                    return MatchResult.FALSE;
                }

                // if there are no named capture groups, return immediately
                if (namedCaptureGroupsFromRegex.isEmpty()) {
                	return MatchResult.TRUE;
				}

				// there are 1+ named capture groups in the regex, let's fetch the values for these
				final Map<String, String> extractedParms = new LinkedHashMap<>(namedCaptureGroupsFromRegex.size());
                for (String namedCaptureGroup : namedCaptureGroupsFromRegex) {
                    try {
                        final String groupValue = eventValueMatcher.group(namedCaptureGroup);
                        if (groupValue != null) {
                            extractedParms.put(namedCaptureGroup, groupValue);
                        }
                    } catch (IllegalArgumentException e) {
                        // There is no capturing group in the pattern with the given name, skip it
                    }
                }

                // include the extracted parameters in the match
                return MatchResult.of(true, extractedParms);
            }

            @Override
            public String toString() {
                return field + "~" + eventValueRegex;
            }
        };
	}

	public static EventMatcher valueEqualsMatcher(final Field field, final String value) {
		return new EventMatcher() {

			@Override
			public MatchResult matches(Event matchingEvent) {
				String eventValue = field.get(matchingEvent);
				return MatchResult.of(eventValue != null && eventValue.equals(value));
			}

			@Override
			public String toString() {
				return field + "==" + value;
			}
		};
	}

}
