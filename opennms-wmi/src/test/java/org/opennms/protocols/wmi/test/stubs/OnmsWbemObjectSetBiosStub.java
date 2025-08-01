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
package org.opennms.protocols.wmi.test.stubs;

import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;
import org.opennms.protocols.wmi.WmiException;

public class OnmsWbemObjectSetBiosStub implements OnmsWbemObjectSet {
    public OnmsWbemObject objStub;

    public OnmsWbemObjectSetBiosStub(OnmsWbemObject obj) {
        objStub = obj;
    }

    @Override
    public Integer count() throws WmiException {
        return 1;
    }

    @Override
    public OnmsWbemObject get(Integer idx) throws WmiException {
        if (idx == 0) {
            return objStub;
        } else {
            throw new WmiException("Failed to enumerate WbemObject variant: Incorrect function. [0x00000001]");
        }
    }
}
