/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
        String[] boundries = range.split("-");
        if (val < Integer.valueOf(boundries[0]) || val > Integer.valueOf(boundries[1])) {
            return false;
        } else {
            return true;
        }
    }
}
