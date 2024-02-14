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
package org.opennms.netmgt.dao.api;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class CountedObject<T> implements Comparable<CountedObject<T>> {
    private T m_object;
    private long m_count;

    public CountedObject() {
    }

    public CountedObject(final T object, final Long count) {
        m_object = object;
        m_count = count;
    }

    public void setObject(final T object) {
        m_object = object;
    }

    public T getObject() {
        return m_object;
    }
    
    public void setCount(final int count) {
        m_count = count;
    }
    
    public Long getCount() {
        return m_count;
    }

    @Override
    public int compareTo(final CountedObject<T> o) {
        return new CompareToBuilder()
            .append(this.getCount(), (o == null? null:o.getCount()))
            .append(this.getObject(), (o == null? null:o.getObject()))
            .toComparison();
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append(this.getObject())
            .append(this.getCount())
            .toString();
    }
}
