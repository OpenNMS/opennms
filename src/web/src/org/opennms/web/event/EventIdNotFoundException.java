package org.opennms.web.event;


public class EventIdNotFoundException extends RuntimeException
{
    protected String badId;
    protected String message;
    
    public EventIdNotFoundException( String msg, String id ) {
        this.message = msg;
	this.badId = id;
    }
    
    public String getMessage() {
	return this.message;
    }
    
    public String getBadID() {
        return this.badId;
    }
}
