package org.opennms.web.event.filter;


/** Encapsulates all interface filtering functionality. */
public class IPLikeFilter extends Object implements Filter {
    public static final String TYPE = "iplike";
    protected String ipLikePattern;

    public IPLikeFilter( String ipLikePattern ) {
        if( ipLikePattern == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.ipLikePattern = ipLikePattern;
    }

    public String getSql() {
        return( " IPLIKE(IPADDR,'" + this.ipLikePattern + "')" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.ipLikePattern );
    }
    
    public String getTextDescription() {
        return( "IP Address like \"" + this.ipLikePattern + "\"" );
    }

    public String toString() {
        return( "<IPLikeFilter: " + this.getDescription() + ">" );
    }

    public String getIpLikePattern() {
        return( this.ipLikePattern );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}


