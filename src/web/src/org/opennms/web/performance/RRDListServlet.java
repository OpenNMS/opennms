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
//      http://www.blast.com/
//

package org.opennms.web.performance;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.resource.Vault;


/**
 * A servlet that creates a plain text file with the list of RRD files.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class RRDListServlet extends HttpServlet
{
	/** Encapsulates the logic for this servlet. */
	protected PerformanceModel model;


	/**
	* Initializes this servlet by reading the rrdtool-graph properties file.
	*/
	public void init() throws ServletException {
		try {
			this.model = new PerformanceModel( Vault.getHomeDir() );
		}
		catch( Exception e ) {
			throw new ServletException( "Could not initialize the performance model", e );
		}
	}


	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
		response.setContentType( "text/xml" );
		PrintWriter out = response.getWriter();

		//String[][] rrds = this.model.getQueryableRRDs();
		try
		{
			PerformanceModel.QueryableNode[] nodes = this.model.getQueryableNodes();
			for (int i = 0; i < nodes.length; i++)
			{
				if ((nodes[i].nodeLabel != null) && (!nodes[i].nodeLabel.equals("")))
				{
					out.println(nodes[i].nodeLabel + ", " + nodes[i].nodeId);
				}
				else
				{
//
					out.println("&lt;blank&gt; (change this later)");
				}
			}

			out.close();
		}
		catch (java.sql.SQLException e)
		{
			throw new ServletException("An error occurred while trying to search for nodes with performance data: " + e.getLocalizedMessage(), e);
		}
	}
}
