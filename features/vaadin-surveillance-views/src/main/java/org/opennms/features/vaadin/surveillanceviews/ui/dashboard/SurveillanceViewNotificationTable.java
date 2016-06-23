/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;
import org.opennms.features.topology.api.support.InfoWindow;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * This class represents a table for displaying the notifications for a surveillance view dashboard.
 *
 * @author Christian Pape
 */
public class SurveillanceViewNotificationTable extends SurveillanceViewDetailTable {
    /**
     * Helper class for handling notifications.
     */
    public class Notification {
        /**
         * the notification's id
         */
        private int id;
        /**
         * the node's id
         */
        private int nodeId;
        /**
         * the node label
         */
        private String nodeLabel;
        /**
         * the respond time
         */
        private Date respondTime;
        /**
         * the start time
         */
        private Date pageTime;
        /**
         * the responder
         */
        private String answeredBy;
        /**
         * the text message
         */
        private String textMsg;
        /**
         * the service type
         */
        private String serviceType;
        /**
         * the custom severity
         */
        private String severity;

        /**
         * Constructor for instantiating new instances.
         *
         * @param id          the notification id
         * @param nodeId      the node id
         * @param nodeLabel   the node label
         * @param pageTime    the start time
         * @param respondTime the respond time
         * @param answeredBy  the responder
         * @param textMsg     the text message
         * @param serviceType the service type
         * @param severity    the custom severity
         */
        public Notification(int id, int nodeId, String nodeLabel, Date pageTime, Date respondTime, String answeredBy, String textMsg, String serviceType, String severity) {
            this.id = id;
            this.nodeId = nodeId;
            this.nodeLabel = nodeLabel;
            this.respondTime = respondTime;
            this.textMsg = textMsg;
            this.answeredBy = answeredBy;
            this.severity = severity;
            this.serviceType = serviceType;
            this.pageTime = pageTime;
        }

        public Date getPageTime() {
            return pageTime;
        }

        public String getAnsweredBy() {
            return answeredBy;
        }

        public int getId() {
            return id;
        }

        public int getNodeId() {
            return nodeId;
        }

        public String getNodeLabel() {
            return nodeLabel;
        }

        public Date getRespondTime() {
            return respondTime;
        }

        public String getTextMsg() {
            return textMsg;
        }

        public String getServiceType() {
            return serviceType;
        }

        public String getSeverity() {
            return severity;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Notification that = (Notification) o;

            if (id != that.id) {
                return false;
            }
            if (nodeId != that.nodeId) {
                return false;
            }
            if (answeredBy != null ? !answeredBy.equals(that.answeredBy) : that.answeredBy != null) {
                return false;
            }
            if (nodeLabel != null ? !nodeLabel.equals(that.nodeLabel) : that.nodeLabel != null) {
                return false;
            }
            if (pageTime != null ? !pageTime.equals(that.pageTime) : that.pageTime != null) {
                return false;
            }
            if (respondTime != null ? !respondTime.equals(that.respondTime) : that.respondTime != null) {
                return false;
            }
            if (serviceType != null ? !serviceType.equals(that.serviceType) : that.serviceType != null) {
                return false;
            }
            if (severity != null ? !severity.equals(that.severity) : that.severity != null) {
                return false;
            }
            if (textMsg != null ? !textMsg.equals(that.textMsg) : that.textMsg != null) {
                return false;
            }

            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + nodeId;
            result = 31 * result + (nodeLabel != null ? nodeLabel.hashCode() : 0);
            result = 31 * result + (respondTime != null ? respondTime.hashCode() : 0);
            result = 31 * result + (pageTime != null ? pageTime.hashCode() : 0);
            result = 31 * result + (answeredBy != null ? answeredBy.hashCode() : 0);
            result = 31 * result + (textMsg != null ? textMsg.hashCode() : 0);
            result = 31 * result + (serviceType != null ? serviceType.hashCode() : 0);
            result = 31 * result + (severity != null ? severity.hashCode() : 0);
            return result;
        }
    }

    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewNotificationTable.class);
    /**
     * the bean container for storing notifications
     */
    private BeanItemContainer<Notification> m_beanItemContainer = new BeanItemContainer<>(Notification.class);
    /**
     * the refresh future
     */
    protected ListenableFuture<List<Notification>> m_future;

    /**
     * Constructor for instantiating this component.
     *
     * @param surveillanceViewService the surveillance view service to be used
     * @param enabled                 the flag should links be enabled?
     */
    public SurveillanceViewNotificationTable(SurveillanceViewService surveillanceViewService, boolean enabled) {
        /**
         * calling the super constructor
         */
        super("Notifications", surveillanceViewService, enabled);

        /**
         * set the datasource
         */
        setContainerDataSource(m_beanItemContainer);

        /**
         * set the base style name
         */
        addStyleName("surveillance-view");

        /**
         * add node column
         */
        addGeneratedColumn("nodeLabel", new ColumnGenerator() {
            @Override
            public Object generateCell(final Table table, final Object itemId, final Object propertyId) {
                final Notification notification = (Notification) itemId;

                Button icon = getClickableIcon("glyphicon glyphicon-bell", new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        final URI currentLocation = Page.getCurrent().getLocation();
                        final String contextRoot = VaadinServlet.getCurrent().getServletContext().getContextPath();
                        final String redirectFragment = contextRoot + "/notification/detail.jsp?quiet=true&notice=" + notification.getId();

                        LOG.debug("notification {} clicked, current location = {}, uri = {}", notification.getId(), currentLocation, redirectFragment);

                        try {
                            SurveillanceViewNotificationTable.this.getUI().addWindow(new InfoWindow(new URL(currentLocation.toURL(), redirectFragment), new InfoWindow.LabelCreator() {
                                @Override
                                public String getLabel() {
                                    return "Notification Info " + notification.getId();
                                }
                            }));
                        } catch (MalformedURLException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                });

                Button button = new Button(notification.getNodeLabel());
                button.setPrimaryStyleName(BaseTheme.BUTTON_LINK);
                button.setEnabled(m_enabled);

                button.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        final URI currentLocation = Page.getCurrent().getLocation();
                        final String contextRoot = VaadinServlet.getCurrent().getServletContext().getContextPath();
                        final String redirectFragment = contextRoot + "/element/node.jsp?quiet=true&node=" + notification.getNodeId();
                        ;

                        LOG.debug("node {} clicked, current location = {}, uri = {}", notification.getNodeId(), currentLocation, redirectFragment);

                        try {
                            SurveillanceViewNotificationTable.this.getUI().addWindow(new InfoWindow(new URL(currentLocation.toURL(), redirectFragment), new InfoWindow.LabelCreator() {
                                @Override
                                public String getLabel() {
                                    return "Node Info " + notification.getNodeId();
                                }
                            }));
                        } catch (MalformedURLException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                });

                HorizontalLayout horizontalLayout = new HorizontalLayout();

                horizontalLayout.addComponent(icon);
                horizontalLayout.addComponent(button);

                horizontalLayout.setSpacing(true);

                return horizontalLayout;
            }
        });

        /**
         * set the cell style generator
         */
        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(final Table source, final Object itemId, final Object propertyId) {
                Notification notification = ((Notification) itemId);
                return notification.getSeverity().toLowerCase();
            }
        });

        /**
         * set column headers
         */
        setColumnHeader("nodeLabel", "Node");
        setColumnHeader("serviceType", "Service");
        setColumnHeader("textMsg", "Message");
        setColumnHeader("pageTime", "Sent Time");
        setColumnHeader("answeredBy", "Responder");
        setColumnHeader("respondTime", "Respond Time");

        setColumnExpandRatio("nodeLabel", 2.0f);
        setColumnExpandRatio("serviceType", 1.0f);
        setColumnExpandRatio("textMsg", 4.0f);
        setColumnExpandRatio("pageTime", 2.0f);
        setColumnExpandRatio("answeredBy", 1.0f);
        setColumnExpandRatio("respondTime", 2.0f);

        /**
         * define visible columns
         */
        setVisibleColumns("nodeLabel", "serviceType", "textMsg", "pageTime", "answeredBy", "respondTime");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshDetails(final Set<OnmsCategory> rowCategories, final Set<OnmsCategory> colCategories) {
        if (m_future != null && !m_future.isDone()) {
            return;
        }

        m_future = getSurveillanceViewService().getExecutorService().submit(new Callable<List<Notification>>() {
            @Override
            public List<Notification> call() throws Exception {
                /**
                 * create the custom severity map
                 */
                Map<OnmsNotification, String> customSeverity = new HashMap<>();

                /**
                 * retrieve the matching notifications
                 */
                List<OnmsNotification> onmsNotifications = getSurveillanceViewService().getNotificationsForCategories(rowCategories, colCategories, customSeverity);

                /**
                 * create the notifications list
                 */
                List<Notification> notifications = new ArrayList<>();

                for (OnmsNotification onmsNotification : onmsNotifications) {
                    notifications.add(new Notification(onmsNotification.getNotifyId(), onmsNotification.getNodeId(), onmsNotification.getNodeLabel(), onmsNotification.getPageTime(), onmsNotification.getRespondTime(), onmsNotification.getAnsweredBy(), onmsNotification.getTextMsg(), onmsNotification.getServiceType() != null ? onmsNotification.getServiceType().getName() : "", customSeverity.get(onmsNotification)));
                }

                return notifications;
            }
        });

        m_future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Notification> notifications = m_future.get();
                    getUI().access(new Runnable() {
                        @Override
                        public void run() {
                            /**
                             * empty the bean container
                             */
                            m_beanItemContainer.removeAllItems();

                            /**
                             * add items to container
                             */
                            if (notifications != null && !notifications.isEmpty()) {
                                for (Notification notification : notifications) {
                                    m_beanItemContainer.addItem(notification);
                                }
                            }

                            /**
                             * sort the items
                             */
                            sort(new Object[]{"pageTime"}, new boolean[]{false});

                            /**
                             * refresh the table
                             */
                            refreshRowCache();
                        }
                    });
                } catch (InterruptedException e) {
                    LOG.error("Interrupted", e);
                } catch (ExecutionException e) {
                    LOG.error("Exception in task", e.getCause());
                }
            }
        }, MoreExecutors.sameThreadExecutor());
    }
}
