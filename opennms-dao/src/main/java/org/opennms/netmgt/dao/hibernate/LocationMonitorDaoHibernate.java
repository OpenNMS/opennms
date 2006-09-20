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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.monitoringLocations.LocationDef;
import org.opennms.netmgt.config.monitoringLocations.MonitoringLocationsConfiguration;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.springframework.core.io.Resource;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class LocationMonitorDaoHibernate extends AbstractDaoHibernate<OnmsLocationMonitor, Integer> implements
        LocationMonitorDao {
    
    MonitoringLocationsConfiguration m_monitoringLocationsConfiguration;
    Resource m_moniotoringLocationConfiguration;

    /**
     * Constructor that also initializes the required XML configurations
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    public LocationMonitorDaoHibernate() throws IOException, MarshalException, ValidationException {
        super(OnmsLocationMonitor.class);
        initializeConfigurations();
    }
    
    @SuppressWarnings("unchecked")
    public Collection<OnmsMonitoringLocationDefinition> findAllMonitoringLocationDefinitions() {
        return m_monitoringLocationsConfiguration.getLocations().getLocationDefCollection();
    }
    
    @Override
    public Collection<OnmsLocationMonitor> find(String query) {
        final Collection<OnmsLocationMonitor> monitors = super.find(query);
        return addLocationDefinitions(monitors);
    }

    @Override
    public Collection<OnmsLocationMonitor> findAll() {
        final Collection<OnmsLocationMonitor> monitors = super.findAll();
        return addLocationDefinitions(monitors);
    }

    @Override
    public OnmsLocationMonitor get(Integer id) {
        final OnmsLocationMonitor monitor = super.get(id);
        return addLocationDefinition(monitor);
    }

    @Override
    public OnmsLocationMonitor load(Integer id) {
        final OnmsLocationMonitor monitor = super.load(id);
        return addLocationDefinition(monitor);
    }

    /**
     * Location definitions are configured via XML, this method sets converts
     * XML configured defnitions and sets them for each location monitor in passed collection.
     * @param monitors
     * @return
     */
    private Collection<OnmsLocationMonitor> addLocationDefinitions(Collection<OnmsLocationMonitor> monitors) {
        if (monitors != null) {
            for (OnmsLocationMonitor monitor : monitors) {
                addLocationDefinition(monitor);
            }
        }
        return monitors;
    }
    
    /**
     * Sets the OnmsLocationMontiorDefinition for the passed in location monitor based on
     * the matching XML configured definition (by name)
     * @param monitor
     */
    private OnmsLocationMonitor addLocationDefinition(OnmsLocationMonitor monitor) {
        monitor.setLocationDefinition(createEntityDef(getLocationDef(monitor.getDefinitionName())));
        return monitor;
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
    private void initializeConfigurations() throws MarshalException, ValidationException, IOException {
        initializeMonitoringLocationDefinitions();
    }

    /**
     * Initializes the monitoring  locations configuration file
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    private void initializeMonitoringLocationDefinitions() throws IOException, MarshalException, ValidationException {
        final InputStream stream = m_moniotoringLocationConfiguration.getInputStream();
        Reader rdr = new InputStreamReader(stream);
        m_monitoringLocationsConfiguration = (MonitoringLocationsConfiguration) 
                Unmarshaller.unmarshal(MonitoringLocationsConfiguration.class, rdr);
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
        eDef.setName(def.getMonitoringArea());
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


}
