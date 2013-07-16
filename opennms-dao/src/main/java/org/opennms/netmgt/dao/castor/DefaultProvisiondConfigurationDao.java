/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.castor;

import java.util.List;

import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.dao.api.ProvisiondConfigurationDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Default implementation of <code>AckdConfiguration</code> containing utility methods for manipulating
 * the <code>Ackd</code> and <code>AckdReader</code>s.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class DefaultProvisiondConfigurationDao extends AbstractCastorConfigDao<ProvisiondConfiguration, ProvisiondConfiguration> implements ProvisiondConfigurationDao {

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
     */
    @Override
    public ProvisiondConfiguration getConfig() {
        return getContainer().getObject();
    }

    /** {@inheritDoc} */
    @Override
    public ProvisiondConfiguration translateConfig(ProvisiondConfiguration castorConfig) {
        return castorConfig;
    }

    /**
     * The exception boils up from the container class  The container class should
     * indicate this.
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    @Override
    public void reloadConfiguration() throws DataAccessResourceFailureException {
        getContainer().reload();
    }

    /** {@inheritDoc} */
    @Override
    public RequisitionDef getDef(String defName) {
        final List<RequisitionDef> defs = getDefs();
        if (defs != null) {
            for (RequisitionDef def : defs) {
                if (def.getImportName().equals(defName)) {
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
    public List<RequisitionDef> getDefs() {
        return getConfig().getRequisitionDefCollection();
    }

    /**
     * <p>getForeignSourceDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getForeignSourceDir() {
        return getConfig().getForeignSourceDir();
    }

    /**
     * <p>getRequisitionDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getRequisitionDir() {
        return getConfig().getRequistionDir();
    }

    /**
     * <p>getImportThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getImportThreads() {
        return Integer.valueOf((int)getConfig().getImportThreads());
    }

    /**
     * <p>getScanThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getScanThreads() {
        return Integer.valueOf((int)getConfig().getScanThreads());
    }

    /**
     * <p>getRescanThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getRescanThreads() {
        return Integer.valueOf((int)getConfig().getRescanThreads());
    }

    /**
     * <p>getWriteThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getWriteThreads() {
        return Integer.valueOf((int)getConfig().getWriteThreads());
    }
    
}
