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
package org.opennms.netmgt.mock;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;

public class MockPersister implements Persister {

    @Override
    public void visitCollectionSet(CollectionSet set) {
        // pass
    }

    @Override
    public void visitResource(CollectionResource resource) {
        // pass
    }

    @Override
    public void visitGroup(AttributeGroup group) {
        // pass
    }

    @Override
    public void visitAttribute(CollectionAttribute attribute) {
        // pass
    }

    @Override
    public void completeAttribute(CollectionAttribute attribute) {
        // pass 
    }

    @Override
    public void completeGroup(AttributeGroup group) {
        // pass
    }

    @Override
    public void completeResource(CollectionResource resource) {
        // pass
    }

    @Override
    public void completeCollectionSet(CollectionSet set) {
        // pass
    }

    @Override
    public void persistNumericAttribute(CollectionAttribute attribute) {
        // pass
    }

    @Override
    public void persistStringAttribute(CollectionAttribute attribute) {
        // pass
    }
}
