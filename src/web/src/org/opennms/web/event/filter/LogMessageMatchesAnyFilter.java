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
package org.opennms.web.event.filter;

import java.util.ArrayList;
import java.util.StringTokenizer;


public class LogMessageMatchesAnyFilter extends Object implements Filter 
{
    public static final String TYPE = "msgmatchany";
    protected String[] substrings;

    /**
     * @param stringList a space-delimited list of search substrings 
     */
    public LogMessageMatchesAnyFilter(String stringList) {
        if( stringList == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");            
        }
        
        StringTokenizer tokenizer = new StringTokenizer(stringList);
        ArrayList list = new ArrayList();
        
        while(tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
            
        if(list.size() == 0) {
            throw new IllegalArgumentException("Cannot take a zero-length list of substrings");
        }
        
        this.substrings = (String[])list.toArray(new String[list.size()]);
    }    
    
    public LogMessageMatchesAnyFilter(String[] substrings) {
        if( substrings == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        this.substrings = substrings;
    }
    
    public String getSql() {
        StringBuffer buffer = new StringBuffer(" (");
        
        buffer.append("UPPER(EVENTLOGMSG) LIKE '%");
        buffer.append(this.substrings[0].toUpperCase());
        buffer.append("%'");
        
        for(int i=1; i < this.substrings.length; i++ ) {
            buffer.append(" OR ");

            buffer.append("UPPER(EVENTLOGMSG) LIKE '%");
            buffer.append(this.substrings[i].toUpperCase());
            buffer.append("%'");
        }
        
        buffer.append(")");
        
        return buffer.toString();
    }
    
    public String getDescription() {
        return( TYPE + "=" + this.getQueryString() );
    }
    
    public String getTextDescription(){
        StringBuffer buffer = new StringBuffer("message containing \"");        
        buffer.append(this.substrings[0]);
        buffer.append("\"");

        for(int i=1; i < this.substrings.length; i++ ) {
            buffer.append(" or \"");
            buffer.append(this.substrings[i]);
            buffer.append("\"");            
        }
                
        return buffer.toString();
    }

    public String toString() {
        return( "<LogMessageMatchesAnyFilter: " + this.getDescription() + ">" );
    }

    public String[] getSubstrings() {
        return( this.substrings );
    }

    public boolean equals( Object obj ) {
        return( this.toString().equals( obj.toString() ));
    }
    
    public String getQueryString() {
        StringBuffer buffer = new StringBuffer();        
        buffer.append(this.substrings[0]);

        for(int i=1; i < this.substrings.length; i++ ) {
            buffer.append(" ");
            buffer.append(this.substrings[i]);
        }
                
        return( buffer.toString() );
    }
    
}

