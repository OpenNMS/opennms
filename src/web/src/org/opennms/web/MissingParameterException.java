package org.opennms.web;


public class MissingParameterException extends RuntimeException
{

    protected String missingParameter;
    protected String[] requiredParameters;

    public MissingParameterException( String missingParameter ) {
        this( missingParameter, new String[] { missingParameter } );
    }


    public MissingParameterException( String missingParameter, String[] requiredParameters ) {
        if( missingParameter == null || requiredParameters == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.missingParameter = missingParameter;
        this.requiredParameters = requiredParameters;
    }


    public String getMissingParameter() {
        return( this.missingParameter );
    }


    public String[] getRequiredParameters() {
        return( this.requiredParameters );
    }
}
