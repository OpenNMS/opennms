package org.opennms.web.event.filter;


/** Encapsulates all interface filtering functionality. */
public class NegativeInterfaceFilter extends Object implements Filter {
    public static final String TYPE = "interfacenot";
    protected String ipAddress;

    public NegativeInterfaceFilter( String ipAddress ) {
        if( ipAddress == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.ipAddress = ipAddress;
    }

    public String getSql() {
        return( " (IPADDR<>'" + this.ipAddress + "' OR IPADDR IS NULL)" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.ipAddress );
    }
    
    public String getTextDescription() {
        return( "interface is not " + this.ipAddress );
    }

    public String toString() {
        return( "<EventFactory.NegativeInterfaceFilter: " + this.getDescription() + ">" );
    }

    public String getIpAddress() {
        return( this.ipAddress );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}
