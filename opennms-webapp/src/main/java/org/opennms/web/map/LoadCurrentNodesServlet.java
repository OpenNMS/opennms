package org.opennms.web.map;

/*
 * Created on 8-giu-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.element.DataLinkInterface;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.element.Node;
import org.opennms.web.map.db.Factory;
import org.opennms.web.map.view.Manager;
import org.opennms.web.map.view.VElement;
import org.opennms.web.map.view.VLink;
import org.opennms.web.map.view.VMap;


import java.util.*;

/**
 * @author mmigliore
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LoadCurrentNodesServlet extends HttpServlet {

	private static final String ADDNODES_ACTION = "AddNodes";

	private static final String ADDRANGE_ACTION = "AddRange";

	private static final String ADDMAPS_ACTION = "AddMaps";

	private static final String REFRESH_ACTION = "Refresh";

	private static final String ADDNODES_WITH_NEIG_ACTION = "AddNodesWithNeig";

	private static final String ADDMAPS_WITH_NEIG_ACTION = "AddMapsWithNeig";

	private static final String ADDNODES_NEIG_ACTION = "AddNodesNeig";

	private static final String ADDMAPS_NEIG_ACTION = "AddMapsNeig";

	private static final String DELETENODES_ACTION = "DeleteNodes";

	private static final String DELETEMAPS_ACTION = "DeleteMaps";

	private static final String CLEAR_ACTION = "Clear";

	private static final String LOG4J_CATEGORY = "OpenNMS.Map";

	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		String elems = request.getParameter("elems");
		String action = request.getParameter("action");
		log.debug("Received action="+action+" elems="+elems);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream()));
		String strToSend = "";
		try {
			HttpSession session = request.getSession(false);
			String refreshTime = (String)session.getAttribute("refreshTime");
			int refreshtime = 300; 
			if (refreshTime != null) {
				refreshtime = Integer.parseInt(refreshTime)*60;
			}
			Manager m = new Manager();
			m.startSession();
			//Factory.createDbConnection();
			VMap map = null;
			List velems = new ArrayList();
//			List links = new ArrayList();

			if (session != null) {
				map = (VMap) session.getAttribute("sessionMap");
				if (map != null) {
					strToSend = action + "OK";
					Integer[] nodeids = null;
					String TYPE = VElement.NODE_TYPE;

					boolean actionfound = false;
					
					if (action.equals(ADDNODES_ACTION)) {
						actionfound = true;
						String[] snodeids = elems.split(",");
						nodeids = new Integer[snodeids.length];
						for (int i = 0; i<snodeids.length;i++) {
							nodeids[i] = new Integer(snodeids[i]);
						}
					}

					if (action.equals(ADDRANGE_ACTION)) {
						actionfound = true;
						nodeids = (Integer[]) NetworkElementFactory.getNodeIdsWithIpLike(elems).toArray(new Integer[0]);
					}

					if (action.equals(ADDNODES_NEIG_ACTION)) {
						actionfound = true;
						nodeids = (Integer[]) NetworkElementFactory.getLinkedNodeIdOnNode(Integer.parseInt(elems)).toArray(new Integer[0]);
					}

					if (action.equals(ADDNODES_WITH_NEIG_ACTION)) {
						actionfound = true;
						Set linkednodeids = NetworkElementFactory.getLinkedNodeIdOnNode(Integer.parseInt(elems));
						linkednodeids.add(new Integer(elems));
						nodeids = (Integer[]) linkednodeids.toArray(new Integer[0]);
					} 
					
					if (action.equals(ADDMAPS_ACTION)) {
						actionfound = true;
						TYPE = VElement.MAP_TYPE;
						String[] snodeids = elems.split(",");
						nodeids = new Integer[snodeids.length];
						for (int i = 0; i<snodeids.length;i++) {
							nodeids[i] = new Integer(snodeids[i]);
						}
					}

					// response for addElement
					if (actionfound) {
						log.debug("Before Checking map contains elems");
						
						for (int i = 0; i < nodeids.length; i++) {
							int elemId = nodeids[i].intValue();
							if (map.containsElement(elemId, TYPE)) {
								log.debug("Action: " + action + " . Map Contains Element: " + elemId+TYPE);
								continue;
								
							}
							if (TYPE.equals(VElement.MAP_TYPE) && m.foundLoopOnMaps(map,elemId)) {
								strToSend += "&loopfound" + elemId;
								log.debug("Action: " + action + " . Map " + map.getName()+ "Loop Found On Element: " + elemId+TYPE);
								continue;
							}
							VElement curVElem = m.newElement(map.getId(),
									elemId, TYPE);
							velems.add(curVElem);
						} // end for
						
						log.debug("After Checking map contains elems");
						log.debug("Before RefreshElements");
						velems = m.refreshElements((VElement[]) velems.toArray(new VElement[0]),false);
						log.debug("After RefreshElements");
						log.debug("Before getting/adding links");
						//List vElemLinks = m.getLinks(map.getAllElements());
						if (velems != null) {
							Iterator ite = velems.iterator();
							while (ite.hasNext()) {
								// take the VElement object
								VElement ve = (VElement) ite.next();
								// Get the link between ma objects and new Element
								//List vElemLinks = new ArrayList();
								List vElemLinks = m.getLinksOnElem(map.getAllElements(), ve);
								// add MapElement to Map
								map.addElement(ve);
								// Add correpondant Links to Map
								map.addLinks((VLink[]) vElemLinks
										.toArray(new VLink[0]));
								// Add String to return to client
								strToSend += "&" + ve.getId() + ve.getType() + "+"
										+ ve.getIcon() + "+" + ve.getLabel();
								strToSend += "+" + ve.getRtc() + "+"
										+ ve.getStatus() + "+" + ve.getSeverity();
								// add String to return containing Links
								if (vElemLinks != null) {
									Iterator sub_ite = vElemLinks.iterator();
									while (sub_ite.hasNext()) {
										VLink vl = (VLink) sub_ite.next();
										strToSend += "&" + vl.getFirst().getId()
												+ vl.getFirst().getType() + "+"
												+ vl.getSecond().getId()
												+ vl.getSecond().getType();
									}
								}
							} // end cicle on element found
						}
						
						log.debug("After getting/adding links");
						//end if velement to add
					
					}   // and first if action found	
					
					if (!actionfound) {
						if (action.equals(DELETENODES_ACTION)) {
							actionfound = true;
							TYPE = VElement.NODE_TYPE;
							String[] snodeids = elems.split(",");
							nodeids = new Integer[snodeids.length];
							for (int i = 0; i<snodeids.length;i++) {
								nodeids[i] = new Integer(snodeids[i]);
							}
						}
						
						if (action.equals(DELETEMAPS_ACTION)) {
							actionfound = true;
							TYPE = VElement.MAP_TYPE;
							String[] snodeids = elems.split(",");
							nodeids = new Integer[snodeids.length];
							for (int i = 0; i<snodeids.length;i++) {
								nodeids[i] = new Integer(snodeids[i]);
							}
						}

						if (actionfound) {
	
							for (int i = 0; i < nodeids.length; i++) {
								int elemId = nodeids[i].intValue();
								if (map.containsElement(elemId, TYPE)){
									map.removeLinksOnElementList(elemId,TYPE);
									velems.add(map.removeElement(elemId,TYPE));
									strToSend += "&" + elemId + TYPE;
								}
							}
						} 
					}

					if (action.equals(REFRESH_ACTION)) {
						actionfound = true;

						// First refresh Element objects
						velems = m.refreshElements(map.getAllElements(),true);
						//checks for only changed velements 
						if (velems != null) {
							Iterator ite = velems.iterator();
							while (ite.hasNext()) {
								VElement ve = (VElement) ite.next();
								strToSend += "&" + ve.getId() + ve.getType() + "+"
										+ ve.getIcon() + "+" + ve.getLabel();
								strToSend += "+" + ve.getRtc() + "+"
										+ ve.getStatus() + "+" + ve.getSeverity();
							}
						}

						// Second Refresh Link Object on Map
						// Now is done using a very simple way
						// but really it's slow
						// the alternativ is anyway to analize all 
						// links, 1 against other.
						// So with this solution more traffic
						// less stress on server
						// more work on client
						
						// We are waiting to attempt to mapd
						//map.removeAllLinks();

						// get all links on map
						//List links = null;
						List links = m.getLinks(map.getAllElements());

						// add links to map
						//map.addLinks((VLink[]) links.toArray(new VLink[0]));

						// write to client
						if (links != null) {
							Iterator ite = links.iterator();
							while (ite.hasNext()) {
								VLink vl = (VLink) ite.next();
									strToSend += "&" + vl.getFirst().getId()
									+ vl.getFirst().getType() + "+"
									+ vl.getSecond().getId()
									+ vl.getSecond().getType();
							}
						} 
						
					} 

					if (action.equals(CLEAR_ACTION)) {
						actionfound = true;
						map.removeAllLinks();
						map.removeAllElements();
					}

					if (actionfound) {
						session.setAttribute("sessionMap",map);
					} else {
						throw new Exception("action " + action + " not exists");
					}

				} else {
					throw new Exception("Attribute session sessionMap is null");
				}
			} else {
				throw new Exception("HttpSession not initialized");
			}
			m.endSession();
			//Factory.releaseDbConnection();
		} catch (Exception e) {
			strToSend = action + "Failed";
			log.error("Exception catch " + e);
		} finally {
			bw.write(strToSend);
			bw.close();
			log.info("Sending response to the client '" + strToSend + "'");
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	private String nodeToString(Node node) throws Exception {
		String strToSend = node.getNodeId() + "+" + node.getLabel() + "+";
		DataLinkInterface[] links = NetworkElementFactory
				.getDataLinksOnNode(node.getNodeId());
		if (links != null) {
			for (int i = 0; i < links.length; i++) {
				DataLinkInterface dli = links[i];
				int id = dli.get_nodeparentid();
				strToSend += id + "+";

			}
		}
		return strToSend;
	}
}