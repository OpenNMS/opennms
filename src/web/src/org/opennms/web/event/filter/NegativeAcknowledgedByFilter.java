package org.opennms.web.event.filter;


/** Encapsulates filtering on exact unique event identifiers. */
public class NegativeAcknowledgedByFilter extends Object implements Filter {
    public static final String TYPE = "acknowledgedByNot";
    protected String user;

    public NegativeAcknowledgedByFilter( String user ) {
        if( user == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.user = user;
    }

    public String getSql() {
        return( " (EVENTACKUSER<>'" + this.user + "' OR EVENTACKUSER IS NULL)" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.user );
    }
    
    public String getTextDescription() {
        return( "not acknowledged by " + this.user );
    }

    public String toString() {
        return( "<EventFactory.NegativeAcknowledgedByFilter: " + this.getDescription() + ">" );
    }

    public String getAcknowledgedByFilter() {
        return( this.user );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

