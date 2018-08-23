/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.features.distributed.coordination.base;

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
    private final Map<String, DomainManager> haManagers = new HashMap<>();

    /**
     * Creates the appropriate manager for the given domain.
     *
     * @param domain the domain to manage
     * @return the manager for the given domain
     */
    protected abstract DomainManager createManagerForDomain(String domain);

    @Override
    public synchronized final DomainManager getManagerForDomain(String domain) {
        if (haManagers.containsKey(Objects.requireNonNull(domain))) {
            LOG.debug("Returning existing manager for domain '", domain, "'");

            return haManagers.get(domain);
        } else {
            LOG.debug("Creating new manager for domain '", domain, "'");
            DomainManager domainManager = createManagerForDomain(domain);
            haManagers.put(domain, domainManager);

            return domainManager;
        }
    }

    @Override
    public String toString() {
        return "AbstractDomainManagerFactory{" +
                "haManagers=" + haManagers +
                '}';
    }
}
