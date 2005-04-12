//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.web.admin.schedule;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.common.BasicSchedule;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Outages;

public class ScheduleEditorServlet extends HttpServlet {
    
    private ScheduleManager m_schedMgr;

    interface ScheduleManager {
        public void loadSchedules(String fileName) throws ServletException;
        public void saveSchedules(String fileName) throws ServletException;
        public void deleteSchedule(int index) throws ServletException;
        public void addSchedule(BasicSchedule schedule) throws ServletException;
        public void setSchedule(int index, BasicSchedule schedule) throws ServletException;
        public BasicSchedule createSchedule(String name, String type);
        public BasicSchedule getSchedule(int index);
        public BasicSchedule[] getSchedules();
    }
    
    static class OutageManager implements ScheduleManager {

        private Outages m_outages;

        public void loadSchedules(String fileName) throws ServletException {
            if (fileName == null) {
                throw new ServletException("Loading from outage factory not implemented yet!");
            } else {
                try {
                    FileReader reader = new FileReader(fileName);
                    m_outages = (Outages)Unmarshaller.unmarshal(Outages.class, reader);
                    reader.close();
                } catch (MarshalException e) {
                    throw new ServletException("Unable to unmarshal "+fileName, e);
                } catch (ValidationException e) {
                    throw new ServletException("Invalid xml in file "+fileName, e);
                } catch (FileNotFoundException e) {
                    throw new ServletException("Unable to locate file "+fileName, e);
                } catch (IOException e) {
                    throw new ServletException("Error reading file "+fileName, e);
                }
            }
        }

        public void saveSchedules(String fileName) throws ServletException {
            if (fileName == null) {
                throw new ServletException("Saving to outage factory not implemented yet!");
            } else {
                try {
                    FileWriter writer = new FileWriter(fileName);
                    Marshaller.marshal(m_outages, writer);
                    writer.close();
                } catch (MarshalException e) {
                    throw new ServletException("Unable to unmarshal "+fileName, e);
                } catch (ValidationException e) {
                    throw new ServletException("Invalid xml in file "+fileName, e);
                } catch (FileNotFoundException e) {
                    throw new ServletException("Unable to locate file "+fileName, e);
                } catch (IOException e) {
                    throw new ServletException("Error reading file "+fileName, e);
                }
            }
        }

        public void deleteSchedule(int index) throws ServletException {
            ArrayList outages = m_outages.getOutageCollection();
            outages.remove(index);
        }

        public void addSchedule(BasicSchedule schedule) throws ServletException {
            Outage outage = (Outage)schedule;
            m_outages.addOutage(outage);
        }

        public void setSchedule(int index, BasicSchedule schedule) throws ServletException {
            m_outages.setOutage(index, (Outage)schedule);
        }

        public BasicSchedule createSchedule(String name, String type) {
            Outage outage = new Outage();
            outage.setName(name);
            outage.setType(type);
            return outage;
        }

        public BasicSchedule getSchedule(int index) {
            return m_outages.getOutage(index);
        }

        public BasicSchedule[] getSchedules() {
            return m_outages.getOutage();
        }
        
    }
    
    public ScheduleEditorServlet() {
        m_schedMgr = new OutageManager();
    }
    
    

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        

        m_schedMgr.loadSchedules(request.getParameter("file"));
        
        String schedName=request.getParameter("name");
        if (schedName != null) {
            System.err.println("SCHED: "+schedName);
        }

        String cmd=request.getParameter("do");
        if (cmd != null) {
            System.err.println("CMD: "+cmd);
        }
        
        // forward the request for proper display
        RequestDispatcher dispatcher = request.getRequestDispatcher("/admin/schedule/schedule-editor.jsp");
        request.setAttribute("scheduleList", m_schedMgr.getSchedules());
        dispatcher.forward(request, response);

    }

}
