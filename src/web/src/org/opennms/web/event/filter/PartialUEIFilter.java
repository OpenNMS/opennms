package org.opennms.web.event.filter;


/** Encapsulates filtering on partial unique event identifiers. */
public class PartialUEIFilter extends Object implements Filter {
    public static final String TYPE = "partialUei";
    protected String uei;

    public PartialUEIFilter( String uei ) {
        if( uei == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.uei = uei;
    }

    public String getSql() {
        return( " LOWER(EVENTUEI) LIKE '%" + this.uei.toLowerCase() + "%'" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.uei );
    }
    
    public String getTextDescription() {
        return this.getDescription();
    }

    public String toString() {
        return( "<EventFactory.PartialUEIFilter: " + this.getDescription() + ">" );
    }

    public String getUEI() {
        return( this.uei );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

