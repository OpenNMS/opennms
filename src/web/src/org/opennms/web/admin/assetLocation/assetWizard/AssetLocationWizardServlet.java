//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

package org.opennms.web.admin.assetLocation.assetWizard;

import java.util.Date;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;


import org.opennms.netmgt.config.*;
import org.opennms.netmgt.config.assetLocation.*;
import org.opennms.web.asset.*;
import org.opennms.web.MissingParameterException;

/**
 * A servlet that handles the data comming in from the Asset Location wizard jsps.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo</A>
 * 
 * */
public class AssetLocationWizardServlet extends HttpServlet {

	public static final String SOURCE_PAGE_BUILDING = "building.jsp";
	public static final String SOURCE_PAGE_EDIT_BUILDING = "buildingEdit.jsp";
	public static final String SOURCE_PAGE_ROOM = "room.jsp";
	public static final String SOURCE_PAGE_EDIT_ROOM = "roomEdit.jsp";
	public static final String SOURCE_PAGE_NODE = "node.jsp";
	public static final String SOURCE_PAGE_EDIT_NODE = "nodeEdit.jsp";
	public static final String SOURCE_PAGE_CHOOSE_NODE = "chooseNode.jsp";
	public static final String SOURCE_PAGE_CHOOSE_ROOM = "chooseRoom.jsp";

	protected AssetModel model;

	public void init() throws ServletException {
		this.model = new AssetModel();
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */

	public void doPost(
		HttpServletRequest request,
		HttpServletResponse response)
		throws ServletException, IOException {
		String sourcePage = request.getParameter("sourcePage");
		HttpSession user = request.getSession(true);

		StringBuffer redirectString = new StringBuffer();

		if (sourcePage.equals(SOURCE_PAGE_BUILDING)) {
			String userAction = request.getParameter("userAction");

			if (userAction.equals("delete")) {
				try {
					AssetLocationFactory.getInstance().removeBuilding(
						request.getParameter("building"),
						request.getRemoteUser());
					redirectString.append(SOURCE_PAGE_BUILDING);
				} catch (Exception e) {
					throw new ServletException(
						"Couldn't save/reload Asset Location configuration file.",
						e);
				}
			} else if (
				userAction.equals("edit") || userAction.equals("copy")) {

				Building oldBuild = null;

				try {
					oldBuild =
						AssetLocationFactory.getInstance().getBuilding(
							request.getParameter("building"));
				} catch (Exception e) {
					throw new ServletException(
						"couldn't get a copy of the Building to edit.",
						e);
				}

				Building newBuild = copyBuild(oldBuild);
				user.setAttribute("newBuild", newBuild);
				user.setAttribute("Action", userAction);

				redirectString.append(SOURCE_PAGE_EDIT_BUILDING);
			} else if (userAction.equals("new")) {
				Building newBuild = new Building();
				user.setAttribute("newBuild", newBuild);
				user.setAttribute("Action", userAction);

				redirectString.append(SOURCE_PAGE_EDIT_BUILDING);
			} else if (userAction.equals("addRoom")) {
				Building oldBuild = null;

				try {
					oldBuild =
						AssetLocationFactory.getInstance().getBuilding(
							request.getParameter("building"));
				} catch (Exception e) {
					throw new ServletException(
						"couldn't get a copy of the Building to edit.",
						e);
				}
				user.setAttribute("newBuild", oldBuild);

				redirectString.append(SOURCE_PAGE_ROOM);
			}

		} else if (sourcePage.equals(SOURCE_PAGE_ROOM)) {
			String userAction = request.getParameter("userAction");

			if (userAction.equals("delete")) {
				try {
					AssetLocationFactory.getInstance().removeRoom(
						request.getParameter("building"),
						request.getParameter("room"),
						request.getRemoteUser());
				} catch (Exception e) {
					throw new ServletException(
						"Couldn't save/reload Asset Location configuration file.",
						e);
				}
				Building oldBuild = null;

				try {
					oldBuild =
						AssetLocationFactory.getInstance().getBuilding(
							request.getParameter("building"));
				} catch (Exception e) {
					throw new ServletException(
						"couldn't get a copy of the Building to edit.",
						e);
				}
				user.setAttribute("newBuild", oldBuild);

				redirectString.append(SOURCE_PAGE_ROOM);
			} else if (
				userAction.equals("edit") || userAction.equals("copy")) {
				Room oldRoom = null;

				try {
					oldRoom =
						AssetLocationFactory.getInstance().getRoom(
							request.getParameter("building"),
							request.getParameter("room"));
				} catch (Exception e) {
					throw new ServletException(
						"couldn't get a copy of the Room to edit.",
						e);
				}

				Room newRoom = copyRoom(oldRoom);

				user.setAttribute("newRoom", newRoom);
				user.setAttribute("building", request.getParameter("building"));
				user.setAttribute("Action", userAction);

				redirectString.append(SOURCE_PAGE_EDIT_ROOM);
			} else if (userAction.equals("new")) {
				Room newRoom = new Room();

				user.setAttribute("newRoom", newRoom);
				user.setAttribute("building", request.getParameter("building"));
				user.setAttribute("Action", userAction);

				redirectString.append(SOURCE_PAGE_EDIT_ROOM);
			} else if (userAction.equals("addNode")) {
				Building oldBuild = null;

				try {
					oldBuild =
						AssetLocationFactory.getInstance().getBuilding(
							request.getParameter("building"));
				} catch (Exception e) {
					throw new ServletException(
						"couldn't get a copy of the Building to edit.",
						e);
				}

				Room oldRoom = null;

				try {
					oldRoom =
						AssetLocationFactory.getInstance().getRoom(
							request.getParameter("building"),
							request.getParameter("room"));
				} catch (Exception e) {
					throw new ServletException(
						"couldn't get a copy of the Room to edit.",
						e);
				}

				user.setAttribute("newBuild", oldBuild);
				user.setAttribute("newRoom", oldRoom);

				redirectString.append(SOURCE_PAGE_NODE);
			}

		} else if (sourcePage.equals(SOURCE_PAGE_NODE)) {
			String userAction = request.getParameter("userAction");

			if (userAction.equals("delete")) {

				try {
					removeAsset(Integer.parseInt(request.getParameter("node")));

				} catch (Exception e) {
					throw new ServletException(
						"Couldn't save/reload Asset DB Info.",
						e);
				}

				redirectString.append(SOURCE_PAGE_NODE);
			} else if (userAction.equals("edit")) {

				Asset newAsset = new Asset();
				boolean isNew = false;
				try {
					newAsset =
						this.model.getAsset(
							Integer.parseInt(request.getParameter("node")));
				} catch (Exception e) {
					throw new ServletException(
						"Couldn't save/reload Asset DB Info.",
						e);
				}

				user.setAttribute("newAsset", newAsset);
				user.setAttribute("isNew", String.valueOf(isNew));

				redirectString.append(SOURCE_PAGE_EDIT_NODE);
			} else if (userAction.equals("new")) {

				Asset newAsset = new Asset();
				boolean isNew = false;

				String building = request.getParameter("building");
				String room = request.getParameter("room");
				String node = request.getParameter("newnode");

				int nodeId = Integer.parseInt(node);
				newAsset.setNodeId(nodeId);

				Room newRoom = null;

				try {
					newRoom =
						AssetLocationFactory.getInstance().getRoom(
							building,
							room);
				} catch (Exception e) {
					throw new ServletException(
						"couldn't get a copy of the Room to edit.",
						e);
				}

				Building newBuild = null;

				try {
					newBuild =
						AssetLocationFactory.getInstance().getBuilding(
							building);
				} catch (Exception e) {
					throw new ServletException(
						"couldn't get a copy of the Building to edit.",
						e);
				}

				if (newBuild != null && newRoom != null) {

					newAsset.setAddress1(newBuild.getAddress1());
					newAsset.setAddress2(newBuild.getAddress2());
					newAsset.setCity(newBuild.getCity());
					newAsset.setState(newBuild.getState());
					newAsset.setZip(newBuild.getZIP());
					newAsset.setBuilding(newBuild.getName());
					newAsset.setFloor(newRoom.getFloor());
					newAsset.setRoom(newRoom.getRoomID());

				}

				Asset oldAsset = new Asset();

				AssetModel model = new AssetModel();
				try {
					oldAsset = model.getAsset(nodeId);

				} catch (Exception e) {
					throw new ServletException(
						"couldn't get a copy of the Node to edit.",
						e);
				}

				if (oldAsset == null) {
					isNew = true;
				} else {
					newAsset.setCategory(oldAsset.getCategory());
					newAsset.setCircuitId(oldAsset.getCircuitId());
					newAsset.setPort(oldAsset.getPort());
					newAsset.setRack(oldAsset.getRack());
					newAsset.setSlot(oldAsset.getSlot());
					newAsset.setLastModifiedDate(
						oldAsset.getLastModifiedDate());
					newAsset.setUserLastModified(
						oldAsset.getUserLastModified());
				}

				user.setAttribute("newAsset", newAsset);
				user.setAttribute("isNew", String.valueOf(isNew));

				redirectString.append(SOURCE_PAGE_EDIT_NODE);
			}
		} else if (sourcePage.equals(SOURCE_PAGE_EDIT_BUILDING)) {

			InsertBuilding(request, user);

			redirectString.append(SOURCE_PAGE_BUILDING);
		} else if (sourcePage.equals(SOURCE_PAGE_EDIT_ROOM)) {

			InsertRoom(request, user);
			Building newBuild = null;

			try {
				newBuild =
					AssetLocationFactory.getInstance().getBuilding(
						request.getParameter("building"));
			} catch (Exception e) {
				throw new ServletException(
					"couldn't get a copy of the Building to edit.",
					e);
			}
			user.setAttribute("newBuild", newBuild);

			redirectString.append(SOURCE_PAGE_ROOM);
		} else if (sourcePage.equals(SOURCE_PAGE_EDIT_NODE)) {

			try {
				InsertAsset(request, user);
			} catch (Exception e) {
				throw new ServletException(
					"couldn't get a copy of the Asset to edit.",
					e);
			}

			Building oldBuild = null;

			try {
				oldBuild =
					AssetLocationFactory.getInstance().getBuilding(
						request.getParameter("building"));
			} catch (Exception e) {
				throw new ServletException(
					"couldn't get a copy of the Building to edit.",
					e);
			}

			Room oldRoom = null;

			try {
				oldRoom =
					AssetLocationFactory.getInstance().getRoom(
						request.getParameter("building"),
						request.getParameter("room"));
			} catch (Exception e) {
				throw new ServletException(
					"couldn't get a copy of the Room to edit.",
					e);
			}

			user.setAttribute("newBuild", oldBuild);
			user.setAttribute("newRoom", oldRoom);

			redirectString.append(SOURCE_PAGE_NODE);
		} else if (sourcePage.equals(SOURCE_PAGE_CHOOSE_NODE)) {

			String building = request.getParameter("building");

			Building newBuild = null;

			try {
				newBuild =
					AssetLocationFactory.getInstance().getBuilding(building);
			} catch (Exception e) {
				throw new ServletException(
					"couldn't get a copy of the Building to edit.",
					e);
			}

			user.setAttribute("node", request.getParameter("node"));
			user.setAttribute("newBuild", newBuild);

			redirectString.append(SOURCE_PAGE_CHOOSE_ROOM);
		} else if (sourcePage.equals(SOURCE_PAGE_CHOOSE_ROOM)) {

			Asset newAsset = new Asset();
			boolean isNew = false;

			String building = request.getParameter("building");
			String room = request.getParameter("room");
			String node = request.getParameter("node");

			int nodeId = Integer.parseInt(node);
			newAsset.setNodeId(nodeId);

			Room newRoom = null;

			try {
				newRoom =
					AssetLocationFactory.getInstance().getRoom(building, room);
			} catch (Exception e) {
				throw new ServletException(
					"couldn't get a copy of the Room to edit.",
					e);
			}

			Building newBuild = null;

			try {
				newBuild =
					AssetLocationFactory.getInstance().getBuilding(building);
			} catch (Exception e) {
				throw new ServletException(
					"couldn't get a copy of the Building to edit.",
					e);
			}

			if (newBuild != null && newRoom != null) {

				newAsset.setAddress1(newBuild.getAddress1());
				newAsset.setAddress2(newBuild.getAddress2());
				newAsset.setCity(newBuild.getCity());
				newAsset.setState(newBuild.getState());
				newAsset.setZip(newBuild.getZIP());
				newAsset.setBuilding(newBuild.getName());
				newAsset.setFloor(newRoom.getFloor());
				newAsset.setRoom(newRoom.getRoomID());

			}

			Asset oldAsset = new Asset();

			AssetModel model = new AssetModel();
			try {
				oldAsset = model.getAsset(nodeId);

			} catch (Exception e) {
				throw new ServletException(
					"couldn't get a copy of the Node to edit.",
					e);
			}

			if (oldAsset == null) {
				isNew = true;
			} else {
				newAsset.setCategory(oldAsset.getCategory());
				newAsset.setCircuitId(oldAsset.getCircuitId());
				newAsset.setPort(oldAsset.getPort());
				newAsset.setRack(oldAsset.getRack());
				newAsset.setSlot(oldAsset.getSlot());
				newAsset.setLastModifiedDate(oldAsset.getLastModifiedDate());
				newAsset.setUserLastModified(oldAsset.getUserLastModified());
			}

			user.setAttribute("newAsset", newAsset);
			user.setAttribute("isNew", String.valueOf(isNew));

			redirectString.append(SOURCE_PAGE_EDIT_NODE);
		}

		if (redirectString.toString().equals(""))
			throw new ServletException("no redirect specified for this wizard!");

		response.sendRedirect(redirectString.toString());
	}

	private boolean InsertBuilding(
		HttpServletRequest request,
		HttpSession user)
		throws ServletException, IOException {

		String userAction = request.getParameter("userAction");

		Building newBuild = (Building) user.getAttribute("newBuild");

		String address1 = request.getParameter("address1");
		if (address1 != null && !address1.trim().equals(""))
			newBuild.setAddress1(address1);
		else
			newBuild.setAddress1(null);

		String address2 = request.getParameter("address2");
		if (address2 != null && !address2.trim().equals(""))
			newBuild.setAddress2(address2);
		else
			newBuild.setAddress2(null);

		String City = request.getParameter("city");
		if (City != null && !City.trim().equals(""))
			newBuild.setCity(City);
		else
			newBuild.setCity(null);

		String State = request.getParameter("state");
		if (State != null && !State.trim().equals(""))
			newBuild.setState(State);
		else
			newBuild.setAddress2(null);

		String Zip = request.getParameter("zip");
		if (Zip != null && !Zip.trim().equals(""))
			newBuild.setZIP(Zip);
		else
			newBuild.setZIP(null);

		String oldName = newBuild.getName();

		newBuild.setName(request.getParameter("name"));

		try {
			if (oldName != null && userAction.equals("edit")) {

				AssetLocationFactory.getInstance().replaceBuilding(
					oldName,
					newBuild,
					request.getRemoteUser());
			} else {
				AssetLocationFactory.getInstance().addBuilding(newBuild);
			}
		} catch (Exception e) {
			throw new ServletException(
				"Couldn't save/reload asset location configuration file.",
				e);
		}
		return true;
	}

	private Building copyBuild(Building oldBuild) {
		Building newBuild = new Building();

		newBuild.setName(oldBuild.getName());
		newBuild.setAddress1(oldBuild.getAddress1());
		newBuild.setAddress2(oldBuild.getAddress2());
		newBuild.setCity(oldBuild.getCity());
		newBuild.setState(oldBuild.getState());
		newBuild.setZIP(oldBuild.getZIP());
		Room buildrooms[] = oldBuild.getRoom();
		for (int i = 0; i < buildrooms.length; i++) {
			newBuild.addRoom(i, buildrooms[i]);
		}

		return newBuild;
	}

	private boolean InsertRoom(HttpServletRequest request, HttpSession user)
		throws ServletException, IOException {

		String userAction = request.getParameter("userAction");

		Room newRoom = (Room) user.getAttribute("newRoom");

		String Floor = request.getParameter("floor");
		if (Floor != null && !Floor.trim().equals(""))
			newRoom.setFloor(Floor);
		else
			newRoom.setFloor(null);

		String oldName = newRoom.getRoomID();

		newRoom.setRoomID(request.getParameter("name"));

		try {
			if (oldName != null && userAction.equals("edit")) {
				AssetLocationFactory.getInstance().replaceRoom(
					oldName,
					newRoom,
					request.getParameter("building"),
					request.getRemoteUser());
			} else {
				AssetLocationFactory.getInstance().addRoom(
					newRoom,
					request.getParameter("building"));
			}
		} catch (Exception e) {
			throw new ServletException(
				"Couldn't save/reload asset location configuration file.",
				e);
		}
		return true;
	}

	private Room copyRoom(Room oldRoom) {
		Room newRoom = new Room();

		newRoom.setRoomID(oldRoom.getRoomID());
		newRoom.setFloor(oldRoom.getFloor());

		return newRoom;
	}

	private synchronized void removeAsset(int nodeid)
		throws SQLException, IOException, ClassNotFoundException {

		Asset asset = this.model.getAsset(nodeid);
		if (asset != null) {
			asset.setAddress1(null);
			asset.setAddress2(null);
			asset.setCity(null);
			asset.setState(null);
			asset.setZip(null);
			asset.setBuilding(null);
			asset.setFloor(null);
			asset.setRoom(null);
			asset.setRack(null);
			asset.setSlot(null);
			asset.setPort(null);
			asset.setCircuitId(null);
			AssetLocationFactory.getInstance().modifyAsset(asset);
		}
	}

	private synchronized void InsertAsset(
		HttpServletRequest request,
		HttpSession user)
		throws SQLException, ServletException, IOException {
		String nodeIdString = request.getParameter("node");
		String isNewString = request.getParameter("isnew");

		if (nodeIdString == null) {
			throw new MissingParameterException(
				"node",
				new String[] { "node", "isnew" });
		}

		if (isNewString == null) {
			throw new MissingParameterException(
				"isnew",
				new String[] { "node", "isnew" });
		}

		int nodeId = Integer.parseInt(nodeIdString);
		boolean isNew = Boolean.valueOf(isNewString).booleanValue();

		Asset asset = this.parms2Asset(request, nodeId);

		if (isNew) {
			AssetLocationFactory.getInstance().createAsset(asset);
		} else {
			AssetLocationFactory.getInstance().modifyAsset(asset);
		}

	}

	protected Asset parms2Asset(HttpServletRequest request, int nodeId) {
		Asset asset = new Asset();

		asset.setNodeId(nodeId);
		asset.setCategory(
			this.stripBadCharacters(request.getParameter("category")));
		asset.setCircuitId(
			this.stripBadCharacters(request.getParameter("circuitid")));
		asset.setRack(this.stripBadCharacters(request.getParameter("rack")));
		asset.setSlot(this.stripBadCharacters(request.getParameter("slot")));
		asset.setPort(this.stripBadCharacters(request.getParameter("port")));
		asset.setAddress1(
			this.stripBadCharacters(request.getParameter("address1")));
		asset.setAddress2(
			this.stripBadCharacters(request.getParameter("address2")));
		asset.setCity(this.stripBadCharacters(request.getParameter("city")));
		asset.setState(this.stripBadCharacters(request.getParameter("state")));
		asset.setZip(this.stripBadCharacters(request.getParameter("zip")));
		asset.setBuilding(
			this.stripBadCharacters(request.getParameter("building")));
		asset.setFloor(this.stripBadCharacters(request.getParameter("floor")));
		asset.setRoom(this.stripBadCharacters(request.getParameter("room")));

		asset.setUserLastModified(request.getRemoteUser());
		asset.setLastModifiedDate(new Date());

		return (asset);
	}

	public String stripBadCharacters(String s) {
		if (s != null) {
			s = s.replace('\n', ' ');
			s = s.replace('\f', ' ');
			s = s.replace('\r', ' ');
			s = s.replace(',', ' ');
		}

		return (s);
	}

}
