package org.opennms.web.event.filter;


/** Encapsulates filtering on partial unique event identifiers. */
public class NegativePartialUEIFilter extends Object implements Filter {
    public static final String TYPE = "partialUeiNot";
    protected String uei;

    public NegativePartialUEIFilter( String uei ) {
        if( uei == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.uei = uei;
    }

    public String getSql() {
        return( " LOWER(EVENTUEI) NOT LIKE '%" + this.uei.toLowerCase() + "%'" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.uei );
    }
    
    public String getTextDescription() {
        return( "partial UEI not like " + this.uei );
    }

    public String toString() {
        return( "<EventFactory.NegativePartialUEIFilter: " + this.getDescription() + ">" );
    }

    public String getUEI() {
        return( this.uei );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}


