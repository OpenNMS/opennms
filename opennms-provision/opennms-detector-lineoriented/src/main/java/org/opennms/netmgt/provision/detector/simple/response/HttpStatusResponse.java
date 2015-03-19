/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

public class HttpStatusResponse extends LineOrientedResponse {
    private static final Pattern HTTP_RESPONSE_REGEX = Pattern.compile("([H][T][T][P+]/[1].[0-1]) ([0-9][0-9][0-9]) ([a-zA-Z ]+)");

    public HttpStatusResponse(final String response) {
        super(response);
    }

    /**
     * <p>validateResponse: Validate the HTTP response from {@link #getResponse()}.</p>
     *
     * @param pattern Unused.
     * @param url Unused.
     * @param isCheckCode Whether to check the response code against maxRetCode for validity.
     * @param maxRetCode The maximum return code to accept.
     * @return a boolean.
     */
    public boolean validateResponse(final String pattern, final String url, final boolean isCheckCode, final int maxRetCode) throws Exception {
        final Matcher m = HTTP_RESPONSE_REGEX.matcher(getResponse().trim());
        if (m.matches()) {
            if (isCheckCode) {
                final int returnCode = Integer.valueOf(m.group(2)).intValue();
                return (returnCode <= maxRetCode);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

}
