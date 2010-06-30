/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.simple.response;

import java.util.regex.Pattern;


/**
 * <p>MultilineHttpResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MultilineHttpResponse extends MultilineOrientedResponse {
    
    
    /**
     * <p>Constructor for MultilineHttpResponse.</p>
     */
    public MultilineHttpResponse(){}
    
    /**
     * <p>validateResponse</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @param url a {@link java.lang.String} object.
     * @param isCheckCode a boolean.
     * @param maxRetCode a int.
     * @return a boolean.
     * @throws java.lang.Exception if any.
     */
    public boolean validateResponse(String pattern, String url, boolean isCheckCode, int maxRetCode) throws Exception {
        String codeStr = Integer.toString(maxRetCode);
        String[] codeArray = codeStr.split("");
        if(codeArray.length < 3) {
            throw new Exception("Max Ret Code is too Short");
        }
        String REGEX = String.format("([H][T][T][P+]/[1].[0-1]) ([0-%s][0-2][0-%s]) ([a-zA-Z ]+)\r\n", codeArray[1], codeArray[3]);
        
        if(!isCheckCode) {
            REGEX = "([H][T][T][P+]/[1].[0-1]) ([0-6]+) ([a-zA-Z ]+)\r\n";
        }
        
        String httpResponse = (String)getResponseList().toArray()[0];

        if(Pattern.matches(REGEX, httpResponse)){
           return getResponseListAsString(getResponseList().toArray()).contains(pattern);
        }
        
        return false;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return getResponseList().isEmpty() ? "MultilineHttpResponse" : String.format("Response: %s", getResponseListAsString(getResponseList().toArray()));
    }
    
    private String getResponseListAsString(Object[] array) {
        StringBuffer retVal = new StringBuffer();
        for(int i = 0; i < array.length; i++){
            retVal.append((String)array[i]);
        }
        return retVal.toString();
    }

    /**
     * <p>getResponse</p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getResponse(){
        return getResponseList().toArray().toString();
    }
    
}
