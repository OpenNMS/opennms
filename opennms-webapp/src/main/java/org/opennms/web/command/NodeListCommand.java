/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 9, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.web.command;

/**
 * <p>NodeListCommand class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class NodeListCommand {
    private String m_nodename = null;
    private String m_iplike = null;
    private String m_maclike = null;
    private Integer m_service = null;
    private String m_ifAlias = null;
    private String[] m_category1 = null;
    private String[] m_category2 = null;
    private String m_statusViewName = null;
    private String m_statusSite = null;
    private String m_statusRowLabel = null;
    private boolean m_nodesWithOutages = false;
    private boolean m_nodesWithDownAggregateStatus = false;
    private boolean m_listInterfaces = false;
    
    /**
     * <p>setNodename</p>
     *
     * @param nodename a {@link java.lang.String} object.
     */
    public void setNodename(String nodename) {
        m_nodename = nodename;
    }
    /**
     * <p>getNodename</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodename() {
        return m_nodename;
    }
    /**
     * <p>hasNodename</p>
     *
     * @return a boolean.
     */
    public boolean hasNodename() {
        return m_nodename != null;
    }
    
    /**
     * <p>setIplike</p>
     *
     * @param iplike a {@link java.lang.String} object.
     */
    public void setIplike(String iplike) {
        m_iplike = iplike;
    }
    /**
     * <p>getIplike</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIplike() {
        return m_iplike;
    }
    /**
     * <p>hasIplike</p>
     *
     * @return a boolean.
     */
    public boolean hasIplike() {
        return m_iplike != null;
    }
    
    /**
     * <p>setMaclike</p>
     *
     * @param maclike a {@link java.lang.String} object.
     */
    public void setMaclike(String maclike) {
        m_maclike = maclike;
    }
    /**
     * <p>getMaclike</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMaclike() {
        return m_maclike;
    }
    /**
     * <p>hasMaclike</p>
     *
     * @return a boolean.
     */
    public boolean hasMaclike() {
        return m_maclike != null;
    }
    
    /**
     * <p>setService</p>
     *
     * @param service a {@link java.lang.Integer} object.
     */
    public void setService(Integer service) {
        m_service = service;
    }
    /**
     * <p>getService</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getService() {
        return m_service;
    }
    /**
     * <p>hasService</p>
     *
     * @return a boolean.
     */
    public boolean hasService() {
        return m_service != null;
    }
    
    /**
     * <p>setIfAlias</p>
     *
     * @param ifAlias a {@link java.lang.String} object.
     */
    public void setIfAlias(String ifAlias) {
        m_ifAlias = ifAlias;
    }
    /**
     * <p>getIfAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIfAlias() {
        return m_ifAlias;
    }
    /**
     * <p>hasIfAlias</p>
     *
     * @return a boolean.
     */
    public boolean hasIfAlias() {
        return m_ifAlias != null;
    }
        
    /**
     * <p>setCategory1</p>
     *
     * @param category1 an array of {@link java.lang.String} objects.
     */
    public void setCategory1(String[] category1) {
        m_category1 = category1;
    }
    /**
     * <p>getCategory1</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getCategory1() {
        return m_category1;
    }
    /**
     * <p>hasCategory1</p>
     *
     * @return a boolean.
     */
    public boolean hasCategory1() {
        return m_category1 != null && m_category1.length > 0;
    }
    
    /**
     * <p>setCategory2</p>
     *
     * @param category2 an array of {@link java.lang.String} objects.
     */
    public void setCategory2(String[] category2) {
        m_category2 = category2;
    }
    /**
     * <p>getCategory2</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getCategory2() {
        return m_category2;
    }
    /**
     * <p>hasCategory2</p>
     *
     * @return a boolean.
     */
    public boolean hasCategory2() {
        return m_category2 != null && m_category2.length > 0;
    }
    
    /**
     * <p>setStatusViewName</p>
     *
     * @param statusViewName a {@link java.lang.String} object.
     */
    public void setStatusViewName(String statusViewName) {
        m_statusViewName = statusViewName;
    }
    /**
     * <p>getStatusViewName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatusViewName() {
        return m_statusViewName;
    }
    /**
     * <p>hasStatusViewName</p>
     *
     * @return a boolean.
     */
    public boolean hasStatusViewName() {
        return m_statusViewName != null;
    }
    
    /**
     * <p>setStatusSite</p>
     *
     * @param statusSite a {@link java.lang.String} object.
     */
    public void setStatusSite(String statusSite) {
        m_statusSite = statusSite;
    }
    /**
     * <p>getStatusSite</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatusSite() {
        return m_statusSite;
    }
    /**
     * <p>hasStatusSite</p>
     *
     * @return a boolean.
     */
    public boolean hasStatusSite() {
        return m_statusSite != null;
    }

    /**
     * <p>setStatusRowLabel</p>
     *
     * @param statusRowLabel a {@link java.lang.String} object.
     */
    public void setStatusRowLabel(String statusRowLabel) {
        m_statusRowLabel = statusRowLabel;
    }
    /**
     * <p>getStatusRowLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatusRowLabel() {
        return m_statusRowLabel;
    }
    /**
     * <p>hasStatusRowLabel</p>
     *
     * @return a boolean.
     */
    public boolean hasStatusRowLabel() {
        return m_statusRowLabel != null;
    }
    
    /**
     * <p>setNodesWithOutages</p>
     *
     * @param nodesWithOutages a boolean.
     */
    public void setNodesWithOutages(boolean nodesWithOutages) {
        m_nodesWithOutages = nodesWithOutages;
    }
    /**
     * <p>getNodesWithOutages</p>
     *
     * @return a boolean.
     */
    public boolean getNodesWithOutages() {
        return m_nodesWithOutages;
    }
    
    /**
     * <p>setNodesWithDownAggregateStatus</p>
     *
     * @param nodesWithDownAggregateStatus a boolean.
     */
    public void setNodesWithDownAggregateStatus(boolean nodesWithDownAggregateStatus) {
        m_nodesWithDownAggregateStatus = nodesWithDownAggregateStatus;
    }
    /**
     * <p>getNodesWithDownAggregateStatus</p>
     *
     * @return a boolean.
     */
    public boolean getNodesWithDownAggregateStatus() {
        return m_nodesWithDownAggregateStatus;
    }
    
    /**
     * <p>setListInterfaces</p>
     *
     * @param listInterfaces a boolean.
     */
    public void setListInterfaces(boolean listInterfaces) {
        m_listInterfaces = listInterfaces;
    }
    /**
     * <p>getListInterfaces</p>
     *
     * @return a boolean.
     */
    public boolean getListInterfaces() {
        return m_listInterfaces;
    }
}
