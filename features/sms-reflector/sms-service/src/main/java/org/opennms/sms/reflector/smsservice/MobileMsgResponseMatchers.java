package org.opennms.sms.reflector.smsservice;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.smslib.USSDSessionStatus;

/**
 * <p>MobileMsgResponseMatchers class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MobileMsgResponseMatchers {

	/**
	 * <p>smsFrom</p>
	 *
	 * @param originator a {@link java.lang.String} object.
	 * @return a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseMatcher} object.
	 */
	public static MobileMsgResponseMatcher smsFrom(final String originator) {
		return new MobileMsgResponseMatcher() {
			
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				tracef("smsFrom.matches(%s, %s, %s)", originator, request, response);
				if (response instanceof SmsResponse) {
					SmsResponse resp = (SmsResponse)response;
					return isAMatch(originator, resp.getOriginator());
				}
				return false;
			}
			
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
			
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				tracef("smsFromRecipient.matches(%s, %s)", request, response);
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
			
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				tracef("and.matches(%s)", (Object)matchers);
				for (MobileMsgResponseMatcher matcher : matchers) {
					if (!matcher.matches(request, response)) {
						return false;
					}
				}

				return true;
			}
			
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
			
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				tracef("or.matches(%s)", (Object)matchers);
				for (MobileMsgResponseMatcher matcher : matchers) {
					if (matcher.matches(request, response)) {
						return true;
					}
				}

				return false;
			}

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
			
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				tracef("textMatches(%s, %s, %s)", regex, request, response);
				String text = response.getText() == null ? "" : response.getText();
				return text.matches(regex);
			}
			
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
			
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				tracef("sms(%s, %s)", request, response);
				if (response instanceof SmsResponse) {
					return true;
				}
				return false;
			}
			
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
			
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				tracef("ussd(%s, %s)", request, response);
				if (response instanceof UssdResponse) {
					return true;
				}
				return false;
			}
			
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
			
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				tracef("ussdStatusIs(%s, %s)", status, request, response);
				if (response instanceof UssdResponse) {
					UssdResponse resp = (UssdResponse)response;
					
					return status.equals(resp.getSessionStatus());
				}

				return false;
			}
			
			public String toString() {
				return "ussdStatusIs(" + status + ")";
			}
		};
	}

	/**
	 * <p>tracef</p>
	 *
	 * @param format a {@link java.lang.String} object.
	 * @param args a {@link java.lang.Object} object.
	 */
	public static void tracef(String format, Object... args) {
		ThreadCategory log = ThreadCategory.getInstance(MobileMsgResponseMatchers.class);
		
		if (log.isTraceEnabled()) {
			log.trace(String.format(format, args));
		}
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
