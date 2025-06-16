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
package org.opennms.netmgt.model;

/**
 * <p>Abstract OnmsEntity class.</p>
 */
public abstract class OnmsEntity {
	
	/**
	 * <p>hasNewValue</p>
	 *
	 * @param newVal a {@link java.lang.Object} object.
	 * @param existingVal a {@link java.lang.Object} object.
	 * @return a boolean.
	 */
	protected static boolean hasNewValue(Object newVal, Object existingVal) {
        return newVal != null && !newVal.equals(existingVal);
    }

    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.netmgt.model.EntityVisitor} object.
     */
    public abstract void visit(EntityVisitor visitor);

}
