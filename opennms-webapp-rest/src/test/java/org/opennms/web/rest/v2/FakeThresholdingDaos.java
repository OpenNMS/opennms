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
package org.opennms.web.rest.v2;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.netmgt.config.dao.thresholding.api.WriteableThreshdDao;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThresholdingDao;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;

/**
 * In-memory stand-ins for the two configuration DAOs.
 *
 * <p>Hand-written rather than mocked, because {@code withWriteLock} is a default method on the interface:
 * a Mockito mock would stub it out, and the tests would then never execute the code path that production
 * actually runs. These fakes keep the real lock and the real read-modify-write semantics, including the
 * distinction between the merged read-only view and the writeable one.</p>
 */
abstract class FakeThresholdingDaos {

    private FakeThresholdingDaos() {
    }

    static class FakeThresholdingDao implements WriteableThresholdingDao {
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        private ThresholdingConfig writeableConfig;

        /** Stands in for the configuration contributed by OSGi extensions; null means "no extensions". */
        private ThresholdingConfig mergedConfig;

        private int saveCount;
        private RuntimeException saveFailure;

        FakeThresholdingDao(final ThresholdingConfig writeableConfig) {
            this.writeableConfig = writeableConfig;
        }

        void setMergedConfig(final ThresholdingConfig mergedConfig) {
            this.mergedConfig = mergedConfig;
        }

        void failNextSaveWith(final RuntimeException saveFailure) {
            this.saveFailure = saveFailure;
        }

        int getSaveCount() {
            return saveCount;
        }

        @Override
        public ThresholdingConfig getReadOnlyConfig() {
            return mergedConfig != null ? mergedConfig : writeableConfig;
        }

        @Override
        public ThresholdingConfig getWriteableConfig() {
            return writeableConfig;
        }

        @Override
        public void saveConfig() {
            if (saveFailure != null) {
                throw saveFailure;
            }
            saveCount++;
        }

        @Override
        public void reload() {
        }

        @Override
        public void onConfigChanged() {
        }

        @Override
        public Lock getReadLock() {
            return lock.readLock();
        }

        @Override
        public Lock getWriteLock() {
            return lock.writeLock();
        }
    }

    static class FakeThreshdDao implements WriteableThreshdDao {
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        private ThreshdConfiguration config;
        private int saveCount;

        FakeThreshdDao(final ThreshdConfiguration config) {
            this.config = config;
        }

        int getSaveCount() {
            return saveCount;
        }

        @Override
        public ThreshdConfiguration getReadOnlyConfig() {
            return config;
        }

        @Override
        public ThreshdConfiguration getWriteableConfig() {
            return config;
        }

        @Override
        public void saveConfig() {
            saveCount++;
        }

        @Override
        public void reload() {
        }

        @Override
        public void onConfigChanged() {
        }

        @Override
        public void rebuildPackageIpListMap() {
        }

        @Override
        public boolean interfaceInPackage(final String iface, final Package pkg) {
            return false;
        }

        @Override
        public Lock getReadLock() {
            return lock.readLock();
        }

        @Override
        public Lock getWriteLock() {
            return lock.writeLock();
        }
    }
}
