package edu.ncsu.pdgrenon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMessage {

	private String m_logMessage;
	
	public LogMessage(String logMessage) {
		m_logMessage = logMessage;
	}
	public String getMessage () {
		return m_logMessage;
	}
	boolean isEndMessage() {
		return getMessage().contains("end");
	}
	@Override
	public String toString() {
		return m_logMessage;
	}
	boolean isPersistMessage() {
		return getMessage().contains("persist");
	}
	boolean isBeginMessage() {
		return getMessage().contains("begin");
	}
	boolean isErrorMessage() {
		return getMessage().contains("error");
	}
	boolean isCollectorBeginMessage() {
		return getMessage().contains("collectData: begin:");
	}
	boolean isCollectorEndMessage() {
		return getMessage().contains("collectData: end:");
	}
	
	Date getDate() {
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
	public String getServiceID() {
		String regex = "\\d+/\\d+\\.\\d+\\.\\d+\\.\\d+/[\\w-]+";
		Pattern service = Pattern.compile(regex);
		Matcher serviceMatcher = service.matcher(getMessage());
		if(serviceMatcher.find()) {
			return serviceMatcher.group();
		}
		return null;
	}
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
