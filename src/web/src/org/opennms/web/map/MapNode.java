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

/**
 * This class stores the information that will be represented on the
 * map we generate.  It's easier to have an intermediary class like
 * this than to try to massage the data directly from our source into
 * the format we need.  This also helps by abstracting the maths
 * needed to figure out where to put everything on the page into this
 * class where its easier to change globally if we want to change how
 * we place things.
 *
 * @author <A HREF="mailto:dglidden@opennms.org">Derek Glidden</A>
 * @author <A HREF="http://www.nksi.com/">NKSi</A>
 */

public class MapNode {

    private int nodeID;
    private int nodeParent;
    private int x;
    private int y;

    private double RTC;

    private String hostname;
    private String iconName;
    private String ipAddress;
    private String status;

    public static int defaultNodeWidth = 100;
    public static int defaultNodeHeight = 90;
    public static int widthBuffer = 10;
    public static int heightBuffer = 20;


    /**
     * constructor
     */

    public MapNode() {
	this.nodeID = -1;
	this.nodeParent = -1;
	this.x = -1;
	this.y = -1;

	this.RTC = 0.0;

	this.hostname = new String();
	this.iconName = new String();
	this.ipAddress = new String();
	this.status = new String();
    }


    /**
     * setters
     */

    public void setNodeID(int id) {
	this.nodeID = id;
    }

    public void setNodeParent(int nodeParent) {
	this.nodeParent = nodeParent;
    }

    public void setX(int x) {
	this.x = x;
    }

    public void setY(int y) {
	this.y = y;
    }

    public void setHostname(String hostname) {
	this.hostname = hostname;
    }

    public void setIconName(String iconName) {
	this.iconName = iconName;
    }

    public void setIPAddress(String ipAddress) {
	this.ipAddress = ipAddress;
    }

    public void setRTC(double rtc) {
	this.RTC = rtc;
    }

    public void setStatus(String status) {
	this.status = status;
    }


    /**
     * getters
     */

    public int getNodeID() {
	return this.nodeID;
    }

    public int getNodeParent() {
	return this.nodeParent;
    }

    public int getX() {
	return this.x;
    }

    public int getY() {
	return this.y;
    }

    public int getBaseX() {
	return (this.x * this.defaultNodeWidth) + (this.x+1 * this.widthBuffer);
    }

    public int getBaseY() {
	return (this.y * this.defaultNodeHeight) + (this.y+1 * this.heightBuffer);
    }

    public int getCenterX() {
	return getBaseX() + ( this.defaultNodeWidth/2 );
    }

    public int getCenterY() {
	return getBaseY() + ( this.defaultNodeHeight/2 );
    }

    public String getLineFromX() {
	return new Integer(getCenterX()).toString();
    }

    public String getLineFromY() {
	return new Integer(getCenterY() + 32).toString();
    }

    public String getLineToX() {
	return new Integer(getCenterX()).toString();
    }

    public String getLineToY() {
	return new Integer(getCenterY() - 30).toString();
    }

    public String getIconX() {
	return new Integer(getCenterX() - 20).toString();
    }

    public String getIconY() {
	return new Integer(getBaseY()).toString();
    }

    public String getIconMinX() {
	return new Integer(getCenterX() - 20).toString();
    }

    public String getIconMinY() {
	return new Integer(getBaseY()).toString();
    }

    public String getIconMaxX() {
	return new Integer(getCenterX() + 20).toString();
    }

    public String getIconMaxY() {
	return new Integer(getBaseY()+40).toString();
    }

    public String getHostnameX() { 
	return new Integer(getCenterX()).toString();
    }

    public String getHostnameY() {
	return new Integer(getBaseY() + 52).toString();
    }

    public String getIPX() {
	return new Integer(getCenterX()).toString();
    }

    public String getIPY() {
	return new Integer(getBaseY() + 64).toString();
    }

    public String getRTCX() {
	return new Integer(getCenterX()).toString();
    }

    public String getRTCY() {
	return new Integer(getBaseY() + 64).toString();
    }

    public String getStatusX() {
	return new Integer(getCenterX()).toString();
    }

    public String getStatusY() {
	return new Integer(getBaseY() + 76).toString();
    }

    public String getHostname() {
	return this.hostname;
    }

    public String getIconName() {
	return this.iconName;
    }

    public String getIPAddress() {
	return this.ipAddress;
    }

    public double getRTC() {
	return this.RTC;
    }

    public String getStatus() {
	return this.status;
    }

}
