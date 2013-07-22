/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.dashboard.config.ui.editors;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * This component is used to construct criterias.
 *
 * @author Christian Pape
 */
public class CriteriaBuilderComponent extends Panel {
    /**
     * list of criteria components
     */
    private List<CriteriaRestrictionComponent> m_criteriaRestrictionComponents = new ArrayList<CriteriaRestrictionComponent>();
    /**
     * the {@link CriteriaBuilderHelper} instance
     */
    private CriteriaBuilderHelper m_criteriaBuilderHelper;
    /**
     * basic layout
     */
    private final VerticalLayout m_criteriaLayout = new VerticalLayout();

    /**
     * Constructor used for instantiating new objects.
     *
     * @param criteriaBuilderHelper the {@link CriteriaBuilderHelper} to be used
     * @param criteriaString        the criteria {@link String}
     */
    public CriteriaBuilderComponent(CriteriaBuilderHelper criteriaBuilderHelper, String criteriaString) {
        /**
         * setting the member fields
         */
        m_criteriaBuilderHelper = criteriaBuilderHelper;

        /**
         * setting the caption
         */
        setCaption("Criteria");

        /**
         * parsing the criteria string...
         */
        if (!"".equals(criteriaString) && criteriaString != null) {
            String arr[] = criteriaString.split("\\)\\.");
            for (String criteria : arr) {
                /**
                 * ...and adding components
                 */
                m_criteriaRestrictionComponents.add(new CriteriaRestrictionComponent(criteriaBuilderHelper, criteria));
            }
        }

        /**
         * now build the layout
         */
        renderComponents();

        m_criteriaLayout.setMargin(true);
        m_criteriaLayout.setSpacing(true);

        setSizeFull();

        /**
         * setting the content
         */
        setContent(m_criteriaLayout);
    }

    /**
     * This method updates the criteria components.
     */
    private void renderComponents() {
        m_criteriaLayout.removeAllComponents();

        boolean isFirst = true;

        for (final CriteriaRestrictionComponent criteriaRestrictionComponent : m_criteriaRestrictionComponents) {
            criteriaRestrictionComponent.getRightLayout().removeAllComponents();

            if (!isFirst) {
                Button minusButton = new Button("-");
                minusButton.setStyleName("small");

                minusButton.addClickListener(new Button.ClickListener() {
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        m_criteriaRestrictionComponents.remove(criteriaRestrictionComponent);
                        renderComponents();
                    }
                });

                criteriaRestrictionComponent.getRightLayout().addComponent(minusButton);
                criteriaRestrictionComponent.getRightLayout().setComponentAlignment(minusButton, Alignment.MIDDLE_RIGHT);
            } else {
                Button plusButton = new Button("+");
                plusButton.setStyleName("small");

                plusButton.addClickListener(new Button.ClickListener() {
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        m_criteriaRestrictionComponents.add(new CriteriaRestrictionComponent(m_criteriaBuilderHelper, "Limit(10)"));
                        renderComponents();
                    }
                });

                criteriaRestrictionComponent.getRightLayout().addComponent(plusButton);
                criteriaRestrictionComponent.getRightLayout().setComponentAlignment(plusButton, Alignment.MIDDLE_RIGHT);

                isFirst = false;
            }

            m_criteriaLayout.addComponent(criteriaRestrictionComponent);
        }
    }

    /**
     * This method return the {@link String} representation of the criterias.
     *
     * @return the criteria string
     */
    public String getCriteria() {
        String criterias = "";

        boolean first = true;

        for (CriteriaRestrictionComponent criteriaRestrictionComponent : m_criteriaRestrictionComponents) {
            if (!first) {
                criterias += ".";
            }
            criterias += criteriaRestrictionComponent.getRestriction();
            first = false;
        }

        return criterias;
    }
}
