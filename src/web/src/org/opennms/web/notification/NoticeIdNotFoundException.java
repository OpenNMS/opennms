package org.opennms.web.notification;


public class NoticeIdNotFoundException extends RuntimeException
{
    protected String badId;
    protected String message;
    
    public NoticeIdNotFoundException( String msg, String id ) {
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
