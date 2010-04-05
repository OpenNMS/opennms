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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.config.monitoringLocations.LocationDef;
import org.opennms.netmgt.config.monitoringLocations.MonitoringLocationsConfiguration;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
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
    
    private MonitoringLocationsConfiguration m_monitoringLocationsConfiguration;
    private Resource m_monitoringLocationConfigResource;

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
    public List<OnmsMonitoringLocationDefinition> findAllMonitoringLocationDefinitions() {
        assertPropertiesSet();
        
        List<OnmsMonitoringLocationDefinition> onmsDefs = new ArrayList();
        final List<LocationDef> locationDefCollection = m_monitoringLocationsConfiguration.getLocations().getLocationDefCollection();
        if (locationDefCollection != null) {
            onmsDefs = convertDefs(locationDefCollection);
        }
        return onmsDefs;
    }
    
    private List<OnmsMonitoringLocationDefinition> convertDefs(List<LocationDef> defs) {
        List<OnmsMonitoringLocationDefinition> onmsDefs = new LinkedList<OnmsMonitoringLocationDefinition>();
        for (LocationDef def : defs) {
            OnmsMonitoringLocationDefinition onmsDef = new OnmsMonitoringLocationDefinition();
            onmsDef.setArea(def.getMonitoringArea());
            onmsDef.setName(def.getLocationName());
            onmsDef.setPollingPackageName(def.getPollingPackageName());
            onmsDef.setGeolocation(def.getGeolocation());
            onmsDef.setCoordinates(def.getCoordinates());
            onmsDefs.add(onmsDef);
        }
        return onmsDefs;
    }
    
    /**
     * Don't call this for now.
     */
    public void saveMonitoringLocationDefinitions(Collection<OnmsMonitoringLocationDefinition> onmsDefs) {
        Collection<LocationDef> defs = m_monitoringLocationsConfiguration.getLocations().getLocationDefCollection();
        for (OnmsMonitoringLocationDefinition onmsDef : onmsDefs) {
            for (LocationDef def : defs) {
                if (def.getLocationName().equals(onmsDef.getName())) {
                    def.setMonitoringArea(onmsDef.getArea());
                    def.setPollingPackageName(onmsDef.getArea());
                    def.setGeolocation(onmsDef.getGeolocation());
                    def.setCoordinates(onmsDef.getCoordinates());
                }
            }
        }
        saveMonitoringConfig();
    }

    public void saveMonitoringLocationDefinition(OnmsMonitoringLocationDefinition onmsDef) {
        Collection<LocationDef> defs = m_monitoringLocationsConfiguration.getLocations().getLocationDefCollection();
        for (LocationDef def : defs) {
            if (onmsDef.getName().equals(def.getLocationName())) {
                def.setMonitoringArea(onmsDef.getArea());
                def.setPollingPackageName(onmsDef.getPollingPackageName());
                def.setGeolocation(onmsDef.getGeolocation());
                def.setCoordinates(onmsDef.getCoordinates());
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
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_monitoringLocationConfigResource.getFile()), "UTF-8");
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
        m_monitoringLocationsConfiguration = CastorUtils.unmarshalWithTranslatedExceptions(MonitoringLocationsConfiguration.class, m_monitoringLocationConfigResource);
    }
    
    public Collection<OnmsMonitoringLocationDefinition> findAllLocationDefinitions() {
        assertPropertiesSet();
        List<OnmsMonitoringLocationDefinition> eDefs = new LinkedList<OnmsMonitoringLocationDefinition>();
        Collection<LocationDef> defs = m_monitoringLocationsConfiguration.getLocations().getLocationDefCollection();
        for (LocationDef def : defs) {
            eDefs.add(createEntityDef(def));
        }
        return eDefs;
    }

    private void assertPropertiesSet() {
        if (m_monitoringLocationConfigResource == null
                && m_monitoringLocationsConfiguration == null) {
            throw new IllegalStateException("either "
                                            + "monitoringLocationConfigResource "
                                            + "or monitorLocationsConfiguration "
                                            + "must be set but is not");
        }
        
    }

    private OnmsMonitoringLocationDefinition createEntityDef(LocationDef def) {
        OnmsMonitoringLocationDefinition eDef = new OnmsMonitoringLocationDefinition();
        eDef.setArea(def.getMonitoringArea());
        eDef.setName(def.getLocationName());
        eDef.setPollingPackageName(def.getPollingPackageName());
        eDef.setGeolocation(def.getGeolocation());
        eDef.setCoordinates(def.getCoordinates());
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
        if (monitoringLocationDefinitionName == null) {
            throw new IllegalArgumentException("monitoringLocationDefinitionName must not be null");
        }
        assertPropertiesSet();
        LocationDef locationDef = getLocationDef(monitoringLocationDefinitionName);
        if (locationDef == null) {
            return null;
        }
        return createEntityDef(locationDef);
    }

    public OnmsLocationSpecificStatus getMostRecentStatusChange(final OnmsLocationMonitor locationMonitor, final OnmsMonitoredService monSvc) {
        HibernateCallback<OnmsLocationSpecificStatus> callback = new HibernateCallback<OnmsLocationSpecificStatus>() {

            public OnmsLocationSpecificStatus doInHibernate(Session session)
                    throws HibernateException, SQLException {
                return (OnmsLocationSpecificStatus) session.createQuery("from OnmsLocationSpecificStatus status where status.locationMonitor = :locationMonitor and status.monitoredService = :monitoredService order by status.pollResult.timestamp desc")
                    .setEntity("locationMonitor", locationMonitor)
                    .setEntity("monitoredService", monSvc)
                    .setMaxResults(1)
                    .uniqueResult();
            }

        };
        return getHibernateTemplate().execute(callback);
    }

    public void saveStatusChange(OnmsLocationSpecificStatus statusChange) {
        getHibernateTemplate().save(statusChange);
    }

    public Collection<OnmsLocationMonitor> findByLocationDefinition(OnmsMonitoringLocationDefinition locationDefinition) {
    	return (Collection<OnmsLocationMonitor>)find("from OnmsLocationMonitor as mon where mon.definitionName = ?", locationDefinition.getName());
    }

    public Collection<OnmsLocationSpecificStatus> getAllMostRecentStatusChanges() {
    	return getAllStatusChangesAt(new Date());
    }
    
    public Collection<OnmsLocationSpecificStatus> getAllStatusChangesAt(Date timestamp) {
    	return findObjects(OnmsLocationSpecificStatus.class,
    			"from OnmsLocationSpecificStatus as status " +
    			"where status.pollResult.timestamp = ( " +
    			"    select max(recentStatus.pollResult.timestamp) " +
    			"    from OnmsLocationSpecificStatus as recentStatus " +
    			"    where recentStatus.pollResult.timestamp < ? " +
    			"    group by recentStatus.locationMonitor, recentStatus.monitoredService " +
    			"    having recentStatus.locationMonitor = status.locationMonitor " +
    			"    and recentStatus.monitoredService = status.monitoredService " +
    			")",
    			timestamp); 
    }
    
    public Collection<OnmsLocationSpecificStatus> getStatusChangesBetween(Date startDate, Date endDate) {
    	return findObjects(OnmsLocationSpecificStatus.class,
    			"from OnmsLocationSpecificStatus as status " +
    			"where ? <= status.pollResult.timestamp and status.pollResult.timestamp < ?",
    			startDate, endDate
    			);
    }

    @SuppressWarnings("unchecked")
    public Collection<LocationMonitorIpInterface> findStatusChangesForNodeForUniqueMonitorAndInterface(int nodeId) {
        List l = getHibernateTemplate().find(
                        "select distinct status.locationMonitor, status.monitoredService.ipInterface from OnmsLocationSpecificStatus as status " +
                        "where status.monitoredService.ipInterface.node.id = ?",
                        nodeId
                        );
        
        HashSet<LocationMonitorIpInterface> ret = new HashSet<LocationMonitorIpInterface>();
        for (Object o : l) {
            OnmsLocationMonitor mon = (OnmsLocationMonitor) ((Object[]) o)[0];
            OnmsIpInterface ip = (OnmsIpInterface) ((Object[]) o)[1];
            ret.add(new LocationMonitorIpInterface(mon, ip));
        }
        
        return ret;
    }
    
}
