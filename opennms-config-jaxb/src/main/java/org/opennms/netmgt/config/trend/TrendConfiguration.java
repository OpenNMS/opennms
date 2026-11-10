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
package org.opennms.netmgt.config.trend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name="trend-configuration")
@ValidateUsing("trend-configuration.xsd")
@XmlAccessorType(XmlAccessType.NONE)
public class TrendConfiguration implements Serializable {
    private static final long serialVersionUID = 3402898044699865749L;

    @XmlElement(name="trend-definition")
    private List<TrendDefinition> trendDefinitions = new ArrayList<>();

    public List<TrendDefinition> getTrendDefinitions() {
        return trendDefinitions;
    }

    public void setTrendDefinitions(List<TrendDefinition> trendDefinitions) {
        this.trendDefinitions = trendDefinitions;
    }

    public TrendDefinition getTrendDefintionForName(final String name) {
        for(final TrendDefinition trendDefinition : getTrendDefinitions()) {
            if (name.equals(trendDefinition.getName())) {
                return trendDefinition;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrendConfiguration that = (TrendConfiguration) o;

        return trendDefinitions != null ? trendDefinitions.equals(that.trendDefinitions) : that.trendDefinitions == null;
    }

    @Override
    public int hashCode() {
        return trendDefinitions != null ? trendDefinitions.hashCode() : 0;
    }
}
