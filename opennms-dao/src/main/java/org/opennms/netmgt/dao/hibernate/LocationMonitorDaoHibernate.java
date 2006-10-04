//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.dao.hibernate;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.config.monitoringLocations.LocationDef;
import org.opennms.netmgt.config.monitoringLocations.MonitoringLocationsConfiguration;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class LocationMonitorDaoHibernate extends AbstractDaoHibernate<OnmsLocationMonitor, Integer> implements
        LocationMonitorDao {
    
    MonitoringLocationsConfiguration m_monitoringLocationsConfiguration;
    Resource m_monitoringLocationConfigResource;

    /**
     * Constructor that also initializes the required XML configurations
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    public LocationMonitorDaoHibernate() {
        super(OnmsLocationMonitor.class);
        if (m_monitoringLocationConfigResource != null) {
            initializeConfigurations();
        }
    }
    
    @SuppressWarnings("unchecked")
    public Collection<OnmsMonitoringLocationDefinition> findAllMonitoringLocationDefinitions() {
        
        Collection<OnmsMonitoringLocationDefinition> onmsDefs = new ArrayList();
        final Collection<LocationDef> locationDefCollection = m_monitoringLocationsConfiguration.getLocations().getLocationDefCollection();
        if (locationDefCollection != null) {
            onmsDefs = convertDefs(locationDefCollection);
        }
        return onmsDefs;
    }
    
    private Collection<OnmsMonitoringLocationDefinition> convertDefs(Collection<LocationDef> defs) {
        Collection<OnmsMonitoringLocationDefinition> onmsDefs = new LinkedList<OnmsMonitoringLocationDefinition>();
        for (LocationDef def : defs) {
            OnmsMonitoringLocationDefinition onmsDef = new OnmsMonitoringLocationDefinition();
            onmsDef.setArea(def.getMonitoringArea());
            onmsDef.setName(def.getLocationName());
            onmsDef.setPollingPackageName(def.getPollingPackageName());
            onmsDefs.add(onmsDef);
        }
        return onmsDefs;
    }
    
    /**
     * Don't call this for now.
     */
    @SuppressWarnings("unchecked")
    public void saveMonitoringLocationDefinitions(Collection<OnmsMonitoringLocationDefinition> onmsDefs) {
        Collection<LocationDef> defs = m_monitoringLocationsConfiguration.getLocations().getLocationDefCollection();
        for (OnmsMonitoringLocationDefinition onmsDef : onmsDefs) {
            for (LocationDef def : defs) {
                if (def.getLocationName().equals(onmsDef.getName())) {
                    def.setMonitoringArea(onmsDef.getArea());
                    def.setPollingPackageName(onmsDef.getArea());
                }
            }
        }
        saveMonitoringConfig();
    }

    @SuppressWarnings("unchecked")
    public void saveMonitoringLocationDefinition(OnmsMonitoringLocationDefinition onmsDef) {
        Collection<LocationDef> defs = m_monitoringLocationsConfiguration.getLocations().getLocationDefCollection();
        for (LocationDef def : defs) {
            if (onmsDef.getName().equals(def.getLocationName())) {
                def.setMonitoringArea(onmsDef.getArea());
                def.setPollingPackageName(onmsDef.getPollingPackageName());
            }
        }
        saveMonitoringConfig();
    }
    
    //TODO: figure out way to synchronize this
    //TODO: write a castor template for the DAOs to use and do optimistic
    //      locking.
    /**
     * @deprecated
     */
    protected void saveMonitoringConfig() {
        String xml = null;
        StringWriter writer = new StringWriter();
        try {
            Marshaller.marshal(m_monitoringLocationsConfiguration, writer);
            xml = writer.toString();
            saveXml(xml);
        } catch (MarshalException e) {
            throw new CastorDataAccessFailureException("saveMonitoringConfig: couldn't marshal confg: \n"+
                   (xml != null ? xml : ""), e);
        } catch (ValidationException e) {
            throw new CastorDataAccessFailureException("saveMonitoringConfig: couldn't validate confg: \n"+
                    (xml != null ? xml : ""), e);
        } catch (IOException e) {
            throw new CastorDataAccessFailureException("saveMonitoringConfig: couldn't write confg: \n"+
                    (xml != null ? xml : ""), e);
        }
    }
    
    protected void saveXml(String xml) throws IOException {
        if (xml != null) {
            FileWriter fileWriter = new FileWriter(m_monitoringLocationConfigResource.getFile());
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
        }
    }
    
    /**
     * 
     * @param definitionName
     * @return
     */
    @SuppressWarnings("unchecked")
    private LocationDef getLocationDef(final String definitionName) {
        Collection<LocationDef> defs = m_monitoringLocationsConfiguration.getLocations().getLocationDefCollection();
        LocationDef matchingDef = null;
        for (LocationDef def : defs) {
            if (def.getLocationName().equals(definitionName)) {
                matchingDef = def;
            }
        }
        return matchingDef;
    }


    /**
     * Initializes all required XML configuration files
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException
     */
    private void initializeConfigurations() {
        initializeMonitoringLocationDefinition();
    }

    /**
     * Initializes the monitoring  locations configuration file
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    private void initializeMonitoringLocationDefinition() {
        Reader rdr = null;
        try {
            rdr = new InputStreamReader(m_monitoringLocationConfigResource.getInputStream());
            m_monitoringLocationsConfiguration = (MonitoringLocationsConfiguration) 
            Unmarshaller.unmarshal(MonitoringLocationsConfiguration.class, rdr);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MarshalException e) {
            throw new CastorDataAccessFailureException("initializeMonitoringLocationDefinition: " +
                    "Could not marshal configuration", e);
        } catch (ValidationException e) {
            throw new CastorObjectRetrievalFailureException("initializeMonitoringLocationDefinition: " +
                    "Could not marshal configuration", e);
        } finally {
            try {
                if (rdr != null) rdr.close();
            } catch (IOException e) {
                throw new CastorDataAccessFailureException("initializeMonitoringLocatinDefintion: " +
                        "Could not close XML stream", e);
            }
        }
    }
    
    protected class CastorObjectRetrievalFailureException 
    extends org.springframework.orm.ObjectRetrievalFailureException {
        private static final long serialVersionUID = -5906087948002738350L;

        public CastorObjectRetrievalFailureException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }
    
    protected class CastorDataAccessFailureException 
    extends org.springframework.dao.DataAccessResourceFailureException {
        private static final long serialVersionUID = -5546624359373413751L;
        
        public CastorDataAccessFailureException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<OnmsMonitoringLocationDefinition> findAllLocationDefinitions() {
        List<OnmsMonitoringLocationDefinition> eDefs = new LinkedList<OnmsMonitoringLocationDefinition>();
        Collection<LocationDef> defs = m_monitoringLocationsConfiguration.getLocations().getLocationDefCollection();
        for (LocationDef def : defs) {
            eDefs.add(createEntityDef(def));
        }
        return eDefs;
    }

    private OnmsMonitoringLocationDefinition createEntityDef(LocationDef def) {
        OnmsMonitoringLocationDefinition eDef = new OnmsMonitoringLocationDefinition();
        eDef.setArea(def.getMonitoringArea());
        eDef.setName(def.getLocationName());
        eDef.setPollingPackageName(def.getPollingPackageName());
        return eDef;
    }

    public MonitoringLocationsConfiguration getMonitoringLocationsConfiguration() {
        return m_monitoringLocationsConfiguration;
    }

    public void setMonitoringLocationsConfiguration(
            MonitoringLocationsConfiguration monitoringLocationsConfiguration) {
        m_monitoringLocationsConfiguration = monitoringLocationsConfiguration;
    }
    
    public Resource getMonitoringLocationConfigResource() {
        return m_monitoringLocationConfigResource;
    }

    public void setMonitoringLocationConfigResource(Resource monitoringLocationResource) {
        m_monitoringLocationConfigResource = monitoringLocationResource;
        initializeMonitoringLocationDefinition();
    }

    public OnmsMonitoringLocationDefinition findMonitoringLocationDefinition(String monitoringLocationDefinitionName) {
        return createEntityDef(getLocationDef(monitoringLocationDefinitionName));
    }

    public OnmsLocationSpecificStatus getMostRecentStatusChange(final OnmsLocationMonitor locationMonitor, final OnmsMonitoredService monSvc) {
        HibernateCallback callback = new HibernateCallback() {

            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                return session.createQuery("from OnmsLocationSpecificStatus status where status.locationMonitor = :locationMonitor and status.monitoredService = :monitoredService order by status.pollResult.timestamp")
                    .setEntity("locationMonitor", locationMonitor)
                    .setEntity("monitoredService", monSvc)
                    .setMaxResults(1)
                    .uniqueResult();
            }

        };
        return (OnmsLocationSpecificStatus)getHibernateTemplate().execute(callback);
    }

    public void saveStatusChange(OnmsLocationSpecificStatus statusChange) {
        getHibernateTemplate().save(statusChange);
    }

}
