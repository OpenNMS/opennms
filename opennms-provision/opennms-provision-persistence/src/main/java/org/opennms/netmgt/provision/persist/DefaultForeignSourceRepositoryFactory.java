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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;

/**
 * A factory for creating ForeignSourceRepository objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class DefaultForeignSourceRepositoryFactory implements ForeignSourceRepositoryFactory, InitializingBean {

    /** The Constant REPOSITORY_IMPLEMENTATION. */
    public static final String REPOSITORY_IMPLEMENTATION = "org.opennms.provisiond.repositoryImplementation";

    /** The Constant DEFAULT_IMPLEMENTATION. */
    public static final String DEFAULT_IMPLEMENTATION = "file";

    /** The file based pending repository. */
    @Autowired
    @Qualifier("filePending")
    private ForeignSourceRepository m_filePendingRepository;

    /** The file based deployed repository. */
    @Autowired
    @Qualifier("fileDeployed")
    private ForeignSourceRepository m_fileDeployedRepository;

    /** The fast file based file pending repository. */
    @Autowired
    @Qualifier("fastFilePending")
    private ForeignSourceRepository m_fastFilePendingRepository;

    /** The fast file based file deployed repository. */
    @Autowired
    @Qualifier("fastFileDeployed")
    private ForeignSourceRepository m_fastFileDeployedRepository;

    /** The fused repository. */
    @Autowired
    @Qualifier("fused")
    private ForeignSourceRepository m_fusedRepository;

    /** The fast fused repository. */
    @Autowired
    @Qualifier("fastFused")
    private ForeignSourceRepository m_fastFusedRepository;

    /** The caching repository. */
    @Autowired
    @Qualifier("caching")
    private ForeignSourceRepository m_cachingRepository;

    /** The fast caching repository. */
    @Autowired
    @Qualifier("fastCaching")
    private ForeignSourceRepository m_fastCachingRepository;

    /** The queueing repository. */
    @Autowired
    @Qualifier("queueing")
    private ForeignSourceRepository m_queueingRepository;

    /** The fast queueing repository. */
    @Autowired
    @Qualifier("fastQueueing")
    private ForeignSourceRepository m_fastQueueingRepository;

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepositoryFactory#getPendingRepository()
     */
    @Override
    public ForeignSourceRepository getPendingRepository() {
        switch (getRepositoryStrategy()) {
        case fastQueueing:
            return m_fastQueueingRepository;
        case queueing:
            return m_queueingRepository;
        case fastCaching:
            return m_fastCachingRepository;
        case caching:
            return m_cachingRepository;
        case fastFused:
            return m_fastFusedRepository;
        case fused:
            return m_fusedRepository;
        case fastFile:
            return m_fastFilePendingRepository;
        case file:
        default:
            return m_filePendingRepository;
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepositoryFactory#getDeployedRepository()
     */
    @Override
    public ForeignSourceRepository getDeployedRepository() {
        switch (getRepositoryStrategy()) {
        case fastQueueing:
            return m_fastQueueingRepository;
        case queueing:
            return m_queueingRepository;
        case fastCaching:
            return m_fastCachingRepository;
        case caching:
            return m_cachingRepository;
        case fastFused:
            return m_fastFusedRepository;
        case fused:
            return m_fusedRepository;
        case fastFile:
            return m_fastFileDeployedRepository;
        case file:
        default:
            return m_fileDeployedRepository;
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepositoryFactory#getRepositoryStrategy()
     */
    @Override
    public FactoryStrategy getRepositoryStrategy() {
        return FactoryStrategy.valueOf(System.getProperty(REPOSITORY_IMPLEMENTATION, DEFAULT_IMPLEMENTATION));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.persist.ForeignSourceRepositoryFactory#setRepositoryStrategy(java.lang.String)
     */
    @Override
    public synchronized void setRepositoryStrategy(FactoryStrategy strategy) {
        if (strategy != null) {
            getDeployedRepository().flush();
            getPendingRepository().flush();
            System.setProperty(REPOSITORY_IMPLEMENTATION, strategy.toString());
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_fastQueueingRepository);
        Assert.notNull(m_queueingRepository);
        Assert.notNull(m_fastCachingRepository);
        Assert.notNull(m_cachingRepository);
        Assert.notNull(m_fastFusedRepository);
        Assert.notNull(m_fusedRepository);
        Assert.notNull(m_fastFileDeployedRepository);
        Assert.notNull(m_fastFilePendingRepository);
        Assert.notNull(m_fileDeployedRepository);
        Assert.notNull(m_filePendingRepository);
    }

}
