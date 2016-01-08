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

package org.opennms.groovy.poller.remote

import groovy.model.DefaultTableModel
import java.awt.Color
import java.awt.Component;
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

import javax.swing.JPanel
import javax.swing.JTable;
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.table.TableCellRenderer

import org.apache.batik.swing.JSVGCanvas
import org.opennms.netmgt.config.monitoringLocations.LocationDef
import org.opennms.netmgt.poller.remote.PollerBackEnd
import org.opennms.netmgt.poller.remote.PollerFrontEnd.PollerFrontEndStates
import org.opennms.netmgt.poller.remote.support.PollResult
import org.opennms.netmgt.poller.remote.support.ScanReport
import org.opennms.netmgt.poller.remote.support.ScanReportPollerFrontEnd
import org.opennms.netmgt.poller.remote.support.ScanReportPollerFrontEnd.ScanReportProperties
import org.springframework.beans.factory.InitializingBean
import org.springframework.util.Assert

class ScanGui extends AbstractGui implements InitializingBean, PropertyChangeListener {
    def m_metadataFieldNames = ['Customer Account Number', 'Reference ID', 'Customer Name']
    def m_locations = new ArrayList<String>()
    def m_scanReport;

    def m_backEnd

    def m_metadataFields = new HashMap<String, JTextField>()
    def m_progressBar
    def m_passFailPanel
    def updateDetails

    public ScanGui() {
        super()
    }

    public void setPollerBackEnd(final PollerBackEnd pollerBackEnd) {
        m_backEnd = pollerBackEnd
    }

    @Override
    protected String getHeaderText() {
        return "Network Scanner"
    }

    /**
     * This method is injected by Spring by using a <lookup-method>.
     *
     * @see applicationContext-scan-gui.xml
     */
    protected ScanReportPollerFrontEnd createPollerFrontEnd() {
    }

    public void afterPropertiesSet() {
        Assert.notNull(m_backEnd)
        Collection<LocationDef> monitoringLocations = m_backEnd.getMonitoringLocations()
        for (final LocationDef d : monitoringLocations) {
            m_locations.add(d.getLocationName())
        }
        SwingUtilities.invokeLater( { createAndShowGui() } )
    }

    public String validateFields() {
        for (final String key : m_metadataFieldNames) {
            final String fieldKey = getFieldKey(key);
            final JTextField field = m_metadataFields.get(fieldKey);
            if (field != null) {
                if (field.getText() == null || "".equals(field.getText())) {
                    return key + " is required!"
                }
            }
        }
        return null
    }

    protected String getFieldKey(final String name) {
        if (name == null) {
            return null
        }
        return name.replace(" ", "-").toLowerCase()
    }

    @Override
    public JPanel getMainPanel() {
        def errorLabel

        def updateValidation = {
            def errorMessage = validateFields()
            //System.err.println("error message: " + errorMessage)
            if (errorLabel != null) {
                if (errorMessage == null) {
                    errorLabel.setText("")
                    errorLabel.setVisible(false)
                    return false
                } else {
                    errorLabel.setText(errorMessage)
                    errorLabel.setVisible(true)
                    return true
                }
            } else {
                return true
            }
        }


        return swing.panel(background:getBackgroundColor(), opaque:true, constraints:"grow") {
            migLayout(
                    layoutConstraints:"fill" + debugString,
                    columnConstraints:"[right,grow][left,grow]",
                    rowConstraints:"[grow]"
                    )

            panel(constraints:"top", opaque:false) {
                migLayout(
                        layoutConstraints:"fill" + debugString,
                        columnConstraints:"[right,grow][left][left]",
                        rowConstraints:"[grow]"
                        )

                label(text:"Location:", font:getLabelFont())
                def locationCombo = comboBox(items:m_locations, toolTipText:"Choose your location.", foreground:getForegroundColor(), background:getBackgroundColor(), renderer:getRenderer(), actionPerformed:{
                    m_progressBar.setValue(0)
                    m_progressBar.setString("0%")
                    m_progressBar.setVisible(false)
                    m_passFailPanel.removeAll()
                    m_passFailPanel.updateUI()
                })
                button(text:'Go', font:getLabelFont(), foreground:getBackgroundColor(), background:getDetailColor(), opaque:true, constraints:"wrap", actionPerformed:{
                    if (updateValidation()) {
                        return
                    }

                    m_progressBar.setValue(0)
                    m_progressBar.setStringPainted(true)
                    m_progressBar.setString("0%")
                    m_progressBar.setVisible(true)
                    m_progressBar.updateUI()
                    final ScanReportPollerFrontEnd fe = createPollerFrontEnd()

                    final Map<String,String> metadata = new HashMap<>()
                    for (final Map.Entry<String,JTextField> field : m_metadataFields) {
                        metadata.put(field.getKey(), field.getValue().getText())
                    }
                    fe.setMetadata(metadata)

                    fe.addPropertyChangeListener(this)
                    fe.initialize()
                    fe.register(locationCombo.getSelectedItem())
                })

                m_progressBar = progressBar(borderPainted:false, visible:false, value:0, constraints:"grow, spanx 3, wrap")

                m_passFailPanel = panel(background:getBackgroundColor(), constraints:"center, spanx 3, spany 2, height 200!, grow, wrap") {
                    migLayout(
                            layoutConstraints:"fill" + debugString,
                            columnConstraints:"[center grow,fill]",
                            rowConstraints:"[center grow,fill]"
                            )

                }
                //label(text:"Yes/No", font:getHeaderFont(), constraints:"center, spanx 3, spany 2, height 200!, wrap")
            }
            panel(constraints:"top", opaque:false) {
                migLayout(
                        layoutConstraints:"fill" + debugString,
                        columnConstraints:"[right][left grow,fill, 200::]",
                        rowConstraints:""
                        )

                for (def field : m_metadataFieldNames) {
                    final String key = getFieldKey(field)
                    label(text:field, font:getLabelFont(), constraints:"")
                    def textField = textField(toolTipText:"Enter your " + field.toLowerCase() + ".", columns:25, constraints:"wrap", actionPerformed:updateValidation, focusGained:updateValidation, focusLost:updateValidation)
                    m_metadataFields.put(key, textField)
                }

                errorLabel = label(text:"", visible:false, foreground:Color.RED, constraints:"grow, skip 1, wrap")
            }

            def detailsOpen = false
            def pendingResize = true
            def detailsButton
            def detailsParent
            def detailsPanel

            updateDetails = {
                if (detailsButton == null || detailsParent == null) {
                    return
                }
                def oldDetailsPanel = detailsPanel

                if (detailsOpen) {
                    detailsButton.setText("Details \u25BC")
                    detailsPanel = panel(opaque:false) {
                        migLayout(
                                layoutConstraints:"fill" + debugString,
                                columnConstraints:"[center grow,fill]",
                                rowConstraints:""
                                )

                        def results = m_scanReport == null? [] : m_scanReport.getPollResults()

                        def tab = table(constraints:"grow, wrap", gridColor:getDetailColor()) {
                            tableModel( list : results) {
                                propertyColumn(header:"Service", propertyName:"serviceName", editable:false)
                                propertyColumn(header:"Node", propertyName:"nodeLabel", editable:false)
                                propertyColumn(header:"IP Address", propertyName:"ipAddress", editable:false)
                                propertyColumn(header:"Success", propertyName:"up", preferredWidth:25, editable:false)
                            }
                        }
                        widget(constraints:"dock north, grow, wrap", tab.tableHeader)
                    }
                    detailsPanel.setVisible(false)
                    // println "detailsPanel size = " + detailsPanel.getSize()
                    if (oldDetailsPanel != null) {
                        detailsParent.remove(oldDetailsPanel)
                    }
                    detailsParent.add(detailsPanel)
                } else {
                    detailsButton.setText("Details \u25B2")
                    if (detailsParent != null && detailsPanel != null) {
                        detailsPanel.setVisible(false)
                        detailsPanel.repaint(repaintDelay)
                        detailsParent.remove(detailsPanel)
                        detailsPanel = null
                    }
                }

                pendingResize = true

                def gui = getGui()
                if (gui != null) {
                    repaint()
                } else {
                    detailsParent.repaint(repaintDelay)
                }
            }

            detailsParent = panel(constraints:"bottom, center, spanx 2, shrinky 1000, dock south", opaque:false) {
                migLayout(
                        layoutConstraints:"fill" + debugString,
                        columnConstraints:"[center grow, fill]",
                        rowConstraints:"0[]5[]0"
                        )

                detailsButton = button(text:"Details \u25BC", font:getLabelFont(), foreground:getDetailColor(), background:getBackgroundColor(), opaque:false, border:null, constraints:"wrap", actionPerformed:{
                    detailsOpen = !detailsOpen
                    if (updateDetails != null) {
                        updateDetails()
                    }
                })
            }

            def lastDimension = detailsParent.getSize()
            detailsParent.addComponentListener(new ComponentAdapter() {
                        public void componentResized(final ComponentEvent e) {
                            if (!pendingResize) {
                                return
                            }
                            def newDimension = e.getComponent().getSize()
                            def difference = Math.abs(newDimension.height - lastDimension.height)
                            def guiSize = getWindowSize()
                            def newSize
                            if (newDimension.height > lastDimension.height) {
                                newSize = new Dimension(Double.valueOf(guiSize.width).intValue(), Double.valueOf(guiSize.height + difference).intValue())
                            } else {
                                newSize = new Dimension(Double.valueOf(guiSize.width).intValue(), Double.valueOf(guiSize.height - difference).intValue())
                            }
                            lastDimension = newDimension
                            pendingResize = false

                            if (detailsPanel != null) {
                                detailsPanel.setVisible(true)
                            }
                            setWindowSize(newSize)
                        }
                    })

            if (updateDetails != null) {
                updateDetails()
            }

        }
    }

    public static void main(String[] args) {
        def g = new ScanGui()
        g.createAndShowGui()
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ScanReportProperties.percentageComplete.toString())) {
            def percentComplete = (Double)evt.getNewValue()
            System.out.println("Percent complete: " + (percentComplete * 100));
            def intPercent = new Double(percentComplete * 100).intValue()
            m_progressBar.setValue(intPercent)
            m_progressBar.setString(intPercent + "%")
        } else if (evt.getPropertyName().equals(PollerFrontEndStates.exitNecessary.toString())) {
            def report = (ScanReport)evt.getNewValue()
            System.out.println("Finished scan: " + report)
            def passed = true
            for (final PollResult result : report.getPollResults()) {
                if (!result.getPollStatus().isUp()) {
                    passed = false;
                    break;
                }
            }
            m_passFailPanel.removeAll()
            def svg = new JSVGCanvas()
            //final JSVGComponent svg = new JSVGComponent()
            def uri = this.getClass().getResource(passed? "/passed.svg":"/failed.svg")
            svg.setURI(uri.toString())
            m_passFailPanel.add(svg)
            m_passFailPanel.revalidate()
            m_scanReport = report;

            m_progressBar.updateUI()
            m_passFailPanel.updateUI()
            if (updateDetails != null) {
                updateDetails()
            }
            //getGui().repaint()
        } else {
            System.err.println("Unhandled property change event: " + evt.getPropertyName())
        }
    }
}

