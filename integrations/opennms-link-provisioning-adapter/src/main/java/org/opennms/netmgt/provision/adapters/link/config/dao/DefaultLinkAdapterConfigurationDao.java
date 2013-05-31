/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.adapters.link.config.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.netmgt.provision.adapters.link.config.linkadapter.LinkAdapterConfiguration;
import org.opennms.netmgt.provision.adapters.link.config.linkadapter.LinkPattern;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

public class DefaultLinkAdapterConfigurationDao extends AbstractJaxbConfigDao<LinkAdapterConfiguration, LinkAdapterConfiguration> implements LinkAdapterConfigurationDao {
    /**
     * <p>Constructor for DefaultLinkAdapterConfigurationDao.</p>
     */
    public DefaultLinkAdapterConfigurationDao() {
        super(LinkAdapterConfiguration.class, "Map Link Adapter Configuration");
    }
    
    /**
     * <p>Constructor for DefaultLinkAdapterConfigurationDao.</p>
     *
     * @param entityClass a {@link java.lang.Class} object.
     * @param description a {@link java.lang.String} object.
     */
    public DefaultLinkAdapterConfigurationDao(Class<LinkAdapterConfiguration> entityClass, String description) {
        super(entityClass, description);
    }

    /** {@inheritDoc} */
    @Override
    public LinkAdapterConfiguration translateConfig(LinkAdapterConfiguration config) {
        return config;
    }

    /**
     * <p>getPatterns</p>
     *
     * @return a {@link java.util.Set} object.
     */
    @Override
    public Set<LinkPattern> getPatterns() {
        Assert.notNull(getContainer(), "LinkAdapterConfigDao has no container!");
        Assert.notNull(getContainer().getObject(), "LinkAdapterConfigDao has no configuration loaded!");
        return getContainer().getObject().getPatterns();
    }

    /** {@inheritDoc} */
    @Override
    public void setPatterns(Set<LinkPattern> patterns) {
        Assert.notNull(getContainer(), "LinkAdapterConfigDao has no container!");
        Assert.notNull(getContainer().getObject(), "LinkAdapterConfigDao has no configuration loaded!");
        getContainer().getObject().setPatterns(patterns);
    }

    /**
     * <p>saveCurrent</p>
     */
    @Override
    public synchronized void saveCurrent() {
        File file;
        try {
            file = getConfigResource().getFile();
        } catch (IOException e) {
            throw new DataAccessResourceFailureException("Unable to determine file for " + getConfigResource() + ": " + e, e);
        }
        if (file == null) {
            throw new DataAccessResourceFailureException("Unable to determine file for " + getConfigResource());
        }

        final StringWriter stringWriter = new StringWriter();
        JaxbUtils.marshal(getContainer().getObject(), stringWriter);

        if (stringWriter.toString() != null) {
            OutputStream os = null;
            Writer fileWriter = null;
            try {
                os = new FileOutputStream(file);
                fileWriter = new OutputStreamWriter(os, "UTF-8");
                fileWriter.write(stringWriter.toString());
            } catch (final IOException e) {
                throw new DataAccessResourceFailureException("Could not write resource " + getConfigResource() + " to file " + file.getPath() + ": " + e, e);
            } finally {
                IOUtils.closeQuietly(fileWriter);
                IOUtils.closeQuietly(os);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected LinkAdapterConfiguration loadConfig(final Resource resource) {
        long startTime = System.currentTimeMillis();

        if (log().isDebugEnabled()) {
            log().debug("Loading " + getDescription() + " configuration from " + resource);
        }

        try {
            LinkAdapterConfiguration config = JaxbUtils.unmarshal(LinkAdapterConfiguration.class, resource);
            long endTime = System.currentTimeMillis();
            log().info(createLoadedLogMessage(config, (endTime - startTime)));
            log().info(config.toString());
            return config;
        } catch (Throwable e) {
            throw new MarshallingResourceFailureException("Unable to unmarshal the link adapter configuration.", e);
        }
    }
}
