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
import org.opennms.protocols.wmi.wbem.OnmsWbemMethodSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectPath;
import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;
import org.opennms.protocols.wmi.WmiException;

import java.util.List;

public class OnmsWbemObjectBiosStub implements OnmsWbemObject {
    public OnmsWbemPropertySet props;
    public OnmsWbemObjectBiosStub(OnmsWbemPropertySet propset) {
        props = propset;
    }

    @Override
    public OnmsWbemObject wmiExecMethod(String methodName, List<?> params, List<?> namedValueSet) {
        return null;
    }

    @Override
    public List<String> wmiInstances() {
        return null;
    }

    @Override
    public String wmiPut() {
        return null;
    }

    @Override
    public OnmsWbemMethodSet getWmiMethods() throws WmiException {
        return null;
    }

    @Override
    public OnmsWbemObjectPath getWmiPath() throws WmiException {
        return null;
    }

    @Override
    public String getWmiObjectText() throws WmiException {
        return null;
    }

    @Override
    public OnmsWbemPropertySet getWmiProperties() throws WmiException {
        return props;
    }
}
