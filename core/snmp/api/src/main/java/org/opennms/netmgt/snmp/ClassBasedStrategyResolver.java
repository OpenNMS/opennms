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

public class ClassBasedStrategyResolver implements StrategyResolver {

    private static SnmpStrategy snmpStrategy;

    public SnmpStrategy getStrategyInstance() {
        final String strategyClass = SnmpUtils.getStrategyClassName();
        try {
            return snmpStrategy = (SnmpStrategy) Class.forName(strategyClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate class " + strategyClass, e);
        }
    }

    @Override
    public SnmpStrategy getStrategy() {
        if (snmpStrategy == null) {
            return getStrategyInstance();
        }
        if (SnmpUtils.getStrategyClassName().equals(snmpStrategy.getClass().getName())) {
            return snmpStrategy;
        } else {
            return getStrategyInstance();
        }
    }
}
