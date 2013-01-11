package org.opennms.features.vaadin.nodemaps.ui;

import org.opennms.features.vaadin.nodemaps.gwt.client.VOpenlayersWidget;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.VerticalLayout;

@ClientWidget(value=VOpenlayersWidget.class)
public class OpenlayersWidgetComponent extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        super.paintContent(target);

        target.startTag("nodes");
        target.startTag("foo-1");
        target.addAttribute("latitude", -79.16243f);
        target.addAttribute("longitude", 35.71569f);
        target.endTag("foo-1");
        target.endTag("nodes");
    }
}
