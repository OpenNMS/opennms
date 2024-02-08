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
package org.opennms.netmgt.provision.detector.simple.response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpStatusResponse extends LineOrientedResponse {
    private static final Pattern HTTP_RESPONSE_REGEX = Pattern.compile("([H][T][T][P+]/[1].[0-1]) ([0-9][0-9][0-9])(?: ([\\p{Alnum} ]*))?");

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
