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
package org.opennms.netmgt.threshd;

import java.util.Date;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.model.ResourcePath;

public class MockCollectionResourceWrapper extends CollectionResourceWrapper {

    public MockCollectionResourceWrapper(final String instance) {
        super(new Date(), 0, null, null, new CollectionResource() {
            @Override
            public String getInstance() {
                return instance;
            }
            @Override
            public String getUnmodifiedInstance() {
                return instance;
            }
            @Override
            public String getInterfaceLabel() {
                return null;
            }
            @Override
            public String getResourceTypeName() {
                return "test";
            }
            @Override
            public boolean rescanNeeded() {
                return false;
            }
            @Override
            public boolean shouldPersist(ServiceParameters params) {
                return false;
            }
            @Override
            public void visit(CollectionSetVisitor visitor) {
            }
            @Override
            public String getOwnerName() {
                return null;
            }
            @Override
            public ResourcePath getParent() {
                return null;
            }
            @Override
            public TimeKeeper getTimeKeeper() {
                return null;
            }
            @Override
            public ResourcePath getPath() {
                return null;
            }
        }, null, null, null);
    }

}
