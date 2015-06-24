/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
 * <p>MultilineHttpResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MultilineHttpResponse extends MultilineOrientedResponse {
    private static final Logger LOG = LoggerFactory.getLogger(MultilineHttpResponse.class);
    private static final Pattern HTTP_RESPONSE_REGEX = Pattern.compile("([H][T][T][P+]/[1].[0-1]) ([0-9][0-9][0-9]) ([a-zA-Z ]+)\r?\n");

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
    public boolean validateResponse(final String pattern, final String url, final boolean isCheckCode, final int maxRetCode) throws Exception {
        final String httpResponse = (String)getResponseList().toArray()[0];
        LOG.debug("HTTP Response: {}", httpResponse);
        final Matcher m = HTTP_RESPONSE_REGEX.matcher(httpResponse);
        if (m.matches()) {
            if (isCheckCode) {
                final int returnCode = Integer.valueOf(m.group(2)).intValue();
                LOG.debug("return code = {}, max return code = {}", returnCode, maxRetCode);
                return (returnCode <= maxRetCode);
            } else {
                return true;
            }
        } else {
            LOG.debug("does not match");
            return false;
        }
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getResponseList().isEmpty() ? "MultilineHttpResponse" : String.format("Response: %s", getResponseListAsString(getResponseList().toArray()));
    }

    private String getResponseListAsString(final Object[] array) {
        final StringBuffer retVal = new StringBuffer();
        for(int i = 0; i < array.length; i++){
            retVal.append((String)array[i]);
        }
        return retVal.toString();
    }
}
