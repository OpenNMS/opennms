package org.opennms.web.event.filter;


/** Encapsulates filtering on exact unique event identifiers. */
public class ExactUEIFilter extends Object implements Filter {
    public static final String TYPE = "exactUei";
    protected String uei;

    public ExactUEIFilter( String uei ) {
        if( uei == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.uei = uei;
    }

    public String getSql() {
        return( " EVENTUEI='" + this.uei + "'" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.uei );
    }
    
    public String getTextDescription() {
        return this.getDescription();
    }

    public String toString() {
        return( "<EventFactory.ExactUEIFilter: " + this.getDescription() + ">" );
    }

    public String getUEI() {
        return( this.uei );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}


