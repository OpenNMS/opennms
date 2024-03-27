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
package org.opennms.netmgt.vaadin.core;

import java.util.function.Supplier;

import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * A confirmation dialog.
 */
public class ConfirmationDialog extends Window implements Window.CloseListener, Button.ClickListener {

    /** The action to execute when the ok/cancel button is pressed. */
    public interface Action {
        void execute(ConfirmationDialog window);
    }

    private Action okAction;
    private Action cancelAction;
    private final VerticalLayout layout = new VerticalLayout();
    private final Label label = new Label("", ContentMode.HTML);
    private final Button cancelButton;
    private final Button okButton;

    private boolean okPressed;

    public ConfirmationDialog() {
        this("Continue?", "Do you really want to continue?");
    }

    public ConfirmationDialog(String caption, String description) {
        setCaption(caption);
        setModal(true);
        setResizable(false);
        setClosable(false);
        setWidth(400, Unit.PIXELS);
        setHeight(200, Unit.PIXELS);
        addCloseListener(this);

        okButton = UIHelper.createButton("ok", null, null, this);
        okButton.setId("confirmationDialog.button.ok");
        cancelButton = UIHelper.createButton("cancel", "cancels the current action.", null, this);
        cancelButton.setId("confirmationDialog.button.cancel");
        label.setDescription(description);

        final HorizontalLayout buttonLayout = new HorizontalLayout(okButton, cancelButton);
        buttonLayout.setSpacing(true);

        layout.setSpacing(true);
        layout.setMargin(true);
        layout.setSizeFull();
        layout.addComponent(label);
        layout.addComponent(buttonLayout);
        layout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);

        setContent(layout);
        center();
    }

    public ConfirmationDialog withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    public ConfirmationDialog withDescription(String description) {
        label.setValue(description);
        return this;
    }

    public ConfirmationDialog withDescription(Supplier<String> descriptionSupplier) {
        withDescription(descriptionSupplier.get());
        return this;
    }

    public ConfirmationDialog withOkAction(Action okAction) {
        this.okAction = okAction;
        return this;
    }

    public ConfirmationDialog withCancelButton(boolean cancelButtonVisible) {
        cancelButton.setVisible(cancelButtonVisible);
        return this;
    }

    public ConfirmationDialog withCancelAction(Action cancelAction) {
        this.cancelAction = cancelAction;
        return this;
    }

    public void open() {
        UI.getCurrent().addWindow(this);
    }

    public ConfirmationDialog withOkLabel(String okLabel) {
        okButton.setCaption(okLabel);
        return this;
    }

    public ConfirmationDialog withCancelLabel(String cancelLabel) {
        cancelButton.setCaption(cancelLabel);
        return this;
    }


    @Override
    public void windowClose(CloseEvent e) {
        if (okPressed) {
            if (okAction != null) {
                okAction.execute(this);
            }
        } else {
            if (cancelAction != null) {
                cancelAction.execute(this);
            }
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        okPressed = event.getSource() == okButton;
        close();
    }
}
