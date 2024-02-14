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
package org.opennms.features.distributed.coordination.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.features.distributed.coordination.api.DomainManager;
import org.opennms.features.distributed.coordination.api.DomainManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A skeleton implementation of {@link DomainManagerFactory}.
 */
public abstract class AbstractDomainManagerFactory implements DomainManagerFactory {
    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDomainManagerFactory.class);

    /**
     * The map of domains to managers.
     */
    private final Map<String, DomainManager> domainManagers = new HashMap<>();

    /**
     * Creates the appropriate manager for the given domain.
     *
     * @param domain the domain to manage
     * @return the manager for the given domain
     */
    protected abstract DomainManager createManagerForDomain(String domain);

    @Override
    public synchronized final DomainManager getManagerForDomain(String domain) {
        if (!Objects.requireNonNull(domain).matches("^[a-zA-Z0-9.-_]*$")) {
            throw new IllegalArgumentException("Invalid domain");
        }

        if (domainManagers.containsKey(domain)) {
            LOG.debug("Returning existing manager for domain {}", domain);

            return domainManagers.get(domain);
        } else {
            LOG.debug("Creating new manager for domain {}", domain);
            DomainManager domainManager = createManagerForDomain(domain);
            domainManagers.put(domain, domainManager);

            return domainManager;
        }
    }

    @Override
    public String toString() {
        return "AbstractDomainManagerFactory{" +
                "domainManagers=" + domainManagers +
                '}';
    }
}
