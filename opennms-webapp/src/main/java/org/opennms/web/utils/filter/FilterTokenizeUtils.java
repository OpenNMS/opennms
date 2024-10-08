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
package org.opennms.web.utils.filter;

import org.apache.commons.lang.ArrayUtils;

import java.util.NoSuchElementException;

public class FilterTokenizeUtils {

    private static final String QUERY_PARAM_DELIMITER = "=";

    /**
     * <p>tokenizeFilterString</p>
     *
     * @param filterString a {@link java.lang.String} object.
     * @return a {@link java.lang.String}[] object representing the type and value tokenized.
     */
    public static String[] tokenizeFilterString(String filterString) {
        String[] tempTokens = filterString.split(QUERY_PARAM_DELIMITER);
        try {
            String type = tempTokens[0];
            String[] values = (String[]) ArrayUtils.remove(tempTokens, 0);
            String value = org.apache.commons.lang.StringUtils.join(values, QUERY_PARAM_DELIMITER);
            return new String[]{type, value};
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException("Could not tokenize filter string: " + filterString);
        }
    }
}
