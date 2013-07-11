/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.simple.response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>HttpStatusResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class HttpStatusResponse extends LineOrientedResponse {
    
    private static final Logger LOG = LoggerFactory.getLogger(HttpStatusResponse.class);
    
    private static final Pattern DEFAULT_REGEX = Pattern.compile("([H][T][T][P+]/[1].[0-1]) ([0-6]+) ([a-zA-Z ]+)");

    /**
     * <p>Constructor for HttpStatusResponse.</p>
     *
     * @param response a {@link java.lang.String} object.
     */
    public HttpStatusResponse(final String response) {
        super(response);
        
    }

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
    public boolean validateResponse(final String pattern, final String url, final boolean isCheckCode, final int maxRetCode) throws Exception {
        String[] codeArray = Integer.toString(maxRetCode).split("");
        if(codeArray.length < 3) {
            throw new IllegalArgumentException("Maximum HTTP return code is too short, must be at least 3 digits");
        }
        
        final Pattern p;
        
        if (isCheckCode) {
            p = Pattern.compile(String.format("([H][T][T][P+]/[1].[0-1]) ([0-%s][0-2][0-%s]) ([a-zA-Z ]+)", codeArray[1], codeArray[3]));
        } else {
            p = DEFAULT_REGEX;
        }

        final Matcher m = p.matcher(getResponse().trim());
        LOG.info("HTTP status regex: {}\n", p.pattern());
        return m.matches();
    }

}
