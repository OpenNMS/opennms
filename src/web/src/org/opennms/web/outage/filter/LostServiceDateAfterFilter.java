package org.opennms.web.outage.filter;

import java.text.DateFormat;
import java.util.Date;
import org.opennms.netmgt.EventConstants;


public class LostServiceDateAfterFilter extends Object implements Filter 
{
    public static final String TYPE = "lostafter";
    protected static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);    

    protected Date date;

    public LostServiceDateAfterFilter(Date date) {
        if( date == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        this.date = date;
    }
        
    public LostServiceDateAfterFilter(long epochTime) {
        this(new Date(epochTime));
    }

    public String getSql() {
        return( " ifLostService > to_timestamp(\'" + this.date.toString() + "\'," + EventConstants.POSTGRES_DATE_FORMAT+ ")" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.date.getTime() );
    }
    
    public String getTextDescription(){
        return( "lost service date after \"" + DATE_FORMAT.format(this.date) + "\"" );
    }

    public String toString() {
        return( "<Lost Service Date After Filter: " + this.getDescription() + ">" );
    }

    public Date getDate() {
        return( this.date );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

