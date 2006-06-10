//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="vulnplugins"
 *     
*/
public class OnmsVulnPlugin extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6665164524726559523L;

    /** identifier field */
    private Integer m_plugInId;

    /** identifier field */
    private Integer m_pluginSubId;

    /** identifier field */
    private String m_name;

    /** identifier field */
    private String m_category;

    /** identifier field */
    private String m_copyright;

    /** identifier field */
    private String m_descr;

    /** identifier field */
    private String m_summary;

    /** identifier field */
    private String m_family;

    /** identifier field */
    private String m_version;

    /** identifier field */
    private String m_cveEntry;

    /** identifier field */
    private String m_md5;

    private Integer m_id;

    /** full constructor */
    public OnmsVulnPlugin(Integer pluginId, Integer pluginSubId, String name, String category, String copyright, String descr, String summary, String family, String version, String cveEntry, String md5) {
        m_plugInId = pluginId;
        m_pluginSubId = pluginSubId;
        m_name = name;
        m_category = category;
        m_copyright = copyright;
        m_descr = descr;
        m_summary = summary;
        m_family = family;
        m_version = version;
        m_cveEntry = cveEntry;
        m_md5 = md5;
    }

    /** default constructor */
    public OnmsVulnPlugin() {
    }
    
    /**
     * Unique identifier for ipInterface.
     * 
     * @hibernate.id generator-class="native" column="id"
     * @hibernate.generator-param name="sequence" value="vlnPlgnNxtId"
     *         
     */
    public Integer getId() {
        return m_id;
    }
    
    public void setId(Integer id) {
        m_id = id;
    }

    

    /** 
     *                @hibernate.property
     *                 column="pluginId"
     *                 length="4"
     *             
     */
    public Integer getPlugInId() {
        return m_plugInId;
    }

    public void setPlugInId(Integer pluginId) {
        m_plugInId = pluginId;
    }

    /** 
     *                @hibernate.property
     *                 column="pluginSubId"
     *                 length="4"
     *             
     */
    public Integer getPluginSubId() {
        return m_pluginSubId;
    }

    public void setPluginSubId(Integer pluginSubId) {
        m_pluginSubId = pluginSubId;
    }

    /** 
     *                @hibernate.property
     *                 column="name"
     *                 length="128"
     *             
     */
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    /** 
     *                @hibernate.property
     *                 column="category"
     *                 length="32"
     *             
     */
    public String getCategory() {
        return m_category;
    }

    public void setCategory(String category) {
        m_category = category;
    }

    /** 
     *                @hibernate.property
     *                 column="copyright"
     *                 length="128"
     *             
     */
    public String getCopyright() {
        return m_copyright;
    }

    public void setCopyright(String copyright) {
        m_copyright = copyright;
    }

    /** 
     *                @hibernate.property
     *                 column="descr"
     *                 length="1024"
     *             
     */
    public String getDescr() {
        return m_descr;
    }

    public void setDescr(String descr) {
        m_descr = descr;
    }

    /** 
     *                @hibernate.property
     *                 column="summary"
     *                 length="256"
     *             
     */
    public String getSummary() {
        return m_summary;
    }

    public void setSummary(String summary) {
        m_summary = summary;
    }

    /** 
     *                @hibernate.property
     *                 column="family"
     *                 length="32"
     *             
     */
    public String getFamily() {
        return m_family;
    }

    public void setFamily(String family) {
        m_family = family;
    }

    /** 
     *                @hibernate.property
     *                 column="version"
     *                 length="32"
     *             
     */
    public String getVersion() {
        return m_version;
    }

    public void setVersion(String version) {
        m_version = version;
    }

    /** 
     *                @hibernate.property
     *                 column="cveEntry"
     *                 length="14"
     *             
     */
    public String getCveEntry() {
        return m_cveEntry;
    }

    public void setCveEntry(String cveEntry) {
        m_cveEntry = cveEntry;
    }

    /** 
     *                @hibernate.property
     *                 column="md5"
     *                 length="32"
     *             
     */
    public String getMd5() {
        return m_md5;
    }

    public void setMd5(String md5) {
        m_md5 = md5;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("pluginId", getPlugInId())
            .append("pluginSubId", getPluginSubId())
            .append("name", getName())
            .append("category", getCategory())
            .append("copyright", getCopyright())
            .append("descr", getDescr())
            .append("summary", getSummary())
            .append("family", getFamily())
            .append("version", getVersion())
            .append("cveEntry", getCveEntry())
            .append("md5", getMd5())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		// TODO Auto-generated method stub
		throw new RuntimeException("visitor method not implemented");

	}

}
