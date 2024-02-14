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
package org.opennms.features.topology.app.internal.gwt.client.d3;

public class SimpleTransition extends D3Behavior {

    private int m_duration;
    private int m_delay;
    private String m_property;
    private double m_value;

    public SimpleTransition(String property, int value, int duration, int delay) {
        m_duration = duration;
        m_delay = delay;
        m_property = property;
        m_value = value;
    }
    
    public SimpleTransition(String property, double d, int duration, int delay) {
        
    }

    @Override
    public D3 run(D3 selection) {
        return selection.transition().duration(m_duration).delay(m_delay).attr(m_property, m_value);
    }

}
