package org.opennms.dashboard.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class Dashboard implements EntryPoint {

    public void onModuleLoad() {
        final Label label = new Label ( "gwt-maven-plugin Archetype :: Project org.opennms.opennms-webapp-dashboard" );
        RootPanel.get().add( label );

    }

}
