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

package org.opennms.features.vaadin.dashboard.ui.dashboard;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import fi.jasoft.dragdroplayouts.DDGridLayout;
import fi.jasoft.dragdroplayouts.client.ui.LayoutDragMode;
import fi.jasoft.dragdroplayouts.events.LayoutBoundTransferable;
import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSelectorAccess;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class implements a portal-like dashboard.
 *
 * @author Christian Pape
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class DashboardBody extends DDGridLayout {
    /**
     * the displayed dashlet components
     */
    Map<Component, DashletComponent> m_displayDashlets;

    /**
     * Default constructor.
     */
    public DashboardBody() {
        setSizeFull();

        setMargin(true);
        setSpacing(true);

        setDragMode(LayoutDragMode.NONE);

        setDropHandler(new DropHandler() {

            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }

            public void drop(DragAndDropEvent event) {
                DDGridLayout.GridLayoutTargetDetails details = (DDGridLayout.GridLayoutTargetDetails) event.getTargetDetails();

                LayoutBoundTransferable transferable = (LayoutBoundTransferable) event.getTransferable();

                int column = details.getOverColumn();
                int row = details.getOverRow();

                Component c = transferable.getComponent();

                if (getComponent(column, row) == null) {
                    removeComponent(c);
                    addComponent(c, column, row);
                    setComponentAlignment(c, Alignment.MIDDLE_CENTER);
                } else {
                    Component componentToBeSwapped = getComponent(column, row);

                    Area area = getComponentArea(c);

                    removeComponent(c);
                    removeComponent(componentToBeSwapped);

                    addComponent(c, column, row);
                    addComponent(componentToBeSwapped, area.getColumn1(), area.getRow1());

                    setComponentAlignment(c, Alignment.MIDDLE_CENTER);
                    setComponentAlignment(componentToBeSwapped, Alignment.MIDDLE_CENTER);
                }
            }
        });

        setShim(false);
    }

    /**
     * Method for retrieving {@link Dashlet} instances for a given {@link DashletSpec}.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     * @return the new {@link Dashlet} instance
     */
    private Dashlet getDashletInstance(DashletSpec dashletSpec) {
        DashletSelector dashletSelector = ((DashletSelectorAccess) getUI()).getDashletSelector();
        return dashletSelector.getDashletFactoryForName(dashletSpec.getDashletName()).newDashletInstance(dashletSpec);
    }

    private boolean suitableForDashboard(DashletSpec dashletSpec) {
        DashletSelector dashletSelector = ((DashletSelectorAccess) getUI()).getDashletSelector();
        return dashletSelector.getDashletFactoryForName(dashletSpec.getDashletName()).isSuitableForDashboard();
    }

    /**
     * This method sets the {@link List} of {@link DashletSpec} instances.
     *
     * @param dashletSpecs the list of {@link DashletSpec} instances
     */
    public void setDashletSpecs(List<DashletSpec> dashletSpecs) {

        m_displayDashlets = new HashMap<Component, DashletComponent>();

        int c = 0;

        List<DashletSpec> dashboardSuitableDashlets = new LinkedList<>();

        if (dashletSpecs.size() == 0) {
            return;
        } else {
            for (DashletSpec dashletSpec : dashletSpecs) {
                if (suitableForDashboard(dashletSpec)) {
                    dashboardSuitableDashlets.add(dashletSpec);
                }
            }
        }

        if (dashboardSuitableDashlets.size() == 0) {
            return;
        }

        int columns = (int) Math.ceil(Math.sqrt(dashboardSuitableDashlets.size()));

        int rows = (int) Math.ceil((double) dashboardSuitableDashlets.size() / (double) columns);

        setColumns(columns);
        setRows(rows);

        int i = 0;

        removeAllComponents();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                if (i < dashboardSuitableDashlets.size()) {
                    Dashlet dashlet = getDashletInstance(dashboardSuitableDashlets.get(i));

                    DashletComponent dashletComponent = dashlet.getDashboardComponent();

                    m_displayDashlets.put(dashletComponent.getComponent(), dashletComponent);

                    dashletComponent.refresh();

                    String caption = dashlet.getName();

                    if (dashlet.getDashletSpec().getTitle() != null) {
                        if (!"".equals(dashlet.getDashletSpec().getTitle())) {
                            caption += ": " + "" + dashlet.getDashletSpec().getTitle();
                        }
                    }

                    addComponent(createPanel(dashletComponent.getComponent(), caption), x, y);

                    i++;
                }
            }
        }
    }

    public void updateAll() {
        for (int y = 0; y < getRows(); y++) {
            for (int x = 0; x < getColumns(); x++) {
                Panel panel = (Panel) getComponent(x, y);
                if (panel != null) {
                    Component component = panel.getContent();

                    if (component != null) {
                        m_displayDashlets.get(component).refresh();
                    }
                }
            }
        }
    }

    private Panel createPanel(Component content, String caption) {
        Panel panel = new Panel();
        panel.setSizeFull();
        panel.setCaption(caption);
        panel.setContent(content);
        panel.addStyleName("novscroll");

        return panel;
    }
}
