package org.opennms.web.outage.filter;


/** Encapsulates all interface filtering functionality. */
public class InterfaceFilter extends Object implements Filter 
{
    public static final String TYPE = "interface";
    protected String ipAddress;

    public InterfaceFilter( String ipAddress ) {
        if( ipAddress == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.ipAddress = ipAddress;
    }

    public String getSql() {
        return( " OUTAGES.IPADDR='" + this.ipAddress + "'" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.ipAddress );
    }

    public String getTextDescription() {
        return( TYPE + " is " + this.ipAddress );
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



