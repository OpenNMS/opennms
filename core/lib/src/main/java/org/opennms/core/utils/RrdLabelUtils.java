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
package org.opennms.core.utils;

public abstract class RrdLabelUtils {
    public static final boolean PREFER_IFDESCR = Boolean.getBoolean("org.opennms.core.utils.preferIfDescr");
    public static final boolean DONT_SANITIZE_IFNAME = Boolean.getBoolean("org.opennms.core.utils.dontSanitizeIfName");
    
    public static String computeNameForRRD(String ifname, String ifdescr) {
        String firstChoice = PREFER_IFDESCR ? ifdescr : ifname;
        String secondChoice = PREFER_IFDESCR ? ifname : ifdescr;
        String label = null;
	
        if (firstChoice != null && !"".equals(firstChoice)) {
            label = DONT_SANITIZE_IFNAME ? firstChoice : AlphaNumeric.parseAndReplace(firstChoice, '_');
        } else if (secondChoice != null && !"".equals(secondChoice)) {
            label = DONT_SANITIZE_IFNAME ? secondChoice : AlphaNumeric.parseAndReplace(secondChoice, '_');
        }
        return label;
        
    }
    
    public static String computePhysAddrForRRD(String physaddr) {
        String physAddrForRRD = null;

        if (physaddr != null && !physaddr.equals("")) {
            String parsedPhysAddr = AlphaNumeric.parseAndTrim(physaddr);
            if (parsedPhysAddr.length() == 12) {
                physAddrForRRD = parsedPhysAddr;
            } 
        }
       
        return physAddrForRRD;
        
    }
    
    public static String computeLabelForRRD(String ifname, String ifdescr, String physaddr) {
        String name = computeNameForRRD(ifname, ifdescr);
        String physAddrForRRD = computePhysAddrForRRD(physaddr);
        return (physAddrForRRD == null ? name : name + '-' + physAddrForRRD);
    }
}
