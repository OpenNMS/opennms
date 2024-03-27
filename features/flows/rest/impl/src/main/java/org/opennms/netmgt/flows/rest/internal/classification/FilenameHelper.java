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
package org.opennms.netmgt.flows.rest.internal.classification;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FilenameHelper {

    public final static String REGEX_ALLOWED_CHAR = "^[a-zA-Z1-9_ .-]{1,}$";

    private Pattern p = Pattern.compile(REGEX_ALLOWED_CHAR);

    boolean isValidFileName(String filename){
        if(filename == null
                || filename.startsWith(" " )
                || filename.endsWith(" ")
                || filename.trim().isEmpty()){
            return false;
        }
        Matcher m = p.matcher(filename);
        return m.matches();
    }

    String createFilenameForGroupExport(int groupId, String requestedFilename){
        return isValidFileName(requestedFilename) ? requestedFilename: groupId + "_rules.csv";
    }
}
