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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class DefaultForeignSourceRepositoryFactoryIT.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class DefaultForeignSourceRepositoryFactoryIT extends ForeignSourceRepositoryTestCase {

    /** The m_foreign source repository factory. */
    @Autowired
    private ForeignSourceRepositoryFactory m_foreignSourceRepositoryFactory;

    /**
     * Test factory.
     */
    @Test
    public void testFactory() {
        // Testing default implementation

        assertTrue(m_foreignSourceRepositoryFactory.getDeployedRepository() instanceof FilesystemForeignSourceRepository);
        assertTrue(m_foreignSourceRepositoryFactory.getPendingRepository() instanceof FilesystemForeignSourceRepository);

        // Testing custom implementation

        m_foreignSourceRepositoryFactory.setRepositoryStrategy(FactoryStrategy.fastFile);
        assertTrue(m_foreignSourceRepositoryFactory.getDeployedRepository() instanceof FasterFilesystemForeignSourceRepository);
        assertTrue(m_foreignSourceRepositoryFactory.getPendingRepository() instanceof FasterFilesystemForeignSourceRepository);

        m_foreignSourceRepositoryFactory.setRepositoryStrategy(FactoryStrategy.fused);
        assertTrue(m_foreignSourceRepositoryFactory.getDeployedRepository() instanceof FusedForeignSourceRepository);
        assertTrue(m_foreignSourceRepositoryFactory.getPendingRepository() instanceof FusedForeignSourceRepository);

        m_foreignSourceRepositoryFactory.setRepositoryStrategy(FactoryStrategy.caching);
        assertTrue(m_foreignSourceRepositoryFactory.getDeployedRepository() instanceof CachingForeignSourceRepository);
        assertTrue(m_foreignSourceRepositoryFactory.getPendingRepository() instanceof CachingForeignSourceRepository);

        m_foreignSourceRepositoryFactory.setRepositoryStrategy(FactoryStrategy.fastCaching);
        assertTrue(m_foreignSourceRepositoryFactory.getDeployedRepository() instanceof CachingForeignSourceRepository);
        assertTrue(m_foreignSourceRepositoryFactory.getPendingRepository() instanceof CachingForeignSourceRepository);

        m_foreignSourceRepositoryFactory.setRepositoryStrategy(FactoryStrategy.queueing);
        assertTrue(m_foreignSourceRepositoryFactory.getDeployedRepository() instanceof QueueingForeignSourceRepository);
        assertTrue(m_foreignSourceRepositoryFactory.getPendingRepository() instanceof QueueingForeignSourceRepository);

        m_foreignSourceRepositoryFactory.setRepositoryStrategy(FactoryStrategy.fastQueueing);
        assertTrue(m_foreignSourceRepositoryFactory.getDeployedRepository() instanceof QueueingForeignSourceRepository);
        assertTrue(m_foreignSourceRepositoryFactory.getPendingRepository() instanceof QueueingForeignSourceRepository);
    }
}
