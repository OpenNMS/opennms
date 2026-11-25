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
package org.opennms.core.mate.model;

import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.NoOpEntityScopeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean that creates either an {@link EntityScopeProviderImpl} or 
 * {@link NoOpEntityScopeProvider} based on the {@code org.opennms.metadata.interpolation.enabled}
 * system property.
 * 
 * <p>When the property is set to {@code false}, the no-op implementation is returned,
 * which improves performance for deployments that don't use metadata interpolation.</p>
 * 
 * <p>Default is enabled ({@code true}) to maintain backward compatibility.</p>
 */
public class EntityScopeProviderFactory implements FactoryBean<EntityScopeProvider> {

    private static final Logger LOG = LoggerFactory.getLogger(EntityScopeProviderFactory.class);

    public static final String ENABLED_PROPERTY = "org.opennms.metadata.interpolation.enabled";

    private EntityScopeProviderImpl delegate;

    public void setDelegate(EntityScopeProviderImpl delegate) {
        this.delegate = delegate;
    }

    @Override
    public EntityScopeProvider getObject() {
        if (isEnabled()) {
            LOG.debug("Metadata interpolation is enabled");
            return delegate;
        }
        LOG.info("Metadata interpolation is DISABLED via system property '{}'. " +
                 "Metadata expressions will not be evaluated.", ENABLED_PROPERTY);
        return NoOpEntityScopeProvider.INSTANCE;
    }

    @Override
    public Class<?> getObjectType() {
        return EntityScopeProvider.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty(ENABLED_PROPERTY, "true"));
    }
}
