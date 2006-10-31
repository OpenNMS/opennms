package org.opennms.web.map;

/*
 * Created on 8-giu-2005
 *
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.element.Node;

/**
 * @author mmigliore
 *
 */
public class LoadNodesServlet extends HttpServlet {

	

	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		String action = request.getParameter("action");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream()));
		String strToSend = action + "OK";
		log.info("Loading nodes");

		Node[] onmsNodes = null;

		try {
			if (action.equals(MapsConstants.LOADNODES_ACTION)) {
				onmsNodes = NetworkElementFactory.getAllNodes();
				for (int i = 0; i < onmsNodes.length; i++) {
					Node n = onmsNodes[i];
						if (i > 0) {
							strToSend += "&";
						}
		
						String nodeStr = n.getNodeId() + "+" + n.getLabel();
						strToSend += nodeStr;
				}
			} else {
				strToSend = MapsConstants.LOADNODES_ACTION + "Failed";
			}
		} catch (SQLException e) {
			log.error(e.toString());
			strToSend = MapsConstants.LOADNODES_ACTION + "Failed";
		}
		bw.write(strToSend);
		bw.close();
		log.info("Sending response to the client '" + strToSend + "'");

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

}