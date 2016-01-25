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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.persistence.api;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.collect.Sets;

@Entity
@Table(name = "bsm_service_reductionkeys")
@PrimaryKeyJoinColumn(name="id")
@DiscriminatorValue("reductionkeys")
public class SingleReductionKeyEdgeEntity extends BusinessServiceEdgeEntity {

    private String reductionKey;

    public void setReductionKey(String reductionKey) {
        this.reductionKey = reductionKey;
    }

    // TODO MVR add a constraint that the reductionkey must be unique
    @Column(name = "reductionkey", nullable = false)
    public String getReductionKey() {
        return reductionKey;
    }

    @Override
    @Transient
    public Set<String> getReductionKeys() {
        return Sets.newHashSet(reductionKey);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("reductionKey", reductionKey)
                .toString();
    }
}
