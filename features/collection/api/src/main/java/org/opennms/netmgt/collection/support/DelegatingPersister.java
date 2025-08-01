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
package org.opennms.netmgt.collection.support;

import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;

public class DelegatingPersister implements Persister {

    private final List<Persister> delegates;

    public DelegatingPersister(List<Persister> delegates) {
        this.delegates = Objects.requireNonNull(delegates);
    }

    @Override
    public void persistNumericAttribute(CollectionAttribute attribute) {
        delegates.forEach(p  -> p.persistNumericAttribute(attribute));
    }

    @Override
    public void persistStringAttribute(CollectionAttribute attribute) {
        delegates.forEach(p  -> p.persistStringAttribute(attribute));
    }

    @Override
    public void visitCollectionSet(CollectionSet set) {
        delegates.forEach(p  -> p.visitCollectionSet(set));
    }

    @Override
    public void visitResource(CollectionResource resource) {
        delegates.forEach(p  -> p.visitResource(resource));
    }

    @Override
    public void visitGroup(AttributeGroup group) {
        delegates.forEach(p  -> p.visitGroup(group));
    }

    @Override
    public void visitAttribute(CollectionAttribute attribute) {
        delegates.forEach(p  -> p.visitAttribute(attribute));
    }

    @Override
    public void completeAttribute(CollectionAttribute attribute) {
        delegates.forEach(p  -> p.completeAttribute(attribute));
    }

    @Override
    public void completeGroup(AttributeGroup group) {
        delegates.forEach(p  -> p.completeGroup(group));
    }

    @Override
    public void completeResource(CollectionResource resource) {
        delegates.forEach(p  -> p.completeResource(resource));
    }

    @Override
    public void completeCollectionSet(CollectionSet set) {
        delegates.forEach(p  -> p.completeCollectionSet(set));
    }
}
