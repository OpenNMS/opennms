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

package org.opennms.features.vaadin.dashboard.config.ui.editors;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.Collections;
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
    private List<CriteriaRestrictionComponent> m_criteriaRestrictionComponents = new ArrayList<>();
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
            String[] arr = criteriaString.split("\\)\\.");
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
        boolean isLast;

        for (int i = 0; i < m_criteriaRestrictionComponents.size(); i++) {
            final CriteriaRestrictionComponent criteriaRestrictionComponent = m_criteriaRestrictionComponents.get(i);
            final int index = i;

            isLast = (i == m_criteriaRestrictionComponents.size() - 1);

            criteriaRestrictionComponent.getRightLayout().removeAllComponents();

            Button plusButton = new Button();
            plusButton.setStyleName("small");
            plusButton.setIcon(new ThemeResource("../runo/icons/16/document-add.png"));
            plusButton.setDescription("Add a new criteria entry");

            Button minusButton = new Button();
            minusButton.setStyleName("small");
            minusButton.setIcon(new ThemeResource("../runo/icons/16/document-delete.png"));
            minusButton.setDescription("Remove this criteria entry");

            Button upButton = new Button();
            upButton.setStyleName("small");
            upButton.setIcon(new ThemeResource("../runo/icons/16/arrow-up.png"));
            upButton.setDescription("Move this a criteria entry one position up");

            Button downButton = new Button();
            downButton.setStyleName("small");
            downButton.setIcon(new ThemeResource("../runo/icons/16/arrow-down.png"));
            downButton.setDescription("Move this a criteria entry one position down");

            criteriaRestrictionComponent.getRightLayout().addComponent(upButton);
            criteriaRestrictionComponent.getRightLayout().addComponent(downButton);
            criteriaRestrictionComponent.getRightLayout().addComponent(plusButton);
            criteriaRestrictionComponent.getRightLayout().addComponent(minusButton);

            if (m_criteriaRestrictionComponents.size() == 1) {
                minusButton.setEnabled(false);
                upButton.setEnabled(false);
                downButton.setEnabled(false);
            } else {
                if (isFirst) {
                    upButton.setEnabled(false);
                }

                if (isLast) {
                    downButton.setEnabled(false);
                }
            }

            upButton.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    Collections.swap(m_criteriaRestrictionComponents, index, index - 1);
                    renderComponents();
                }
            });

            downButton.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    Collections.swap(m_criteriaRestrictionComponents, index, index + 1);
                    renderComponents();
                }
            });

            minusButton.addClickListener(new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent clickEvent) {
                    m_criteriaRestrictionComponents.remove(criteriaRestrictionComponent);
                    renderComponents();
                }
            });

            plusButton.addClickListener(new Button.ClickListener() {
                public void buttonClick(Button.ClickEvent clickEvent) {
                    m_criteriaRestrictionComponents.add(index + 1, new CriteriaRestrictionComponent(m_criteriaBuilderHelper, "Limit(10)"));
                    renderComponents();
                }
            });

            isFirst = false;

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
