package org.opennms.web.outage;


public class OutageIdNotFoundException extends RuntimeException
{
    protected String badId;
    protected String message;
    
    public OutageIdNotFoundException( String msg, String id ) {
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
