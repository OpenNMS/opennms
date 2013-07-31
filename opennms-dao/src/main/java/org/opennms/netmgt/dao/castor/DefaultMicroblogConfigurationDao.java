/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.config.microblog.MicroblogConfiguration;
import org.opennms.netmgt.config.microblog.MicroblogProfile;
import org.opennms.netmgt.dao.api.MicroblogConfigurationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Default implementation of <code>MicroblogConfiguration</code> containing utility methods for manipulating
 * the <code>MicroblogNotificationStrategy</code> and companion classes.
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 */
public class DefaultMicroblogConfigurationDao extends AbstractCastorConfigDao<MicroblogConfiguration, MicroblogConfiguration> implements MicroblogConfigurationDao {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMicroblogConfigurationDao.class);

    /**
     * <p>Constructor for DefaultMicroblogConfigurationDao.</p>
     */
    public DefaultMicroblogConfigurationDao() {
        super(MicroblogConfiguration.class, "Microblog Configuration");
    }
    
    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.microblog.MicroblogConfiguration} object.
     */
    @Override
    public MicroblogConfiguration getConfig() {
        return getContainer().getObject();
    }

    /** {@inheritDoc} */
    @Override
    public MicroblogConfiguration translateConfig(MicroblogConfiguration castorConfig) {
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

    /**
     * <p>getDefaultProfile</p>
     *
     * @return a {@link org.opennms.netmgt.config.microblog.MicroblogProfile} object.
     */
    @Override
    public MicroblogProfile getDefaultProfile() {
        String defaultProfileName = getContainer().getObject().getDefaultMicroblogProfileName();
        LOG.debug("Requesting default microblog, which is called '{}'", defaultProfileName);
        return getProfile(defaultProfileName);
    }

    /** {@inheritDoc} */
    @Override
    public MicroblogProfile getProfile(String name) {
        if (name == null)
            return null;
        for (MicroblogProfile profile : getContainer().getObject().getMicroblogProfileCollection()) {
            if (name.equals(profile.getName()))
                return profile;
        }
        return null;
    }

}
