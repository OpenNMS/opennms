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
