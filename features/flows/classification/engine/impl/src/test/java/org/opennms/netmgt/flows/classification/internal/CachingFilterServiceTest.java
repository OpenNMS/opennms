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
package org.opennms.netmgt.flows.classification.internal;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.exception.InvalidFilterException;

public class CachingFilterServiceTest {

    @Test
    public void verifyCaching() {
        // Create caching engine and spy o original engine
        final FilterService originalFilterService = new FilterService() {
            @Override
            public void validate(String filterExpression) throws InvalidFilterException {

            }

            @Override
            public boolean matches(String address, String filterExpression) {
                return false;
            }
        };
        final FilterService filterService = Mockito.spy(originalFilterService);
        final FilterService cachingService = new CachingFilterService(filterService, new CacheConfigBuilder().withName("classificationFilter").withExpireAfterRead(5).withMaximumSize(1000).build());


        // Invoke filter
        cachingService.matches("8.8.8.8", "categoryName == 'Routers");
        cachingService.matches("8.8.8.8", "categoryName == 'Routers");

        // Should only be invoked once
        Mockito.verify(filterService, Mockito.times(1)).matches(Mockito.anyString(), Mockito.anyString());
    }
}
