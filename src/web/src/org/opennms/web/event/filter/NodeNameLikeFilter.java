package org.opennms.web.event.filter;


/** Encapsulates all node filtering functionality. */
public class NodeNameLikeFilter extends Object implements Filter {
    public static final String TYPE = "nodenamelike";
    protected String substring;

    public NodeNameLikeFilter(String substring) {
        if( substring == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        this.substring = substring;
    }

    public String getSql() {
        return( " UPPER(NODE.NODELABEL) LIKE '%" + this.substring.toUpperCase() + "%'" );
    }

    public String getDescription() {
        return( TYPE + "=" + this.substring );
    }
    
    public String getTextDescription(){
        return( "node name containing \"" + this.substring + "\"" );
    }

    public String toString() {
        return( "<EventFactory.NodeNameContainingFilter: " + this.getDescription() + ">" );
    }

    public String getSubstring() {
        return( this.substring );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
}

