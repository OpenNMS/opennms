package org.opennms.util.ilr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleLogMessage implements LogMessage {

	public static LogMessage create(String logMessage) {
		return new SimpleLogMessage(logMessage);
	}
	private String m_logMessage;
	
	private SimpleLogMessage(String logMessage) {
		m_logMessage = logMessage;
	}
	/* (non-Javadoc)
	 * @see org.opennms.util.ilr.LogMessage#getMessage()
	 */
	public String getMessage () {
		return m_logMessage;
	}
	/* (non-Javadoc)
	 * @see org.opennms.util.ilr.LogMessage#isEndMessage()
	 */
	public boolean isEndMessage() {
		return getMessage().contains("end");
	}
	@Override
	public String toString() {
		return m_logMessage;
	}
	/* (non-Javadoc)
	 * @see org.opennms.util.ilr.LogMessage#isPersistMessage()
	 */
	public boolean isPersistMessage() {
		return getMessage().contains("persist");
	}
	/* (non-Javadoc)
	 * @see org.opennms.util.ilr.LogMessage#isBeginMessage()
	 */
	public boolean isBeginMessage() {
		return getMessage().contains("begin");
	}
	/* (non-Javadoc)
	 * @see org.opennms.util.ilr.LogMessage#isErrorMessage()
	 */
	public boolean isErrorMessage() {
		return getMessage().contains("error");
	}
	/* (non-Javadoc)
	 * @see org.opennms.util.ilr.LogMessage#isCollectorBeginMessage()
	 */
	public boolean isCollectorBeginMessage() {
		return getMessage().contains("collectData: begin:") || getMessage().contains("collector.initialize: begin");
	}
	/* (non-Javadoc)
	 * @see org.opennms.util.ilr.LogMessage#isCollectorEndMessage()
	 */
	public boolean isCollectorEndMessage() {
		return getMessage().contains("collectData: end:") || getMessage().contains("collector.initialize: end");
	}
	
	/* (non-Javadoc)
	 * @see org.opennms.util.ilr.LogMessage#getDate()
	 */
	public Date getDate() {
		String regex =  "\\d+-\\d+-\\d+\\s*\\d+:\\d+:\\d+,\\d+";
		Pattern timestamp = Pattern.compile(regex);
		Matcher timestampMatcher = timestamp.matcher(getMessage());
		SimpleDateFormat f = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss,S");
		if(timestampMatcher.find()) {	
				try {
					return f.parse(timestampMatcher.group());
				} catch (ParseException e) {
					throw (new IllegalArgumentException(e));
				}
		}else{
			throw (new IllegalArgumentException("Does not match"));
		}
	}
	/* (non-Javadoc)
	 * @see org.opennms.util.ilr.LogMessage#getServiceID()
	 */
	public String getServiceID() {
		String regex = "\\d+/\\d+\\.\\d+\\.\\d+\\.\\d+/[\\w-]+";
		Pattern service = Pattern.compile(regex);
		Matcher serviceMatcher = service.matcher(getMessage());
		if(serviceMatcher.find()) {
			return serviceMatcher.group();
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.opennms.util.ilr.LogMessage#getThread()
	 */
	public String getThread() {
		String regex = "\\[\\w+-\\d+\\s\\w+-\\w+\\d+\\]";
		//example: [CollectdScheduler-50 Pool-fiber11]
		Pattern thread = Pattern.compile(regex);
		Matcher threadMatcher = thread.matcher(getMessage());
		if(threadMatcher.find()) {
			return threadMatcher.group();
		}
		return null;
	}
}
