/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * <p>LocationMonitorDaoHibernate class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class LocationMonitorDaoHibernate extends AbstractDaoHibernate<OnmsLocationMonitor, String> implements LocationMonitorDao {

    private static final Logger LOG = LoggerFactory.getLogger(LocationMonitorDaoHibernate.class);

    /**
     * Constructor that also initializes the required XML configurations
     *
     * @throws IOException if any.
     */
    public LocationMonitorDaoHibernate() {
        super(OnmsLocationMonitor.class);
    }

    /** {@inheritDoc} */
    @Override
    protected void initDao() throws Exception {
        super.initDao();
    }

    /** {@inheritDoc} */
    @Override
    public OnmsLocationSpecificStatus getMostRecentStatusChange(final OnmsLocationMonitor locationMonitor, final OnmsMonitoredService monSvc) {
    	final HibernateCallback<OnmsLocationSpecificStatus> callback = new HibernateCallback<OnmsLocationSpecificStatus>() {

            @Override
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

    /** {@inheritDoc} */
    @Override
    public void saveStatusChange(final OnmsLocationSpecificStatus statusChange) {
        getHibernateTemplate().save(statusChange);
    }

    /**
     * {@inheritDoc}
     *
     * Returns the location monitors which have reported on services belonging to the provided application
     */
    @Override
    public Collection<OnmsLocationMonitor> findByApplication(final OnmsApplication application) {
        
        return findObjects(OnmsLocationMonitor.class, "select distinct l from OnmsLocationSpecificStatus as status " +
        		"join status.monitoredService as m " +
        		"join m.applications a " +
        		"join status.locationMonitor as l " +
        		"where a = ? and status.id in ( " +
                    "select max(s.id) from OnmsLocationSpecificStatus as s " +
                    "group by s.locationMonitor, s.monitoredService " +
                ")", application);

        
//    	final Collection<OnmsLocationMonitor> monitors = new HashSet<>();
//    	for (final OnmsLocationSpecificStatus status : getAllMostRecentStatusChanges()) {
//    		if (status.getMonitoredService().getApplications() != null
//    				&& status.getMonitoredService().getApplications().contains(application)) {
//    			monitors.add(status.getLocationMonitor());
//    		}
//    	}
//    	return monitors;
    }
    
    /** {@inheritDoc} */
    @Override
    public Collection<OnmsLocationMonitor> findByLocationDefinition(final OnmsMonitoringLocation locationDefinition) {
    	if (locationDefinition == null) {
    		throw new IllegalArgumentException("Location definition is null");
    	}
    	return (Collection<OnmsLocationMonitor>)find("from OnmsLocationMonitor as mon where mon.location = ?", locationDefinition.getLocationName());
    }

    /**
     * <p>getAllMostRecentStatusChanges</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsLocationSpecificStatus> getAllMostRecentStatusChanges() {
    	return getAllStatusChangesAt(new Date());
    }
    
    /** {@inheritDoc} */
    @Override
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
    
    /** {@inheritDoc} */
    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesBetween(final Date startDate, final Date endDate) {
    	return findObjects(OnmsLocationSpecificStatus.class,
    			"from OnmsLocationSpecificStatus as status " +
    			"where ? <= status.pollResult.timestamp and status.pollResult.timestamp < ?",
    			startDate, endDate
    			);
    }

    /** {@inheritDoc} */
    @Override
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
            "and status.locationMonitor.location = ?",
            startDate, endDate, locationName
        ));
        return statuses;
    }
    
    /** {@inheritDoc} */
    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesForApplicationBetween(final Date startDate, final Date endDate, final String applicationName) {

        return findObjects(OnmsLocationSpecificStatus.class, 
                "from OnmsLocationSpecificStatus as status " +
                "left join fetch status.monitoredService as m " +
                "left join fetch m.applications as a " +
                "left join fetch status.locationMonitor as lm " +
                "where " +
                "a.name = ? " +
                "and " +
                "( status.pollResult.timestamp between ? and ?" +
                "  or" +
                "  status.id in " +
                "   (" +
                "       select max(s.id) from OnmsLocationSpecificStatus as s " +
                "       where s.pollResult.timestamp < ? " +
                "       group by s.locationMonitor, s.monitoredService " +
                "   )" +
                ")",
                applicationName, startDate, endDate, startDate);
        
    }
    
    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesBetweenForApplications(final Date startDate, final Date endDate, final Collection<String> applicationNames) {
        return getHibernateTemplate().execute(new HibernateCallback<List<OnmsLocationSpecificStatus>>() {

            @SuppressWarnings("unchecked")
            @Override
            public List<OnmsLocationSpecificStatus> doInHibernate(Session session) throws HibernateException, SQLException {
                
                return (List<OnmsLocationSpecificStatus>)session.createQuery(
                        "select distinct status from OnmsLocationSpecificStatus as status " +
                        "left join fetch status.monitoredService as m " +
                        "left join fetch m.serviceType " +
                        "left join fetch m.applications as a " +
                        "left join fetch status.locationMonitor as lm " +
                        "where " +
                        "a.name in (:applicationNames) " +
                        "and " +
                        "( status.pollResult.timestamp between :startDate and :endDate" +
                        "  or" +
                        "  status.id in " +
                        "   (" +
                        "       select max(s.id) from OnmsLocationSpecificStatus as s " +
                        "       where s.pollResult.timestamp < :startDate " +
                        "       group by s.locationMonitor, s.monitoredService " +
                        "   )" +
                        ") order by status.pollResult.timestamp")
                .setParameterList("applicationNames", applicationNames)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .list();
                

            }

        });
        
    }


    /** {@inheritDoc} */
    @Override
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
                           ") and l.location = ?",
                           date, locationName); 
    }

    /** {@inheritDoc} */
    @Override
    public Collection<LocationMonitorIpInterface> findStatusChangesForNodeForUniqueMonitorAndInterface(final int nodeId) {

		@SuppressWarnings("unchecked")
        final List<Object[]> l = (List<Object[]>)getHibernateTemplate().find(
                        "select distinct status.locationMonitor, status.monitoredService.ipInterface from OnmsLocationSpecificStatus as status " +
                        "where status.monitoredService.ipInterface.node.id = ?",
                        nodeId
                        );
        
    	final HashSet<LocationMonitorIpInterface> ret = new HashSet<>();
        for (Object[] tuple : l) {
            OnmsLocationMonitor mon = (OnmsLocationMonitor) tuple[0];
            OnmsIpInterface ip = (OnmsIpInterface) tuple[1];
            ret.add(new LocationMonitorIpInterface(mon, ip));
        }
        
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public void pauseAll() {
        getHibernateTemplate().bulkUpdate("update OnmsLocationMonitor as mon set mon.status = ? where mon.status != ?", MonitorStatus.PAUSED, MonitorStatus.STOPPED); 
    }

    /** {@inheritDoc} */
    @Override
    public void resumeAll() {
        getHibernateTemplate().bulkUpdate("update OnmsLocationMonitor as mon set mon.status = ? where mon.status = ?", MonitorStatus.STARTED, MonitorStatus.PAUSED); 
    }

}
