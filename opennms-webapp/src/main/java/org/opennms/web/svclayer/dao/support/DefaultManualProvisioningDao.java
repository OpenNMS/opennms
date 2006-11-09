package org.opennms.web.svclayer.dao.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.opennms.netmgt.dao.CastorObjectRetrievalFailureException;
import org.opennms.web.svclayer.dao.ManualProvisioningDao;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PermissionDeniedDataAccessException;

public class DefaultManualProvisioningDao implements ManualProvisioningDao {
    
    private File m_importFileDir;

    public void setImportFileDirectory(File importFileDir) {
        m_importFileDir = importFileDir;
    }

    public ModelImport get(String name) {
        if (!"manual".equals(name)) return null;
        
        File importFile = getImportFile(name);
        
        if (!importFile.exists())
            return null;
        
        if (!importFile.canRead())
            throw new PermissionDeniedDataAccessException("Unable to read file "+importFile, null);
        
        Reader r = null;
        try {
            r = new FileReader(importFile);
            return (ModelImport)Unmarshaller.unmarshal(ModelImport.class, r);
        } catch (FileNotFoundException e) {
            throw new CastorDataAccessFailureException("Unable to locate File "+importFile, e);
        } catch (MarshalException e) {
            throw new CastorObjectRetrievalFailureException("Exception occurrred reading data from "+importFile, e);
        } catch (ValidationException e) {
            throw new CastorObjectRetrievalFailureException("Validation error reading data from "+importFile, e);
        } finally {
            if (r != null) IOUtils.closeQuietly(r);
        }
        
        
    }

    public Collection<String> getProvisioningGroupNames() {
        return Collections.singleton("manual");
    }

    public void save(String groupName, ModelImport group) {
        if (!"manual".equals(groupName))
            throw new InvalidDataAccessApiUsageException("groups other than 'manual' aren't currently supported");
        
        File importFile = getImportFile(groupName);
        
        if (importFile.exists()) {
            ModelImport currentData = get(groupName);
            if (currentData.getDateStamp().after(group.getDateStamp())) {
                throw new OptimisticLockingFailureException("Data in file "+importFile+" is newer than data to be saved!");
            }
        }
        
        
        Writer w = null;
        try {
            // write to a string to check constraints
            StringWriter strWriter = new StringWriter();
            group.setDateStamp(new Date());
            Marshaller.marshal(group, strWriter);
            
            // if we successfully get here then the file is correct
            w = new FileWriter(importFile);
            w.write(strWriter.toString());
            
        } catch (IOException e) {
            throw new PermissionDeniedDataAccessException("Unable to write file "+importFile, e);
        } catch (MarshalException e) {
            throw new CastorDataAccessFailureException("Unable to marshall import data to file "+importFile, e);
        } catch (ValidationException e) {
            throw new CastorDataAccessFailureException("Invalid data for group "+groupName, e);
        } finally {
            if (w != null) IOUtils.closeQuietly(w);
        }
        

    }

    private File getImportFile(String groupName) {
        return new File(m_importFileDir, "imports-"+groupName+".xml");
    }

    public String getUrlForGroup(String groupName) {
        File groupFile = getImportFile(groupName);
        try {
            return groupFile.toURL().toString();
        } catch (MalformedURLException e) {
            // can this really happen?
            throw new IllegalArgumentException("Unable to find URL for group "+groupName, e);
        }
    }

}
