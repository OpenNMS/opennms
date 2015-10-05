/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.admin.nodeManagement;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.Util;

import com.google.common.collect.Lists;

/**
 * A servlet that handles managing or unmanaging interfaces and services on a
 * node
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @since 1.8.1
 */
public class ManageNodesServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -4938417809629844445L;

    private static final String UPDATE_SERVICE = "UPDATE ifservices SET status = ? WHERE ipaddr = ? AND nodeID = ? AND serviceid = ?";

    private static final String DELETE_SERVICE_OUTAGES = "DELETE FROM outages WHERE ipaddr = ? AND nodeID = ? AND serviceid = ? AND ifregainedservice IS NULL";

    private static final String INCLUDE_FILE_NAME = "include";

    /** Constant <code>NOTICE_NAME="Email-Reporting"</code> */
    public static final String NOTICE_NAME = "Email-Reporting";

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        try {
            NotificationFactory.init();
        } catch (Throwable e) {
            throw new ServletException("Could not initialize notification factory: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession userSession = request.getSession(false);
        List<ManagedInterface> allNodes = getManagedInterfacesFromSession(userSession);

        // the list of all interfaces marked as managed
        List<String> interfaceList = Lists.newArrayList();
        if(request.getParameterValues("interfaceCheck") != null)
        	interfaceList = Arrays.asList(request.getParameterValues("interfaceCheck"));

        // the list of all services marked as managed
        List<String> serviceList  = Lists.newArrayList();
    	if(request.getParameterValues("serviceCheck") != null)
    		serviceList = Arrays.asList(request.getParameterValues("serviceCheck"));

        // the list of interfaces that need to be put into the URL file
        List<String> addToURL = Lists.newArrayList();
        
        List<String> unmanageInterfacesList = Lists.newArrayList();
        List<String> manageInterfacesList = Lists.newArrayList();

        final DBUtils d = new DBUtils(getClass());
        try {
            Connection connection = DataSourceFactory.getInstance().getConnection();
            d.watch(connection);
            try {
                connection.setAutoCommit(false);
                PreparedStatement stmt = connection.prepareStatement(UPDATE_SERVICE);
                d.watch(stmt);
                PreparedStatement outagesstmt = connection.prepareStatement(DELETE_SERVICE_OUTAGES);
                d.watch(outagesstmt);

                for (ManagedInterface curInterface : allNodes) {
                    String intKey = curInterface.getNodeid() + "-" + curInterface.getAddress();

                    // see if this interface needs added to the url list
                    if (interfaceList.contains(intKey)) {
                        addToURL.add(curInterface.getAddress());
                    }

                    // determine what is managed and unmanged
                    if (interfaceList.contains(intKey) && curInterface.getStatus().equals("unmanaged")) {
                        manageInterfacesList.add(curInterface.getAddress());
                        
                    } else if (!interfaceList.contains(intKey) && curInterface.getStatus().equals("managed")) {
                        unmanageInterfacesList.add(curInterface.getAddress());
                    }

                    List<ManagedService> interfaceServices = curInterface.getServices();

                    for (ManagedService curService : interfaceServices) {
                        String serviceKey = intKey + "-" + curService.getId();

                        if (serviceList.contains(serviceKey) && curService.getStatus().equals("unmanaged")) {
                        	EventBuilder bldr = new EventBuilder(EventConstants.SERVICE_MANAGED_EVENT_UEI, "web ui");
                            bldr.setNodeid(curInterface.getNodeid())
                            	.setInterface(addr(curInterface.getAddress()))
                            	.setService(curService.getName());

                            sendEvent(bldr.getEvent());
                            
                        	stmt.setString(1, "R");

                            stmt.setString(2, curInterface.getAddress());
                            stmt.setInt(3, curInterface.getNodeid());
                            stmt.setInt(4, curService.getId());
                            stmt.executeUpdate();
                            
                            this.log("DEBUG: executing manage service update for " + curInterface.getAddress() + " " + curService.getName());
                        } else if (!serviceList.contains(serviceKey) && curService.getStatus().equals("managed")) {
                            EventBuilder bldr = new EventBuilder(EventConstants.SERVICE_UNMANAGED_EVENT_UEI, "web ui");
                            bldr.setNodeid(curInterface.getNodeid())
                                .setInterface(addr(curInterface.getAddress()))
                                .setService(curService.getName());

                            sendEvent(bldr.getEvent());

                            stmt.setString(1, "S");
                            stmt.setString(2, curInterface.getAddress());
                            stmt.setInt(3, curInterface.getNodeid());
                            stmt.setInt(4, curService.getId());
                            stmt.executeUpdate();
                            
                            outagesstmt.setString(1, curInterface.getAddress());
                            outagesstmt.setInt(2, curInterface.getNodeid());
                            outagesstmt.setInt(3, curService.getId());
                            outagesstmt.executeUpdate();
                            
                            this.log("DEBUG: executed unmanage service update for " + curInterface.getAddress() + " " + curService.getName());
                        }
                    } // end k loop
                    
                    manageInterfaces(manageInterfacesList, ManageState.M);
                    manageInterfaces(unmanageInterfacesList, ManageState.F);
                } // end j loop
                	
            	// update the packages url file
                writeURLFile(addToURL);
                
                connection.commit();
            } finally { // close off the db connection
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
            d.cleanUp();
        }

        // send the event to restart SCM
        sendSCMRestartEvent();

        // forward the request for proper display
        RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/admin/manageNodesFinish.jsp");
        dispatcher.forward(request, response);
    }

    @SuppressWarnings("unchecked")
    private List<ManagedInterface> getManagedInterfacesFromSession(HttpSession userSession) {
        if (userSession == null) {
            return null;
        } else {
            return (List<ManagedInterface>) userSession.getAttribute("listAll.manage.jsp");
        }
    }

    private void manageInterfaces(List<String> interfaces, ManageState state) throws SQLException {
        if(interfaces != null && !interfaces.isEmpty()) {
        	switch(state) {
	        	case M: new UpdateStatement<>()
					        	.update("ipinterface")
					        	.set("isManaged", new StringValue(state.name()))
					        	.where("ipaddr")
					        	.in(interfaces)
					        	.execute();
		    					 break;
	    					
	        	case F:	new UpdateStatement<>()
					        	.update("ipinterface")
					        	.set("isManaged", new StringValue(state.name()))
					        	.where("ipaddr")
					        	.in(interfaces)
					        	.execute();
	    						 break;
	    						 
	    		default: log("Error: Unknown Manage State");
        	}
        }
    }

    /**
     */
    private void sendSCMRestartEvent() throws ServletException {
        EventBuilder bldr = new EventBuilder("uei.opennms.org/internal/restartSCM", "web ui");

        sendEvent(bldr.getEvent());
    }

    /**
     */
    // FIXME: This is totally the wrong place to be doing this.
    // FIXME: This is totally the wrong place to be doing this.
    private void writeURLFile(List<String> interfaceList) throws ServletException {
        String path = System.getProperty("opennms.home") + File.separator + "etc" + File.separator;

        String fileName = path + INCLUDE_FILE_NAME;

        Writer fileWriter = null;
        FileOutputStream fos = null;
        try {
        	fos = new FileOutputStream(fileName);
        	fileWriter = new OutputStreamWriter(fos, "UTF-8");

            for (int i = 0; i < interfaceList.size(); i++) {
                fileWriter.write(interfaceList.get(i) + System.getProperty("line.separator"));
            }

            // write out the file and close
            fileWriter.flush();
        } catch (IOException e) {
            throw new ServletException("Error writing the include url file " + fileName + ": " + e.getMessage(), e);
        } finally {
        	IOUtils.closeQuietly(fileWriter);
        	IOUtils.closeQuietly(fos);
        }
    }

    /**
     */
    private static void sendEvent(Event event) throws ServletException {
        try {
            Util.createEventProxy().send(event);
        } catch (Throwable e) {
            throw new ServletException("Could not send event " + event.getUei(), e);
        }
    }
    
    enum ManageState {
    	F, M
    }
    
    class UpdateStatement<T> { 
    	private List<String> elements = Lists.newArrayList();
    	private final DBUtils dbUtils;
        private Connection connection;
        private Statement statement;
    	
    	public UpdateStatement() throws SQLException { 
    		this.connection = DataSourceFactory.getInstance().getConnection();
    		this.connection.setAutoCommit(true);
    		this.statement = connection.createStatement();
    		this.dbUtils = new DBUtils(getClass());
    		dbUtils.watch(connection);
    	}
    	
    	public UpdateStatement<T> update(String tableName) {
    		elements.add("UPDATE");
    		elements.add(tableName);
    		return this;
    	}
    	
    	public UpdateStatement<T> set(String column, T value) {
    		elements.add("SET");
    		elements.add(column);
    		elements.add("=");
    		elements.add(value.toString());
    		return this;
    	}
    	
    	public UpdateStatement<T> where(String column) {
    		elements.add("WHERE");
    		elements.add(column);
    		return this;
    	}
    	
    	public UpdateStatement<T> in(List<String> inList) {
    		String inValues;
    		
    		if(inList != null && !inList.isEmpty()) {
    			inValues = "'" + StringUtils.join(inList.toArray(), "','") + "'";
    		} else {
    			inValues = "'-1'";
    		}
    		
    		elements.add("IN");
    		elements.add("(");
    		elements.add(inValues);
    		elements.add(")");
    		elements.add(";");
    		return this;
    	}

    	public void execute() throws SQLException {
    		String sql = StringUtils.join(elements.toArray(), " ");
    		statement.execute(sql);
    		statement.close();
    		log("Executed " + sql);
    	}
    }
    
    
	class StringValue {
		private String value;
		
		public StringValue(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return "'" + value.toString() + "'";
		}
    }
    
    class NumberValue {
    	private String value;
    	
    	public NumberValue(String value) {
    		this.value = value;
    	}
    	
		@Override
		public String toString() {
			return value.toString();
		}
    }
}
