package org.opennms.features.vaadin.dashboard;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class HeaderLayout extends HorizontalLayout {

    public HeaderLayout() {
        addStyleName("header");
        setMargin(true);
        setSpacing(true);
        setWidth("100%");
        Image logo = new Image(null, new ThemeResource("img/logo.png"));
        addComponent(logo);
        setExpandRatio(logo, 1.0f);

        Button dashboardButton = new Button("Dashboard", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().getNavigator().navigateTo("dashboard");
            }
        });


        Button sandwichboardButton = new Button("Sandwich board", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                UI.getCurrent().getNavigator().navigateTo("sandwichboard");
            }
        });

        addComponents(dashboardButton,sandwichboardButton);
        setComponentAlignment(dashboardButton, Alignment.MIDDLE_CENTER);
        setComponentAlignment(sandwichboardButton, Alignment.MIDDLE_CENTER);
    }
}
