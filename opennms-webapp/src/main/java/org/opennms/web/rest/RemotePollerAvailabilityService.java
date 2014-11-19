/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationAvailDataPoint;
import org.opennms.netmgt.model.OnmsLocationAvailDefinition;
import org.opennms.netmgt.model.OnmsLocationAvailDefinitionList;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinitionList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.rest.support.TimeChunker;
import org.opennms.web.rest.support.TimeChunker.TimeChunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Path("remotelocations")
@Transactional
public class RemotePollerAvailabilityService extends OnmsRestService {


    @Autowired
    LocationMonitorDao m_locationMonitorDao;
    
    @Autowired
    ApplicationDao m_applicationDao;
    
    @Autowired
    MonitoredServiceDao m_monitoredServiceDao;
    
    @Autowired
    NodeDao m_nodeDao;
    
    @Autowired
    TransactionTemplate m_transactionTemplate;
    
    @Context
    UriInfo m_uriInfo;
    
    OnmsLocationAvailDefinitionList m_defList = null;
    
    private Timer m_timer = null;
    
    public RemotePollerAvailabilityService() {
        super();
        
    }
    
    
    protected TimeChunker getTimeChunkerFromMidnight() {
        Calendar calendar = Calendar.getInstance();
        Date startTime = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0,0,0).getTime();
        return new TimeChunker(TimeChunker.MINUTE, startTime, new Date(System.currentTimeMillis()));
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public OnmsMonitoringLocationDefinitionList getRemoteLocationList(){
        readLock();
        try {
            List<OnmsMonitoringLocationDefinition> monitors = m_locationMonitorDao.findAllMonitoringLocationDefinitions();
            return new OnmsMonitoringLocationDefinitionList(monitors);
        } finally {
            readUnlock();
        }
    }
    
    /**
     * Currently only here for world IPv6 day, returns all nodelabels. 
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("participants")
    public String getParticipants(){
        readLock();
        try {
            List<OnmsNode> nodes = m_nodeDao.findAll();
            StringBuffer retVal = new StringBuffer();
            
            retVal.append("{\"participants\":[");
            for(int i  = 0; i < nodes.size(); i++) {
                OnmsNode node = nodes.get(i);
                if(i == 0) {
                    retVal.append("{\"name\":\"" + node.getLabel() + "\"}");
                }else {
                    retVal.append(",{\"name\":\"" + node.getLabel() + "\"}");
                }
            }
            retVal.append("]}");
        
            return retVal.toString();
        } finally {
            readUnlock();
        }
    }
    
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("availability")
    public OnmsLocationAvailDefinitionList getAvailability() throws InterruptedException {
        readLock();
        
        try {
            if(m_timer == null) {
                MultivaluedMap<String, String> queryParameters = m_uriInfo.getQueryParameters();
                m_defList =  getAvailabilityList(createTimeChunker(queryParameters), getSortedApplications(), null, getSelectedNodes(queryParameters));
                
                TimerTask task = new TimerTask() {
    
                    @Override
                    public void run() {
                        m_defList = m_transactionTemplate.execute(new TransactionCallback<OnmsLocationAvailDefinitionList>() {
    
                            @Override
                            public OnmsLocationAvailDefinitionList doInTransaction(TransactionStatus status) {
                                return getAvailabilityList(getTimeChunkerFromMidnight(), getSortedApplications(), null, null);
                            }
                            
                        });
                    }
                };
                
                m_timer = new Timer("AvailCalculator-Timer");
                m_timer.scheduleAtFixedRate(task, TimeChunker.MINUTE, TimeChunker.MINUTE);
            }
            
            return m_defList;
        } finally {
            readUnlock();
        }
    }
    

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("availability/{location}")
    public OnmsLocationAvailDefinitionList getAvailabilityByLocation(@PathParam("location") String location) {
        readLock();
        try {
            MultivaluedMap<String, String> queryParameters = m_uriInfo.getQueryParameters();
            
            OnmsMonitoringLocationDefinition locationDefinition = m_locationMonitorDao.findMonitoringLocationDefinition(location);
            if (locationDefinition == null) {
                throw new IllegalArgumentException("Cannot find location definition: " + location);
            }
            Collection<OnmsLocationMonitor> monitors = m_locationMonitorDao.findByLocationDefinition(locationDefinition);
            
            OnmsLocationAvailDefinitionList availList = getAvailabilityList(createTimeChunker(queryParameters), getSortedApplications(), monitors, null);
            
            return availList;
        } finally {
            readUnlock();
        }
    }
    
    
    /**
     * 
     * @param timeChunker
     * @param sortedApplications
     * @param selectedMonitors
     * @param selectedNodes
     * @return
     */
    private OnmsLocationAvailDefinitionList getAvailabilityList(TimeChunker timeChunker, List<OnmsApplication> sortedApplications, Collection<OnmsLocationMonitor> selectedMonitors, Collection<OnmsNode> selectedNodes) {
        OnmsLocationAvailDefinitionList availList = new OnmsLocationAvailDefinitionList();
        
        List<String> names = new ArrayList<String>(sortedApplications.size());
        for(OnmsApplication app : sortedApplications) {
            names.add(app.getName());
        }
        
        Collection<OnmsLocationSpecificStatus> statusesPeriod = m_locationMonitorDao.getStatusChangesBetweenForApplications(timeChunker.getStartDate(), timeChunker.getEndDate(), names);
        
        AvailCalculator availCalc = new AvailCalculator(timeChunker);

        removeUnneededMonitors(statusesPeriod, selectedMonitors);
        removeUnneededServices(statusesPeriod, selectedNodes);

        for(OnmsLocationSpecificStatus statusChange : statusesPeriod) {
            availCalc.onStatusChange(statusChange);
        }

        int counter = 0;
        for(int i =0; i < timeChunker.getSegmentCount(); i++) {
            counter++;
            TimeChunk timeChunk = timeChunker.getAt(i);
            
            OnmsLocationAvailDataPoint point = new OnmsLocationAvailDataPoint();
            point.setTime(timeChunk.getEndDate());
            
            
            for(OnmsApplication application : sortedApplications) {
                
                double percentage = availCalc.getAvailabilityFor(m_monitoredServiceDao.findByApplication(application), i);
                String strPercent = new DecimalFormat("0.0").format(percentage * 100);
                point.addAvailDefinition(new OnmsLocationAvailDefinition(application.getName(), strPercent));
                
            }
            
            availList.add(point);
        }
        System.err.println(new Date() + "After Calculations total loops: " + counter);
        
        return availList;
    }
    
    

    private void removeUnneededServices(Collection<OnmsLocationSpecificStatus> statusesPeriod, Collection<OnmsNode> selectedNodes) {
        if(selectedNodes != null) {
            Collection<OnmsLocationSpecificStatus> unneededStatuses = new ArrayList<OnmsLocationSpecificStatus>();
            
            for(OnmsLocationSpecificStatus status : statusesPeriod) {
                
                for(OnmsNode node : selectedNodes) {
                    if(status.getMonitoredService().getNodeId() == node.getId()) {
                        unneededStatuses.add(status);
                    }
                }
            }
            
            statusesPeriod.removeAll(unneededStatuses);
        }
    }

    private void removeUnneededMonitors(Collection<OnmsLocationSpecificStatus> statusesPeriod, Collection<OnmsLocationMonitor> selectedMonitors) {
        if(selectedMonitors != null) {
            Collection<OnmsLocationSpecificStatus> unneededStatuses = new ArrayList<OnmsLocationSpecificStatus>();
            
            for(OnmsLocationSpecificStatus status : statusesPeriod) {
                
                for(OnmsLocationMonitor monitor : selectedMonitors) {
                    if(status.getLocationMonitor().getId() == monitor.getId()) {
                        unneededStatuses.add(status);
                    }
                }
            }
            
            statusesPeriod.removeAll(unneededStatuses);
        }
    }

    private int getResolution(MultivaluedMap<String, String> params) {
        if(params.containsKey("resolution")) {
            String resolution = params.getFirst("resolution");
            if(resolution.equalsIgnoreCase("minute")) {
                return TimeChunker.MINUTE;
            } else if(resolution.equalsIgnoreCase("hourly")) {
                return TimeChunker.HOURLY;
            }else if(resolution.equalsIgnoreCase("daily")){
                return TimeChunker.DAILY;
            }else {
                return TimeChunker.MINUTE;
            }
            
        } else {
            return TimeChunker.MINUTE;
        }
        
    }

    private Date getEndTime(MultivaluedMap<String, String> params) {
        if(params.containsKey("endTime")) {
            String value = params.getFirst("endTime");
            return new Date(Long.valueOf(value));
        } else {
            // If no end time is specified, then use the current time
            return new Date();
        }
    }
    
    private Date getStartTime(MultivaluedMap<String, String> params) {
        if(params.containsKey("startTime")) {
            String startTime = params.getFirst("startTime");
            return new Date(Long.valueOf(startTime));
        } else {
            // If no start time is specified, then use the previous midnight as the start time

            // Current time
            Calendar calendar = Calendar.getInstance();

            // Previous midnight
            Calendar previousMidnight = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0);

            // If midnight was less than 5 minutes ago, use midnight of the previous day so that we have
            // enough samples to calculate at the given resolution
            //
            // @see http://issues.opennms.org/browse/NMS-6779
            //
            if (calendar.getTimeInMillis() - previousMidnight.getTimeInMillis() <= TimeChunker.MINUTE) {
                previousMidnight.add(Calendar.DAY_OF_MONTH, -1);
            }
            return previousMidnight.getTime();
        }
    }
    
    private Collection<OnmsNode> getSelectedNodes(MultivaluedMap<String, String> queryParameters) {
        if(queryParameters.containsKey("host")) {
            String nodeLabel = queryParameters.getFirst("host");
            return m_nodeDao.findByLabel(nodeLabel);
        }else {
            return null;
        }
    }
    
    private TimeChunker createTimeChunker(MultivaluedMap<String, String> params) {
        TimeChunker timeChunker;
        Date start = getStartTime(params);
        Date end = getEndTime(params);
        if((end.getTime() - start.getTime()) < TimeChunker.MINUTE) {
            throw new IllegalArgumentException("The endTime has to be after the startTime by 5 minutes.\nCurrently the startTime is " + start + " and endTime is " + end);
        }
        
        timeChunker = new TimeChunker(getResolution(params), start, end);
        return timeChunker;
    }

    private List<OnmsApplication> getSortedApplications() {
        List<OnmsApplication> sortedApplications;
        Collection<OnmsApplication> applications = m_applicationDao.findAll();
        
        if (applications.size() == 0) {
            throw new IllegalArgumentException("there are no applications");
        }
        
        sortedApplications = new ArrayList<OnmsApplication>(applications);
        Collections.sort(sortedApplications);
        return sortedApplications;
    }

}
