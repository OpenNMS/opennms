package org.opennms.web.outage.filter;

/** Encapsulates all node filtering functionality. */
public class OutageFilter extends Object implements Filter 
{
    public static final String TYPE = "outage";
    protected int outageId;

    public OutageFilter( int outageId ) {
        this.outageId = outageId;
    }

    public String getSql() {
        return ( " OUTAGEID= " + this.outageId );
    }

    public String getDescription() {
        return( TYPE + "=" + this.outageId );
    }

    public String getTextDescription() {
        return( TYPE + " is " + this.outageId );
    }

    public String toString() {
        return( "<OutageFactory.OutageFilter: " + this.getDescription() + ">" );
    }

    public int getOutage() {
        return( this.outageId );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}


