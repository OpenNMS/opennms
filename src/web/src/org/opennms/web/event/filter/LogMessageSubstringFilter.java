package org.opennms.web.event.filter;


public class LogMessageSubstringFilter extends Object implements Filter 
{
    public static final String TYPE = "msgsub";
    protected String substring;

    public LogMessageSubstringFilter(String substring) {
        if( substring == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        this.substring = substring;
    }

    public String getSql() {
        return( " UPPER(EVENTLOGMSG) LIKE '%" + this.substring.toUpperCase() + "%'" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.substring );
    }
    
    public String getTextDescription(){
        return( "description containing \"" + this.substring + "\"" );
    }

    public String toString() {
        return( "<LogMessageSubstringFilter: " + this.getDescription() + ">" );
    }

    public String getSubstring() {
        return( this.substring );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

