package org.opennms.web.event.filter;


/** Encapsulates filtering on exact unique event identifiers. */
public class AcknowledgedByFilter extends Object implements Filter {
    public static final String TYPE = "acknowledgedBy";
    protected String user;

    public AcknowledgedByFilter( String user ) {
        if( user == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.user = user;
    }

    public String getSql() {
        return( " EVENTACKUSER='" + this.user + "'" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.user );
    }
    
    public String getTextDescription() {
        return this.getDescription();
    }

    public String toString() {
        return( "<EventFactory.AcknowledgedByFilter: " + this.getDescription() + ">" );
    }

    public String getAcknowledgedByFilter() {
        return( this.user );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

