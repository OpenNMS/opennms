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
package org.opennms.core.daemon.loader;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.api.ProvisiondConfigurationDao;

/**
 * Standalone provisiond configuration DAO that returns sensible defaults.
 * Avoids needing DefaultProvisiondConfigurationDao from opennms-dao (which
 * can't be classloaded in the daemon-loader's OSGi context).
 */
public class InlineProvisiondConfigDao implements ProvisiondConfigurationDao {

    private static final int DEFAULT_THREADS = 4;
    private static final String FOREIGN_SOURCE_DIR = "/opt/sentinel/etc/foreign-sources";
    private static final String REQUISITION_DIR = "/opt/sentinel/etc/imports";

    private final ProvisiondConfiguration config;

    public InlineProvisiondConfigDao() {
        config = new ProvisiondConfiguration();
        config.setForeignSourceDir(FOREIGN_SOURCE_DIR);
        config.setRequistionDir(REQUISITION_DIR);
        config.setImportThreads((long) DEFAULT_THREADS);
        config.setScanThreads((long) DEFAULT_THREADS);
        config.setRescanThreads((long) DEFAULT_THREADS);
        config.setWriteThreads((long) DEFAULT_THREADS);
    }

    @Override
    public ProvisiondConfiguration getConfig() {
        return config;
    }

    @Override
    public RequisitionDef getDef(String defName) {
        return null;
    }

    @Override
    public List<RequisitionDef> getDefs() {
        return Collections.emptyList();
    }

    @Override
    public String getForeignSourceDir() {
        return FOREIGN_SOURCE_DIR;
    }

    @Override
    public String getRequisitionDir() {
        return REQUISITION_DIR;
    }

    @Override
    public Integer getImportThreads() {
        return DEFAULT_THREADS;
    }

    @Override
    public Integer getScanThreads() {
        return DEFAULT_THREADS;
    }

    @Override
    public Integer getRescanThreads() {
        return DEFAULT_THREADS;
    }

    @Override
    public Integer getWriteThreads() {
        return DEFAULT_THREADS;
    }

    @Override
    public void reloadConfiguration() {
        // No-op in standalone container
    }

    @Override
    public Map<String, Long> getRequisitionSchemeCount() {
        return Collections.emptyMap();
    }
}
