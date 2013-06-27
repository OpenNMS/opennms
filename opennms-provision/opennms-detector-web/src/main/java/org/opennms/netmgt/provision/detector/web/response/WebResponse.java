/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.web.response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.opennms.netmgt.provision.detector.web.request.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>WebResponse class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class WebResponse {
    
    private static final Logger LOG = LoggerFactory.getLogger(WebResponse.class);
    private String expectedRange;
    private String expectedText;
    private HttpResponse httpResponse;

    public WebResponse(WebRequest request, HttpResponse response) {
        if (request != null) {
            this.expectedRange = request.getResponseRange();
            this.expectedText = request.getResponseText();
        }
        this.httpResponse = response;
    }

    public boolean isValid() {
        if (httpResponse == null || expectedRange == null) {
            return false;
        }

        Integer statusCode = httpResponse.getStatusLine().getStatusCode();
        LOG.debug("HTTP response status code: {}", statusCode);
        boolean retval = inRange(expectedRange, statusCode);

        if (expectedText != null) {
            try {
                String responseText = EntityUtils.toString(httpResponse.getEntity());
                LOG.debug("HTTP response text: {}", responseText);
                LOG.debug("HTTP checking if output matches {}", expectedText);
                if (expectedText.charAt(0) == '~') {
                    final Pattern p = Pattern.compile(expectedText.substring(1), Pattern.MULTILINE);
                    final Matcher m = p.matcher(responseText);
                    retval = m.find();
                } else {
                    retval = responseText.equals(expectedText);
                }
            } catch (Exception e) {
                LOG.info(e.getMessage(), e);
                retval = false;
            }
        }

        LOG.debug("HTTP detected ? {}", retval);
        return retval;
    }

    private boolean inRange(String range,Integer val) {
        String boundries[] = range.split("-");
        if (val < new Integer(boundries[0]) || val > new Integer(boundries[1])) {
            return false;
        } else {
            return true;
        }
    }
}
