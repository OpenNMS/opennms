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
package org.opennms.netmgt.snmp.mock;


public class BulkPdu extends RequestPdu {
    int m_nonRepeaters;
    int m_maxRepititions;

    public BulkPdu() {
        super();
    }

    public void setNonRepeaters(int nonRepeaters) {
        m_nonRepeaters = nonRepeaters;
    }

    public void setMaxRepititions(int maxRepititions) {
        m_maxRepititions = maxRepititions;
    }

    @Override
    public int getNonRepeaters() {
        return m_nonRepeaters;
    }

    @Override
    public int getMaxRepititions() {
        return m_maxRepititions;
    }

    @Override
    public ResponsePdu send(TestAgent agent) {
        if (agent.isVersion1())
            throw new IllegalStateException("can't send a getBulk pack to a V1 Agent");
        
        return super.send(agent);
    }

    @Override
    protected ResponsePdu handleTooBig(TestAgent agent, ResponsePdu resp) {
        resp.getVarBinds().subList(agent.getMaxResponseSize(), resp.size()).clear();
        return resp;
    }
}