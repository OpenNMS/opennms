package org.opennms.web.event.filter;


public class DescriptionSubstringFilter extends Object implements Filter 
{
    public static final String TYPE = "descsub";
    protected String substring;

    public DescriptionSubstringFilter(String substring) {
        if( substring == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        this.substring = substring;
    }

    public String getSql() {
        return( " UPPER(EVENTDESCR) LIKE '%" + this.substring.toUpperCase() + "%'" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.substring );
    }
    
    public String getTextDescription(){
        return( "description containing \"" + this.substring + "\"" );
    }

    public String toString() {
        return( "<DescriptionSubstringFilter: " + this.getDescription() + ">" );
    }

    public String getSubstring() {
        return( this.substring );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

