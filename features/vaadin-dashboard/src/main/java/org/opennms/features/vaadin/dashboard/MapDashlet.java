package org.opennms.features.vaadin.dashboard;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Image;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class MapDashlet extends VerticalLayout {


    public MapDashlet() {
        setCaption("Network disruptions within the last 30 minutes");
        Image image = new Image(null, new ThemeResource("img/map.png"));
        image.setSizeFull();
        addComponent(image);
    }
}
