/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: November 7, 2006
 *
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
package org.opennms.web.svclayer.dao.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.opennms.netmgt.dao.castor.CastorUtils;
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
    
    private File m_importFileDir;

    /**
     * <p>setImportFileDirectory</p>
     *
     * @param importFileDir a {@link java.io.File} object.
     */
    public void setImportFileDirectory(File importFileDir) {
        m_importFileDir = importFileDir;
        if (!m_importFileDir.exists()) {
            if (!m_importFileDir.mkdirs()) {
                throw new NonTransientDataAccessResourceException("import file directory (" + m_importFileDir.getPath() + ") does not exist");
            }
        }
    }

    /** {@inheritDoc} */
    public Requisition get(String name) {
        checkGroupName(name);
        
        File importFile = getImportFile(name);
        
        if (!importFile.exists()) {
            return null;
        }
        
        if (!importFile.canRead()) {
            throw new PermissionDeniedDataAccessException("Unable to read file "+importFile, null);
        }
        
        return CastorUtils.unmarshalWithTranslatedExceptions(Requisition.class, new FileSystemResource(importFile));
    }

    private void checkGroupName(String name) {
        Assert.hasLength(name, "Group name must not be null or the empty string");
    }

    /**
     * <p>getProvisioningGroupNames</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<String> getProvisioningGroupNames() {
        
        String[] importFiles = m_importFileDir.list(getImportFilenameFilter());
        
        String[] groupNames = new String[importFiles.length];
        for (int i = 0; i < importFiles.length; i++) {
            groupNames[i] = getGroupNameForImportFileName(importFiles[i]);
        }
        
        return Arrays.asList(groupNames);
    }

    /** {@inheritDoc} */
    public void save(String groupName, Requisition group) {
        checkGroupName(groupName);
        
        File importFile = getImportFile(groupName);
        
        if (importFile.exists()) {
            Requisition currentData = get(groupName);
            if (currentData.getDateStamp().compare(group.getDateStamp()) > 0) {
                throw new OptimisticLockingFailureException("Data in file "+importFile+" is newer than data to be saved!");
            }
        }
        
        
        Writer w = null;
        try {
            // write to a string to check constraints
            StringWriter strWriter = new StringWriter();
            group.updateDateStamp();
            Marshaller.marshal(group, strWriter);
            
            // if we successfully get here then the file is correct
            w = new OutputStreamWriter(new FileOutputStream(importFile), "UTF-8");
            w.write(strWriter.toString());
            
        } catch (IOException e) {
            throw new PermissionDeniedDataAccessException("Unable to write file "+importFile, e);
        } catch (MarshalException e) {
            throw new CastorDataAccessFailureException("Unable to marshall import data to file "+importFile, e);
        } catch (ValidationException e) {
            throw new CastorDataAccessFailureException("Invalid data for group "+groupName, e);
        } finally {
            if (w != null) {
                IOUtils.closeQuietly(w);
            }
        }
        

    }

    private File getImportFile(String groupName) {
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

            public boolean accept(File dir, String name) {
                return name.matches(".*\\.xml");
            }
            
        };
    }
    
    private String getGroupNameForImportFileName(String filename) {
        Matcher matcher = Pattern.compile("^(.*)\\.xml$").matcher(filename);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid import gorup file name "+filename+", doesn't match form *.xml");
        }
        
        return matcher.group(1);
        
    }



    /** {@inheritDoc} */
    public String getUrlForGroup(String groupName) {
        checkGroupName(groupName);
        File groupFile = getImportFile(groupName);
        try {
            return groupFile.toURL().toString();
        } catch (MalformedURLException e) {
            // can this really happen?
            throw new IllegalArgumentException("Unable to find URL for group "+groupName, e);
        }
    }

    /** {@inheritDoc} */
    public void delete(String groupName) {
        File groupFile = getImportFile(groupName);
        groupFile.delete();
    }

}
