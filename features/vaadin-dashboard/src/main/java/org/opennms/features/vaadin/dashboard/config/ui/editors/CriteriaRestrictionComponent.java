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

import com.vaadin.ui.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents a component for editing a single restriction of a criteria.
 *
 * @author Christian Pape
 */
public class CriteriaRestrictionComponent extends HorizontalLayout {
    /**
     * the selection field for the restriction
     */
    private final NativeSelect m_restrictionSelect;
    /**
     * the list of components
     */
    private final List<AbstractField> m_componentList = new ArrayList<AbstractField>();
    /**
     * left layout
     */
    private HorizontalLayout m_leftLayout;
    /**
     * right layout used for the plus and minus buttons
     */
    private HorizontalLayout m_rightLayout;
    /**
     * the {@link CriteriaBuilderHelper} used
     */
    private CriteriaBuilderHelper m_criteriaBuilderHelper;

    /**
     * Constructor for creating ne instances of this class.
     *
     * @param criteriaBuilderHelper the {@link CriteriaBuilderHelper} to be used
     * @param restriction           the criteria string
     */
    public CriteriaRestrictionComponent(CriteriaBuilderHelper criteriaBuilderHelper, String restriction) {
        /**
         * Setting the member fields
         */
        m_criteriaBuilderHelper = criteriaBuilderHelper;

        /**
         * Setting up this component
         */
        setWidth(100, Unit.PERCENTAGE);

        m_rightLayout = new HorizontalLayout();
        m_leftLayout = new HorizontalLayout();

        setSpacing(true);

        /**
         * Adding the restriction's select box
         */
        m_restrictionSelect = new NativeSelect();
        m_restrictionSelect.setCaption("Restriction");
        m_restrictionSelect.setNullSelectionAllowed(false);
        m_restrictionSelect.setMultiSelect(false);
        m_restrictionSelect.setNewItemsAllowed(false);
        m_restrictionSelect.setImmediate(true);

        for (CriteriaRestriction criteriaRestriction : CriteriaRestriction.values()) {
            m_restrictionSelect.addItem(criteriaRestriction.name());
        }

        /**
         * Parsing the criteria
         */
        final String arr[] = restriction.split("[(),]+");

        CriteriaRestriction criteriaRestriction = CriteriaRestriction.valueOf(arr[0]);

        m_restrictionSelect.select(criteriaRestriction.toString());

        m_restrictionSelect.addValueChangeListener(new com.vaadin.data.Property.ValueChangeListener() {
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent valueChangeEvent) {
                CriteriaRestriction newCriteriaRestriction = CriteriaRestriction.valueOf(String.valueOf(valueChangeEvent.getProperty().getValue()));
                refreshComponents(newCriteriaRestriction); //, Arrays.copyOfRange(arr, 1, arr.length));
            }
        });

        setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        m_leftLayout.addComponent(m_restrictionSelect);
        m_leftLayout.setSpacing(true);

        refreshComponents(criteriaRestriction, Arrays.copyOfRange(arr, 1, arr.length));

        /**
         * Adding the layouts
         */
        addComponent(m_leftLayout);
        addComponent(m_rightLayout);

        setExpandRatio(m_leftLayout, 3.0f);
        setExpandRatio(m_rightLayout, 1.0f);

        setComponentAlignment(m_leftLayout, Alignment.MIDDLE_LEFT);
        setComponentAlignment(m_rightLayout, Alignment.MIDDLE_RIGHT);
    }

    /**
     * Returns the right layout used for adding buttons.
     *
     * @return the right {@link HorizontalLayout}
     */
    public HorizontalLayout getRightLayout() {
        return m_rightLayout;
    }

    /**
     * This method refreshes the components.
     *
     * @param criteriaRestriction the new {@link CriteriaRestriction}
     */
    private void refreshComponents(CriteriaRestriction criteriaRestriction) {
        refreshComponents(criteriaRestriction, null);
    }

    /**
     * This method refreshes the components.
     *
     * @param criteriaRestriction the new {@link CriteriaRestriction}
     * @param arr                 the values to be set
     */
    private void refreshComponents(CriteriaRestriction criteriaRestriction, String arr[]) {
        for (AbstractComponent abstractComponent : m_componentList) {
            m_leftLayout.removeComponent(abstractComponent);
        }

        m_componentList.clear();

        int i = 0;

        for (CriteriaEntry criteriaEntry : criteriaRestriction.getEntries()) {
            AbstractField abstractField = criteriaEntry.getComponent(m_criteriaBuilderHelper);

            if (arr != null && arr.length > i) {
                abstractField.setValue(arr[i]);
            }

            m_leftLayout.addComponent(abstractField);
            m_componentList.add(abstractField);

            i++;
        }
    }

    /**
     * Returns the {@link String} representation of this restriction.
     *
     * @return the restriction
     */
    public String getRestriction() {
        CriteriaRestriction criteriaRestriction = CriteriaRestriction.valueOf(String.valueOf(m_restrictionSelect.getValue()));

        String criteria = criteriaRestriction.name() + "(";

        boolean first = true;

        for (AbstractField abstractField : m_componentList) {
            if (!first) {
                criteria += ",";
            }

            criteria += abstractField.getValue();

            first = false;
        }

        criteria += ")";

        return criteria;
    }
}
