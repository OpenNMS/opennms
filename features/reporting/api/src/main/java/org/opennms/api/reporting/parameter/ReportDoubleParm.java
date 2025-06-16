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
package org.opennms.api.reporting.parameter;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>ReportFloatParm class.</p>
 *
 * @author jonathan@opennms.org
 * @version $Id: $
 */
public class ReportDoubleParm extends ReportParm implements Serializable {
    
    private static final long serialVersionUID = 5242917854258286117L;

    Double m_value;
    String m_type;
    
    public Double getValue() {
        return m_value;
    }
    
    public void setValue(Double value) {
        m_value = value;
    }
    
    public String getInputType() {
        return m_type;
    }

    public void setInputType(String type) {
        m_type = type;
    }

    @Override
    void accept(ReportParmVisitor visitor) {
        Objects.requireNonNull(visitor).visit(this);
    }

}
