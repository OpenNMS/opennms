/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.xml.eventconf;

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

import static org.opennms.netmgt.xml.eventconf.Maskelement.*;

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
		} else if (name.equals(TAG_SNMP_TRAPOID)) {
			return new EventField(name) {
				@Override
				public String get(Event event) {
					return (event.getSnmp() == null || !event.getSnmp().hasTrapOID()) ?
							null : event.getSnmp().getTrapOID();
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
