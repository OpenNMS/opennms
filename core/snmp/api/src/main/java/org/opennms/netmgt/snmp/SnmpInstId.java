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
package org.opennms.netmgt.snmp;

import java.util.StringTokenizer;

public class SnmpInstId extends SnmpObjId {
    
    public static final SnmpInstId INST_ZERO = new SnmpInstId(0);

    public SnmpInstId(int[] instanceIds) {
        super(instanceIds);
    }

    public SnmpInstId(String instance) {
        super(instance);
    }
    
    public SnmpInstId(SnmpObjId instance) {
        super(instance);
    }

    public SnmpInstId(int instance) {
        super(new int[] { instance }, false);
    }
    
    @Override
    protected boolean addPrefixDotInToString() {
        return false;
    }

    public static SnmpInstId[] convertToSnmpInstIds(String instances) {
        StringTokenizer tokenizer = new StringTokenizer(instances, ",");
        SnmpInstId[] insts = new SnmpInstId[tokenizer.countTokens()];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            SnmpInstId inst = new SnmpInstId(token);
            insts[index] = inst;
            index++;
        }
        return insts;
    }

    public int toInt() {
        if (this.length() != 1)
            throw new IllegalStateException("Cannot convert "+this+" to an int");
        
        return getLastSubId();
    }
    
    
}
