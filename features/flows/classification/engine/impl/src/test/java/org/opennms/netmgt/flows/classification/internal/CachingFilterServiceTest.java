/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
