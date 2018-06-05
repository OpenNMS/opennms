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

package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.SqlRestriction.Type;
import org.opennms.features.vaadin.dashboard.model.*;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

/**
 * This class implements a {@link Dashlet} for testing purposes.
 *
 * @author Christian Pape
 */
public class SummaryDashlet extends AbstractDashlet {

    /**
     * The {@link AlarmDao} used
     */
    private AlarmDao m_alarmDao;
    /**
     * Timeslot to use
     */
    private long m_timeslot = 3600;
    /**
     * boosted value
     */
    private boolean m_boosted = false;
    /**
     * Trend identifiers
     */
    private static int TREND_NORTH = 4;
    private static int TREND_NORTHEAST = 3;
    private static int TREND_EAST = 2;
    private static int TREND_SOUTHEAST = 1;
    private static int TREND_SOUTH = 0;

    private DashletComponent m_dashboardComponent;
    private DashletComponent m_wallboardComponent;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     */
    public SummaryDashlet(String name, DashletSpec dashletSpec, AlarmDao alarmDao) {
        super(name, dashletSpec);
        /**
         * Setting the member fields
         */
        m_alarmDao = alarmDao;
    }

    /**
     * Returns a human-readable {@link String} representation of a timestamp in the past.
     *
     * @param secondsAll the timestamp to be used
     * @return a human-readable representation
     */
    public String getHumanReadableFormat(long secondsAll) {
        long seconds = secondsAll;
        long minutes = 0;
        long hours = 0;
        long days = 0;

        if (seconds / 60 > 0) {
            long rest = seconds % 60;
            minutes = seconds / 60;
            seconds = rest;
        }

        if (minutes / 60 > 0) {
            long rest = minutes % 60;
            hours = minutes / 60;
            minutes = rest;
        }

        if (hours / 24 > 0) {
            long rest = hours % 24;
            days = hours / 24;
            hours = rest;
        }

        String output = "";

        if (days > 0) {
            output += days + "d";
        }
        if (hours > 0) {
            output += hours + "h";
        }
        if (minutes > 0) {
            output += minutes + "m";
        }
        if (seconds > 0) {
            output += seconds + "s";
        }

        return output;
    }

    /**
     * Computes the trend for acknowledged and not acknowledged alarms
     *
     * @param ack    number of acknowledged alarms
     * @param notAck number of unacknowledged alarms
     * @return the trend value
     */
    private int computeTrend(int ack, int notAck) {
        if (ack == notAck) {
            return TREND_EAST;
        } else {
            if (notAck == 0) {
                return TREND_NORTH;
            } else {
                if (ack == 0) {
                    return TREND_NORTH;
                } else {
                    double ratio = (double) ack / (double) notAck;

                    if (ratio < 0.5) {
                        return TREND_SOUTH;
                    } else {
                        if (ratio < 1) {
                            return TREND_SOUTHEAST;
                        } else {
                            if (ratio > 2) {
                                return TREND_NORTH;
                            } else {
                                return TREND_NORTHEAST;
                            }
                        }
                    }
                }
            }
        }
    }

    private Component getLegend(String entity) {
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setSpacing(true);
        horizontalLayout.addStyleName("summary");
        horizontalLayout.addStyleName("global");

        Label labelx = new Label(entity);
        labelx.addStyleName("summary-font-legend");

        Image ackdImage = new Image(null, new ThemeResource("img/acknowledged.png"));
        ackdImage.setWidth(16, Sizeable.Unit.PIXELS);

        Image unackdImage = new Image(null, new ThemeResource("img/unacknowledged.png"));
        unackdImage.setWidth(16, Sizeable.Unit.PIXELS);

        Label dummyLabel = new Label();
        dummyLabel.setWidth(32, Sizeable.Unit.PIXELS);

        horizontalLayout.addComponent(labelx);
        horizontalLayout.addComponent(ackdImage);
        horizontalLayout.addComponent(unackdImage);
        horizontalLayout.addComponent(dummyLabel);

        horizontalLayout.setComponentAlignment(ackdImage, Alignment.TOP_RIGHT);
        horizontalLayout.setComponentAlignment(unackdImage, Alignment.TOP_RIGHT);

        horizontalLayout.setExpandRatio(labelx, 4.0f);
        horizontalLayout.setExpandRatio(ackdImage, 1.0f);
        horizontalLayout.setExpandRatio(unackdImage, 1.0f);
        horizontalLayout.setExpandRatio(dummyLabel, 1.0f);

        horizontalLayout.setWidth(375, Sizeable.Unit.PIXELS);

        return horizontalLayout;
    }

    /**
     * Returns the component showing the alarms and the trends by severity
     *
     * @return the {@link Component}
     */
    private Component getComponentSeverity(int width) {
        VerticalLayout verticalLayout = new VerticalLayout();

        int overallSum = 0;
        int severitySum = 0;

        verticalLayout.addComponent(getLegend("Severity"));

        for (OnmsSeverity onmsSeverity : OnmsSeverity.values()) {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setSpacing(true);
            horizontalLayout.addStyleName("summary");
            horizontalLayout.addStyleName(onmsSeverity.name().toLowerCase());

            int acknowledged = countBySeverity(true, m_timeslot, onmsSeverity);
            int notAcknowledged = countBySeverity(false, m_timeslot, onmsSeverity);

            Label labelSeverity = new Label(onmsSeverity.getLabel());
            labelSeverity.addStyleName("summary-font");
            Label labelAcknowledge = new Label(String.valueOf(acknowledged));
            labelAcknowledge.addStyleName("summary-font");
            Label labelNotAcknowledged = new Label(String.valueOf(notAcknowledged));
            labelNotAcknowledged.addStyleName("summary-font");

            horizontalLayout.addComponent(labelSeverity);
            horizontalLayout.addComponent(labelAcknowledge);
            horizontalLayout.addComponent(labelNotAcknowledged);

            int status = computeTrend(acknowledged, notAcknowledged);

            severitySum += onmsSeverity.getId();
            overallSum += onmsSeverity.getId() * status;

            Image image = new Image(null, new ThemeResource("img/a" + status + ".png"));
            image.setWidth(width, Sizeable.Unit.PIXELS);
            horizontalLayout.addComponent(image);

            horizontalLayout.setExpandRatio(labelSeverity, 4.0f);
            horizontalLayout.setExpandRatio(labelAcknowledge, 1.0f);
            horizontalLayout.setExpandRatio(labelNotAcknowledged, 1.0f);
            horizontalLayout.setExpandRatio(image, 1.0f);

            horizontalLayout.setComponentAlignment(image, Alignment.TOP_CENTER);

            horizontalLayout.setWidth(375, Sizeable.Unit.PIXELS);
            verticalLayout.addComponent(horizontalLayout);
        }

        int globalTrend = (int) Math.max(0, Math.min(4, Math.round(((double) overallSum) / ((double) severitySum))));

        Image image = new Image(null, new ThemeResource("img/a" + globalTrend + ".png"));
        image.setWidth(width * 8f, Sizeable.Unit.PIXELS);

        VerticalLayout globalTrendLayout = new VerticalLayout();
        globalTrendLayout.setSpacing(true);
        globalTrendLayout.addStyleName("summary");
        globalTrendLayout.addStyleName("global");
        globalTrendLayout.setSizeFull();

        Label labelTitle = new Label("Alarms trend by severity");
        labelTitle.addStyleName("summary-font");
        labelTitle.setSizeUndefined();

        Label labelTimeslot = new Label("(" + getHumanReadableFormat(m_timeslot) + ")");
        labelTimeslot.addStyleName("summary-font");
        labelTimeslot.setSizeUndefined();

        globalTrendLayout.addComponent(labelTitle);
        globalTrendLayout.addComponent(labelTimeslot);
        globalTrendLayout.addComponent(image);

        globalTrendLayout.setWidth(375, Sizeable.Unit.PIXELS);

        globalTrendLayout.setComponentAlignment(labelTitle, Alignment.MIDDLE_CENTER);
        globalTrendLayout.setComponentAlignment(labelTimeslot, Alignment.MIDDLE_CENTER);
        globalTrendLayout.setComponentAlignment(image, Alignment.MIDDLE_CENTER);

        globalTrendLayout.setExpandRatio(labelTitle, 1.0f);

        verticalLayout.addComponent(globalTrendLayout, 0);

        m_boosted = (globalTrend > 2);

        return verticalLayout;
    }

    /**
     * Returns the component showing the alarms and the trends by severity
     *
     * @return the {@link Component}
     */
    private Component getComponentUei(int width) {
        VerticalLayout verticalLayout = new VerticalLayout();

        int overallSum = 0;
        int severitySum = 0;

        verticalLayout.addComponent(getLegend("UEI"));

        String[] ueis = {"uei.opennms.org/nodes/nodeLostService", "uei.opennms.org/nodes/interfaceDown", "uei.opennms.org/nodes/nodeDown"};

        for (int i = 0; i < ueis.length; i++) {
            String uei = ueis[i];
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setSpacing(true);
            horizontalLayout.addStyleName("summary");

            if (i == 0) {
                horizontalLayout.addStyleName(OnmsSeverity.MINOR.name().toLowerCase());
            } else {
                if (i == 1) {
                    horizontalLayout.addStyleName(OnmsSeverity.MINOR.name().toLowerCase());
                } else {
                    horizontalLayout.addStyleName(OnmsSeverity.MAJOR.name().toLowerCase());
                }
            }

            int acknowledged = countByUei(true, m_timeslot, uei);
            int notAcknowledged = countByUei(false, m_timeslot, uei);

            Label labelSeverity = new Label(uei.replace("uei.opennms.org/nodes/", ""));
            labelSeverity.addStyleName("summary-font");
            Label labelAcknowledge = new Label(String.valueOf(acknowledged));
            labelAcknowledge.addStyleName("summary-font");
            Label labelNotAcknowledged = new Label(String.valueOf(notAcknowledged));
            labelNotAcknowledged.addStyleName("summary-font");

            horizontalLayout.addComponent(labelSeverity);
            horizontalLayout.addComponent(labelAcknowledge);
            horizontalLayout.addComponent(labelNotAcknowledged);

            int status = computeTrend(acknowledged, notAcknowledged);

            severitySum += i;
            overallSum += i * status;

            Image image = new Image(null, new ThemeResource("img/a" + status + ".png"));
            image.setWidth(width, Sizeable.Unit.PIXELS);
            horizontalLayout.addComponent(image);

            horizontalLayout.setExpandRatio(labelSeverity, 4.0f);
            horizontalLayout.setExpandRatio(labelAcknowledge, 1.0f);
            horizontalLayout.setExpandRatio(labelNotAcknowledged, 1.0f);
            horizontalLayout.setExpandRatio(image, 1.0f);

            horizontalLayout.setComponentAlignment(image, Alignment.TOP_CENTER);

            horizontalLayout.setWidth(375, Sizeable.Unit.PIXELS);
            verticalLayout.addComponent(horizontalLayout);
        }

        int globalTrend = (int) Math.max(0, Math.min(4, Math.round(((double) overallSum) / ((double) severitySum))));

        Image image = new Image(null, new ThemeResource("img/a" + globalTrend + ".png"));
        image.setWidth(width * 8f, Sizeable.Unit.PIXELS);

        VerticalLayout globalTrendLayout = new VerticalLayout();
        globalTrendLayout.setSpacing(true);
        globalTrendLayout.addStyleName("summary");
        globalTrendLayout.addStyleName("global");
        globalTrendLayout.setSizeFull();

        Label labelTitle = new Label("Alarms trend by UEI");
        labelTitle.addStyleName("summary-font");
        labelTitle.setSizeUndefined();

        Label labelTimeslot = new Label("(" + getHumanReadableFormat(m_timeslot) + ")");
        labelTimeslot.addStyleName("summary-font");
        labelTimeslot.setSizeUndefined();

        globalTrendLayout.addComponent(labelTitle);
        globalTrendLayout.addComponent(labelTimeslot);
        globalTrendLayout.addComponent(image);

        globalTrendLayout.setWidth(375, Sizeable.Unit.PIXELS);

        globalTrendLayout.setComponentAlignment(labelTitle, Alignment.MIDDLE_CENTER);
        globalTrendLayout.setComponentAlignment(labelTimeslot, Alignment.MIDDLE_CENTER);
        globalTrendLayout.setComponentAlignment(image, Alignment.MIDDLE_CENTER);

        globalTrendLayout.setExpandRatio(labelTitle, 1.0f);

        verticalLayout.addComponent(globalTrendLayout, 0);

        m_boosted = (globalTrend > 2);

        return verticalLayout;
    }

    /**
     * Searches for alarms with the given criterias and returns the number found.
     *
     * @param acknowledged search for acknowledged or unacknowledged alarms
     * @param age          the age of the alarms
     * @param onmsSeverity the {@link OnmsSeverity} to search for
     * @return number of alarms found
     */
    public int countBySeverity(boolean acknowledged, long age, OnmsSeverity onmsSeverity) {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsAlarm.class);

        if (acknowledged) {
            criteriaBuilder.isNotNull("alarmAckUser");
        } else {
            criteriaBuilder.isNull("alarmAckUser");
        }

        criteriaBuilder.eq("severity", onmsSeverity);

        criteriaBuilder.sql("EXTRACT(EPOCH FROM CURRENT_TIMESTAMP - lastEventTime) < ?", age, Type.LONG);

        return m_alarmDao.countMatching(criteriaBuilder.toCriteria());
    }

    /**
     * Searches for alarms with the given criterias and returns the number found.
     *
     * @param acknowledged search for acknowledged or unacknowledged alarms
     * @param age          the age of the alarms
     * @param uei          search for alarms with the specified uei
     * @return number of alarms found
     */
    public int countByUei(boolean acknowledged, long age, String uei) {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsAlarm.class);

        if (acknowledged) {
            criteriaBuilder.isNotNull("alarmAckUser");
        } else {
            criteriaBuilder.isNull("alarmAckUser");
        }

        criteriaBuilder.eq("uei", uei);

        criteriaBuilder.sql("EXTRACT(EPOCH FROM CURRENT_TIMESTAMP - lastEventTime) < ?", age, Type.LONG);

        return m_alarmDao.countMatching(criteriaBuilder.toCriteria());
    }

    @Override
    public DashletComponent getWallboardComponent() {
        if (m_wallboardComponent == null) {
            m_wallboardComponent = new AbstractDashletComponent() {
                private HorizontalLayout m_horizontalLayout = new HorizontalLayout();

                {
                    m_horizontalLayout.setCaption(getName());
                    m_horizontalLayout.setSizeFull();
                }

                /**
                 * Injects CSS styles in the current page
                 */
                private void injectWallboardStyles() {
                    Page.getCurrent().getStyles().add(".summary.cleared { background: #000000; border-left: 15px solid #858585; }");
                    Page.getCurrent().getStyles().add(".summary.normal { background: #000000; border-left: 15px solid #336600; }");
                    Page.getCurrent().getStyles().add(".summary.indeterminate {  background: #000000; border-left: 15px solid #999; }");
                    Page.getCurrent().getStyles().add(".summary.warning { background: #000000; border-left: 15px solid #FFCC00; }");
                    Page.getCurrent().getStyles().add(".summary.minor { background: #000000;  border-left: 15px solid #FF9900; }");
                    Page.getCurrent().getStyles().add(".summary.major { background: #000000; border-left: 15px solid #FF3300; }");
                    Page.getCurrent().getStyles().add(".summary.critical { background: #000000; border-left: 15px solid #CC0000; }");
                    Page.getCurrent().getStyles().add(".summary.global { background: #000000; border-left: 15px solid #000000; }");
                    Page.getCurrent().getStyles().add(".summary { padding: 5px 5px; margin: 1px; }");
                    Page.getCurrent().getStyles().add(".summary-font { font-size: 24px; line-height: normal; text-align: right; color: #3ba300; }");
                    Page.getCurrent().getStyles().add(".summary-font-legend { font-size: 16px; line-height: normal; text-align: right; color: #3ba300; }");
                }

                @Override
                public void refresh() {
                    m_timeslot = 3600;

                    try {
                        m_timeslot = Math.max(1, Integer.parseInt(getDashletSpec().getParameters().get("timeslot")));
                    } catch (NumberFormatException numberFormatException) {
                        /**
                         * Just ignore
                         */
                    }

                    m_horizontalLayout.removeAllComponents();

                    injectWallboardStyles();

                    Component severity = getComponentSeverity(32);
                    Component uei = getComponentUei(32);

                    m_horizontalLayout.addComponent(severity);
                    m_horizontalLayout.addComponent(uei);

                    m_horizontalLayout.setSizeFull();
                    m_horizontalLayout.setComponentAlignment(severity, Alignment.TOP_CENTER);
                    m_horizontalLayout.setComponentAlignment(uei, Alignment.TOP_CENTER);
                }

                @Override
                public Component getComponent() {
                    return m_horizontalLayout;
                }

                @Override
                public boolean isBoosted() {
                    return SummaryDashlet.this.m_boosted;
                }
            };
        }

        return m_wallboardComponent;
    }

    @Override
    public DashletComponent getDashboardComponent() {
        if (m_dashboardComponent == null) {
            m_dashboardComponent = new AbstractDashletComponent() {
                private HorizontalLayout m_horizontalLayout = new HorizontalLayout();

                {
                    m_horizontalLayout.setCaption(getName());
                    m_horizontalLayout.setSizeFull();
                }

                /**
                 * Injects CSS styles in the current page
                 */
                private void injectDashboardStyles() {
                    Page.getCurrent().getStyles().add(".summary.cleared { background: #000000; border-left: 8px solid #858585; }");
                    Page.getCurrent().getStyles().add(".summary.normal { background: #000000; border-left: 8px solid #336600; }");
                    Page.getCurrent().getStyles().add(".summary.indeterminate {  background: #000000; border-left: 8px solid #999; }");
                    Page.getCurrent().getStyles().add(".summary.warning { background: #000000; border-left: 8px solid #FFCC00; }");
                    Page.getCurrent().getStyles().add(".summary.minor { background: #000000;  border-left: 8px solid #FF9900; }");
                    Page.getCurrent().getStyles().add(".summary.major { background: #000000; border-left: 8px solid #FF3300; }");
                    Page.getCurrent().getStyles().add(".summary.critical { background: #000000; border-left: 8px solid #CC0000; }");
                    Page.getCurrent().getStyles().add(".summary.global { background: #000000; border-left: 8px solid #000000; }");
                    Page.getCurrent().getStyles().add(".summary { padding: 5px 5px; margin: 1px; }");
                    Page.getCurrent().getStyles().add(".summary-font { font-size: 17px; line-height: normal; text-align: right; color: #3ba300; }");
                    Page.getCurrent().getStyles().add(".summary-font-legend { font-size: 9px; line-height: normal; text-align: right; color: #3ba300; }");
                }

                @Override
                public void refresh() {
                    m_timeslot = 3600;

                    try {
                        m_timeslot = Math.max(1, Integer.parseInt(getDashletSpec().getParameters().get("timeslot")));
                    } catch (NumberFormatException numberFormatException) {
                        /**
                         * Just ignore
                         */
                    }

                    m_horizontalLayout.removeAllComponents();

                    Accordion accordion = new Accordion();
                    accordion.setSizeFull();

                    injectDashboardStyles();

                    Component severity = getComponentSeverity(16);
                    Component uei = getComponentUei(16);

                    VerticalLayout v1 = new VerticalLayout(severity);
                    v1.setSizeFull();
                    v1.setComponentAlignment(severity, Alignment.MIDDLE_CENTER);
                    v1.setMargin(true);
                    accordion.addTab(v1, "by Severity");

                    VerticalLayout v2 = new VerticalLayout(uei);
                    v2.setSizeFull();
                    v2.setComponentAlignment(uei, Alignment.MIDDLE_CENTER);
                    v2.setMargin(true);
                    accordion.addTab(v2, "by Uei");

                    m_horizontalLayout.addComponent(accordion);
                }

                @Override
                public Component getComponent() {
                    return m_horizontalLayout;
                }

                @Override
                public boolean isBoosted() {
                    return SummaryDashlet.this.m_boosted;
                }
            };
        }

        return m_dashboardComponent;
    }
}
