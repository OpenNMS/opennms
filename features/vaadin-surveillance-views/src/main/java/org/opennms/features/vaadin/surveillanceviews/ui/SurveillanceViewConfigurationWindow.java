package org.opennms.features.vaadin.surveillanceviews.ui;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.opennms.features.vaadin.surveillanceviews.config.SurveillanceViewProvider;
import org.opennms.features.vaadin.surveillanceviews.model.View;

public class SurveillanceViewConfigurationWindow extends Window {

    public SurveillanceViewConfigurationWindow(final View view) {
        /**
         * Setting the title
         */
        super("Surveillance view configuration");

        /**
         * Setting the modal and size properties
         */
        setModal(true);
        setClosable(false);
        setResizable(false);
        setWidth(80, Sizeable.Unit.PERCENTAGE);
        setHeight(70, Sizeable.Unit.PERCENTAGE);

        /**
         * Title and refresh seconds
         */
        final TextField titleField = new TextField();
        titleField.setValue(view.getName());
        titleField.setImmediate(true);
        titleField.setCaption("Title");
        titleField.setDescription("Title of this surveillance view");
        titleField.setWidth(25, Unit.PERCENTAGE);

        titleField.addValidator(new AbstractStringValidator("Please use an unique name for the surveillance view") {
            @Override
            protected boolean isValidValue(String s) {
                return (!SurveillanceViewProvider.getInstance().containsView(s) || view.getName().equals(s));
            }
        });

        /**
         * Duration field setup, layout and adding listener and validator
         */
        final TextField refreshSecondsField = new TextField();
        refreshSecondsField.setValue(String.valueOf(view.getRefreshSeconds()));
        refreshSecondsField.setImmediate(true);
        refreshSecondsField.setCaption("Refresh seconds");
        refreshSecondsField.setDescription("Refresh duration in seconds");

        refreshSecondsField.addValidator(new AbstractStringValidator("Only numbers allowed here") {
            @Override
            protected boolean isValidValue(String s) {
                try {
                    Integer.parseInt(s);
                } catch (NumberFormatException numberFormatException) {
                    return false;
                }
                return true;
            }
        });

        /**
         * Columns table
         */
        Table columnsTable = new Table();
        columnsTable.setSortEnabled(false);
        columnsTable.addContainerProperty("Column categories", String.class, "Hallo");
        columnsTable.setColumnExpandRatio("Column categories", 1.0f);
        columnsTable.setWidth(25, Unit.PERCENTAGE);

        /**
         * Adding the buttons...
         */
        Button columnsAddButton = new Button("Add");
        columnsAddButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        columnsAddButton.setEnabled(true);
        columnsAddButton.setStyleName("small");
        columnsAddButton.setDescription("Add column category");

        Button columnsRemoveButton = new Button("Remove");
        columnsRemoveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        columnsRemoveButton.setEnabled(true);
        columnsRemoveButton.setStyleName("small");
        columnsRemoveButton.setDescription("Remove column category");

        /**
         * Rows table
         */

        Table rowsTable = new Table();
        rowsTable.setSortEnabled(false);
        rowsTable.addContainerProperty("Row categories", String.class, "Hallo");
        rowsTable.setColumnExpandRatio("Row categories", 1.0f);
        rowsTable.setWidth(25, Unit.PERCENTAGE);

        /**
         * Adding the buttons...
         */
        Button rowsAddButton = new Button("Add");
        rowsAddButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        rowsAddButton.setEnabled(true);
        rowsAddButton.setStyleName("small");
        rowsAddButton.setDescription("Add row category");

        Button rowsRemoveButton = new Button("Remove");
        rowsRemoveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
            }
        });

        rowsTable.setSizeFull();
        columnsTable.setSizeFull();

        rowsRemoveButton.setEnabled(true);
        rowsRemoveButton.setStyleName("small");
        rowsRemoveButton.setDescription("Remove row category");

        /**
         * Create form layouts...
         */
        FormLayout baseFormLayout = new FormLayout();
        baseFormLayout.addComponent(titleField);
        baseFormLayout.addComponent(refreshSecondsField);

        FormLayout columnTableFormLayout = new FormLayout();
        columnTableFormLayout.addComponent(columnsAddButton);
        columnTableFormLayout.addComponent(columnsRemoveButton);

        FormLayout rowTableFormLayout = new FormLayout();
        rowTableFormLayout.addComponent(rowsAddButton);
        rowTableFormLayout.addComponent(rowsRemoveButton);

        /**
         * Adding the different {@link com.vaadin.ui.FormLayout} instances to a {@link com.vaadin.ui.GridLayout}
         */
        baseFormLayout.setMargin(true);
        columnTableFormLayout.setMargin(true);
        rowTableFormLayout.setMargin(true);

        GridLayout gridLayout = new GridLayout();
        gridLayout.setSizeFull();
        gridLayout.setColumns(4);
        gridLayout.setRows(1);
        gridLayout.setMargin(true);

        gridLayout.addComponent(columnsTable);
        gridLayout.addComponent(columnTableFormLayout);
        gridLayout.addComponent(rowsTable);
        gridLayout.addComponent(rowTableFormLayout);

        gridLayout.setColumnExpandRatio(1, 0.5f);
        gridLayout.setColumnExpandRatio(2, 1.0f);
        gridLayout.setColumnExpandRatio(3, 0.5f);
        gridLayout.setColumnExpandRatio(4, 1.0f);

        /**
         * Creating the vertical layout...
         */
        VerticalLayout verticalLayout = new VerticalLayout();

        verticalLayout.addComponent(baseFormLayout);
        verticalLayout.addComponent(gridLayout);

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
                if (titleField.isValid() && refreshSecondsField.isValid()) {

                }
                /*
                for (Map.Entry<String, String> entry : requiredParameters.entrySet()) {
                    String newValue = table.getItem(entry.getKey()).getItemProperty("Value").getValue().toString();
                    dashletSpec.getParameters().put(entry.getKey(), newValue);
                }

                WallboardProvider.getInstance().save();
                ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Properties");
                */
                close();
            }
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        horizontalLayout.addComponent(ok);

        verticalLayout.addComponent(horizontalLayout);

        setContent(verticalLayout);
    }
}
