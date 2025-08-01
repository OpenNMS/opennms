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
package org.opennms.netmgt.dao.support;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.newts.support.SearchableResourceMetadataCache;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.cassandra.search.ResourceMetadata;

import com.google.common.base.Optional;

public class MockSearchableResourceMetadataCache implements SearchableResourceMetadataCache {
    @Override
    public void merge(Context context, Resource resource,
            ResourceMetadata rMetadata) {
        // pass
    }

    @Override
    public Optional<ResourceMetadata> get(Context context,
            Resource resource) {
        return Optional.absent();
    }

    @Override
    public void delete(final Context context, final Resource resource) {

    }

    @Override
    public List<String> getResourceIdsWithPrefix(Context context, String resourceIdPrefix) {
        return Collections.emptyList();
    }
}
