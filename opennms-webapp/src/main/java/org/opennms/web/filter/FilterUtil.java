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
package org.opennms.web.filter;

import org.opennms.core.utils.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class FilterUtil {

    public static String toFilterURL(String[] filters) {
        return toFilterURL(filters != null ? Arrays.asList(filters) : new ArrayList<String>());
    }

    public static String toFilterURL(List<String> filters) {
        final StringBuilder buffer = new StringBuilder();
        if( filters != null ) {
            for( int i=0; i < filters.size(); i++ ) {
                if (i == 0) buffer.append("filter=");
                else buffer.append( "&amp;filter=" );
                String filterString = filters.get(i);
                buffer.append( java.net.URLEncoder.encode(filterString) );
            }
        }
        return( buffer.toString() );
    }

    public static String[] parse(String filterString) {
        String decodedString = URLDecoder.decode(filterString, StandardCharsets.UTF_8);

        return Arrays.stream(getFilterParameters(decodedString))
                .map(fp -> fp.replace("filter=", ""))
                .distinct().toArray(String[]::new);
    }

    public static String[] getFilterParameters(String filterString) {
        if (StringUtils.isEmpty(filterString)) {
            return new String[0];
        }
        if (filterString.contains("&amp;")) {
            return filterString.split("&amp;");
        }
        return filterString.split("&");
    }
}
