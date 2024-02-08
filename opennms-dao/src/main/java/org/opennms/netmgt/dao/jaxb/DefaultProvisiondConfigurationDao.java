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
package org.opennms.netmgt.dao.jaxb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.api.ProvisiondConfigurationDao;
import org.opennms.netmgt.dao.jaxb.callback.ConfigurationReloadEventCallback;
import org.opennms.netmgt.dao.jaxb.callback.ProvisiondConfigurationValidationCallback;
import org.opennms.netmgt.events.api.EventForwarder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of <code>ProvisiondConfigurationDao</code>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class DefaultProvisiondConfigurationDao extends AbstractCmJaxbConfigDao<ProvisiondConfiguration> implements ProvisiondConfigurationDao {

    public static final String CONFIG_NAME = "provisiond";

    @Autowired
    private EventForwarder eventForwarder;

    /**
     * <p>Constructor for DefaultProvisiondConfigurationDao.</p>
     */
    public DefaultProvisiondConfigurationDao() {
        super(ProvisiondConfiguration.class, "Provisiond Configuration");
    }

    /**
     * <p>getConfig</p>
     *
     * @return a {@link ProvisiondConfiguration} object.
     * @throws IOException
     */
    @Override
    public ProvisiondConfiguration getConfig() throws IOException {
        return this.getConfig(this.getDefaultConfigId());
    }

    /**
     * The exception boils up from the container class  The container class should
     * indicate this.
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    @Override
    public void reloadConfiguration() throws IOException {
        this.loadConfig(this.getDefaultConfigId());
    }

    /** {@inheritDoc} */
    @Override
    public RequisitionDef getDef(String defName) throws IOException {
        final List<RequisitionDef> defs = getDefs();
        if (defs != null) {
            for (RequisitionDef def : defs) {
                if (defName.equals(def.getImportName().orElse(null))) {
                    return def;
                }
            }
        }
        return null;
    }

    /**
     * <p>getDefs</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<RequisitionDef> getDefs() throws IOException {
        return getConfig().getRequisitionDefs();
    }

    /**
     * <p>getForeignSourceDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getForeignSourceDir() throws IOException {
        return getConfig().getForeignSourceDir();
    }

    /**
     * <p>getRequisitionDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getRequisitionDir() throws IOException {
        return getConfig().getRequistionDir();
    }

    /**
     * <p>getImportThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getImportThreads() throws IOException {
        return getConfig().getImportThreads().intValue();
    }

    /**
     * <p>getScanThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getScanThreads() throws IOException {
        return getConfig().getScanThreads().intValue();
    }

    /**
     * <p>getRescanThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getRescanThreads() throws IOException {
        return getConfig().getRescanThreads().intValue();
    }

    /**
     * <p>getWriteThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getWriteThreads() throws IOException {
        return getConfig().getWriteThreads().intValue();
    }

    @Override
    public Consumer<ConfigUpdateInfo> getUpdateCallback(){
        return new ConfigurationReloadEventCallback(eventForwarder);
    }

    @Override
    public Consumer<ConfigUpdateInfo> getValidationCallback(){
        return new ProvisiondConfigurationValidationCallback();
    }

    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    public Map<String, Long> getRequisitionSchemeCount() throws IOException {
        return getDefs().stream()
                .filter(r -> r.getImportUrlResource().isPresent())
                .map(r -> {
                    try {
                        return new URL(r.getImportUrlResource().get());
                    } catch (MalformedURLException e) {
                        return null;
                    }
                })
                .filter(r -> r != null)
                .collect(Collectors.groupingBy(r -> r.getProtocol(), Collectors.counting()));
    }
}