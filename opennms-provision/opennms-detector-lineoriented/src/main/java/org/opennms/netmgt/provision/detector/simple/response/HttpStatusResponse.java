/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.simple.response;

import java.util.regex.Pattern;



public class HttpStatusResponse extends LineOrientedResponse {
    
    
    /**
     * @param response
     */
    public HttpStatusResponse(String response) {
        super(response);
        
    }

    public boolean validateResponse(String pattern, String url, boolean isCheckCode, int maxRetCode) throws Exception {
        String codeStr = Integer.toString(maxRetCode);
        String[] codeArray = codeStr.split("");
        if(codeArray.length < 3) {
            throw new Exception("Max Ret Code is too Short");
        }
        String REGEX = String.format("([H][T][T][P+]/[1].[0-1]) ([0-%s][0-2][0-%s]) ([a-zA-Z ]+)", codeArray[1], codeArray[3]);
        
        if(!isCheckCode) {
            REGEX = "([H][T][T][P+]/[1].[0-1]) ([0-6]+) ([a-zA-Z ]+)";
        }
        
        System.out.printf("REGEX: %s\n", REGEX);
        return Pattern.matches(REGEX, getResponse().trim());
    }

}
