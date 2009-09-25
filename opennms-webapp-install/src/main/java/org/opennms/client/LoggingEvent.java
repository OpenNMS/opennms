package org.opennms.client;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *  This class is a thin facade for {@link org.apache.log4j.spi.LoggingEvent}
 *  log4j messages on the server side.
 */
public class LoggingEvent implements IsSerializable{
	public enum LogLevel { DEBUG, INFO, WARN, ERROR, FATAL }
	
	public LoggingEvent() {}
	
	public String getCategory() { return "Fake.Category"; }
	public long getTimeStamp() { return new Date().getTime(); }
	public LogLevel getLevel() { return LogLevel.INFO; }
	public String getMessage() { return "Hello world."; }
}
