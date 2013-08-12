/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.map;

/*
 * Created on 2-Lug-2007
 */
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.map.view.Command;
import org.opennms.web.map.view.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>
 * ExecCommandAjaxController class.
 * </p>
 * 
 * @author antonio this class provides to create pages for executing ping and
 *         traceroute commands with ajax call back
 * @version $Id: $
 * @since 1.8.18
 */
public class ExecCommandAjaxController extends MapsLoggingController {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExecCommandAjaxController.class);

    private Manager manager;

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String id = request.getParameter("id");

        String command = request.getParameter("command");
        
        String address = request.getParameter("address");

        
        response.setBufferSize(0);
        response.setContentType("text/html");
        response.setHeader("pragma", "no-Cache");
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "no-Cache");
        
        final OutputStreamWriter os = new OutputStreamWriter(
                                                             response.getOutputStream(),
                                                             "UTF-8");        
        try {
            final Command p;
            if (id == null) {
                if (command == null )
                    throw new IllegalArgumentException("Command or id is required");
                if (address == null )
                    throw new IllegalArgumentException("Address is required");
                
                if ( NetworkElementFactory.getInstance(getServletContext()).getInterfacesWithIpAddress(address).length == 0 ) {
                    os.write("NOADDRESSINDATABASE");
                } else if (!manager.checkCommandExecution()) {
                    os.write("NOEXECUTIONALLOWED");
                } else {
                    String commandToExec = getCommandToExec(request,command,address);
                    p = new Command(commandToExec);
                    LOG.info("Executing {}", commandToExec);
                    os.write(manager.execCommand(p));
                }
            } else {
                LOG.info("Getting output for id: {}", id);
                p=manager.getCommand(id);
                String s = p.getNextLine();
                if (p.runned() && s == null) {
                    LOG.info("Process ended and no more output for id: {}", id);
                    manager.removeCommand(id);
                    os.write("END");
                } else {
                    if (s == null ) {
                        os.write("BLANCK");
                        LOG.debug("no lines in buffer found");
                    } else {
                        os.write(s);
                        LOG.debug("Got line: {}", s);
                    }
                }
            }
        } catch (Throwable e) {
            LOG.error("An error occourred while executing command.", e);
            os.write("ERROR");
        } finally {
            os.flush();
            os.close();
        }

        return null;
    }

    private String getCommandToExec(HttpServletRequest request, String command, String address) {

        String commandToExec = command;
        int timeOut = 1;
        int numberOfRequest = 10;
        int packetSize = 56;
        String hopAddress = null;

        String numericoutput = request.getParameter("numericOutput");
        if (numericoutput != null && numericoutput.equals("true")) {
            commandToExec = commandToExec + " -n ";
        }

        if (command.equals("ping")) {
            String timeout = request.getParameter("timeOut");
            if (timeout != null)
                timeOut = WebSecurityUtils.safeParseInt(timeout);
            String numberofrequest = request.getParameter("numberOfRequest");
            if (numberofrequest != null)
                numberOfRequest = WebSecurityUtils.safeParseInt(numberofrequest);
            String packetsize = request.getParameter("packetSize");
            if (packetsize != null)
                packetSize = WebSecurityUtils.safeParseInt(packetsize);
            // these are optionals
            String solaris = request.getParameter("solaris");
            if (solaris != null && solaris.equals("true")) {
                commandToExec = commandToExec + " -I " + timeOut + " "
                        + address + " " + packetSize + " " + numberOfRequest;
            } else {
                commandToExec = commandToExec + " -c " + numberOfRequest
                        + " -i " + timeOut + " -s " + packetSize + " "
                        + address;
            }

        } else if (command.equals("traceroute")) {
            hopAddress = request.getParameter("hopAddress");
            if (hopAddress != null) {
                commandToExec = commandToExec + " -g " + hopAddress + " "
                        + address;
            } else {
                commandToExec = commandToExec + " " + address;
            }
        } else if (command.equals("ipmitool")) {
            String ipmiCommand = request.getParameter("ipmiCommand");
            String ipmiUserName = request.getParameter("ipmiUser");
            String ipmiPassword = request.getParameter("ipmiPassword");
            String ipmiProtocol = request.getParameter("ipmiProtocol");

            if (ipmiCommand != null && ipmiUserName != null
                    && ipmiPassword != null) {
                commandToExec = commandToExec + " -I " + ipmiProtocol
                        + " -U " + ipmiUserName + " -P " + ipmiPassword
                        + " -H " + address + " " + ipmiCommand;
            } else
                throw new IllegalStateException(
                                                "IPMITool requires Protocol, Command, Usernane and Password");

        } else {
            throw new IllegalStateException("Command " + command
                    + " not supported.");
        }
        return commandToExec;
    }
    

}
