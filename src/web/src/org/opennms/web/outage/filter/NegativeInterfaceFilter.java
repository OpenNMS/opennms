package org.opennms.web.outage.filter;


/** Encapsulates all interface filtering functionality. */
public class NegativeInterfaceFilter extends Object implements Filter 
{
    public static final String TYPE = "interfacenot";
    protected String ipAddress;

    public NegativeInterfaceFilter( String ipAddress ) {
        if( ipAddress == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.ipAddress = ipAddress;
    }

    public String getSql() {
        return( " (OUTAGES.IPADDR<>'" + this.ipAddress + "' OR OUTAGES.IPADDR IS NULL)" );        
    }

    public String getDescription() {
        return( TYPE + "=" + this.ipAddress );
    }

    public String getTextDescription() {
        return( "interface is not " + this.ipAddress );
    }
    
    public String toString() {
        return( "<OutageFactory.InterfaceFilter: " + this.getDescription() + ">" );
    }

    public String getIpAddress() {
        return( this.ipAddress );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}



