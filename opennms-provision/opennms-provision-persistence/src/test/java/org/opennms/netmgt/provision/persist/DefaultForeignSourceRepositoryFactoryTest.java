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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class DefaultForeignSourceRepositoryFactoryTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class DefaultForeignSourceRepositoryFactoryTest extends ForeignSourceRepositoryTestCase {

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
