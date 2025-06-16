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
package org.opennms.netmgt.collection.api;

import java.io.Serializable;
import java.util.Comparator;


/**
 * <p>ByNameComparator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public final class ByNameComparator implements Comparator<CollectionAttributeType>, Serializable {

    private static final long serialVersionUID = -2596801053643459622L;

    /**
     * <p>compare</p>
     *
     * @param type0 a {@link org.opennms.netmgt.collection.api.CollectionAttributeType} object.
     * @param type1 a {@link org.opennms.netmgt.collection.api.CollectionAttributeType} object.
     * @return a int.
     */
    @Override
    public int compare(final CollectionAttributeType type0, final CollectionAttributeType type1) {
        return type0.getName().compareTo(type1.getName());
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        return o instanceof ByNameComparator;
    }
    
    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
        return 0;
    }
}
