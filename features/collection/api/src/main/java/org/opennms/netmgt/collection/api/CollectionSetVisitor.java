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


/**
 * <p>CollectionSetVisitor interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectionSetVisitor {

    /**
     * <p>visitCollectionSet</p>
     *
     * @param set a {@link org.opennms.netmgt.collectd.CollectionSet} object.
     */
    void visitCollectionSet(CollectionSet set);

    /**
     * <p>visitResource</p>
     *
     * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
     */
    void visitResource(CollectionResource resource);

    /**
     * <p>visitGroup</p>
     *
     * @param group a {@link org.opennms.netmgt.collectd.AttributeGroup} object.
     */
    void visitGroup(AttributeGroup group);

    /**
     * <p>visitAttribute</p>
     *
     * @param attribute a {@link org.opennms.netmgt.collection.api.collectd.CollectionAttribute} object.
     */
    void visitAttribute(CollectionAttribute attribute);

    /**
     * <p>completeAttribute</p>
     *
     * @param attribute a {@link org.opennms.netmgt.collection.api.collectd.CollectionAttribute} object.
     */
    void completeAttribute(CollectionAttribute attribute);

    /**
     * <p>completeGroup</p>
     *
     * @param group a {@link org.opennms.netmgt.collectd.AttributeGroup} object.
     */
    void completeGroup(AttributeGroup group);

    /**
     * <p>completeResource</p>
     *
     * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
     */
    void completeResource(CollectionResource resource);

    /**
     * <p>completeCollectionSet</p>
     *
     * @param set a {@link org.opennms.netmgt.collectd.CollectionSet} object.
     */
    void completeCollectionSet(CollectionSet set);

}
