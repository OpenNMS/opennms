package org.opennms.web.category;


public class CategoryNotFoundException extends RuntimeException
{
    protected String category;

    public CategoryNotFoundException(String category) {
        super( "Could not find the " + category + " category" );

        if( category == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.category = category;
    }


    public String getCategory() {
        return( this.category );
    }
    
}
