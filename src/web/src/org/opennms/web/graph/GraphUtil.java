//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
package org.opennms.web.graph;

import java.io.*;


/**
 * Provides look and feel utilities for the servlets & JSPs presenting  
 * graphs. 
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A> 
 */
public class GraphUtil extends Object 
{
    /** The longest an RRD filename can be, currently 1024 characters.
     * 
     * @deprecated Replaced by 
     *     {@link org.opennms.netmgt.utils.RrdFileConstants#MAX_RRD_FILENAME_LENGTH}
     */
    public static final int MAX_RRD_FILENAME_LENGTH = 1024;

    /** Suffix common to all RRD filenames. 
     * 
     * @deprecated Replaced by 
     *     {@link org.opennms.netmgt.utils.RrdFileConstants#RRD_SUFFIX}
     */
    public static final String RRD_SUFFIX = ".rrd";
    
    /** Convenience filter that matches only RRD files. 
     * 
     * @deprecated Replaced by 
     *     {@link org.opennms.netmgt.utils.RrdFileConstants#RRD_FILENAME_FILTER}
     */
    public static final FilenameFilter RRD_FILENAME_FILTER = new FilenameFilter() {
        public boolean accept(File file, String name) {
            return name.endsWith(RRD_SUFFIX);
        }
    };    


    /** Private constructor so this class cannot be instantiated. */
    private GraphUtil() {}
    

    /**
     * Checks an RRD filename to make sure it is of the proper length
     * and does not contain any unexpected charaters.  
     * 
     * <p>The maximum length is specified by the 
     * {@link #MAX_RRD_FILENAME_LENGTH MAX_RRD_FILENAME_LENGTH} constant.  
     * The only valid characters are letters (A-Z and a-z), numbers (0-9), 
     * dashes (-), dots (-), and underscores (_).  These precautions are 
     * necessary since the RRD filename is used on the commandline and 
     * specified in the graph URL.</p>
     * 
     * @deprecated Replaced by 
     *     {@link org.opennms.netmgt.utils.RrdFileConstants#isValidRRDName}
     */
    public static boolean isValidRRDName(String rrd) {
        if( rrd == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        int length = rrd.length(); 

        if( length > MAX_RRD_FILENAME_LENGTH ) {
            return false;
        }

        //cannot contain references to higher directories for security's sake
        if(rrd.indexOf("..") >= 0) {
            return false;
        }
        
        for( int i=0; i < length; i++ ) {
            char c = rrd.charAt(i);
            
            if( !(('A' <= c && c <= 'Z') || 
                ('a' <= c && c <= 'z') || 
                ('0' <= c && c <= '9') || 
                (c == '_') || 
                (c =='.') || 
                (c =='-') ||
                (c == '/')) 
            ) {
                return false;
            }
        }            
    
        return true;
    }

}
