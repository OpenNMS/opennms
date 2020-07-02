/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.LocationSpecificStatusDao;
import org.opennms.netmgt.model.LocationIpInterface;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;

public class LocationSpecificStatusDaoHibernate extends AbstractDaoHibernate<OnmsLocationSpecificStatus, Integer> implements LocationSpecificStatusDao {
    private static final Logger LOG = LoggerFactory.getLogger(LocationSpecificStatusDaoHibernate.class);

    public LocationSpecificStatusDaoHibernate() {
        super(OnmsLocationSpecificStatus.class);
    }

    @Override
    protected void initDao() throws Exception {
        super.initDao();
    }

    @Override
    public Collection<OnmsMonitoringLocation> findByApplication(final OnmsApplication application) {
        return findObjects(OnmsMonitoringLocation.class, "select distinct l from OnmsLocationSpecificStatus as status " +
                "join status.monitoredService as m " +
                "join m.applications a " +
                "join status.location as l " +
                "where a = ? and status.id in ( " +
                "select max(s.id) from OnmsLocationSpecificStatus as s " +
                "group by s.location, s.monitoredService " +
                ")", application);
    }

    @Override
    public void saveStatusChange(final OnmsLocationSpecificStatus locationSpecificStatus) {
        getHibernateTemplate().save(locationSpecificStatus);
    }

    @Override
    public OnmsLocationSpecificStatus getMostRecentStatusChange(final OnmsMonitoringLocation location, final OnmsMonitoredService monSvc) {
        final HibernateCallback<OnmsLocationSpecificStatus> callback = new HibernateCallback<OnmsLocationSpecificStatus>() {

            @Override
            public OnmsLocationSpecificStatus doInHibernate(final Session session) throws HibernateException, SQLException {
                return (OnmsLocationSpecificStatus)session.createQuery("from OnmsLocationSpecificStatus status where status.location = :location and status.monitoredService = :monitoredService order by status.pollResult.timestamp desc")
                        .setEntity("location", location)
                        .setEntity("monitoredService", monSvc)
                        .setMaxResults(1)
                        .uniqueResult();
            }

        };
        return getHibernateTemplate().execute(callback);
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getAllMostRecentStatusChanges() {
        return getAllStatusChangesAt(new Date());
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getAllStatusChangesAt(final Date timestamp) {
        return findObjects(OnmsLocationSpecificStatus.class,
                "from OnmsLocationSpecificStatus as status " +
                        "left join fetch status.location as l " +
                        "left join fetch status.monitoredService as m " +
                        "left join fetch m.serviceType " +
                        "left join fetch m.ipInterface " +
                        "where status.id in (" +
                        "select max(s.id) from OnmsLocationSpecificStatus as s " +
                        "where s.pollResult.timestamp <? " +
                        "group by s.location, s.monitoredService " +
                        ")",
                timestamp);
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesBetween(final Date startDate, final Date endDate) {
        return findObjects(OnmsLocationSpecificStatus.class,
                "from OnmsLocationSpecificStatus as status " +
                        "where ? <= status.pollResult.timestamp and status.pollResult.timestamp < ?",
                startDate, endDate
        );
    }

    private Collection<OnmsLocationSpecificStatus> getMostRecentStatusChangesForDateAndLocation(final Date date, final String locationName) {
        return findObjects(OnmsLocationSpecificStatus.class,
                "from OnmsLocationSpecificStatus as status " +
                        "left join fetch status.location as l " +
                        "left join fetch status.monitoredService as m " +
                        "left join fetch m.serviceType " +
                        "left join fetch m.ipInterface " +
                        "where status.pollResult.timestamp = ( " +
                        "    select max(recentStatus.pollResult.timestamp) " +
                        "    from OnmsLocationSpecificStatus as recentStatus " +
                        "    where recentStatus.pollResult.timestamp < ? " +
                        "    group by recentStatus.location, recentStatus.monitoredService " +
                        "    having recentStatus.location = status.location " +
                        "    and recentStatus.monitoredService = status.monitoredService " +
                        ") and l.locationName = ?",
                date, locationName);
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesForLocationBetween(final Date startDate, final Date endDate, final String locationName) {
        final Collection<OnmsLocationSpecificStatus> statuses = getMostRecentStatusChangesForDateAndLocation(startDate, locationName);
        statuses.addAll(findObjects(OnmsLocationSpecificStatus.class,
                "from OnmsLocationSpecificStatus as status " +
                        "where ? <= status.pollResult.timestamp " +
                        "and status.pollResult.timestamp < ? " +
                        "and status.location.locationName = ?",
                startDate, endDate, locationName
        ));
        return statuses;    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesForApplicationNameBetween(final Date startDate, final Date endDate, final String applicationName) {
        return findObjects(OnmsLocationSpecificStatus.class,
                "from OnmsLocationSpecificStatus as status " +
                        "left join fetch status.monitoredService as m " +
                        "left join fetch m.applications as a " +
                        "left join fetch status.location as l " +
                        "where " +
                        "a.name = ? " +
                        "and " +
                        "( status.pollResult.timestamp between ? and ?" +
                        "  or" +
                        "  status.id in " +
                        "   (" +
                        "       select max(s.id) from OnmsLocationSpecificStatus as s " +
                        "       where s.pollResult.timestamp < ? " +
                        "       group by s.location, s.monitoredService " +
                        "   )" +
                        ")",
                applicationName, startDate, endDate, startDate);
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesForApplicationIdBetween(final Date startDate, final Date endDate, final Integer applicationId) {
        return findObjects(OnmsLocationSpecificStatus.class,
                "from OnmsLocationSpecificStatus as status " +
                        "left join fetch status.monitoredService as m " +
                        "left join fetch m.applications as a " +
                        "left join fetch status.location as l " +
                        "where " +
                        "a.id = ? " +
                        "and " +
                        "( status.pollResult.timestamp between ? and ?" +
                        "  or" +
                        "  status.id in " +
                        "   (" +
                        "       select max(s.id) from OnmsLocationSpecificStatus as s " +
                        "       where s.pollResult.timestamp < ? " +
                        "       group by s.location, s.monitoredService " +
                        "   )" +
                        ")",
                applicationId, startDate, endDate, startDate);
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
                                "left join fetch status.location as l " +
                                "where " +
                                "a.name in (:applicationNames) " +
                                "and " +
                                "( status.pollResult.timestamp between :startDate and :endDate" +
                                "  or" +
                                "  status.id in " +
                                "   (" +
                                "       select max(s.id) from OnmsLocationSpecificStatus as s " +
                                "       where s.pollResult.timestamp < :startDate " +
                                "       group by s.location, s.monitoredService " +
                                "   )" +
                                ") order by status.pollResult.timestamp")
                        .setParameterList("applicationNames", applicationNames)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .list();
            }
        });
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getMostRecentStatusChangesForLocation(String locationName) {
        return getMostRecentStatusChangesForDateAndLocation(new Date(), locationName);
    }

    @Override
    public Collection<LocationIpInterface> findStatusChangesForNodeForUniqueMonitorAndInterface(final int nodeId) {
        @SuppressWarnings("unchecked")
        final List<Object[]> list = (List<Object[]>) getHibernateTemplate().find(
                "select distinct status.location, status.monitoredService.ipInterface from OnmsLocationSpecificStatus as status " +
                        "where status.monitoredService.ipInterface.node.id = ?", nodeId);

        final HashSet<LocationIpInterface> resultSet = new HashSet<>();
        for (final Object[] tuple : list) {
            final OnmsMonitoringLocation loc = (OnmsMonitoringLocation) tuple[0];
            final OnmsIpInterface ip = (OnmsIpInterface) tuple[1];
            resultSet.add(new LocationIpInterface(loc, ip));
        }

        return resultSet;
    }
}
