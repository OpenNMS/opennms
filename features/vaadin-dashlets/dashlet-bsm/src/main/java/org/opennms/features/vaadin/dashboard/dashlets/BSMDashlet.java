/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.dashboard.dashlets;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessServiceDTO;
import org.opennms.netmgt.model.OnmsSeverity;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * This class represents a Alert Dashlet with minimum details.
 *
 * @author Christian Pape
 */
public class BSMDashlet extends AbstractDashlet {
    /**
     * The {@link BusinessServiceManager} used
     */
    private BusinessServiceManager m_businessServiceManager;
    /**
     * boosted value
     */
    private boolean boosted = false;
    /**
     * wallboard layout
     */
    private DashletComponent m_wallboardComponent = null;
    /**
     * dashboard layout
     */
    private DashletComponent m_dashboardComponent = null;
    private final boolean m_filterByName;
    private final boolean m_filterByAttribute;
    private final String m_nameValue;
    private final String m_attributeKey;
    private final String m_attributeValue;
    private final boolean m_filterBySeverity;
    private String m_severityValue;
    private final int m_resultsLimit;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec            the {@link DashletSpec} to be used
     * @param businessServiceManager the {@link BusinessServiceManager} to be used
     */
    public BSMDashlet(String name, DashletSpec dashletSpec, BusinessServiceManager businessServiceManager) {
        super(name, dashletSpec);
        /**
         * Setting the member fields
         */
        m_businessServiceManager = businessServiceManager;

        /**
         * Retrieve the config...
         */

        m_filterByName = BSMConfigHelper.getBooleanForKey(getDashletSpec(), "filterByName");
        m_nameValue = BSMConfigHelper.getStringForKey(getDashletSpec(), "nameValue");
        m_filterByAttribute = BSMConfigHelper.getBooleanForKey(getDashletSpec(), "filterByAttribute");
        m_attributeKey = BSMConfigHelper.getStringForKey(getDashletSpec(), "attributeKey");
        m_attributeValue = BSMConfigHelper.getStringForKey(getDashletSpec(), "attributeValue");
        m_filterBySeverity = BSMConfigHelper.getBooleanForKey(getDashletSpec(), "filterBySeverity");
        m_severityValue = BSMConfigHelper.getStringForKey(getDashletSpec(), "severityValue");
        m_resultsLimit = BSMConfigHelper.getIntForKey(getDashletSpec(), "resultsLimit");
    }

    @Override
    public DashletComponent getWallboardComponent() {
        if (m_wallboardComponent == null) {
            m_wallboardComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setWidth("100%");
                    refresh();
                }

                @Override
                public void refresh() {
                    m_verticalLayout.removeAllComponents();

                    final List<BusinessServiceDTO> serviceDTOs = getDTOs();
                    if (serviceDTOs.isEmpty()) {
                        m_verticalLayout.addComponent(new Label("There are no Business Services with matching criterias found."));
                    } else {
                        for (BusinessServiceDTO eachService : serviceDTOs) {
                            m_verticalLayout.addComponent(createRow(eachService));
                        }
                    }
                    boosted = false;
                }

                @Override
                public Component getComponent() {
                    return m_verticalLayout;
                }
            };
        }
        return m_wallboardComponent;
    }

    @Override
    public DashletComponent getDashboardComponent() {
        if (m_dashboardComponent == null) {
            m_dashboardComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setWidth("100%");
                    refresh();
                }

                @Override
                public void refresh() {
                    m_verticalLayout.removeAllComponents();

                    final List<BusinessServiceDTO> serviceDTOs = getDTOs();
                    if (serviceDTOs.isEmpty()) {
                        m_verticalLayout.addComponent(new Label("There are no Business Services with matching criterias found."));
                    } else {
                        for (BusinessServiceDTO eachService : serviceDTOs) {
                            m_verticalLayout.addComponent(createRow(eachService));
                        }
                    }
                    boosted = false;
                }

                @Override
                public Component getComponent() {
                    return m_verticalLayout;
                }
            };
        }
        return m_dashboardComponent;
    }

    private List<BusinessServiceDTO> getDTOs() {
        Stream<BusinessServiceDTO> s = m_businessServiceManager.findAll().stream();

        if (m_filterByName) {
            s = s.filter(p -> p.getName().matches(m_nameValue));
        }

        if (m_filterByAttribute) {
            s = s.filter(p -> p.getAttributes().containsKey(m_attributeKey) && p.getAttributes().get(m_attributeKey).matches(m_attributeValue));
        }

        if (m_filterBySeverity) {
            s = s.filter(p -> m_businessServiceManager.getOperationalStatusForBusinessService(p.getId()).isGreaterThanOrEqual(OnmsSeverity.get(m_severityValue)));
        }

        return s.sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
                .limit(m_resultsLimit)
                .collect(Collectors.toList());
    }

    private HorizontalLayout createRow(BusinessServiceDTO serviceDTO) {
        HorizontalLayout rowLayout = new HorizontalLayout();
        rowLayout.setSizeFull();
        rowLayout.setSpacing(true);

        final OnmsSeverity severity = m_businessServiceManager.getOperationalStatusForBusinessService(serviceDTO.getId());
        Label nameLabel = new Label(serviceDTO.getName());
        nameLabel.setSizeFull();
        nameLabel.setStyleName("h1");
        nameLabel.addStyleName("bright");
        nameLabel.addStyleName("severity");
        nameLabel.addStyleName(severity.getLabel());

        rowLayout.addComponent(nameLabel);
        return rowLayout;
    }

    @Override
    public boolean isBoosted() {
        return boosted;
    }
}
