/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * A factory for creating ForeignSourceRepository objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
// TODO MVR simplify this pending vs deployed, und generell wird das nicht mehr benötigt ...
public class DefaultForeignSourceRepositoryFactory implements ForeignSourceRepositoryFactory, InitializingBean {

    /** The Constant REPOSITORY_IMPLEMENTATION. */
    public static final String REPOSITORY_IMPLEMENTATION = "org.opennms.provisiond.repositoryImplementation";

    /** The Constant DEFAULT_IMPLEMENTATION. */
    public static final String DEFAULT_IMPLEMENTATION = "database";

    @Autowired
    @Qualifier("database")
    private ForeignSourceRepository m_databaseRepository;

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepositoryFactory#getPendingRepository()
     */
    @Override
    public ForeignSourceRepository getPendingRepository() {
        switch (getRepositoryStrategy()) {
        case fastQueueing:
        case queueing:
        case fastCaching:
        case caching:
        case fastFused:
        case fused:
        case fastFile:
        case file:
            LoggerFactory.getLogger(getClass()).warn("The configured repository strategy '{}' is no longer supported. FAlling back to default: {}", getRepositoryStrategy(), DEFAULT_IMPLEMENTATION);
        default:
            return m_databaseRepository;
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepositoryFactory#getDeployedRepository()
     */
    @Override
    public ForeignSourceRepository getDeployedRepository() {
        return getPendingRepository();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepositoryFactory#getRepositoryStrategy()
     */
    @Override
    public FactoryStrategy getRepositoryStrategy() {
        // TODO MVR rausrupfen
        return FactoryStrategy.valueOf(System.getProperty(REPOSITORY_IMPLEMENTATION, DEFAULT_IMPLEMENTATION));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepositoryFactory#setRepositoryStrategy(java.lang.String)
     */
    @Override
    public synchronized void setRepositoryStrategy(FactoryStrategy strategy) {
        if (strategy != null) {
            System.setProperty(REPOSITORY_IMPLEMENTATION, strategy.toString());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

}
