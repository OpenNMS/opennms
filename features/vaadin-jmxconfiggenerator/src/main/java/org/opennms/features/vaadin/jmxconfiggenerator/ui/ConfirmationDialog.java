package org.opennms.features.vaadin.jmxconfiggenerator.ui;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

/**
 * Created by mvrueden on 21/07/15.
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
        cancelButton = UIHelper.createButton("cancel", "cancels the current action.", null, this);
        label.setDescription(description);

        final HorizontalLayout buttonLayout = new HorizontalLayout(okButton, cancelButton);
        buttonLayout.setSpacing(true);

        layout.setSpacing(true);
        layout.setMargin(true);
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

    public ConfirmationDialog withOkAction(Action okAction) {
        this.okAction = okAction;
        return this;
    }

    public ConfirmationDialog withCancelAction(Action cancelAction) {
        this.cancelAction = cancelAction;
        return this;
    }

    public void open() {
        getUI().getCurrent().addWindow(this);
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
            okAction.execute(this);
        } else {
            cancelAction.execute(this);
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        okPressed = event.getSource() == okButton;
        close();
    }
}
