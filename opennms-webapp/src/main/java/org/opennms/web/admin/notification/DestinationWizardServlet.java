//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Aug 03: Change Castor methods clearX -> removeAllX. - dj@opennms.org
// 2007 Jul 23: Add serialVersionUID, comment-out unused fields, and use Java 5 generics to eliminate warnings. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.admin.notification;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.netmgt.config.destinationPaths.Escalate;
import org.opennms.netmgt.config.destinationPaths.Path;
import org.opennms.netmgt.config.destinationPaths.Target;
import org.opennms.web.Util;
import org.opennms.web.WebSecurityUtils;

/**
 * A servlet that handles the data comming in from the destination wizard jsps.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class DestinationWizardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String SOURCE_PAGE_PATHS = "destinationPaths.jsp";

    private String SOURCE_PAGE_OUTLINE = "pathOutline.jsp";

    private String SOURCE_PAGE_TARGETS = "chooseTargets.jsp";

    private String SOURCE_PAGE_INTERVALS = "groupIntervals.jsp";

    private String SOURCE_PAGE_COMMANDS = "chooseCommands.jsp";

    // FIXME: Unused
//    private String SOURCE_PAGE_NAME = "pathName.jsp";
//
//    private String SOURCE_PAGE_ESCALATE_REMOVE = "removeEscalation.jsp";
//
//    private String SOURCE_PAGE_ESCALATE_ADD = "addEscalation.jsp";

    /** {@inheritDoc} */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            DestinationPathFactory.init();
        } catch (MarshalException e1) {
            throw new ServletException("Exception initializing DestinationPatchFactory "+e1.getMessage(), e1);
        } catch (ValidationException e1) {
            throw new ServletException("Exception initializing DestinationPatchFactory "+e1.getMessage(), e1);
        } catch (FileNotFoundException e1) {
            throw new ServletException("Exception initializing DestinationPatchFactory "+e1.getMessage(), e1);
        } catch (IOException e1) {
            throw new ServletException("Exception initializing DestinationPatchFactory "+e1.getMessage(), e1);
        }
        String sourcePage = request.getParameter("sourcePage");
        HttpSession user = request.getSession(true);
        StringBuffer redirectString = new StringBuffer();

        if (sourcePage.equals(SOURCE_PAGE_PATHS)) {
            String action = request.getParameter("userAction");

            if (action.equals("edit")) {
                // get the path that was chosen in the select
                try {
                    Path oldPath = DestinationPathFactory.getInstance().getPath(request.getParameter("paths"));
                    user.setAttribute("oldPath", oldPath);
                    user.setAttribute("oldName", oldPath.getName());

                    // copy the old path into the new path
                    Path newPath = copyPath(oldPath);
                    user.setAttribute("newPath", newPath);

                    redirectString.append(SOURCE_PAGE_OUTLINE);
                } catch (Exception e) {
                    throw new ServletException("Couldn't get path to edit.", e);
                }
            } else if (action.equals("delete")) {
                try {
                    DestinationPathFactory.getInstance().removePath(request.getParameter("paths"));
                    redirectString.append(SOURCE_PAGE_PATHS);
                } catch (Exception e) {
                    throw new ServletException("Couldn't save/reload destination path configuration file.", e);
                }
            } else if (action.equals("new")) {
                Path newPath = new Path();
                user.setAttribute("newPath", newPath);

                redirectString.append(SOURCE_PAGE_OUTLINE);
            }
        } else if (sourcePage.equals(SOURCE_PAGE_OUTLINE)) {
            String action = request.getParameter("userAction");
            Path path = (Path) user.getAttribute("newPath");

            // load all chanagable values from the outline page into the editing
            // path
            saveOutlineForm(path, request);

            if (action.equals("add")) {
                int index = WebSecurityUtils.safeParseInt(request.getParameter("index"));
                Escalate newEscalate = new Escalate();
                path.addEscalate(index, newEscalate);

                Map<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("targetIndex", request.getParameter("index"));
                redirectString.append(SOURCE_PAGE_TARGETS).append(makeQueryString(requestParams));
            } else if (action.equals("remove")) {
                int index = WebSecurityUtils.safeParseInt(request.getParameter("index"));
                removeEscalation(path, index);
                redirectString.append(SOURCE_PAGE_OUTLINE);
            } else if (action.equals("edit")) {
                Map<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("targetIndex", request.getParameter("index"));
                redirectString.append(SOURCE_PAGE_TARGETS).append(makeQueryString(requestParams));
            } else if (action.equals("finish")) {
                String oldName = (String) user.getAttribute("oldName");
                path.setName(request.getParameter("name"));
                path.setInitialDelay(request.getParameter("initialDelay"));

                try {
                    if (oldName != null && !oldName.equals(path.getName())) {
                        // replacing a path with a new name
                        DestinationPathFactory.getInstance().replacePath(oldName, path);
                    } else {
                        DestinationPathFactory.getInstance().addPath(path);
                    }
                } catch (Exception e) {

                    throw new ServletException("Couldn't save/reload destination path configuration file.", e);
                }
                // Must clear out this attribute for later edits
                user.setAttribute("oldName", null);

                redirectString.append(SOURCE_PAGE_PATHS);
            } else if (action.equals("cancel")) {
                redirectString.append(SOURCE_PAGE_PATHS);
            }
        } else if (sourcePage.equals(SOURCE_PAGE_TARGETS)) {
            // compare the list of targets chosen to the existing targets,
            // replacing
            // and creating new targets as necessary
            String userTargets[] = request.getParameterValues("users");
            String groupTargets[] = request.getParameterValues("groups");
            String roleTargets[] = request.getParameterValues("roles");
            String emailTargets[] = request.getParameterValues("emails");

            Path newPath = (Path) user.getAttribute("newPath");
            int index = WebSecurityUtils.safeParseInt(request.getParameter("targetIndex"));
            Target[] existingTargets = null;

            try {
                existingTargets = DestinationPathFactory.getInstance().getTargetList(index, newPath);
            } catch (Exception e) {
                throw new ServletException("Unable to get targets for path " + newPath.getName(), e);
            }

            // remove all the targets from the path or escalation
            if (index == -1) {
                newPath.removeAllTarget();
            } else {
                newPath.getEscalate(index).removeAllTarget();
            }

            // reload the new targets into the path or escalation
            if (userTargets != null) {
                for (int i = 0; i < userTargets.length; i++) {
                    Target target = new Target();
                    target.setName(userTargets[i]);

                    // see if this target already exists
                    for (int j = 0; j < existingTargets.length; j++) {
                        if (userTargets[i].equals(existingTargets[j].getName())) {
                            target = existingTargets[j];
                            break;
                        }
                    }

                    if (index == -1)
                        newPath.addTarget(target);
                    else
                        newPath.getEscalate(index).addTarget(target);
                }
            }

            if (groupTargets != null) {
                for (int k = 0; k < groupTargets.length; k++) {
                    Target target = new Target();
                    target.setName(groupTargets[k]);

                    // see if this target already exists
                    for (int j = 0; j < existingTargets.length; j++) {
                        if (groupTargets[k].equals(existingTargets[j].getName())) {
                            target = existingTargets[j];
                            break;
                        }
                    }

                    if (index == -1)
                        newPath.addTarget(target);
                    else
                        newPath.getEscalate(index).addTarget(target);
                }
            }

            if (roleTargets != null) {
                for (int k = 0; k < roleTargets.length; k++) {
                    Target target = new Target();
                    target.setName(roleTargets[k]);

                    // see if this target already exists
                    for (int j = 0; j < existingTargets.length; j++) {
                        if (roleTargets[k].equals(existingTargets[j].getName())) {
                            target = existingTargets[j];
                            break;
                        }
                    }

                    if (index == -1)
                        newPath.addTarget(target);
                    else
                        newPath.getEscalate(index).addTarget(target);
                }
            }

            if (emailTargets != null) {
                for (int l = 0; l < emailTargets.length; l++) {
                    Target target = new Target();
                    target.setName(emailTargets[l]);
                    target.addCommand("email");

                    // see if this target already exists
                    for (int m = 0; m < existingTargets.length; m++) {
                        if (emailTargets[l].equals(existingTargets[m].getName())) {
                            target = existingTargets[m];
                            break;
                        }
                    }

                    if (index == -1)
                        newPath.addTarget(target);
                    else
                        newPath.getEscalate(index).addTarget(target);
                }
            }

            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("targetIndex", request.getParameter("targetIndex"));
            String redirectPage = request.getParameter("nextPage");
            redirectString.append(redirectPage);
            if (redirectPage.equals(SOURCE_PAGE_INTERVALS)) {
                String[] ignores = { "sourcePage", "nextPage", "users" };
                redirectString.append("?").append(Util.makeQueryString(request, ignores));
            } else {
                redirectString.append(makeQueryString(requestParams));
            }
        } else if (sourcePage.equals(SOURCE_PAGE_INTERVALS)) {
            Path newPath = (Path) user.getAttribute("newPath");
            int index = WebSecurityUtils.safeParseInt(request.getParameter("targetIndex"));
            Target targets[] = null;

            try {
                targets = DestinationPathFactory.getInstance().getTargetList(index, newPath);
            } catch (Exception e) {
                throw new ServletException("Couldn't get target list for path " + newPath.getName(), e);
            }

            for (int i = 0; i < targets.length; i++) {
                String name = targets[i].getName();
                if (request.getParameter(name + "Interval") != null) {
                    targets[i].setInterval(request.getParameter(name + "Interval"));
                }
            }

            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("targetIndex", request.getParameter("targetIndex"));
            redirectString.append(SOURCE_PAGE_COMMANDS).append(makeQueryString(requestParams));
        } else if (sourcePage.equals(SOURCE_PAGE_COMMANDS)) {
            Path newPath = (Path) user.getAttribute("newPath");
            int index = WebSecurityUtils.safeParseInt(request.getParameter("targetIndex"));
            Target targets[] = null;

            try {
                targets = DestinationPathFactory.getInstance().getTargetList(index, newPath);
            } catch (Exception e) {
                throw new ServletException("Couldn't get target list for path " + newPath.getName(), e);
            }

            for (int i = 0; i < targets.length; i++) {
                String name = targets[i].getName();
                // don't overwrite the email target command
                if (targets[i].getName().indexOf("@") == -1) {
                    targets[i].removeAllCommand();
                    String commands[] = request.getParameterValues(name + "Commands");
                    for (int j = 0; j < commands.length; j++) {
                        targets[i].addCommand(commands[j]);
                    }
                }
                String autoNotify[] =  request.getParameterValues(name + "AutoNotify");
                if(autoNotify[0] == null) {
                    autoNotify[0] = "auto";
                }
                targets[i].setAutoNotify(autoNotify[0]);
            }

            redirectString.append(SOURCE_PAGE_OUTLINE);
        }

        response.sendRedirect(redirectString.toString());
    }

    private void saveOutlineForm(Path path, HttpServletRequest request) {
        path.setName(request.getParameter("name"));
        Escalate[] escalations = path.getEscalate();

        for (int i = 0; i < escalations.length; i++) {
            escalations[i].setDelay(request.getParameter("escalate" + i + "Delay"));
        }
    }

    private void removeEscalation(Path path, int index) {
        Escalate escalate = path.getEscalate(index);
        path.removeEscalate(escalate);
    }

    private String makeQueryString(Map map) {
        StringBuffer buffer = new StringBuffer();
        String separator = "?";

        Iterator i = map.keySet().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            buffer.append(separator).append(key).append("=").append(Util.encode((String) map.get(key)));
            separator = "&";
        }

        return buffer.toString();
    }

    // have to copy a path field by field until we get a cloning method in the
    // Castor generated classes
    private Path copyPath(Path oldPath) {
        Path newPath = new Path();

        newPath.setName(oldPath.getName());
        newPath.setInitialDelay(oldPath.getInitialDelay());

        Collection targets = oldPath.getTargetCollection();
        Iterator it = targets.iterator();
        while (it.hasNext()) {
            newPath.addTarget(copyTarget((Target) it.next()));
        }

        Collection escalations = oldPath.getEscalateCollection();
        Iterator ie = escalations.iterator();
        while (ie.hasNext()) {
            Escalate curEscalate = (Escalate) ie.next();
            Escalate newEscalate = new Escalate();
            newEscalate.setDelay(curEscalate.getDelay());

            Collection esTargets = curEscalate.getTargetCollection();
            Iterator iet = esTargets.iterator();
            while (iet.hasNext()) {
                newEscalate.addTarget(copyTarget((Target) iet.next()));
            }

            newPath.addEscalate(newEscalate);
        }

        return newPath;
    }

    private Target copyTarget(Target target) {
        Target newTarget = new Target();

        newTarget.setName(target.getName());
        newTarget.setInterval(target.getInterval());
        newTarget.setAutoNotify(target.getAutoNotify());

        for (int i = 0; i < target.getCommand().length; i++) {
            newTarget.addCommand(target.getCommand()[i]);
        }

        return newTarget;
    }
}
