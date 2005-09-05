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

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.opennms.web.asset.Asset;
import org.opennms.web.asset.AssetModel;
import org.opennms.web.category.CategoryModel;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.element.Node;
import org.opennms.web.outage.OutageModel;
import org.opennms.web.outage.OutageSummary;

/**
 * this class encapsulates the issue of generating a Collection of Nodes and
 * somehow transforming them into an appropriate class that the
 * DocumentGenerator knows how to deal with. (i.e. I can make MapNodes myself,
 * as seen in getTestNodes() or I can get the node data from some external
 * source and convert it into the appropriate context as seen in
 * getOpenNMSNodes()
 * 
 * @author <A HREF="mailto:dglidden@opennms.org">Derek Glidden </A>
 * @author <A HREF="http://www.nksi.com/">NKSi </A>
 */

public class MapNodeFactory {

    /**
     * log a message. for now, just spit to System.err
     */

    private void log(String message) {
        System.err.println(message);
    }

    /**
     * hide how we're really getting our node data
     */

    public Vector getNodes() {
        return getOpenNMSNodes();
    }

    /**
     * get node data from OpenNMS and massage it into the appropriate form for
     * the MapNode class
     */

    public Vector getOpenNMSNodes() {
        Node[] onmsNodes = null;
        OutageModel oModel = new OutageModel();
        Vector nodes = new Vector();
        Vector outages = new Vector();

        try {
            OutageSummary[] summaries = oModel.getCurrentOutageSummaries();
            for (int i = 0; i < summaries.length; i++) {
                OutageSummary summary = summaries[i];
                int nodeId = summary.getNodeId();
                outages.add(new Integer(nodeId));
            }
        } catch (Exception e) {
            log("Exception in NodeFactory.getOpenNMSNodes()");
            log("Exception in OutageModel.getCurrentOutageSummaries()");
            log(e.toString());
        }

        CategoryModel cModel;

        AssetModel aModel = new AssetModel();

        // create and add a rootnode with NodeID of 0 to represent the
        // OpenNMS server. this is kind of ugly and could be changed
        // without disturbing anyone if someone thinks of a better way
        // of representing this "root" node

        MapNode rootNode = new MapNode();
        rootNode.setNodeID(0);
        rootNode.setHostname("OpenNMS");
        rootNode.setNodeParent(-1);
        rootNode.setRTC(100.0);
        rootNode.setStatus("Up");
        rootNode.setIconName("opennms");
        nodes.add(rootNode);

        try {
            onmsNodes = NetworkElementFactory.getAllNodes();
        } catch (SQLException e) {
            log("Exception in NodeFactory.getOpenNMSNodes()");
            log("SQLException in NodeFactory.getOpenNMSNodes()");
            log(e.toString());
        }

        // we're inlining this logic here instead of calling
        // getAsset() for each node, since that generates a lot of
        // database traffic

        Asset[] assetarray = null;
        try {
            assetarray = aModel.getAllAssets();
        } catch (Exception e) {
            log("Exception in NodeFactory.getOpenNMSNodes()");
            log("Exception in AssetMode.getAsset()");
            log(e.toString());
        }

        Hashtable assets = new Hashtable();

        for (int i = 0; i < assetarray.length; i++) {
            Asset a = assetarray[i];
            assets.put(new Integer(a.getNodeId()), a);
        }

        for (int i = 0; i < onmsNodes.length; i++) {
            Node n = onmsNodes[i];
            MapNode mn = new MapNode();
            Asset asset;
            double overallRtcValue = 0.0;
            boolean isNew = false;

            if (assets.containsKey(new Integer(n.getNodeId()))) {
                asset = (Asset) assets.get(new Integer(n.getNodeId()));
            } else {
                asset = new Asset();
                isNew = true;
            }

            try {
                // I wish I could inline this logic too, since this
                // also generates lots of database sessions

                cModel = CategoryModel.getInstance();
                overallRtcValue = cModel.getNodeAvailability(n.getNodeId());
            } catch (Exception e) {
                log("Exception in NodeFactory.getOpenNMSNodes()");
                log("Exception in CategoryModel.getInstance()");
                log(e.toString());
            }

            mn.setNodeID(n.getNodeId());
            mn.setHostname(n.getLabel());
            mn.setNodeParent(n.getNodeParent());
            mn.setRTC(overallRtcValue);

            if (isNew) {
                mn.setIconName("unspecified");
            } else {
                mn.setIconName(asset.getCategory().toLowerCase());
            }
            // mn.setIconName("other");

            if (outages.contains(new Integer(n.getNodeId()))) {
                mn.setStatus("Outage");
            } else {
                mn.setStatus("Up");
            }

            nodes.add(mn);
        }

        return nodes;
    }

    /**
     * generate a bunch of made-up nodes with generic node data in them. This
     * won't really work as expected anymore because the IPAddress display has
     * been replaced with RTC display.
     */

    public Vector getTestNodes() {
        Vector nodes = new Vector();

        MapNode onms = new MapNode();
        onms.setNodeID(0);
        onms.setNodeParent(-1);
        onms.setHostname("opennms");
        onms.setIconName("images/svg/opennms.svg");
        onms.setIPAddress("192.168.1.100");
        onms.setStatus("up");
        nodes.add(onms);

        for (int i = 1; i <= 4; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(0);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/other.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        for (int i = 5; i <= 5; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(1);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/unspecified.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        for (int i = 6; i <= 7; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(2);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/telephony.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        for (int i = 8; i <= 10; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(3);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/server.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        for (int i = 11; i <= 11; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(4);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/server.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        for (int i = 12; i <= 12; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(5);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/server.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        for (int i = 13; i <= 14; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(7);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/server.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        for (int i = 15; i <= 16; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(8);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/server.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        for (int i = 17; i <= 21; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(12);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/infrastructure.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        for (int i = 22; i <= 23; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(13);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/laptop.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        for (int i = 24; i <= 27; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(14);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/printer.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        for (int i = 28; i <= 36; i++) {
            MapNode n = new MapNode();

            n.setNodeID(i);
            n.setNodeParent(16);
            n.setHostname("node" + new Integer(i).toString());
            n.setIconName("images/svg/workstation.svg");
            n.setIPAddress("192.168.1." + new Integer(i).toString());
            n.setStatus("up");

            nodes.add(n);
        }

        return nodes;
    }
}
