package org.opennms.web.event.filter;

import org.opennms.web.event.EventUtil;


/** 
 * Encapsulates negative severity filtering functionality, that is filtering
 * OUT this value instead of only filtering IN this value.
 */
public class NegativeSeverityFilter extends Object implements Filter {
    public static final String TYPE = "severitynot";
    protected int severity;

    public NegativeSeverityFilter( int severity ) {
        this.severity = severity;
    }

    public String getSql() {
        return( " EVENTSEVERITY<>" + this.severity );
    }

    public String getDescription() {
        return( TYPE + "=" + this.severity );
    }
    
    public String getTextDescription(){
        return( "severity is not " + EventUtil.getSeverityLabel( this.severity ) );
    }

    public String toString() {
        return( "<EventFactory.NegativeSeverityFilter: " + this.getDescription() + ">" );
    }

    public int getSeverity() {
        return( this.severity );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

