/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.admin.schedule;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.poller.BasicSchedule;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Outages;

/**
 * <p>ScheduleEditorServlet class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ScheduleEditorServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -7117332637559031820L;
    private Map<String, ScheduleOp> m_ops = new HashMap<String, ScheduleOp>();
    private Map<String, SingleMapping> m_maps = new HashMap<String, SingleMapping>();
    private ScheduleOp m_defaultOp;
    private ScheduleMapping m_defaultMapping;
    private String m_defaultView;
    

    public interface ScheduleManager {
        public String getFileName();
        public void setFileName(String fileName);
        public void loadSchedules() throws ServletException;
        public void saveSchedules() throws ServletException;
        public void deleteSchedule(int index) throws ServletException;
        public void addSchedule(BasicSchedule schedule) throws ServletException;
        public void setSchedule(int index, BasicSchedule schedule) throws ServletException;
        public BasicSchedule createSchedule(String name, String type);
        public BasicSchedule getSchedule(int index);
        public BasicSchedule[] getSchedule();
    }
    
    static class OutageManager implements ScheduleManager {

        private Outages m_outages;
        private String m_fileName = null;

        @Override
        public void loadSchedules() throws ServletException {
            if (m_fileName == null) {
                throw new ServletException("Loading from outage factory not implemented yet!");
            } else {
                try {
                    Reader reader = new InputStreamReader(new FileInputStream(m_fileName), "UTF-8");
                    m_outages = CastorUtils.unmarshal(Outages.class, reader);
                    reader.close();
                } catch (MarshalException e) {
                    throw new ServletException("Unable to unmarshal "+m_fileName, e);
                } catch (ValidationException e) {
                    throw new ServletException("Invalid xml in file "+m_fileName, e);
                } catch (FileNotFoundException e) {
                    throw new ServletException("Unable to locate file "+m_fileName, e);
                } catch (IOException e) {
                    throw new ServletException("Error reading file "+m_fileName, e);
                }
            }
        }

        @Override
        public void saveSchedules() throws ServletException {
            if (m_fileName == null) {
                throw new ServletException("Saving to outage factory not implemented yet!");
            } else {
                try {
                    Writer writer = new OutputStreamWriter(new FileOutputStream(m_fileName), "UTF-8");
                    Marshaller.marshal(m_outages, writer);
                    writer.close();
                } catch (MarshalException e) {
                    throw new ServletException("Unable to unmarshal "+m_fileName, e);
                } catch (ValidationException e) {
                    throw new ServletException("Invalid xml in file "+m_fileName, e);
                } catch (FileNotFoundException e) {
                    throw new ServletException("Unable to locate file "+m_fileName, e);
                } catch (IOException e) {
                    throw new ServletException("Error reading file "+m_fileName, e);
                }
            }
        }

        @Override
        public void deleteSchedule(int index) throws ServletException {
            List<Outage> outages = getOutages();
            outages.remove(index);
        }

        private List<Outage> getOutages() {
            return m_outages.getOutageCollection();
        }

        @Override
        public void addSchedule(BasicSchedule schedule) throws ServletException {
            Outage outage = (Outage)schedule;
            m_outages.addOutage(outage);
        }

        @Override
        public void setSchedule(int index, BasicSchedule schedule) throws ServletException {
            m_outages.setOutage(index, (Outage)schedule);
        }

        @Override
        public BasicSchedule createSchedule(String name, String type) {
            Outage outage = new Outage();
            outage.setName(name);
            outage.setType(type);
            return outage;
        }

        @Override
        public BasicSchedule getSchedule(int index) {
            return m_outages.getOutage(index);
        }

        @Override
        public BasicSchedule[] getSchedule() {
            return m_outages.getOutage();
        }

        @Override
        public String getFileName() {
            return m_fileName;
        }

        @Override
        public void setFileName(String fileName) {
            m_fileName = fileName;
        }
        
    }
    
    interface ScheduleOp {
        public String doOp(HttpServletRequest request, HttpServletResponse response, ScheduleMapping map) throws ServletException;
    }
    
    class NewScheduleOp implements ScheduleOp {
        @Override
        public String doOp(HttpServletRequest request, HttpServletResponse response, ScheduleMapping map) throws ServletException {
            ScheduleManager schedMgr = getSchedMgr(request);
            
            
            int schedIndex = WebSecurityUtils.safeParseInt(request.getParameter("scheduleIndex"));
            
            request.getSession().setAttribute("currentSchedIndex", request.getParameter("scheduleIndex"));
            request.getSession().setAttribute("currentSchedule", schedMgr.getSchedule(schedIndex));
            
            return map.get("success");
        }
    }
    
    class EditOp implements ScheduleOp {
        @Override
        public String doOp(HttpServletRequest request, HttpServletResponse response, ScheduleMapping map) throws ServletException {
            ScheduleManager schedMgr = getSchedMgr(request);
            
            int schedIndex = WebSecurityUtils.safeParseInt(request.getParameter("scheduleIndex"));
            
            request.getSession().setAttribute("currentSchedIndex", request.getParameter("scheduleIndex"));
            request.getSession().setAttribute("currentSchedule", schedMgr.getSchedule(schedIndex));
            
            return map.get("success");
        }
    }
    
    class DeleteOp implements ScheduleOp {
        @Override
        public String doOp(HttpServletRequest request, HttpServletResponse response, ScheduleMapping map) throws ServletException {
            ScheduleManager schedMgr = getSchedMgr(request);
            
            // delete the schedule and save
            int schedIndex = WebSecurityUtils.safeParseInt(request.getParameter("scheduleIndex"));
            schedMgr.deleteSchedule(schedIndex);
            schedMgr.saveSchedules();
            
            return map.get("success");
        }
    }
    
    class DisplayOp implements ScheduleOp {
        @Override
        public String doOp(HttpServletRequest request, HttpServletResponse response, ScheduleMapping map) throws ServletException {
            // FIXME: schedMgr isn't used
            //ScheduleManager schedMgr = getSchedMgr(request);
            return map.get("success");
        }
    }
    
    interface ScheduleMapping {
        public String get(String result);
    }
    
    static class SingleMapping implements ScheduleMapping {
        String m_view;
        public SingleMapping(String view) {
            m_view = view;
        }
        @Override
        public String get(String result) {
            return m_view;
        }
    }
    
    /**
     * <p>Constructor for ScheduleEditorServlet.</p>
     */
    public ScheduleEditorServlet() {
        m_defaultOp = new DisplayOp();
        
        // set up operations
        m_ops.put("", m_defaultOp);
        m_ops.put("edit", new EditOp());
        m_ops.put("delete", new DeleteOp());
        m_ops.put("display", new DisplayOp());
        
        // set up mappings
        m_defaultMapping = new SingleMapping("/admin/schedule/displaySchedules.jsp");
        m_maps.put("", new SingleMapping("/admin/schedule/displaySchedules.jsp"));
        m_maps.put("edit", new SingleMapping("/admin/schedule/editSchedule.jsp"));
        
        m_defaultView = "/admin/schedule/displaySchedules.jsp";
       
    }
    
    ScheduleOp getOp(String cmd) {
        
        if (cmd == null) {
            return m_defaultOp;
        }
        
        ScheduleOp op = m_ops.get(cmd);
        if (op == null) {
            throw new IllegalArgumentException("Unrecognized operation "+cmd);
        }
        
        return op;
    }
    
    ScheduleMapping getMap(String cmd) {
        if (cmd == null) {
            return m_defaultMapping;
        }
        ScheduleMapping map = m_maps.get(cmd);
        if (map == null) {
            return m_defaultMapping;
        }
        return map;
    }
    
    void showView(HttpServletRequest request, HttpServletResponse response, String view) throws ServletException, IOException {
        String nextView = view;
        if (nextView == null) {
            nextView = m_defaultView;
        }
        
        // forward the request for proper display
        RequestDispatcher dispatcher = request.getRequestDispatcher(view);
        dispatcher.forward(request, response);

    }
    
    /** {@inheritDoc} */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }
    
    
    /** {@inheritDoc} */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ScheduleOp op = getOp(request.getParameter("op"));
        ScheduleMapping map = getMap(request.getParameter("op"));
        String view = op.doOp(request, response, map);
        showView(request, response, view);
        
    }
    
   

    private ScheduleManager getSchedMgr(HttpServletRequest request) throws ServletException {
        ScheduleManager schedMgr = (ScheduleManager) request.getSession().getAttribute("schedMgr");
        String fileName = request.getParameter("file");
        if (schedMgr == null || (fileName != null && !fileName.equals(schedMgr.getFileName()))) { 
            schedMgr = new OutageManager();
            schedMgr.setFileName(fileName);
            request.getSession().setAttribute("schedMgr", schedMgr);
        }
        schedMgr.loadSchedules();
        return schedMgr;
    }

}
