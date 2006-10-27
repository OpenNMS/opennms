package org.opennms.web.controller;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.web.svclayer.SimpleWebTable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class ApplicationController extends AbstractController {

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SimpleWebTable webTable = new SimpleWebTable();
        webTable.setTitle("Applications");
        
        webTable.addColumn("", "simpleWebTableHeader");
        webTable.addColumn("", "simpleWebTableHeader");
        webTable.addColumn("Application Name", "simpleWebTableHeader");
        
        webTable.newRow();
        webTable.addCell("Delete", "simpleWebTableRowLabel", "#");
        webTable.addCell("Edit", "simpleWebTableRowLabel", "#");
        webTable.addCell("Big Application 1", "simpleWebTableRowLabel");

        webTable.newRow();
        webTable.addCell("Delete", "simpleWebTableRowLabel", "#");
        webTable.addCell("Edit", "simpleWebTableRowLabel", "#");
        webTable.addCell("Big Application 2", "simpleWebTableRowLabel");
        
        Collection<OnmsApplication> applications = new ArrayList<OnmsApplication>();
        
        applications.add(createApp(1, "Big Application 1"));
        applications.add(createApp(2, "Big Application 2"));
        applications.add(createApp(3, "Big Application 3"));
        
        
        ModelAndView modelAndView = new ModelAndView("/admin/applications", "webTable", webTable);
        modelAndView.addObject("applications", applications);
        return modelAndView;
    }

    private OnmsApplication createApp(int id, String name) {
        OnmsApplication app1 = new OnmsApplication();
        app1.setId(id);
        app1.setName(name);
        return app1;
    }

}
