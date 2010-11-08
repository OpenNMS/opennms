package org.opennms.gwt.web.ui.enterprisereporting.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ReportCellRenderer extends Composite {

    private static ReportCellRendererUiBinder uiBinder = GWT.create(ReportCellRendererUiBinder.class);

    interface ReportCellRendererUiBinder extends UiBinder<Widget, ReportCellRenderer> {}
    
    @UiField
    public Label reportName;
    
    @UiField
    public Label reportTemplate;
    
    @UiField
    public Label reportSchedule;
    
    @UiField
    public Label reportFormat;
    
    @UiField
    public Label reportEngine;
    
    public ReportCellRenderer(String name, String template, String schedule, String format, String engine) {
        initWidget(uiBinder.createAndBindUi(this));
        
        reportName.setText(name);
        reportTemplate.setText(template);
        reportSchedule.setText(schedule);
        reportFormat.setText(format);
        reportEngine.setText(engine);
    }

}
