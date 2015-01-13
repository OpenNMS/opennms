package org.opennms.features.vaadin.surveillanceviews.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.surveillanceviews.model.Category;
import org.opennms.features.vaadin.surveillanceviews.model.ColumnDef;
import org.opennms.features.vaadin.surveillanceviews.model.RowDef;
import org.opennms.features.vaadin.surveillanceviews.model.SurveillanceViewConfiguration;
import org.opennms.features.vaadin.surveillanceviews.model.View;

import javax.xml.bind.JAXB;
import java.io.File;

@SuppressWarnings("serial")
@Theme("dashboard")
@Title("OpenNMS Surveillance Views")
public class SurveillanceViewsConfigUI extends UI {

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);
        rootLayout.addComponent(new Label("Hello SurveillanceViewsConfigUI"));
        setContent(rootLayout);

        File cfgFile = new File("etc/surveillance-views.xml");

        if (cfgFile.exists()) {
            SurveillanceViewConfiguration surveillanceViewConfiguration = JAXB.unmarshal(cfgFile, SurveillanceViewConfiguration.class);
            for (View view : surveillanceViewConfiguration.getViews()) {
                System.err.print("View: " + view.getName());
                for(ColumnDef columnDef:view.getColumns()) {
                    System.err.println("ColumnDef: "+columnDef.getLabel());
                    for(Category category:columnDef.getCategories()) {
                        System.err.println(category.getName());
                    }
                }
                for(RowDef rowDef:view.getRows()) {
                    System.err.println("RowDef: " + rowDef.getLabel());
                    for(Category category:rowDef.getCategories()) {
                        System.err.println(category.getName());
                    }
                }
            }
        } else {
            System.err.println("BMRHGA: file not found");
        }
    }
}
