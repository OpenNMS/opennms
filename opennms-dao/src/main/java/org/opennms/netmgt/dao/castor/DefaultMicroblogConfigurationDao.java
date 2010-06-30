/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: May 28, 2010
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.castor;

import org.opennms.netmgt.config.microblog.MicroblogConfiguration;
import org.opennms.netmgt.config.microblog.MicroblogProfile;
import org.opennms.netmgt.dao.MicroblogConfigurationDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Default implementation of <code>MicroblogConfiguration</code> containing utility methods for manipulating
 * the <code>MicroblogNotificationStrategy</code> and companion classes.
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 */
public class DefaultMicroblogConfigurationDao extends AbstractCastorConfigDao<MicroblogConfiguration, MicroblogConfiguration> implements MicroblogConfigurationDao {

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
    public void reloadConfiguration() throws DataAccessResourceFailureException {
        getContainer().reload();
    }

    /**
     * <p>getDefaultProfile</p>
     *
     * @return a {@link org.opennms.netmgt.config.microblog.MicroblogProfile} object.
     */
    public MicroblogProfile getDefaultProfile() {
        String defaultProfileName = getContainer().getObject().getDefaultMicroblogProfileName();
        log().debug("Requesting default microblog, which is called '" + defaultProfileName + "'");
        return getProfile(defaultProfileName);
    }

    /** {@inheritDoc} */
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
