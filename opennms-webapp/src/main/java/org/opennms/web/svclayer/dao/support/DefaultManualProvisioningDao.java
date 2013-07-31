/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.dao.support;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.web.svclayer.dao.ManualProvisioningDao;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.util.Assert;

/**
 * <p>DefaultManualProvisioningDao class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultManualProvisioningDao implements ManualProvisioningDao {
    
    private static final Pattern XML_FILE_PATTERN = Pattern.compile("^(.*)\\.xml$");
    private File m_importFileDir;

    /**
     * <p>setImportFileDirectory</p>
     *
     * @param importFileDir a {@link java.io.File} object.
     */
    public void setImportFileDirectory(final File importFileDir) {
        m_importFileDir = importFileDir;
        if (!m_importFileDir.exists()) {
            if (!m_importFileDir.mkdirs()) {
                throw new NonTransientDataAccessResourceException("import file directory (" + m_importFileDir.getPath() + ") does not exist");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Requisition get(final String name) {
        checkGroupName(name);
        
        final File importFile = getImportFile(name);
        
        if (!importFile.exists()) {
            return null;
        }
        
        if (!importFile.canRead()) {
            throw new PermissionDeniedDataAccessException("Unable to read file "+importFile, null);
        }
        
        return CastorUtils.unmarshalWithTranslatedExceptions(Requisition.class, new FileSystemResource(importFile));
    }

    private void checkGroupName(final String name) {
        Assert.hasLength(name, "Group name must not be null or the empty string");
    }

    /**
     * <p>getProvisioningGroupNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<String> getProvisioningGroupNames() {
        
        final String[] importFiles = m_importFileDir.list(getImportFilenameFilter());
        
        final String[] groupNames = new String[importFiles.length];
        for (int i = 0; i < importFiles.length; i++) {
            groupNames[i] = getGroupNameForImportFileName(importFiles[i]);
        }
        
        return Arrays.asList(groupNames);
    }

    /** {@inheritDoc} */
    @Override
    public void save(final String groupName, final Requisition group) {
        checkGroupName(groupName);
        
        final File importFile = getImportFile(groupName);
        
        if (importFile.exists()) {
            final Requisition currentData = get(groupName);
            if (currentData.getDateStamp().compare(group.getDateStamp()) > 0) {
                throw new OptimisticLockingFailureException("Data in file "+importFile+" is newer than data to be saved!");
            }
        }

        final FileWriter writer;
        try {
            writer = new FileWriter(importFile);
        } catch (final IOException e) {
            throw new PermissionDeniedDataAccessException("Unable to write file "+importFile, e);
        }
        CastorUtils.marshalWithTranslatedExceptions(group, writer);
    }

    private File getImportFile(final String groupName) {
        checkGroupName(groupName);
        return new File(m_importFileDir, groupName+".xml");
    }
    
    /**
     * <p>getImportFilenameFilter</p>
     *
     * @return a {@link java.io.FilenameFilter} object.
     */
    public FilenameFilter getImportFilenameFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                final Matcher matcher = XML_FILE_PATTERN.matcher(name);
                return matcher.matches();
            }
            
        };
    }
    
    private String getGroupNameForImportFileName(final String filename) {
        final Matcher matcher = XML_FILE_PATTERN.matcher(filename);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid import gorup file name "+filename+", doesn't match form *.xml");
        }
        return matcher.group(1);
    }



    /** {@inheritDoc} */
    @Override
    public String getUrlForGroup(final String groupName) {
        checkGroupName(groupName);
        return getImportFile(groupName).toURI().toString();
    }

    /** {@inheritDoc} */
    @Override
    public void delete(final String groupName) {
        getImportFile(groupName).delete();
    }

}
