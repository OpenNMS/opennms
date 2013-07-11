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

package org.opennms.netmgt.provision.persist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public abstract class AbstractForeignSourceRepository implements ForeignSourceRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractForeignSourceRepository.class);
    
    /**
     * <p>Constructor for AbstractForeignSourceRepository.</p>
     */
    public AbstractForeignSourceRepository() {
    }

    /** {@inheritDoc} */
    @Override
    public Requisition importResourceRequisition(final Resource resource) throws ForeignSourceRepositoryException {
        Assert.notNull(resource);
 
        LOG.debug("importing requisition from {}", resource);
        final Requisition requisition = JaxbUtils.unmarshal(Requisition.class, resource);
        requisition.setResource(resource);
        save(requisition);
        return requisition;
    }

    /**
     * <p>getDefaultForeignSource</p>
     *
     * @return a {@link org.opennms.netmgt.provision.persist.foreignsource.ForeignSource} object.
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public ForeignSource getDefaultForeignSource() throws ForeignSourceRepositoryException {
        Resource defaultForeignSource = new ClassPathResource("/default-foreign-source.xml");
        if (!defaultForeignSource.exists()) {
            defaultForeignSource = new ClassPathResource("/org/opennms/netmgt/provision/persist/default-foreign-source.xml");
        }
        final ForeignSource fs = JaxbUtils.unmarshal(ForeignSource.class, defaultForeignSource);
        fs.setDefault(true);
        return fs;
    }

    /** {@inheritDoc} */
    @Override
    public void putDefaultForeignSource(ForeignSource foreignSource) throws ForeignSourceRepositoryException {
        if (foreignSource == null) {
            throw new ForeignSourceRepositoryException("foreign source was null");
        }
        foreignSource.setName("default");
        foreignSource.updateDateStamp();
 
        final File outputFile = new File(ConfigFileConstants.getFilePathString() + "default-foreign-source.xml");
        Writer writer = null;
        OutputStream outputStream = null;
        try {
            foreignSource.updateDateStamp();
            outputStream = new FileOutputStream(outputFile);
			writer = new OutputStreamWriter(outputStream, "UTF-8");
            JaxbUtils.marshal(foreignSource, writer);
        } catch (final Throwable e) {
            throw new ForeignSourceRepositoryException("unable to write requisition to " + outputFile.getPath(), e);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * <p>resetDefaultForeignSource</p>
     *
     * @throws org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException if any.
     */
    @Override
    public void resetDefaultForeignSource() throws ForeignSourceRepositoryException {
    	final File deleteFile = new File(ConfigFileConstants.getFilePathString() + "default-foreign-source.xml");
        if (!deleteFile.exists()) {
            return;
        }
        if (!deleteFile.delete()) {
            LOG.warn("unable to remove {}", deleteFile.getPath());
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsNodeRequisition getNodeRequisition(String foreignSource, String foreignId) throws ForeignSourceRepositoryException {
        Requisition req = getRequisition(foreignSource);
        return (req == null ? null : req.getNodeRequistion(foreignId));
    }
    
    @Override
    public void validate(final ForeignSource foreignSource) throws ForeignSourceRepositoryException {
    	/*
    	final String name = foreignSource.getName();
		if (name.contains(":")) {
    		throw new ForeignSourceRepositoryException("Foreign Source (" + name + ") cannot contain a colon!");
    	}
    	*/
    }
    
    @Override
    public void validate(final Requisition requisition) throws ForeignSourceRepositoryException {
    	/*
    	final String foreignSource = requisition.getForeignSource();
		if (foreignSource.contains(":")) {
    		throw new ForeignSourceRepositoryException("Foreign Source (" + foreignSource + ") cannot contain a colon!");
    	}
    	for (final RequisitionNode node : requisition.getNodes()) {
    		final String foreignId = node.getForeignId();
			if (foreignId.contains(":")) {
        		throw new ForeignSourceRepositoryException("Foreign ID (" + foreignId + ") for node " + node.getNodeLabel() + " in Foreign Source " + foreignSource + " cannot contain a colon!");
    		}
    	}
    	*/
    }
}
