package org.opennms.feature.reporting.dao.remote;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.opennms.features.reporting.model.Report;

public class DefaultRemoteRepository implements RemoteReportDataProvider {

    public List<Report> getReports() {
        // TODO Auto-generated method stub
        return new ArrayList<Report>();
    }

    public List<Report> getOnlineReports() {
        // TODO Auto-generated method stub
        return new ArrayList<Report>();
    }

    public String getReportService(String id) {
        // TODO Auto-generated method stub
        return "";
    }

    public String getDisplayName(String id) {
        // TODO Auto-generated method stub
        return "";
    }

    public String getEngine(String id) {
        // TODO Auto-generated method stub
        return "";
    }

    public String getTemplateLocation(String id) {
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public InputStream getTemplateStream(String id) {
        // TODO Auto-generated method stub
        return null;
    }

}
