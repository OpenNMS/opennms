//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.web;

import java.io.*;
import java.net.URLEncoder;
import java.sql.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import org.opennms.core.resource.Vault;
import org.opennms.web.element.NetworkElementFactory;


/**
 * Provides convenience functions for web-based interfaces.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public abstract class Util extends Object 
{

    /** 
     * Internal flag used to cache a servlet context parameter
     */
    protected static Boolean usePortInBaseUrls;


    /**
     * Return a string that represents the fully qualified URL
     * for our servlet context, suitable for use in the HTML
     * <em>base</em> tag.
     *
     * <p>As an example, suppose your host was www.mycompany.com,
     * you are serving from port 80, and your web application name
     * was "opennms," then this method would return: 
     * <code>http://www.mycompany.com:80/opennms/</code></p>
     *
     * @param request the servlet request you are servicing
     */
    public static String calculateUrlBase( HttpServletRequest request ) {
        if( request == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        //get what the web browser thinks is the URL
        StringBuffer buffer = request.getRequestURL();

        //get a string version of the buffer so we can search in it
        String string = buffer.toString();

        //find the "//" in something like "http://host" 
        int schemeSlashesIndex = string.indexOf("//");
        
        //find the "/" at the end of "http://host:port/"
        int schemeHostPortIndex  = string.indexOf("/", schemeSlashesIndex+2);

        //truncate everything after the base scheme, host, and port values
        buffer.setLength(schemeHostPortIndex);

        String context = request.getContextPath();

        //if the context is not the root context 
        if(!context.equals("")) {
            //context will always start with a slash
            buffer.append(context);            
	}

        //add a trailing slash        
        buffer.append("/");

        return buffer.toString();
    }


    /**
     * Convenience method for resolving the human-readable hostname for
     * an IP address, if at all possible.  If the hostname cannot be found,
     * this method returns the IP address parameter.
     *
     * @param ipAddress the IP address for which you want the hostname
     * @deprecated Please use {@link NetworkElementFactory#getHostname
     * NetworkElementFactory.getHostname} instead.
     */
    public static String getHostname( String ipAddress ) {
        String hostname = ipAddress; 

        try {
            hostname = NetworkElementFactory.getHostname( ipAddress );
        }
        catch( Exception e ) {
            //ignore this exception and just return the IP address
        }

        return( hostname );
    }


    /**
     * Convenience method for resolving the human-readable hostname for
     * an IP address, if at all possible.  If the hostname cannot be found,
     * from the table, this method returns the IP address parameter.
     * This method doesnt throw any exception.
     *
     * @param ipAddress the IP address for which you want the hostname
     * @deprecated Please use {@link NetworkElementFactory#getHostname
     * NetworkElementFactory.getHostname} instead.
     */
    public static String resolveIpAddress( String ipAddress ) {
        String hostname = ipAddress; 

        try {
            hostname = NetworkElementFactory.getHostname( ipAddress );
        }
        catch( Exception e ) {
            //ignore this exception and just return the IP address
        }

        return( hostname );
    }


    /**
     * Method used to convert an integer bits-per-second value
     * and a more commonly recognized abbreviation for network
     * interface speeds. Feel free to expand it as
     * necessary to accomodate different values.
     *
     * @param ifSpeed The bits-per-second value to be converted
     *    into a string description
     * @return A string representation of the speed
     *    (&quot;100 Mbps&quot; for example)
     */
    public static String getHumanReadableIfSpeed(int ifSpeed)
    {
        if (ifSpeed == 10000000)
            return "10 Mbps";
        else if (ifSpeed == 100000000)
            return "100 Mbps";
        else if (ifSpeed == 1000000000)
            return "1.0 Gbps";
        else
            return (String.valueOf(ifSpeed) + " bps");
    }


    /**
     * Creates hidden tags for all the parameters given in the request.
     *
     * @param request the <code>HttpServletRequest</code> to read the parameters from
     * @return A string containing an HTML &lt;input type="hidden" 
     * name="<code>paramName</code>" value="<code>paramValue</code>" /&gt; tag 
     * for each parameter.
     */
    public static String makeHiddenTags( HttpServletRequest request ) {
        return( makeHiddenTags( request, new HashMap(), new String[0] ));
    }


    /**
     * Creates hidden tags for all the parameters given in the request.
     *
     * @param request the <code>HttpServletRequest</code> to read the parameters from
     * @param additions a map of extra parameters to create hidden tags for
     * @return A string containing an HTML &lt;input type="hidden"
     * name="<code>paramName</code>" value="<code>paramValue</code>" /&gt; tag
     * for each parameter.
     */
    public static String makeHiddenTags( HttpServletRequest request, Map additions ) {
        return( makeHiddenTags( request, additions, new String[0] ));
    }


    /**
     * Creates hidden tags for all the parameters given in the request.
     *
     * @param request the <code>HttpServletRequest</code> to read the parameters from
     * @param ignores A string array containing request parameters to ignore
     * @return A string containing an HTML &lt;input type="hidden"
     * name="<code>paramName</code>" value="<code>paramValue</code>" /&gt; tag
     * for each parameter.
     */
    public static String makeHiddenTags( HttpServletRequest request, String[] ignores ) {
        return( makeHiddenTags( request, new HashMap(), ignores ));
    }


    /**
     * Creates hidden tags for all the parameters given in the request plus the additions,
     * except for the parameters and additions listed in the ignore list.
     *
     * @param request the <code>HttpServletRequest</code> to read the parameters from
     * @param additions a map of extra parameters to create hidden tags for
     * @param ignores the list of parameters not to create a hidden tag for
     * @return A string containing an HTML &lt;input type="hidden" 
     * name="<code>paramName</code>" value="<code>paramValue</code>" /&gt; tag 
     * for each parameter not in the ignore list.
     */
    public static String makeHiddenTags( HttpServletRequest request, Map additions, String[] ignores ) {
        return( makeHiddenTags( request, additions, ignores, IgnoreType.BOTH )); 
    }
        
        
    /**
     * Creates hidden tags for all the parameters given in the request plus the additions,
     * except for the parmeters listed in the ignore list.
     *
     * @param request the <code>HttpServletRequest</code> to read the parameters from
     * @param additions a map of extra parameters to create hidden tags for
     * @param ignores the list of parameters not to create a hidden tag for
     * @param ignoreType whether the ignore list applies to the request parameters,
     * values in the additions map, or both
     * @return A string containing an HTML &lt;input type="hidden" 
     * name="<code>paramName</code>" value="<code>paramValue</code>" /&gt; tag 
     * for each parameter not in the ignore list.
     */
    public static String makeHiddenTags( HttpServletRequest request, Map additions, String[] ignores, IgnoreType ignoreType ) {        
        if( request == null || additions == null || ignores == null || ignoreType == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
    
        StringBuffer buffer = new StringBuffer();

        ArrayList ignoreList = new ArrayList();
        for( int i = 0; i < ignores.length; i++ ) {
            ignoreList.add( ignores[i] );
        }
 
        Enumeration names = request.getParameterNames();

        while( names.hasMoreElements() ) {
            String name = (String)names.nextElement();
            String[] values = request.getParameterValues( name );

            if( (ignoreType == IgnoreType.ADDITIONS_ONLY || !ignoreList.contains(name)) && values != null ) {
                for( int i=0; i < values.length; i++ ) {
                    buffer.append( "<input type=\"hidden\" name=\"" );
                    buffer.append( name );
                    buffer.append( "\" value=\"" );
                    buffer.append( values[i] );
                    buffer.append( "\" />" );
                    buffer.append( "\n" ); 
                }
            }
        }

        // Add the additions in
        Set keySet = additions.keySet();
        Iterator keys = keySet.iterator();

        while( keys.hasNext() ) {
            String name = (String)keys.next();

            //handle both a String value or a String[] value
            Object tmp = additions.get( name );
            String[] values = (tmp instanceof String[]) ? ((String[])tmp) : (new String[] {(String)tmp});

            if( (ignoreType == IgnoreType.REQUEST_ONLY || !ignoreList.contains(name)) && values != null ) {
                for( int i=0; i < values.length; i++ ) {
                    buffer.append( "<input type=\"hidden\" name=\"" );
                    buffer.append( name );
                    buffer.append( "\" value=\"" );
                    buffer.append( values[i] );
                    buffer.append( "\" />" );
                    buffer.append( "\n" );
                }
            }
        }

        return( buffer.toString() );
    } 

    
    /**
     * Creates a query string of the format "key1=value1&amp;key2=value2" for
     * each parameter in the given <code>HttpServletRequest</code>.
     *
     * @see #makeQueryString( HttpServletRequest, Map, String[] )
     * @see HttpServletRequest#getQueryString
     */
    public static String makeQueryString( HttpServletRequest request ) {
        return( makeQueryString( request, new HashMap(), new String[0] ));
    }
    

    /**
     * Creates a query string of the format "key1=value1&amp;key2=value2" for
     * each parameter in the given <code>HttpServletRequest</code> and key in 
     * given <code>Map</code>.
     *
     * @see #makeQueryString( HttpServletRequest, Map, String[] )
     * @see HttpServletRequest#getQueryString
     */
    public static String makeQueryString( HttpServletRequest request, Map additions ) {
        return( makeQueryString( request, additions, new String[0] ));
    }


    /**
     * Creates a query string of the format "key1=value1&amp;key2=value2" for
     * each parameter in the given <code>HttpServletRequest</code> that is 
     * not listed in the ignore list.
     *
     * @see #makeQueryString( HttpServletRequest, Map, String[] )
     * @see HttpServletRequest#getQueryString
     */
    public static String makeQueryString( HttpServletRequest request, String[] ignores ) {
        return( makeQueryString( request, new HashMap(), ignores ));
    }

    
    /**
     * Creates a query string of the format "key1=value1&amp;key2=value2" for
     * each parameter in the given <code>HttpServletRequest</code> and key in 
     * given <code>Map</code> that is not listed in the ignore list.
     *
     * @param request the <code>HttpServletRequest</code> to read the parameters from
     * @param additions a mapping of strings to strings or string arrays to be included
     * in the query string
     * @param ignores the list of parameters and map entries not to include
     * @return A string in the <em>x-www-form-urlencoded</em> format that is suitable for adding 
     * to a URL as a query string.
     * @see HttpServletRequest#getQueryString
     */
    public static String makeQueryString( HttpServletRequest request, Map additions, String[] ignores ) {
        return( makeQueryString( request, additions, ignores, IgnoreType.BOTH ));
    }    

    
    /**
     * Creates a query string of the format "key1=value1&amp;key2=value2" for
     * each parameter in the given <code>HttpServletRequest</code> and key in 
     * given <code>Map</code> that is not listed in the ignore list.
     *
     * @param request the <code>HttpServletRequest</code> to read the parameters from
     * @param additions a mapping of strings to strings or string arrays to be included
     * in the query string
     * @param ignores the list of parameters and map entries not to include
     * @return A string in the <em>x-www-form-urlencoded</em> format that is suitable for adding 
     * to a URL as a query string.
     * @see HttpServletRequest#getQueryString
     */
    public static String makeQueryString( HttpServletRequest request, Map additions, String[] ignores, IgnoreType ignoreType ) {
        if( request == null || additions == null || ignores == null || ignoreType == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
    
        StringBuffer buffer = new StringBuffer();

        ArrayList ignoreList = new ArrayList();
        for( int i = 0; i < ignores.length; i++ ) {
            ignoreList.add( ignores[i] );
        }
 
        Enumeration names = request.getParameterNames();

        while( names.hasMoreElements() ) {
            String name = (String)names.nextElement();
            String[] values = request.getParameterValues( name );

            if( (ignoreType == IgnoreType.ADDITIONS_ONLY || !ignoreList.contains( name )) && values != null ) {
                for( int i=0; i < values.length; i++ ) {
                    buffer.append( "&" );
                    buffer.append( name );
                    buffer.append( "=" );
                    buffer.append( URLEncoder.encode(values[i]) );
                }
            }
        }

        Set keySet = additions.keySet();
        Iterator keys = keySet.iterator();

        while( keys.hasNext() ) {
            String name = (String)keys.next();

            //handle both a String value or a String[] value            
            Object tmp = additions.get( name );
            String[] values = (tmp instanceof String[]) ? ((String[])tmp) : (new String[] {(String)tmp});

            if( (ignoreType == IgnoreType.REQUEST_ONLY || !ignoreList.contains( name )) && values != null ) {
                for( int i=0; i < values.length; i++ ) {
                    buffer.append( "&" );
                    buffer.append( name );
                    buffer.append( "=" );
                    buffer.append( URLEncoder.encode(values[i]) );
                }
            }
        }

        //removes the first & from the buffer
        buffer.deleteCharAt( 0 );

        return( buffer.toString() );
    } 

    
    public static class IgnoreType extends Object {
        public static final int _REQUEST_ONLY = 0;
        public static final int _ADDITIONS_ONLY = 0;
        public static final int _BOTH = 0;
        
        public static final IgnoreType REQUEST_ONLY = new IgnoreType(_REQUEST_ONLY);
        public static final IgnoreType ADDITIONS_ONLY = new IgnoreType(_ADDITIONS_ONLY);
        public static final IgnoreType BOTH = new IgnoreType(_BOTH);

        protected int value;
        
        private IgnoreType( int value ) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }


    public static Map getOrderedMap(String names[][])
    {
        TreeMap orderedMap = new TreeMap();
        
        for (int i = 0; i < names.length; i++)
        {
            orderedMap.put(names[i][1], names[i][0]);
        }
        
        return orderedMap;
    }

    
    /** 
     * Convenience method for reading data from a <code>Reader</code> and then 
     * immediately writing that data to a <code>Writer</code> with a default
     * buffer size of one kilobyte (1,024 chars).
     *
     * @param in a data source
     * @param out a data sink
     */    
    public static void streamToStream( Reader in, Writer out ) throws IOException {
        streamToStream(in, out, 1024);
    }
        
    
    /** 
     * Convenience method for reading data from a <code>Reader</code> and then 
     * immediately writing that data to a <code>Writer</code>.
     *
     * @param in a data source
     * @param out a data sink
     * @param bufferSize the size of the <code>char</code> buffer to use for each
     * read/write
     */
    public static void streamToStream( Reader in, Writer out, int bufferSize ) throws IOException {
        if( in == null || out == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
        
        if( bufferSize < 1 ) {
            throw new IllegalArgumentException( "Cannot take negative buffer size." );
        }
        
        char[] b = new char[bufferSize];
        int length;

        while((length = in.read(b)) != -1) {
            out.write(b, 0, length);
        }
    }
    
    
    /** 
     * Convenience method for reading data from an <code>InputStream</code> 
     * and then immediately writing that data to an <code>OutputStream</code> 
     * with a default buffer size of one kilobyte (1,024 bytes).     
     *
     * @param in a data source
     * @param out a data sink
     */        
    public static void streamToStream( InputStream in, OutputStream out ) throws IOException {
        streamToStream(in, out, 1024);       
    }
        

    /** 
     * Convenience method for reading data from an <code>InputStream</code> 
     * and then immediately writing that data to an <code>OutputStream</code>.
     *
     * @param in a data source
     * @param out a data sink
     * @param bufferSize the size of the <code>byte</code> buffer to use for each
     * read/write
     */        
    public static void streamToStream( InputStream in, OutputStream out, int bufferSize ) throws IOException {        
        byte[] b = new byte[bufferSize];
        int length;

        while((length = in.read(b)) != -1) {
          out.write(b, 0, length);
        }
    }

    
    /**
     * Convenience method for creating arrays of strings suitable for use as
     * command-line parameters when executing an external process.
     *
     * <p>The default {@link Runtime#exec Runtime.exec} method will split 
     * a single string based on spaces, but it does not respect spaces within
     * quotation marks, and it will leave the quotation marks in the resulting
     * substrings.  This method solves those problems by replacing all in-quote 
     * spaces with the given delimiter, removes the quotes, and then splits 
     * the resulting string by the remaining out-of-quote spaces.  It then 
     * goes through each substring and replaces the delimiters with spaces.</p>
     * 
     * <p><em>Caveat:</em> This method does not respect escaped quotes!  It 
     * will simply remove them and leave the stray escape characters.</p>
     *
     * @param s the string to split
     * @param delim a char that does not already exist in <code>s</code>
     * @return An array of strings split by spaces outside of quotes.
     * @throws IllegalArgumentException If <code>s</code> is null or if
     * <code>delim</code> already exists in <code>s</code>.
     */
    public static String[] createCommandArray( String s, char delim ) {
        if( s == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
        
        if( s.indexOf( delim ) != -1 ) {
            throw new IllegalArgumentException( "String parameter cannot already contain delimiter character: " + delim );
        }
        
        char[] chars = s.toCharArray();
        boolean inquote = false;
        StringBuffer buffer = new StringBuffer();
        
        //append each char to a StringBuffer, but     
        //leave out quote chars and replace spaces
        //inside quotes with the delim char
        for( int i = 0; i < chars.length; i++ ) {
            if( chars[i] == '"' ) {
                inquote = (inquote) ? false : true;
            }
            else if( inquote && chars[i] == ' ' ) {
                buffer.append( delim );
            }
            else {
                buffer.append( chars[i] );
            }
        }
        
        s = buffer.toString();
        
        //split the new string by the whitespaces that were not in quotes
        ArrayList arrayList = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer( s );
        
        while( tokenizer.hasMoreTokens() ) {
            arrayList.add( tokenizer.nextElement() );
        }
        
        //put the strings in the arraylist into a string[]
        String[] list = (String[])arrayList.toArray( new String[arrayList.size()]);   
        
        //change all the delim characters back to spaces
        for( int i = 0; i < list.length; i++ ) {
            list[i] = list[i].replace( delim, ' ' );
        }
        
        return list;
    }
    
}
