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
package org.opennms.features.vaadin.surveillanceviews.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.config.surveillanceViews.Category;
import org.opennms.netmgt.config.surveillanceViews.ColumnDef;
import org.opennms.netmgt.config.surveillanceViews.Def;
import org.opennms.netmgt.config.surveillanceViews.RowDef;
import org.opennms.netmgt.model.OnmsCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.validator.AbstractStringValidator;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * This class represents the category chooser window for column/row definitions.
 *
 * @author Christian Pape
 */
public class SurveillanceViewConfigurationCategoryWindow extends Window {
    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewConfigurationCategoryWindow.class);

    /**
     * The constructor for instantiating this component.
     *
     * @param surveillanceViewService the surveillance view service to be used.
     * @param defs                    the column/row defs
     * @param def                     the def to be edited
     * @param saveActionListener      the listener for the saving action
     */
    public SurveillanceViewConfigurationCategoryWindow(final SurveillanceViewService surveillanceViewService, final Collection<?> defs, final Def def, final SaveActionListener saveActionListener) {
        /**
         * calling the super constructor
         */
        super("Window title");

        /**
         * Check whether this dialog is for a column or row and alter the window title
         */
        if (def instanceof RowDef) {
            super.setCaption("Row definition");
        } else {
            super.setCaption("Column definition");
        }

        /**
         * Setting the modal and size properties
         */
        setModal(true);
        setClosable(false);
        setResizable(false);
        setWidth(30, Sizeable.Unit.PERCENTAGE);
        setHeight(400, Unit.PIXELS);

        /**
         * Title and refresh seconds
         */
        final TextField labelField = new TextField();
        labelField.setValue(def.getLabel() != null ? def.getLabel() : "");
        labelField.setImmediate(true);
        labelField.setCaption("Label");
        labelField.setDescription("Label of this category");
        labelField.setWidth(100, Unit.PERCENTAGE);

        /**
         * Creating a simple validator for the title field
         */
        labelField.addValidator(new AbstractStringValidator("Please use an unique name for this column/row definition") {
            @Override
            protected boolean isValidValue(String s) {
                if (s == null || s.trim().isEmpty()) {
                    return false;
                }

                /**
                 * check if the name clashes with other defs
                 */
                for (Def defx : (Collection<Def>) defs) {
                    if (defx.getLabel().equals(s)) {
                        if (defx != def) {
                            return false;
                        }
                    }
                }

                return true;
            }
        });

        /**
         * Categories table
         */
        final Table categoriesTable = new Table();
        categoriesTable.setSizeFull();
        categoriesTable.setHeight(250.0f, Unit.PIXELS);
        categoriesTable.setCaption("Categories");
        categoriesTable.setSortEnabled(true);
        categoriesTable.addContainerProperty("name", String.class, "");
        categoriesTable.setColumnHeader("name", "Category");
        categoriesTable.setColumnExpandRatio("Category", 1.0f);
        categoriesTable.setSelectable(true);
        categoriesTable.setMultiSelect(true);

        final List<OnmsCategory> categories = surveillanceViewService.getOnmsCategories();
        final Map<Integer, OnmsCategory> categoriesMap = new HashMap<>();

        for (OnmsCategory onmsCategory : categories) {
            categoriesTable.addItem(new Object[]{onmsCategory.getName()}, onmsCategory.getId());
            categoriesMap.put(onmsCategory.getId(), onmsCategory);
            if (def.containsCategory(onmsCategory.getName())) {
                categoriesTable.select(onmsCategory.getId());
            }
        }

        /**
         * Create form layouts...
         */
        FormLayout baseFormLayout = new FormLayout();
        baseFormLayout.setSizeFull();
        baseFormLayout.setMargin(true);
        baseFormLayout.addComponent(labelField);
        baseFormLayout.addComponent(categoriesTable);

        /**
         * Creating the vertical layout...
         */
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.addComponent(baseFormLayout);
        verticalLayout.setExpandRatio(baseFormLayout, 1.0f);

        /**
         * Using an additional {@link com.vaadin.ui.HorizontalLayout} for layouting the buttons
         */
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setMargin(true);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);

        /**
         * Adding the cancel button...
         */
        Button cancel = new Button("Cancel");
        cancel.setDescription("Cancel editing properties");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        horizontalLayout.addComponent(cancel);
        horizontalLayout.setExpandRatio(cancel, 1);
        horizontalLayout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);

        /**
         * ...and the OK button
         */
        Button ok = new Button("Save");
        ok.setDescription("Save properties and close");

        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Def finalDef = null;

                if (def instanceof RowDef) {
                    finalDef = new RowDef();
                }
                if (def instanceof ColumnDef) {
                    finalDef = new ColumnDef();
                }

                if (finalDef == null) {
                    LOG.warn("unhandled def type: {}", def);
                    return;
                }

                Set<Object> categories = (Set<Object>) categoriesTable.getValue();

                if (!labelField.isValid()) {
                    ((SurveillanceViewsConfigUI) getUI()).notifyMessage("Error", "Please use an unique label for this category", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (categories.isEmpty()) {
                    ((SurveillanceViewsConfigUI) getUI()).notifyMessage("Error", "You must choose at least one surveillance category", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                for (Object object : categories) {
                    Category category = new Category();
                    category.setName(categoriesMap.get(object).getName());
                    finalDef.getCategories().add(category);
                }

                finalDef.setLabel(labelField.getValue());
                saveActionListener.save(finalDef);

                close();
            }
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        horizontalLayout.addComponent(ok);

        verticalLayout.addComponent(horizontalLayout);

        setContent(verticalLayout);
    }

    /**
     * Interface for a listner that will be invoked when OK is clicked
     */
    public static interface SaveActionListener {
        void save(Def def);
    }
}
