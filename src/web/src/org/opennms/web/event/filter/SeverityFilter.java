package org.opennms.web.event.filter;

import org.opennms.web.event.EventUtil;


/** Encapsulates severity filtering functionality. */
public class SeverityFilter extends Object implements Filter {
    public static final String TYPE = "severity";
    protected int severity;

    public SeverityFilter( int severity ) {
        this.severity = severity;
    }

    public String getSql() {
        return( " EVENTSEVERITY=" + this.severity );
    }

    public String getDescription() {
        return( TYPE + "=" + this.severity );
    }
    
    public String getTextDescription(){
        return( TYPE + "=" + EventUtil.getSeverityLabel( this.severity ) );
    }

    public String toString() {
        return( "<EventFactory.SeverityFilter: " + this.getDescription() + ">" );
    }

    public int getSeverity() {
        return( this.severity );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

