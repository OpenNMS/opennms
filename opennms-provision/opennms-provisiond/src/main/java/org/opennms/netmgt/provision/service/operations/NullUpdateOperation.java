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
package org.opennms.netmgt.provision.service.operations;

import org.opennms.netmgt.provision.service.ProvisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullUpdateOperation extends UpdateOperation {

    private static final Logger LOG = LoggerFactory.getLogger(NullUpdateOperation.class);

    public NullUpdateOperation(final Integer nodeId, final String foreignSource, final String foreignId, final String nodeLabel, final String location, final String building, final String city, final ProvisionService provisionService, final String rescanExisting, final String monitorKey) {
        super(nodeId, foreignSource, foreignId, nodeLabel, location, building, city, provisionService, rescanExisting, monitorKey);
    }

    @Override
    protected void doPersist() {
        LOG.debug("Skipping persist for node {}: rescanExisting is false", getNode());
    }
}