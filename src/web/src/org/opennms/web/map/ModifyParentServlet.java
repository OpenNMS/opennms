//
// Copyright (C) 2003 Networked Knowledge Systems, Inc.
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
//      Derek Glidden   <dglidden@opennms.org>
//      http://www.nksi.com/
//
//

package org.opennms.web.map;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.resource.Vault;

/**
 * Update the NODE table in the database to set the NODEPARENTID
 * information to establish Parent-Child relationships between nodes.
 *
 * I'm sure that this is The Wrong Way to do this, but I don't know
 * exactly what The Right Way is.  There doesn't, in fact, seem to be
 * a Right Way.  So I'm doing this.  Which is ugly.  And Wrong.  But
 * it works.  At least I'm using connections from the Vault.
 *
 * @author <A HREF="mailto:dglidden@opennms.org">Derek Glidden</A>
 * @author <A HREF="http://www.nksi.com/">NKSi</A>
 */

public class ModifyParentServlet extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String parentID = request.getParameter("parentID");
        String childID = request.getParameter("childID");

        if(parentID == null || childID == null || childID.equals("0")) {
            // don't need to change anything
            session.setAttribute("message", "No changes made");
            response.sendRedirect("parent.jsp");
        } else {
            try {
                Connection conn = Vault.getDbConnection();
                PreparedStatement stmt = conn.prepareStatement("UPDATE NODE SET NODEPARENTID = ? WHERE NODEID = ?");
                stmt.setInt(1, new Integer(parentID).intValue());
                stmt.setInt(2, new Integer(childID).intValue());
                stmt.executeUpdate();
                stmt.close();

                session.setAttribute("message", "Changes successful");
                Vault.releaseDbConnection(conn);
                response.sendRedirect("parent.jsp");
            } catch(SQLException e) {
                throw new ServletException("SQLException in ModifyParentServlet: " + e.toString());
            } catch(Exception e) {
                throw new ServletException("Exception in ModifyParentServlet: " + e.toString());
            }
        }
    }
}
