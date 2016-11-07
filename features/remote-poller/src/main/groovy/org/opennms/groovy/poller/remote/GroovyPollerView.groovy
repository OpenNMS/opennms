/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.groovy.poller.remote;

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Dimension
import java.text.SimpleDateFormat

import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.table.TableModel

import org.opennms.netmgt.poller.PollStatus
import org.opennms.netmgt.poller.remote.PollerBackEnd
import org.opennms.netmgt.poller.remote.PollerFrontEnd
import org.opennms.poller.remote.MonitoringLocationListCellRenderer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean

/**
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */

class GroovyPollerView extends AbstractGui implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(GroovyPollerView.class);
    private static final String REGISTRATION = "registration";
    private static final String STATUS = "status";

    def m_backEnd;
    def m_frontEnd;

    def m_minimumSize = new Dimension(750, 200)

    def m_theme;
    def m_table;
    def m_frame;
    def m_cardPanel;
    def m_monLocation;
    def m_idLabel;
    def m_statusLabel;
    SimpleDateFormat m_dateFormat = new SimpleDateFormat("K:mm:ss a");

    public void setPollerFrontEnd(PollerFrontEnd pollerFrontEnd) {
        System.err.println("GroovyPollerView: PollerFrontEnd set.");
        m_frontEnd = pollerFrontEnd;
    }

    public void setPollerBackEnd(PollerBackEnd pollerBackEnd) {
        System.err.println("GroovyPollerView: PollerBackEnd set.");
        m_backEnd = pollerBackEnd;
    }

    public void afterPropertiesSet() {
        SwingUtilities.invokeLater( {
            System.err.println("Launching GUI.");
            m_theme = m_backEnd.getTheme()
            m_table = swing.table(model:createTableModel(), font:getMainFont(), foreground:getForegroundColor(), background:getBackgroundColor(), opaque:true)
            createAndShowGui()
            def height = 150 + m_table.getHeight()
            getGui().setSize(new Dimension(750, height))
        } );
    }

    @Override
    protected Color getForegroundColor() {
        def color = m_theme.getForegroundColor()
        if (color != null) {
            return color
        }
        return super.getForegroundColor()
    }

    @Override
    protected Color getBackgroundColor() {
        def color = m_theme.getBackgroundColor()
        if (color != null) {
            return color
        }
        return super.getBackgroundColor()
    }

    @Override
    protected Color getDetailColor() {
        def color = m_theme.getDetailColor()
        if (color != null) {
            return color
        }
        return super.getDetailColor()
    }

    @Override
    protected String getApplicationTitle() {
        return "OpenNMS Remote Poller"
    }

    protected JPanel getMainPanel() {
        m_cardPanel = swing.panel(background:getBackgroundColor(), opaque:true, layout:new CardLayout(), constraints:BorderLayout.CENTER) {
            swing.panel(background:getBackgroundColor(), opaque:true, constraints:REGISTRATION) {
                tableLayout(cellpadding:5) {
                    tr {
                        td(colfill:true) {
                            label(text:'Current monitoring locations: ', font:getLabelFont(), background:getBackgroundColor())
                        }
                        td {
                            def locations = getCurrentMonitoringLocations()
                            if (locations.size() > 0) {
                                m_monLocation = comboBox(items:locations, renderer:new MonitoringLocationListCellRenderer())
                            } else {
                                label(text:'No monitoring locations found', font:getLabelFont(), background:getBackgroundColor())
                            }
                        }
                    }
                    tr {
                        td (colspan:2, align:"CENTER"){
                            button(text:'Register', constraints:BorderLayout.SOUTH, actionPerformed:{ doRegistration() }, font:getLabelFont())
                        }
                    }
                }
            }
            swing.panel(background:getBackgroundColor(), opaque:true, constraints:STATUS, layout:new BorderLayout()) {
                swing.panel(background:getBackgroundColor(), opaque:true, constraints:BorderLayout.NORTH, layout:new BorderLayout()) {
                    m_idLabel = label(constraints:BorderLayout.WEST, text:'Monitor: '+m_frontEnd.getMonitorName(), font:getMainFont(), background:getBackgroundColor(), opaque:true)
                    m_statusLabel = label(constraints:BorderLayout.EAST, text:getStatus(), font:getMainFont(), background:getBackgroundColor(), opaque:true)
                }
                scrollPane(background:getBackgroundColor(), opaque:true, constraints:BorderLayout.CENTER, viewportView:m_table)
            }
        }

        updateCurrentPanel();

        m_frontEnd.pollStateChange = { updateTable() }
        m_frontEnd.propertyChange = { updateCurrentPanel(); m_idLabel.text = 'Monitor: '+m_frontEnd.getMonitorName(); m_statusLabel.text = getStatus() }
        m_frontEnd.configurationChanged = { updateTableModel(); m_idLabel.text = m_frontEnd.getMonitorName() }

        getGui().setMinimumSize(m_minimumSize)
        getGui().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

        return m_cardPanel;

    }

    private String getStatus() {
        if (m_frontEnd.paused) {
            return "Paused";
        } else if (m_frontEnd.disconnected) {
            return "Disconnected";
        } else if (m_frontEnd.started) {
            return "Started";
        } else if (m_frontEnd.exitNecessary) {
            return "Exit Necessary";
        } else if (m_frontEnd.registered) {
            return "Registered";
        }
    }

    private void doRegistration() {
        String loc = m_monLocation?.selectedItem?.locationName;
        if (loc == null) {
            LOG.warn("Null monitoring location selected");
        } else {
            System.err.println("Registering for location " + loc)
            m_frontEnd.register(loc);
        }
    }

    private List getCurrentMonitoringLocations() {
        try {
            return m_backEnd.getMonitoringLocations();
        } catch (final Exception e) {
            LOG.error("an error occurred getting the list of monitoring locations", e);
            System.exit(1);
        }
    }

    private void updateCurrentPanel() {
        SwingUtilities.invokeLater({ setCurrentPanel(m_frontEnd.started ? STATUS : REGISTRATION) });
    }

    private void setCurrentPanel(String panelName) {
        m_cardPanel.layout.show(m_cardPanel, panelName);
    }

    private void updateTable() {
        //System.err.println("Updating Table (status Change)")
        SwingUtilities.invokeLater({ m_table.model.fireTableDataChanged() })
    }

    private void updateTableModel() {
        //System.err.println("Updating Table Model (config Change)")
        SwingUtilities.invokeLater({ m_table.model = createTableModel(); m_table.model.fireTableDataChanged() })
    }

    private TableModel createTableModel() {
        List rows = Collections.EMPTY_LIST;
        if (m_frontEnd.registered)
            rows = m_frontEnd.getPollerPollState();

        return swing.tableModel(list:rows) {
            //closureColumn(header:'Node ID', read:{ it.polledService.nodeId })
            closureColumn(header:'Node Label', read:{ pollState -> pollState.polledService.nodeLabel })
            closureColumn(header:'Interface', read:{ pollState -> pollState.polledService.ipAddr })
            closureColumn(header:'Service', read:{ pollState -> pollState.polledService.svcName })
            closureColumn(header:'Last Status', read: { pollState -> (pollState.lastPoll == null ? '-' : pollState.lastPoll.statusName) })
            closureColumn(header:'Reason/ResponseTime', read: { pollState -> reasonResponse(pollState.lastPoll)})
            closureColumn(header:'Last Poll', read: { pollState -> formatPollTime(pollState.lastPollTime) })
            closureColumn(header:'Next Poll', read: { pollState -> formatPollTime(pollState.nextPollTime) })
        }



    }

    private String reasonResponse(PollStatus lastPoll) {
        if (lastPoll == null)
            return '-';
        if (lastPoll.responseTime >= 0)
            return lastPoll.responseTime+" ms";
        if (lastPoll.reason != null)
            return lastPoll.reason
        return '-';
    }

    private String formatPollTime(Date pollTime) {
        if (pollTime == null) return "-";

        return m_dateFormat.format(pollTime);
    }

}
