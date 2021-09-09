/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb;

import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.api.ProvisiondConfigurationDao;

import java.io.IOException;
import java.util.List;

/**
 * Default implementation of <code>AckdConfiguration</code> containing utility methods for manipulating
 * the <code>Ackd</code> and <code>AckdReader</code>s.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class DefaultProvisiondConfigurationDao extends AbstractCmJaxbConfigDao<ProvisiondConfiguration> implements ProvisiondConfigurationDao {

    private static final String CONFIG_NAME = "provisiond";
    private static final String DEFAULT_CONFIG_ID = "default";

    /**
     * <p>Constructor for DefaultProvisiondConfigurationDao.</p>
     */
    public DefaultProvisiondConfigurationDao() {
        super(ProvisiondConfiguration.class, "Provisiond Configuration");
    }

    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.provisiond.ProvisiondConfiguration} object.
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
    public String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    protected String getDefaultConfigId() {
        return DEFAULT_CONFIG_ID;
    }
}
