/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.vaadin.adminpage;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Class responsible for displaying a confirmation dialog in the UI.
 *
 * @author Christian Pape <christian@opennms.org>
 */
public class ConfirmationDialog extends Window {

    /**
     * Callback interface
     */
    public interface ConfirmationDialogCallback {
        void confirmed();

        void cancelled();
    }

    /**
     * Adapter class for callback interface
     */
    public static class ConfirmationDialogCallbackAdapter implements ConfirmationDialogCallback {
        @Override
        public void confirmed() {
        }

        @Override
        public void cancelled() {
        }
    }

    /**
     * Constructor for creating new instances of this class.
     *
     * @param caption     the window's caption
     * @param question    the question to be displayed
     * @param okLabel     the label for the Ok button
     * @param cancelLabel the label for the Cancel button
     * @param callback    the callback instance
     */
    public ConfirmationDialog(final String caption, final String question,
                              final String okLabel, final String cancelLabel, final ConfirmationDialogCallback callback) {
        super(caption);

        /**
         * setting windows properties
         */
        setClosable(false);
        setResizable(false);
        setModal(true);
        setWidth(30, Unit.PERCENTAGE);
        setHeight(20, Unit.PERCENTAGE);

        /**
         * create the buttons...
         */
        Button okButton = new Button(okLabel);
        Button cancelButton = new Button(cancelLabel);

        /**
         * ...and listeners
         */
        okButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                callback.confirmed();
                ConfirmationDialog.this.close();
            }
        });

        cancelButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                callback.cancelled();
                ConfirmationDialog.this.close();
            }
        });

        /**
         * build up the layouts...
         */
        VerticalLayout verticalLayout = new VerticalLayout();

        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);
        verticalLayout.addComponent(new Label(question));

        final HorizontalLayout buttonLayout = new HorizontalLayout();

        buttonLayout.setSpacing(true);
        buttonLayout.addComponent(okButton);
        buttonLayout.addComponent(cancelButton);

        verticalLayout.addComponent(buttonLayout);
        verticalLayout.setHeight(100, Unit.PERCENTAGE);
        verticalLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);

        /**
         * ...and set the content
         */
        setContent(verticalLayout);
    }
}
