/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.mate.model;

import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.mate.api.ScopeProvider;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.opennms.core.mate.api.EmptyScope.EMPTY;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-entity-scope-provider.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class CachedEntityScopeProviderIT extends EntityScopeProviderIT {

    @Before
    @Override
    public void setup() {
        this.populator.populateDatabase();
        this.provider = new CachedEntityScopeProviderImpl(provider, 0, -1, -1, -1);
    }

    @Test
    public void checkForNullPointerException() {
        assertEquals(EMPTY, this.provider.getScopeForNode(null));
    }

    @Test
    public void testCache() {
        this.provider = new CachedEntityScopeProviderImpl(provider, 5, -1, -1, -1);

        // set meta-data of node
        final OnmsNode node = this.populator.getNode1();
        OnmsMetaData metaData = new OnmsMetaData("context", "key", "value1");
        node.getMetaData().add(metaData);
        this.populator.getNodeDao().saveOrUpdate(node);

        // get a scope provider
        final ScopeProvider scope = this.provider.getScopeProviderForNode(this.populator.getNode1().getId());

        // this will retrieve the meta-data set before
        assertThat(scope.getScope().get(new ContextKey("context", "key")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "value1"))));

        // now update the meta-data
        node.getMetaData().removeAll(Lists.newArrayList(metaData));
        metaData = new OnmsMetaData("context", "key", "value2");
        node.getMetaData().add(metaData);
        this.populator.getNodeDao().saveOrUpdate(node);

        // this should still return the old value
        assertThat(scope.getScope().get(new ContextKey("context", "key")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "value1"))));

        await().atMost(10, SECONDS).pollInterval(250, MILLISECONDS)
                .until(() -> scope.getScope().get(new ContextKey("context", "key")), Matchers.is(Optional.of(new Scope.ScopeValue(Scope.ScopeName.NODE, "value2"))));
    }}