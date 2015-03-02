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
package org.opennms.features.vaadin.surveillanceviews.ui;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.opennms.features.vaadin.surveillanceviews.model.Category;
import org.opennms.features.vaadin.surveillanceviews.model.ColumnDef;
import org.opennms.features.vaadin.surveillanceviews.model.Def;
import org.opennms.features.vaadin.surveillanceviews.model.RowDef;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        setHeight(50, Sizeable.Unit.PERCENTAGE);

        /**
         * Title and refresh seconds
         */
        final TextField labelField = new TextField();
        labelField.setValue(def.getLabel());
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
                if ("".equals(s.trim())) {
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
        categoriesTable.setCaption("Categories");
        categoriesTable.setSizeFull();
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
