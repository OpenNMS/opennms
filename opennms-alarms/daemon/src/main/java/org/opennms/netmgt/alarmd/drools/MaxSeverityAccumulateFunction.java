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
