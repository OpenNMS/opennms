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

package org.opennms.netmgt.bsm.persistence.api.functions.reduce;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="highest-severity-above")
public class HighestSeverityAboveEntity extends AbstractReductionFunctionEntity {

    /**
     * The ordinal number of the Status object.
     */
    @Column(name="threshold_severity", nullable=false)
    private int m_threshold;

    public HighestSeverityAboveEntity() {

    }

    public HighestSeverityAboveEntity(int threshold) {
        setThreshold(threshold);
    }

    public void setThreshold(int threshold) {
        m_threshold = threshold;
    }

    public int getThreshold() {
        return m_threshold;
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("id", getId())
                .add("threshold", m_threshold)
                .toString();
    }

    @Override
    public <T> T accept(ReductionFunctionEntityVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
