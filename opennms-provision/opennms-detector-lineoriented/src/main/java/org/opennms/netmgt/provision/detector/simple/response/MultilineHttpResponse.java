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
    private static final Pattern HTTP_RESPONSE_REGEX = Pattern.compile("([H][T][T][P+]/[1].[0-1]) ([0-9][0-9][0-9])(?: ([\\p{Alnum} ]*))?\r?\n");

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
        final StringBuilder retVal = new StringBuilder();
        for(int i = 0; i < array.length; i++){
            retVal.append((String)array[i]);
        }
        return retVal.toString();
    }
}
