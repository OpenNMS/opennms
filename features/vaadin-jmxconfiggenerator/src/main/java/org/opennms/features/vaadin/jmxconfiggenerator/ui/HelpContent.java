package org.opennms.features.vaadin.jmxconfiggenerator.ui;

import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;

import java.util.Objects;

public class HelpContent implements PopupView.Content {

    private final VerticalLayout layout;

    public HelpContent(UiState uiState) {
        Objects.requireNonNull(uiState);
        if (!uiState.hasUi()) {
            throw new IllegalArgumentException("The provided uiState " + uiState + " does not have a ui.");
        }

        String content = UIHelper.loadContentFromFile(getClass(), String.format("/help/%s.html", uiState.name()));
        content = content.replaceAll("%title%", uiState.getDescription());

        layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setWidth(400, Sizeable.Unit.PIXELS);
        layout.setSpacing(true);
        layout.addComponent(new Label(content, ContentMode.HTML));
    }

    @Override
    public final Component getPopupComponent() {
        return layout;
    }

    @Override
    public final String getMinimizedValueAsHTML() {
        return "";
    }
}
