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

package org.opennms.netmgt.snmp.proxy;

import java.util.Objects;

import org.opennms.netmgt.snmp.SnmpObjId;

public class WalkRequest {

    private final SnmpObjId baseOid;
    private int maxRepetitions = 1;
    private SnmpObjId instance = null;
    private String correlationId = null;

    public WalkRequest(SnmpObjId baseOid) {
        this.baseOid = Objects.requireNonNull(baseOid);
    }

    public SnmpObjId getBaseOid() {
        return baseOid;
    }

    public void setMaxRepetitions(int maxRepetitions) {
        this.maxRepetitions = maxRepetitions;
    }

    public int getMaxRepetitions() {
        return maxRepetitions;
    }

    public void setInstance(SnmpObjId instance) {
        this.instance = instance;
    }

    public SnmpObjId getInstance() {
        return instance;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public String toString() {
        return String.format("WalkRequest[baseOid=%s, correlationId=%s, maxRepetitions=%d, instance=%s]",
                baseOid, correlationId, maxRepetitions, instance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseOid, correlationId, maxRepetitions, instance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final WalkRequest other = (WalkRequest) obj;
        return Objects.equals(this.baseOid, other.baseOid)
                && Objects.equals(this.correlationId, other.correlationId)
                && Objects.equals(this.maxRepetitions, other.maxRepetitions)
                && Objects.equals(this.instance, other.instance);
    }
}
