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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ListIterator;

import org.apache.commons.io.IOUtils;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
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
public class DefaultMicroblogConfigurationDao extends AbstractJaxbConfigDao<MicroblogConfiguration, MicroblogConfiguration> implements MicroblogConfigurationDao {

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
    public MicroblogConfiguration translateConfig(MicroblogConfiguration config) {
        return config;
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
        for (MicroblogProfile profile : getContainer().getObject().getMicroblogProfiles()) {
            if (name.equals(profile.getName()))
                return profile;
        }
        return null;
    }

    public void saveProfile(final MicroblogProfile profile) throws IOException {
        reloadConfiguration();
        final MicroblogConfiguration config = getContainer().getObject();

        boolean found = false;
        final ListIterator<MicroblogProfile> it = config.getMicroblogProfiles().listIterator();
        while (it.hasNext()) {
            final MicroblogProfile existing = it.next();
            if (existing.getName().equals(profile.getName())) {
                found = true;
                it.set(profile);
                break;
            }
        }
        if (!found) config.addMicroblogProfile(profile);

        final File file = getContainer().getFile();
        if (file == null) {
            LOG.warn("No file associated with this config.  Skipping marshal.");
            return;
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            JaxbUtils.marshal(config, writer);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

}
