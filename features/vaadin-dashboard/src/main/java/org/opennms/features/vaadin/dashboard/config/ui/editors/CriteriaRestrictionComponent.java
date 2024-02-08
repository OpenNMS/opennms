/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.dashboard.config.ui.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.AbstractField;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.NativeSelect;

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
    private final List<AbstractField<?>> m_componentList = new ArrayList<AbstractField<?>>();
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
        setMargin(true);

        addAttachListener(new AttachListener() {
            @Override
            public void attach(AttachEvent attachEvent) {
                getUI().getPage().getStyles().add(".criteriaBackground { background:#dddddd; }");
            }
        });

        addStyleName("criteriaBackground");

        /**
         * Adding the restriction's select box
         */
        m_restrictionSelect = new NativeSelect();
        m_restrictionSelect.setCaption("Restriction");
        m_restrictionSelect.setNullSelectionAllowed(false);
        m_restrictionSelect.setMultiSelect(false);
        m_restrictionSelect.setNewItemsAllowed(false);
        m_restrictionSelect.setImmediate(true);
        m_restrictionSelect.setDescription("Restriction selection");

        for (CriteriaRestriction criteriaRestriction : CriteriaRestriction.values()) {
            m_restrictionSelect.addItem(criteriaRestriction.name());
        }

        /**
         * Parsing the criteria
         */
        final String[] arr = restriction.split("[(),]+");

        CriteriaRestriction criteriaRestriction = CriteriaRestriction.valueOf(arr[0]);

        m_restrictionSelect.select(criteriaRestriction.toString());

        m_restrictionSelect.addValueChangeListener((Property.ValueChangeListener) valueChangeEvent -> {
            CriteriaRestriction newCriteriaRestriction = CriteriaRestriction.valueOf(String.valueOf(valueChangeEvent.getProperty().getValue()));
            refreshComponents(newCriteriaRestriction); //, Arrays.copyOfRange(arr, 1, arr.length));
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
    private void refreshComponents(CriteriaRestriction criteriaRestriction, String[] arr) {
        for (AbstractComponent abstractComponent : m_componentList) {
            m_leftLayout.removeComponent(abstractComponent);
        }

        m_componentList.clear();

        int i = 0;

        for (CriteriaEntry criteriaEntry : criteriaRestriction.getEntries()) {
            AbstractField abstractField = criteriaEntry.getComponent(m_criteriaBuilderHelper);

            if (arr != null && arr.length > i) {
                abstractField.setValue(CriteriaBuilderHelper.decode(arr[i]));
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

        for (AbstractField<?> abstractField : m_componentList) {
            if (!first) {
                criteria += ",";
            }

            criteria += CriteriaBuilderHelper.encode(abstractField.getValue().toString());

            first = false;
        }

        criteria += ")";

        return criteria;
    }
}
