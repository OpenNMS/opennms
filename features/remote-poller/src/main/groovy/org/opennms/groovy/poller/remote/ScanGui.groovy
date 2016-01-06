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

import java.awt.Color as C
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

import javax.swing.JPanel


class ScanGui extends AbstractGui {
    def m_metadataFields = ['Company Name', 'Shoe Size', 'Hat Size']

    def m_backEnd;

    public ScanGui() {
        super()
    }

    public void setPollerBackEnd(PollerBackEnd pollerBackEnd) {
        m_backEnd = pollerBackEnd;
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

    @Override
    public JPanel getMainPanel() {
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
                comboBox(items:["US", "Europe", "Other"], toolTipText:"Choose your location.", foreground:getForegroundColor(), background:getBackgroundColor(), renderer:getRenderer())
                button(text:'Go', font:getLabelFont(), foreground:getBackgroundColor(), background:getDetailColor(), opaque:true, constraints:"wrap")

                label(text:"Yes/No", font:getHeaderFont(), constraints:"center, spanx 3, spany 2, height 200!, wrap")
            }
            panel(constraints:"top", opaque:false) {
                migLayout(
                        layoutConstraints:"fill" + debugString,
                        columnConstraints:"[right][left grow,fill, 200::]",
                        rowConstraints:""
                        )

                for (def field : m_metadataFields) {
                    label(text:field, font:getLabelFont(), constraints:"")
                    textField(toolTipText:"Enter your " + field.toLowerCase() + ".", columns:25, constraints:"wrap")
                }
            }

            def detailsOpen = false
            def pendingResize = true
            def detailsButton
            def detailsParent
            def detailsPanel

            def updateDetails = {
                if (detailsButton == null || detailsParent == null) {
                    return
                }
                //println "Details button is " + (detailsOpen? "":"not ") + "open."

                if (detailsOpen) {
                    detailsButton.setText("Details \u25BC")
                    detailsPanel = panel(opaque:false) {
                        migLayout(
                                layoutConstraints:"fill" + debugString,
                                columnConstraints:"[center grow]",
                                rowConstraints:""
                                )

                        label(text:"These are the details!", font:getLabelFont())
                    }
                    detailsPanel.setVisible(false)
                    // println "detailsPanel size = " + detailsPanel.getSize()
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

            detailsParent = panel(constraints:"bottom, center, spanx 2, shrink 1000, dock south", opaque:false) {
                migLayout(
                        layoutConstraints:"fill" + debugString,
                        columnConstraints:"[center grow]",
                        rowConstraints:"0[]5[]0"
                        )

                detailsButton = button(text:"Details \u25BC", font:getLabelFont(), foreground:getDetailColor(), background:getBackgroundColor(), opaque:false, border:null, constraints:"wrap", actionPerformed:{
                    detailsOpen = !detailsOpen
                    updateDetails()
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

            updateDetails()

        }
    }

    public static void main(String[] args) {
        def g = new ScanGui()
        g.createAndShowGui()
    }
}

