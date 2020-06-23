/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.drools;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.drools.core.base.accumulators.AbstractAccumulateFunction;
import org.opennms.netmgt.model.OnmsSeverity;

public class MaxSeverityAccumulateFunction extends AbstractAccumulateFunction<MaxSeverityAccumulateFunction.MaxSeverity> {

    protected static class MaxSeverity implements Externalizable {
        public OnmsSeverity max = null;

        public MaxSeverity() {}

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            max = (OnmsSeverity) in.readObject();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(max);
        }

        @Override
        public String toString() {
            return "max";
        }
    }

    @Override
    public MaxSeverity createContext() {
        return new MaxSeverity();
    }

    @Override
    public void init(MaxSeverity context) {
        context.max = null;
    }

    @Override
    public void accumulate(MaxSeverity context, Object value) {
        if (value instanceof OnmsSeverity) {
            final OnmsSeverity severity = (OnmsSeverity)value;
            context.max = context.max == null || context.max.compareTo( severity ) < 0 ?
                    severity:
                    context.max;
        }
    }

    @Override
    public Object getResult(MaxSeverity context) {
        return context.max;
    }

    @Override
    public boolean supportsReverse() {
        return false;
    }

    @Override
    public void reverse(MaxSeverity context, Object value) {
        // pass
    }

    @Override
    public Class<?> getResultType() {
        return OnmsSeverity.class;
    }

    @Override
    public void writeExternal(ObjectOutput out) {
        // pass
    }

    @Override
    public void readExternal(ObjectInput in) {
        // pass
    }

}
