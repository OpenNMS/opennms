package org.opennms.sms.reflector.smsservice;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.smslib.USSDSessionStatus;

public class MobileMsgResponseMatchers {

	public static MobileMsgResponseMatcher smsFrom(final String originator) {
		return new MobileMsgResponseMatcher() {
			
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				tracef("smsFrom.matches(%s, %s, %s)", originator, request, response);
				if (response instanceof SmsResponse) {
					SmsResponse resp = (SmsResponse)response;
					return resp.getOriginator().equals(originator);
				}
				return false;
			}
			
			public String toString() {
				return "smsFromRecipient()";
			}
		};
	}

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
	public static MobileMsgResponseMatcher textMatches(final String regex) {
		return new MobileMsgResponseMatcher() {
			
			public boolean matches(MobileMsgRequest request, MobileMsgResponse response) {
				tracef("textMatches(%s, %s, %s)", regex, request, response);
				return response.getText().matches(regex);
			}
			
			public String toString() {
				return "textMatches(\"" + regex + "\")";
			}
		};
	}

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

	public static void tracef(String format, Object... args) {
		Logger log = ThreadCategory.getInstance(MobileMsgResponseMatchers.class);
		
		if (log.isTraceEnabled()) {
			log.trace(String.format(format, args));
		}
	}
}
