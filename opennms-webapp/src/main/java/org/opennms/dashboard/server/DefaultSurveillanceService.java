/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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


package org.opennms.dashboard.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.dashboard.client.Alarm;
import org.opennms.dashboard.client.NodeRtc;
import org.opennms.dashboard.client.Notification;
import org.opennms.dashboard.client.SurveillanceData;
import org.opennms.dashboard.client.SurveillanceGroup;
import org.opennms.dashboard.client.SurveillanceService;
import org.opennms.dashboard.client.SurveillanceSet;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.GraphDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.NotificationDao;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.SurveillanceViewConfigDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.svclayer.ProgressMonitor;
import org.opennms.web.svclayer.RtcService;
import org.opennms.web.svclayer.SimpleWebTable;
import org.opennms.web.svclayer.SimpleWebTable.Cell;
import org.opennms.web.svclayer.support.RtcNodeModel;
import org.opennms.web.svclayer.support.RtcNodeModel.RtcNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * <p>DefaultSurveillanceService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
@Transactional(readOnly = true)
public class DefaultSurveillanceService implements SurveillanceService, InitializingBean {

    private NodeDao m_nodeDao;
    private ResourceDao m_resourceDao;
    private GraphDao m_graphDao;
    private NotificationDao m_notificationDao;
    private org.opennms.web.svclayer.SurveillanceService m_webSurveillanceService;
    private SurveillanceViewConfigDao m_surveillanceViewConfigDao;
    private CategoryDao m_categoryDao;
    private AlarmDao m_alarmDao;
    private RtcService m_rtcService;
    private GroupDao m_groupDao;
    private OutageDao m_outageDao;
    
    /**
     * <p>getSurveillanceData</p>
     *
     * @return a {@link org.opennms.dashboard.client.SurveillanceData} object.
     */
    public SurveillanceData getSurveillanceData() {
        SurveillanceData data = new SurveillanceData();

        SimpleWebTable table = m_webSurveillanceService.createSurveillanceTable(getView().getName(), new ProgressMonitor());
        
        data.setName(getView().getName());
        
        List<SurveillanceGroup> columnGroups = new ArrayList<SurveillanceGroup>();
        for (Cell columnHeader : table.getColumnHeaders().subList(1, table.getColumnHeaders().size())) {
            SurveillanceGroup columnGroup = new SurveillanceGroup();
            columnGroup.setId(columnHeader.getContent().toString());
            columnGroup.setLabel(columnHeader.getContent().toString());
            columnGroup.setColumn(true);
            columnGroups.add(columnGroup);
        }
        data.setColumnGroups(columnGroups.toArray(new SurveillanceGroup[columnGroups.size()]));
        
        List<SurveillanceGroup> rowGroups = new ArrayList<SurveillanceGroup>();
        for (List<Cell> row : table.getRows()) {
            Cell rowHeader = row.get(0);
            
            SurveillanceGroup rowGroup = new SurveillanceGroup();
            rowGroup.setId(rowHeader.getContent().toString());
            rowGroup.setLabel(rowHeader.getContent().toString());
            rowGroups.add(rowGroup);
        }
        data.setRowGroups(rowGroups.toArray(new SurveillanceGroup[rowGroups.size()]));

        int rowIndex = 0;
        for (List<Cell> row : table.getRows()) {
            int columnIndex = 0;
            for (Cell cell : row.subList(1, row.size())) {
                data.setCell(rowIndex, columnIndex, cell.getContent().toString(), cell.getStyleClass());
                
                columnIndex++;
            }
            rowIndex++;
        }
        
        data.setComplete(true);

        return data;
    }
    
    
    /*
    private int m_count = 0;
    private Timer m_timer = new Timer();
    private Random m_random = new Random();
    private SurveillanceData m_data;

    public SurveillanceData getSurveillanceData() {
        if (m_data == null) {
            final SurveillanceData data = new SurveillanceData();
            m_data = data;
            
            SurveillanceGroup[] columnGroups = new SurveillanceGroup[] {
                    new SurveillanceGroup("prod", "Production"), 
                    new SurveillanceGroup("test", "Test"), 
                    new SurveillanceGroup("dev", "Developement")
            };
            
            SurveillanceGroup[] rowGroups = new SurveillanceGroup[] {
                    new SurveillanceGroup("ibm", "IBM"),
                    new SurveillanceGroup("hp", "HP"),
                    new SurveillanceGroup("duke", "Duke Hospital"),
                    new SurveillanceGroup("unc", "UNC Hospitals")
            };
            
            data.setColumnGroups(columnGroups);
            data.setRowGroups(rowGroups);
            
            m_timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    
                    data.setCell(m_count / data.getColumnCount(), m_count % data.getColumnCount(), ""+m_count);
                    
                    m_count++;
                    
                    if (m_count < data.getColumnCount()*data.getRowCount()) {
                        data.setComplete(false);
                    } else {
                        this.cancel();
                        data.setComplete(true);
                        m_count = 0;
                    }

                }
                
            }, 3000, 2000);
        } else if (m_data.isComplete()) {
            SurveillanceData data = m_data;
            m_data = null;
            return data;
        }
        
        return m_data;

        
    }

*/

    /** {@inheritDoc} */
    public Alarm[] getAlarmsForSet(SurveillanceSet set) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class, "alarm");
        OnmsCriteria nodeCriteria = criteria.createCriteria("node");
        addCriteriaForSurveillanceSet(nodeCriteria, set);
        nodeCriteria.add(Restrictions.ne("type", "D"));
        criteria.addOrder(Order.desc("alarm.severity"));
        criteria.setMaxResults(100);
        
        List<OnmsAlarm> alarms = m_alarmDao.findMatching(criteria);

        Alarm[] alarmArray = new Alarm[alarms.size()];
        
        int index = 0;
        boolean isDashboardRole = isDashboardRole();
        for (OnmsAlarm alarm : alarms) {
            alarmArray[index] = new Alarm(alarm.getSeverity().getLabel(), alarm.getNode().getLabel(), alarm.getNode().getId(), isDashboardRole, alarm.getLogMsg(), alarm.getDescription(), alarm.getCounter(), new Date(alarm.getFirstEventTime().getTime()), new Date(alarm.getLastEventTime().getTime()));
            index++;
        }
        
        return alarmArray;
    }

    /** {@inheritDoc} */
    public String[] getNodeNames(SurveillanceSet set) {

        List<OnmsNode> nodes = m_nodeDao.findAll();

        List<String> labels = new ArrayList<String>(nodes.size());
        for (OnmsNode node : nodes) {
            labels.add(node.getLabel());
        }

        return labels.toArray(new String[labels.size()]);

    }

    /** {@inheritDoc} */
    public String[][] getResources(SurveillanceSet set) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsNode.class, "node");
        addCriteriaForSurveillanceSet(criteria, set);
        criteria.add(Restrictions.ne("node.type", "D"));
        criteria.addOrder(Order.asc("node.label"));
        
        List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);
        
        List<OnmsResource> resources = new ArrayList<OnmsResource>();
        for (OnmsNode node : nodes) {
            OnmsResource resource = m_resourceDao.getResourceForNode(node);
            if (resource != null && (resource.getAttributes().size() > 0 || resource.getChildResources().size() > 0)) {
                resources.add(resource);
            }
        }
        
        List<String[]> labels = new ArrayList<String[]>(resources.size());
        for (OnmsResource resource : resources) {
            labels.add(new String[] { resource.getId(), resource.getResourceType().getLabel() + ": " + resource.getLabel() });
        }
        
        return labels.toArray(new String[labels.size()][]);
    }
    

    private void addCriteriaForSurveillanceSet(OnmsCriteria criteria, SurveillanceSet set) {
        CriteriaAddingVisitor visitor = new CriteriaAddingVisitor(criteria);
        visitor.setView(getView());
        visitor.setCategoryDao(m_categoryDao);
        visitor.afterPropertiesSet();

        set.visit(visitor);
    }

    private View getView() {
        String user = getUsername();
        log().debug("Looking for surveillance view that matches user '" + user + "'");
        
        View userView = m_surveillanceViewConfigDao.getView(user);
        if (userView != null) {
            log().debug("Found surveillance view '" + userView.getName() + "' matching user name '" + user + "'");
            return userView;
        }
        
        List<Group> groups = m_groupDao.findGroupsForUser(user);
        for (Group group : groups) {
            View groupView = m_surveillanceViewConfigDao.getView(group.getName());
            if (groupView != null) {
                log().debug("Found surveillance view '" + groupView.getName() + "' matching group '" + group.getName() + "' name for user '" + user + "'");
                return groupView;
            }
        }
        
        View defaultView = m_surveillanceViewConfigDao.getDefaultView();
        if (defaultView == null) {
            String message = "There is no default surveillance view and we could not find a surviellance view for the user's username ('" + user + "') or any of their groups";
            log().warn(message);
            throw new ObjectRetrievalFailureException(View.class, message);
        }
        
        log().debug("Did not find a surveillance view matching the user's user name or one of their group names.  Using the default view for user '" + user + "'");
        return defaultView;
    }
    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }


    /**
     * <p>getUsername</p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getUsername() {
        /*
         * This should never be null, as the strategy should create a
         * SecurityContext if one doesn't exist, but let's check anyway.
         */
        SecurityContext context = SecurityContextHolder.getContext();
        Assert.state(context != null, "No security context found when calling SecurityContextHolder.getContext()");
        
        org.springframework.security.core.Authentication auth = context.getAuthentication();
        Assert.state(auth != null, "No Authentication object found when calling getAuthentication on our SecurityContext object");
        
        Object obj = auth.getPrincipal();
        Assert.state(obj != null, "No principal object found when calling getPrinticpal on our Authentication object");
        
        
        if (obj instanceof UserDetails) { 
            return ((UserDetails)obj).getUsername(); 
        } else { 
            throw new IllegalStateException("principal should always be instanceof UserDetails");
        }
    }

    /**
     * <p>isDashboardRole</p>
     *
     * @return a boolean.
     */
    protected boolean isDashboardRole() {
        boolean isDashboardRole = true;
        SecurityContext context = SecurityContextHolder.getContext();
        if((context != null) && !(context.toString().contains(org.opennms.web.springframework.security.Authentication.ROLE_DASHBOARD))) {
            isDashboardRole = false;
        }
        log().debug("User " + getUsername() + " is in dashboard role? " + isDashboardRole);
        return isDashboardRole;
    }

    /** {@inheritDoc} */
    public String[][] getChildResources(String id) {
        OnmsResource parentResource = m_resourceDao.getResourceById(id);
        if (parentResource == null) {
            return null;
        }
        
        List<OnmsResource> resources = parentResource.getChildResources();
        
        List<String[]> labels = new ArrayList<String[]>(resources.size());
        for (OnmsResource resource : resources) {
            labels.add(new String[] { resource.getId(), resource.getResourceType().getLabel() + ": " + resource.getLabel() });
        }
        
        return labels.toArray(new String[labels.size()][]);
    }

    /** {@inheritDoc} */
    public String[][] getPrefabGraphs(String id) {
        OnmsResource resource = m_resourceDao.getResourceById(id);
        if (resource == null) {
            return null;
        }
        
        PrefabGraph[] graphs = m_graphDao.getPrefabGraphsForResource(resource);
        
        List<String[]> labels = new ArrayList<String[]>(graphs.length);
        for (PrefabGraph graph : graphs) {
            labels.add(new String[] { graph.getName(), graph.getName() });
        }

        return labels.toArray(new String[labels.size()][]);
    }
    
    /** {@inheritDoc} */
    public Notification[] getNotificationsForSet(SurveillanceSet set) {
        List<Notification> notifications = new ArrayList<Notification>();
        
        Date fifteenMinutesAgo = new Date(System.currentTimeMillis() - (15 * 60 * 1000));
        Date oneWeekAgo = new Date(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000));
        
        notifications.addAll(getNotificationsWithCriterion(set, "Critical", Restrictions.isNull("notification.respondTime"), Restrictions.le("notification.pageTime", fifteenMinutesAgo)));
        notifications.addAll(getNotificationsWithCriterion(set, "Minor", Restrictions.isNull("notification.respondTime"), Restrictions.gt("notification.pageTime", fifteenMinutesAgo)));
        notifications.addAll(getNotificationsWithCriterion(set, "Normal", Restrictions.isNotNull("notification.respondTime"), Restrictions.gt("notification.pageTime", oneWeekAgo)));

        
        return notifications.toArray(new Notification[notifications.size()]);
    }
    
    private List<Notification> getNotificationsWithCriterion(SurveillanceSet set, String severity, Criterion... criterions) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsNotification.class, "notification");
        
        OnmsCriteria nodeCriteria = criteria.createCriteria("node");
        addCriteriaForSurveillanceSet(nodeCriteria, set);
        
        nodeCriteria.add(Restrictions.ne("type", "D"));
        for (Criterion criterion : criterions) {
            criteria.add(criterion);
        }
        
        criteria.addOrder(Order.desc("notification.pageTime"));
        
        List<OnmsNotification> notifications = m_notificationDao.findMatching(criteria);
        
        return convertOnmsNotificationsToNotifications(notifications, severity);
    }

    private List<Notification> convertOnmsNotificationsToNotifications(List<OnmsNotification> notifications, String severity) {
        List<Notification> notifs = new ArrayList<Notification>(notifications.size());
        boolean isDashboardRole = isDashboardRole();
        for (OnmsNotification notification : notifications) {
            notifs.add(createNotification(notification, severity, isDashboardRole));
        }
        return notifs;
    }

    private Notification createNotification(OnmsNotification onmsNotif, String severity, boolean isDashboardRole) {
        Notification notif = new Notification();
        notif.setNodeLabel(onmsNotif.getNode().getLabel());
        notif.setNodeId(onmsNotif.getNode().getNodeId());
        notif.setIsDashboardRole(isDashboardRole);
        notif.setResponder(onmsNotif.getAnsweredBy());
        notif.setRespondTime(onmsNotif.getRespondTime() == null ? null : new Date(onmsNotif.getRespondTime().getTime()));
        notif.setSentTime(onmsNotif.getPageTime() == null ? null : new Date(onmsNotif.getPageTime().getTime()));
        notif.setServiceName(onmsNotif.getServiceType() == null ? "" : onmsNotif.getServiceType().getName());
        notif.setTextMessage(onmsNotif.getTextMsg() == null ? "" : onmsNotif.getTextMsg());
        notif.setSeverity(severity);
        
        return notif;
    }
    
    /** {@inheritDoc} */
    public NodeRtc[] getRtcForSet(SurveillanceSet set) {
        OnmsCriteria serviceCriteria = m_rtcService.createServiceCriteria();
        OnmsCriteria outageCriteria = m_rtcService.createOutageCriteria();
        addCriteriaForSurveillanceSet(serviceCriteria, set);
        addCriteriaForSurveillanceSet(outageCriteria, set);
        
        RtcNodeModel model = m_rtcService.getNodeListForCriteria(serviceCriteria, outageCriteria);
        
        NodeRtc[] nodeRtc = new NodeRtc[model.getNodeList().size()];
        
        int index = 0;
        boolean isDashboardRole = isDashboardRole();
        for (RtcNode node : model.getNodeList()) {
            NodeRtc n = new NodeRtc();
            
            n.setNodeLabel(node.getNode().getLabel());
            n.setNodeId(node.getNode().getNodeId());
            n.setIsDashboardRole(isDashboardRole);
            n.setDownServiceCount(node.getDownServiceCount());
            n.setServiceCount(node.getServiceCount());
            if (node.getDownServiceCount() == 0) {
                n.setServiceStyle("Normal");
            } else {
                n.setServiceStyle("Critical");
            }
            
            n.setAvailability(node.getAvailabilityAsString());
            if (node.getAvailability() == 1.0) {
                n.setAvailabilityStyle("Normal");
            } else {
                n.setAvailabilityStyle("Critical");
            }
            
            nodeRtc[index++] = n;
        }
        
        return nodeRtc;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_nodeDao != null, "nodeDao property must be set and cannot be null");
        Assert.state(m_resourceDao != null, "resourceDao property must be set and cannot be null");
        Assert.state(m_graphDao != null, "graphDao property must be set and cannot be null");
        Assert.state(m_webSurveillanceService != null, "webSurveillanceService property must be set and cannot be null");
        Assert.state(m_surveillanceViewConfigDao != null, "surveillanceViewConfigDao property must be set and cannot be null");
        Assert.state(m_categoryDao != null, "categoryDao property must be set and cannot be null");
        Assert.state(m_alarmDao != null, "alarmDao property must be set and cannot be null");
        Assert.state(m_notificationDao != null, "notificationDao property must be set and cannot be null");
        Assert.state(m_rtcService != null, "rtcService property must be set and cannot be null");
        Assert.state(m_groupDao != null, "groupDao property must be set and cannot be null");
        Assert.state(m_outageDao != null, "outageDao property must be set and cannot be null");
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
    
    /**
     * <p>setNotificationDao</p>
     *
     * @param notifDao a {@link org.opennms.netmgt.dao.NotificationDao} object.
     */
    public void setNotificationDao(NotificationDao notifDao) {
        m_notificationDao = notifDao;
    }
    
    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    /**
     * <p>setGraphDao</p>
     *
     * @param graphDao a {@link org.opennms.netmgt.dao.GraphDao} object.
     */
    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }

    /**
     * <p>getWebSurveillanceService</p>
     *
     * @return a {@link org.opennms.web.svclayer.SurveillanceService} object.
     */
    public org.opennms.web.svclayer.SurveillanceService getWebSurveillanceService() {
        return m_webSurveillanceService;
    }

    /**
     * <p>setWebSurveillanceService</p>
     *
     * @param webSurveillanceService a {@link org.opennms.web.svclayer.SurveillanceService} object.
     */
    public void setWebSurveillanceService(org.opennms.web.svclayer.SurveillanceService webSurveillanceService) {
        m_webSurveillanceService = webSurveillanceService;
    }

    /**
     * <p>getSurveillanceViewConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.SurveillanceViewConfigDao} object.
     */
    public SurveillanceViewConfigDao getSurveillanceViewConfigDao() {
        return m_surveillanceViewConfigDao;
    }

    /**
     * <p>setSurveillanceViewConfigDao</p>
     *
     * @param surveillanceViewConfigDao a {@link org.opennms.netmgt.dao.SurveillanceViewConfigDao} object.
     */
    public void setSurveillanceViewConfigDao(SurveillanceViewConfigDao surveillanceViewConfigDao) {
        m_surveillanceViewConfigDao = surveillanceViewConfigDao;
    }

    /**
     * <p>getCategoryDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.CategoryDao} object.
     */
    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    /**
     * <p>setCategoryDao</p>
     *
     * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
     */
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    /**
     * <p>getAlarmDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.AlarmDao} object.
     */
    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    /**
     * <p>setAlarmDao</p>
     *
     * @param alarmDao a {@link org.opennms.netmgt.dao.AlarmDao} object.
     */
    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }
    
    /**
     * <p>getRtcService</p>
     *
     * @return a {@link org.opennms.web.svclayer.RtcService} object.
     */
    public RtcService getRtcService() {
        return m_rtcService;
    }

    /**
     * <p>setRtcService</p>
     *
     * @param rtcService a {@link org.opennms.web.svclayer.RtcService} object.
     */
    public void setRtcService(RtcService rtcService) {
        m_rtcService = rtcService;
    }

    /**
     * <p>getGroupDao</p>
     *
     * @return a {@link org.opennms.netmgt.config.GroupDao} object.
     */
    public GroupDao getGroupDao() {
        return m_groupDao;
    }

    /**
     * <p>setGroupDao</p>
     *
     * @param groupDao a {@link org.opennms.netmgt.config.GroupDao} object.
     */
    public void setGroupDao(GroupDao groupDao) {
        m_groupDao = groupDao;
    }

    /**
     * <p>getOutageDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.OutageDao} object.
     */
    public OutageDao getOutageDao() {
        return m_outageDao;
    }

    /**
     * <p>setOutageDao</p>
     *
     * @param outageDao a {@link org.opennms.netmgt.dao.OutageDao} object.
     */
    public void setOutageDao(OutageDao outageDao) {
        m_outageDao = outageDao;
    }
}
