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
import java.util.Set;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.config.monitoringLocations.LocationDef;
import org.opennms.netmgt.config.monitoringLocations.Locations;
import org.opennms.netmgt.config.monitoringLocations.MonitoringLocationsConfiguration;
import org.opennms.netmgt.config.tags.Tag;
import org.opennms.netmgt.config.tags.Tags;
import org.opennms.netmgt.dao.CastorDataAccessFailureException;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsApplication;
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
    }

    @Override
    protected void initDao() throws Exception {
        assertPropertiesSet();
        initializeConfigurations();
    }



    public List<OnmsMonitoringLocationDefinition> findAllMonitoringLocationDefinitions() {
        final Locations locations = m_monitoringLocationsConfiguration.getLocations();
        if (locations != null) {
            final List<LocationDef> locationDefCollection = locations.getLocationDefCollection();
            if (locationDefCollection != null) {
                return convertDefs(locationDefCollection);
            }
        }
        return new ArrayList<OnmsMonitoringLocationDefinition>();
    }
    
    private List<OnmsMonitoringLocationDefinition> convertDefs(final List<LocationDef> defs) {
    	final List<OnmsMonitoringLocationDefinition> onmsDefs = new LinkedList<OnmsMonitoringLocationDefinition>();
        for (final LocationDef def : defs) {
        	final OnmsMonitoringLocationDefinition onmsDef = new OnmsMonitoringLocationDefinition();
            onmsDef.setArea(def.getMonitoringArea());
            onmsDef.setName(def.getLocationName());
            onmsDef.setPollingPackageName(def.getPollingPackageName());
            onmsDef.setGeolocation(def.getGeolocation());
            onmsDef.setCoordinates(def.getCoordinates());
            onmsDef.setPriority(def.getPriority());
            if (def.getTags() != null) {
            	final Set<String> tags = new HashSet<String>();
            	for (final Tag t : def.getTags().getTagCollection()) {
            		tags.add(t.getName());
            	}
            	onmsDef.setTags(tags);
            }
            onmsDefs.add(onmsDef);
        }
        return onmsDefs;
    }
    
    /**
     * Don't call this for now.
     */
    public void saveMonitoringLocationDefinitions(final Collection<OnmsMonitoringLocationDefinition> onmsDefs) {
    	final Locations locations = m_monitoringLocationsConfiguration.getLocations();
    	if (locations != null) {
            final Collection<LocationDef> defs = locations.getLocationDefCollection();
            for (final OnmsMonitoringLocationDefinition onmsDef : onmsDefs) {
                for (final LocationDef def : defs) {
                    if (def.getLocationName().equals(onmsDef.getName())) {
                        def.setMonitoringArea(onmsDef.getArea());
                        def.setPollingPackageName(onmsDef.getPollingPackageName());
                        def.setGeolocation(onmsDef.getGeolocation());
                        def.setCoordinates(onmsDef.getCoordinates());
                        def.setPriority(onmsDef.getPriority());
                        
                        final Tags tags = new Tags();
                        for (final String tag : onmsDef.getTags()) {
                        	final Tag t = new Tag();
                        	t.setName(tag);
                        	tags.addTag(t);
                        }
                        def.setTags(tags);
                    }
                }
            }
    	}
        saveMonitoringConfig();
    }

    public void saveMonitoringLocationDefinition(final OnmsMonitoringLocationDefinition onmsDef) {
    	final Locations locations = m_monitoringLocationsConfiguration.getLocations();
    	if (locations != null) {
            final Collection<LocationDef> defs = locations.getLocationDefCollection();
            for (final LocationDef def : defs) {
                if (onmsDef.getName().equals(def.getLocationName())) {
                    def.setMonitoringArea(onmsDef.getArea());
                    def.setPollingPackageName(onmsDef.getPollingPackageName());
                    def.setGeolocation(onmsDef.getGeolocation());
                    def.setCoordinates(onmsDef.getCoordinates());
                    def.setPriority(onmsDef.getPriority());
                    final Tags tags = new Tags();
                    for (final String tag : onmsDef.getTags()) {
                    	final Tag t = new Tag();
                    	t.setName(tag);
                    	tags.addTag(t);
                    }
                    def.setTags(tags);
                }
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
        final StringWriter writer = new StringWriter();
        try {
            Marshaller.marshal(m_monitoringLocationsConfiguration, writer);
            xml = writer.toString();
            saveXml(xml);
        } catch (final MarshalException e) {
            throw new CastorDataAccessFailureException("saveMonitoringConfig: couldn't marshal confg: \n"+
                   (xml != null ? xml : ""), e);
        } catch (final ValidationException e) {
            throw new CastorDataAccessFailureException("saveMonitoringConfig: couldn't validate confg: \n"+
                    (xml != null ? xml : ""), e);
        } catch (final IOException e) {
            throw new CastorDataAccessFailureException("saveMonitoringConfig: couldn't write confg: \n"+
                    (xml != null ? xml : ""), e);
        }
    }
    
    protected void saveXml(final String xml) throws IOException {
        if (xml != null) {
        	final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_monitoringLocationConfigResource.getFile()), "UTF-8");
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
        final Locations locations = m_monitoringLocationsConfiguration.getLocations();
        if (locations != null) {
            for (final LocationDef def : locations.getLocationDefCollection()) {
                if (def.getLocationName().equals(definitionName)) {
                	return def;
                }
            }
        }
        return null;
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
        final List<OnmsMonitoringLocationDefinition> eDefs = new LinkedList<OnmsMonitoringLocationDefinition>();
        for (final LocationDef def : m_monitoringLocationsConfiguration.getLocations().getLocationDefCollection()) {
            eDefs.add(createEntityDef(def));
        }
        return eDefs;
    }

    private void assertPropertiesSet() {
        if (m_monitoringLocationConfigResource == null && m_monitoringLocationsConfiguration == null) {
            throw new IllegalStateException("either "
                                            + "monitoringLocationConfigResource "
                                            + "or monitorLocationsConfiguration "
                                            + "must be set but is not");
        }
        
    }

    private OnmsMonitoringLocationDefinition createEntityDef(final LocationDef def) {
    	final OnmsMonitoringLocationDefinition eDef = new OnmsMonitoringLocationDefinition();
        eDef.setArea(def.getMonitoringArea());
        eDef.setName(def.getLocationName());
        eDef.setPollingPackageName(def.getPollingPackageName());
        eDef.setGeolocation(def.getGeolocation());
        eDef.setCoordinates(def.getCoordinates());
        eDef.setPriority(def.getPriority());
        if (def.getTags() != null) {
            final Set<String> tags = new HashSet<String>();
            for (final Tag t : def.getTags().getTagCollection()) {
            	tags.add(t.getName());
            }
            eDef.setTags(tags);
        }
        return eDef;
    }

    public MonitoringLocationsConfiguration getMonitoringLocationsConfiguration() {
        return m_monitoringLocationsConfiguration;
    }

    public void setMonitoringLocationsConfiguration(final MonitoringLocationsConfiguration monitoringLocationsConfiguration) {
        m_monitoringLocationsConfiguration = monitoringLocationsConfiguration;
    }
    
    public Resource getMonitoringLocationConfigResource() {
        return m_monitoringLocationConfigResource;
    }

    public void setMonitoringLocationConfigResource(final Resource monitoringLocationResource) {
        m_monitoringLocationConfigResource = monitoringLocationResource;
        initializeMonitoringLocationDefinition();
    }

    public OnmsMonitoringLocationDefinition findMonitoringLocationDefinition(final String monitoringLocationDefinitionName) {
        if (monitoringLocationDefinitionName == null) {
            throw new IllegalArgumentException("monitoringLocationDefinitionName must not be null");
        }
        final LocationDef locationDef = getLocationDef(monitoringLocationDefinitionName);
        if (locationDef == null) {
            return null;
        }
        return createEntityDef(locationDef);
    }

    public OnmsLocationSpecificStatus getMostRecentStatusChange(final OnmsLocationMonitor locationMonitor, final OnmsMonitoredService monSvc) {
    	final HibernateCallback<OnmsLocationSpecificStatus> callback = new HibernateCallback<OnmsLocationSpecificStatus>() {

            public OnmsLocationSpecificStatus doInHibernate(final Session session) throws HibernateException, SQLException {
                return (OnmsLocationSpecificStatus)session.createQuery("from OnmsLocationSpecificStatus status where status.locationMonitor = :locationMonitor and status.monitoredService = :monitoredService order by status.pollResult.timestamp desc")
                    .setEntity("locationMonitor", locationMonitor)
                    .setEntity("monitoredService", monSvc)
                    .setMaxResults(1)
                    .uniqueResult();
            }

        };
        return getHibernateTemplate().execute(callback);
    }

    public void saveStatusChange(final OnmsLocationSpecificStatus statusChange) {
        getHibernateTemplate().save(statusChange);
    }

    public Collection<OnmsLocationMonitor> findByApplication(final OnmsApplication application) {
    	final Collection<OnmsLocationMonitor> monitors = new HashSet<OnmsLocationMonitor>();
    	for (final OnmsLocationSpecificStatus status : getAllMostRecentStatusChanges()) {
    		if (status.getMonitoredService().getApplications() != null
    				&& status.getMonitoredService().getApplications().contains(application)) {
    			monitors.add(status.getLocationMonitor());
    		}
    	}
    	return monitors;
    }
    
    public Collection<OnmsLocationMonitor> findByLocationDefinition(final OnmsMonitoringLocationDefinition locationDefinition) {
    	return (Collection<OnmsLocationMonitor>)find("from OnmsLocationMonitor as mon where mon.definitionName = ?", locationDefinition.getName());
    }

    public Collection<OnmsLocationSpecificStatus> getAllMostRecentStatusChanges() {
    	return getAllStatusChangesAt(new Date());
    }
    
    public Collection<OnmsLocationSpecificStatus> getAllStatusChangesAt(final Date timestamp) {
        //select lm.*, lssc.* from location_specific_status_changes lssc join 
        //location_monitors lm on lm.id = lssc.locationmonitorid where lssc.id in 
        //(select max(id) from location_specific_status_changes group by locationmonitorid, ifserviceid) order by statustime;
        return findObjects(OnmsLocationSpecificStatus.class,
                "from OnmsLocationSpecificStatus as status " +
                "left join fetch status.locationMonitor as l " +
                "left join fetch status.monitoredService as m " +
                "left join fetch m.serviceType " +
                "left join fetch m.ipInterface " +
                "where status.id in (" +
                    "select max(s.id) from OnmsLocationSpecificStatus as s " +
                    "where s.pollResult.timestamp <? " +
                    "group by s.locationMonitor, s.monitoredService " +
                    ")",
                timestamp);
        //    	return findObjects(OnmsLocationSpecificStatus.class,
//    			"from OnmsLocationSpecificStatus as status " +
//    			"where status.pollResult.timestamp = ( " +
//    			"    select max(recentStatus.pollResult.timestamp) " +
//    			"    from OnmsLocationSpecificStatus as recentStatus " +
//    			"    where recentStatus.pollResult.timestamp < ? " +
//    			"    group by recentStatus.locationMonitor, recentStatus.monitoredService " +
//    			"    having recentStatus.locationMonitor = status.locationMonitor " +
//    			"    and recentStatus.monitoredService = status.monitoredService " +
//    			")",
//    			timestamp); 
    }
    
    public Collection<OnmsLocationSpecificStatus> getStatusChangesBetween(final Date startDate, final Date endDate) {
    	return findObjects(OnmsLocationSpecificStatus.class,
    			"from OnmsLocationSpecificStatus as status " +
    			"where ? <= status.pollResult.timestamp and status.pollResult.timestamp < ?",
    			startDate, endDate
    			);
    }

    public Collection<OnmsLocationSpecificStatus> getStatusChangesForLocationBetween(final Date startDate, final Date endDate, final String locationName) {
        final Collection<OnmsLocationSpecificStatus> statuses = getMostRecentStatusChangesForDateAndLocation(startDate, locationName);
        statuses.addAll(findObjects(OnmsLocationSpecificStatus.class,
            /*
            "from OnmsLocationSpecificStatus as status " +
            "where " +
            "( " +
            "    select max(recentStatus.pollResult.timestamp) " +
            "    from OnmsLocationSpecificStatus as recentStatus " +
            "    where recentStatus.pollResult.timestamp < ? " +
            "    group by recentStatus.locationMonitor, recentStatus.monitoredService " +
            "    having recentStatus.locationMonitor = status.locationMonitor " +
            "    and recentStatus.monitoredService = status.monitoredService " +
            ") <= status.pollResult.timestamp " +
            "and status.pollResult.timestamp < ?" +
            "and status.locationMonitor.definitionName = ?",
            startDate, endDate, locationName); 
            */
            "from OnmsLocationSpecificStatus as status " +
            "where ? <= status.pollResult.timestamp " +
            "and status.pollResult.timestamp < ? " +
            "and status.locationMonitor.definitionName = ?",
            startDate, endDate, locationName
        ));
        return statuses;
    }

    public Collection<OnmsLocationSpecificStatus> getMostRecentStatusChangesForLocation(final String locationName) {
        return getMostRecentStatusChangesForDateAndLocation(new Date(), locationName);
    }

    private Collection<OnmsLocationSpecificStatus> getMostRecentStatusChangesForDateAndLocation(final Date date, final String locationName) {
        return findObjects(OnmsLocationSpecificStatus.class,
                           "from OnmsLocationSpecificStatus as status " +
                           "left join fetch status.locationMonitor as l " +
                           "left join fetch status.monitoredService as m " +
                           "left join fetch m.serviceType " +
                           "left join fetch m.ipInterface " +
                           "where status.pollResult.timestamp = ( " +
                           "    select max(recentStatus.pollResult.timestamp) " +
                           "    from OnmsLocationSpecificStatus as recentStatus " +
                           "    where recentStatus.pollResult.timestamp < ? " +
                           "    group by recentStatus.locationMonitor, recentStatus.monitoredService " +
                           "    having recentStatus.locationMonitor = status.locationMonitor " +
                           "    and recentStatus.monitoredService = status.monitoredService " +
                           ") and l.definitionName = ?",
                           date, locationName); 
    }

    public Collection<LocationMonitorIpInterface> findStatusChangesForNodeForUniqueMonitorAndInterface(final int nodeId) {
    	@SuppressWarnings("rawtypes")
		final List l = getHibernateTemplate().find(
                        "select distinct status.locationMonitor, status.monitoredService.ipInterface from OnmsLocationSpecificStatus as status " +
                        "where status.monitoredService.ipInterface.node.id = ?",
                        nodeId
                        );
        
    	final HashSet<LocationMonitorIpInterface> ret = new HashSet<LocationMonitorIpInterface>();
        for (final Object o : l) {
            OnmsLocationMonitor mon = (OnmsLocationMonitor) ((Object[]) o)[0];
            OnmsIpInterface ip = (OnmsIpInterface) ((Object[]) o)[1];
            ret.add(new LocationMonitorIpInterface(mon, ip));
        }
        
        return ret;
    }

}
