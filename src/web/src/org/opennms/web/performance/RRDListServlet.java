//
// Copyright (C) 2001 Oculan Corp.
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
//	Brian Weaver   <weave@opennms.org>
//	http://www.opennms.org/
//
//

package org.opennms.web.performance;

import java.io.*;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.core.resource.Vault;
import org.opennms.web.MissingParameterException;
import org.opennms.web.Util;


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
