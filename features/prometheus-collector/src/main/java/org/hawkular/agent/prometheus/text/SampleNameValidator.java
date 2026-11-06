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
package org.hawkular.agent.prometheus.text;

interface SampleNameValidator {
    boolean isValid(String sampleName);
    
    public static SampleNameValidator rejectAll() {
        return sampleName -> false; 
    }
    
    /**
     * Returns a validator that only accepts sample names that exactly match the given family name.
     * @param familyName
     * @return
     */
    public static SampleNameValidator exactName(String familyName) {
        return sampleName -> familyName.equals(sampleName);
    }
    
    /**
     * Returns a validator that only accepts sample names that start with the given family name and end with one of the given suffixes.
     * @param familyName
     * @param suffixes
     * @return
     */
    public static SampleNameValidator matchingNameWithSuffixes(String familyName, String... suffixes) {
        return sampleName -> {
            if(!sampleName.startsWith(familyName)) {
                return false;
            }
            String suffix = sampleName.substring(familyName.length());
            for (String s : suffixes) {
                if (s.equals(suffix)) {
                    return true;
                }
            }
            return false;
        };
    }
}
