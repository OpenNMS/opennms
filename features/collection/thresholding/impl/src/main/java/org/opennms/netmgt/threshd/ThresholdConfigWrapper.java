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
package org.opennms.netmgt.threshd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.config.threshd.Threshold;

/**
 * <p>ThresholdConfigWrapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdConfigWrapper extends BaseThresholdDefConfigWrapper {

    private Threshold m_threshold;
    private Collection<String> m_dataSources;
    
    /**
     * <p>Constructor for ThresholdConfigWrapper.</p>
     *
     * @param threshold a {@link org.opennms.netmgt.config.threshd.Threshold} object.
     */
    public ThresholdConfigWrapper(Threshold threshold) {
        super(threshold);
        m_threshold=threshold;
        m_dataSources=new ArrayList<String>(1);
        m_dataSources.add(m_threshold.getDsName() == null ? null : m_threshold.getDsName().intern());
    }

    /** {@inheritDoc} */
    @Override
    public String getDatasourceExpression() {
        return m_threshold.getDsName();
        
    }

    /** {@inheritDoc} */
    @Override
    public Collection<String> getRequiredDatasources() {
        return m_dataSources;
    }

    /** {@inheritDoc} */
    public double evaluate(Map<String, Double> values) {
        Double result=values.get(m_threshold.getDsName());
        if(result==null) {
            return 0.0;
        }
        return result.doubleValue();
    }

    @Override
    public void accept(ThresholdDefVisitor thresholdDefVisitor) {
        thresholdDefVisitor.visit(this);
    }
}
