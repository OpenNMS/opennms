/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sms.reflector.smsservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.USSDSessionStatus;

/**
 * <p>MobileMsgResponseMatchers class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MobileMsgResponseMatchers {
    private static final Logger LOG = LoggerFactory.getLogger(MobileMsgResponseMatchers.class);

	/**
	 * <p>smsFrom</p>
	 *
	 * @param originator a {@link java.lang.String} object.
	 * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
	 */
	public static MobileMsgResponseMatcher smsFrom(final String originator) {
		return new MobileMsgResponseMatcher() {
			
                        @Override
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				LOG.trace("smsFrom.matches({}, {}, {})", originator, request, response);
				if (response instanceof SmsResponse) {
					SmsResponse resp = (SmsResponse)response;
					return isAMatch(originator, resp.getOriginator());
				}
				return false;
			}
			
                        @Override
			public String toString() {
				return "smsFromRecipient()";
			}
		};
	}

	/**
	 * <p>smsFromRecipient</p>
	 *
	 * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
	 */
	public static MobileMsgResponseMatcher smsFromRecipient() {
		return new MobileMsgResponseMatcher() {
			
                        @Override
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				LOG.trace("smsFromRecipient.matches({}, {})", request, response);
				if (request instanceof SmsRequest && response instanceof SmsResponse) {
					SmsRequest req = (SmsRequest)request;
					SmsResponse resp = (SmsResponse)response;

					if (resp.getOriginator().equals(req.getRecipient())) {
						return true;
					}
					String originator = resp.getOriginator().replaceFirst("^\\+", "");
					String recipient = req.getRecipient().replaceFirst("^\\+", "");
					return originator.equals(recipient);
				}

				return false;
			}
			
                        @Override
			public String toString() {
				return "smsFromRecipient()";
			}
		};
	}

	/**
	 * <p>and</p>
	 *
	 * @param matchers a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
	 * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
	 */
	public static MobileMsgResponseMatcher and(final MobileMsgResponseMatcher... matchers) {
		return new MobileMsgResponseMatcher() {
			
                        @Override
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				LOG.trace("and.matches({})", (Object)matchers);
				for (MobileMsgResponseMatcher matcher : matchers) {
					if (!matcher.matches(request, response)) {
						return false;
					}
				}

				return true;
			}
			
                        @Override
			public String toString() {
				StringBuffer sb = new StringBuffer();
				sb.append("and(");
				boolean first = true;
				for (MobileMsgResponseMatcher matcher : matchers) {
					if (first) {
						first = false;
					} else {
						sb.append(", ");
					}
					sb.append(matcher.toString());
				}
				sb.append(")");
				return sb.toString();
			}
		};
	}

	/**
	 * <p>or</p>
	 *
	 * @param matchers a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
	 * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
	 */
	public static MobileMsgResponseMatcher or(final MobileMsgResponseMatcher... matchers) {
		return new MobileMsgResponseMatcher() {
			
                        @Override
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				LOG.trace("or.matches({})", (Object)matchers);
				for (MobileMsgResponseMatcher matcher : matchers) {
					if (matcher.matches(request, response)) {
						return true;
					}
				}

				return false;
			}

                        @Override
			public String toString() {
				StringBuffer sb = new StringBuffer();
				sb.append("or(");
				boolean first = true;
				for (MobileMsgResponseMatcher matcher : matchers) {
					if (first) {
						first = false;
					} else {
						sb.append(", ");
					}
					sb.append(matcher.toString());
				}
				sb.append(")");
				return sb.toString();
			}
		};
	}
	/**
	 * <p>textMatches</p>
	 *
	 * @param regex a {@link java.lang.String} object.
	 * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
	 */
	public static MobileMsgResponseMatcher textMatches(final String regex) {
		return new MobileMsgResponseMatcher() {
			
                        @Override
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				LOG.trace("textMatches({}, {}, {})", regex, request, response);
				String text = response.getText() == null ? "" : response.getText();
				return text.matches(regex);
			}
			
                        @Override
			public String toString() {
				return "textMatches(\"" + regex + "\")";
			}
		};
	}

	/**
	 * <p>isSms</p>
	 *
	 * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
	 */
	public static MobileMsgResponseMatcher isSms() {
		return new MobileMsgResponseMatcher() {
			
                        @Override
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				LOG.trace("sms({}, {})", request, response);
				if (response instanceof SmsResponse) {
					return true;
				}
				return false;
			}
			
                        @Override
			public String toString() {
				return "isSms()";
			}
		};
	}

	/**
	 * <p>isUssd</p>
	 *
	 * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
	 */
	public static MobileMsgResponseMatcher isUssd() {
		return new MobileMsgResponseMatcher() {
			
                        @Override
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				LOG.trace("ussd({}, {})", request, response);
				if (response instanceof UssdResponse) {
					return true;
				}
				return false;
			}
			
                        @Override
			public String toString() {
				return "isUssd()";
			}
		};
	}
	
	/**
	 * <p>ussdStatusIs</p>
	 *
	 * @param status a {@link org.smslib.USSDSessionStatus} object.
	 * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
	 */
	public static MobileMsgResponseMatcher ussdStatusIs(final USSDSessionStatus status) {
		return new MobileMsgResponseMatcher() {
			
                        @Override
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				LOG.trace("ussdStatusIs({}, {})", status, request, response);
				if (response instanceof UssdResponse) {
					UssdResponse resp = (UssdResponse)response;
					
					return status.equals(resp.getSessionStatus());
				}

				return false;
			}
			
                        @Override
			public String toString() {
				return "ussdStatusIs(" + status + ")";
			}
		};
	}

	/**
	 * <p>isAMatch</p>
	 *
	 * @param expected a {@link java.lang.String} object.
	 * @param actual a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean isAMatch(String expected, String actual) {
	    if (expected.startsWith("~") && expected.length() > 1) {
	        return actual.matches(expected.substring(1));
	    }
	    return actual.equals(expected);
	}
}
