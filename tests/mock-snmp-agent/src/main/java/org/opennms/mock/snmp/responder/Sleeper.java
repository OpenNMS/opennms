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
package org.opennms.mock.snmp.responder;

import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

public class Sleeper implements DynamicVariable {
    private static Sleeper m_instance = null;

    private long m_millisToSleep;
    private Variable m_variable;

    private Sleeper() {
        resetWithVariable(new Integer32(0));
    }

    public static synchronized Sleeper getInstance() {
        if (m_instance == null) {
            m_instance = new Sleeper();
        }
        return m_instance;
    }

    public void resetWithVariable(Variable var) {
        m_millisToSleep = 0;
        m_variable = var;
    }

    public long getSleepTime() {
        return m_millisToSleep;
    }

    public void setSleepTime(long millis) {
        m_millisToSleep = millis;
    }

    public Variable getVariable() {
        return m_variable;
    }

    public void setVariable(Variable var) {
        m_variable = var;
    }

    @Override
    public Variable getVariableForOID(String oidStr) {
        sleep(m_millisToSleep);
        return m_variable;
    }

    private void sleep(long millis) {
        if (millis == 0) return;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
