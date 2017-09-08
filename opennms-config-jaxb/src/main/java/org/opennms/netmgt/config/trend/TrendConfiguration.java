/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
