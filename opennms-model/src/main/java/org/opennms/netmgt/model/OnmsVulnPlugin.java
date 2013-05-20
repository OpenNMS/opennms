/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;


/**
 * <p>OnmsVulnPlugin class.</p>
 */
public class OnmsVulnPlugin extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6665164524726559523L;

    /** identifier field */
    private Integer m_pluginId;

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

    /**
     * full constructor
     *
     * @param pluginId a {@link java.lang.Integer} object.
     * @param pluginSubId a {@link java.lang.Integer} object.
     * @param name a {@link java.lang.String} object.
     * @param category a {@link java.lang.String} object.
     * @param copyright a {@link java.lang.String} object.
     * @param descr a {@link java.lang.String} object.
     * @param summary a {@link java.lang.String} object.
     * @param family a {@link java.lang.String} object.
     * @param version a {@link java.lang.String} object.
     * @param cveEntry a {@link java.lang.String} object.
     * @param md5 a {@link java.lang.String} object.
     */
    public OnmsVulnPlugin(Integer pluginId, Integer pluginSubId, String name, String category, String copyright, String descr, String summary, String family, String version, String cveEntry, String md5) {
        m_pluginId = pluginId;
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

    /**
     * default constructor
     */
    public OnmsVulnPlugin() {
    }
    
    /**
     * Unique identifier for ipInterface.
     *
     * @hibernate.id generator-class="native" column="id"
     * @hibernate.generator-param name="sequence" value="vlnPlgnNxtId"
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getId() {
        return m_id;
    }
    
    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }

    

    /**
     * <p>getPluginId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getPluginId() {
        return m_pluginId;
    }

    /**
     * <p>setPluginId</p>
     *
     * @param pluginId a {@link java.lang.Integer} object.
     */
    public void setPluginId(Integer pluginId) {
        m_pluginId = pluginId;
    }

    /**
     * <p>getPluginSubId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getPluginSubId() {
        return m_pluginSubId;
    }

    /**
     * <p>setPluginSubId</p>
     *
     * @param pluginSubId a {@link java.lang.Integer} object.
     */
    public void setPluginSubId(Integer pluginSubId) {
        m_pluginSubId = pluginSubId;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getCategory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCategory() {
        return m_category;
    }

    /**
     * <p>setCategory</p>
     *
     * @param category a {@link java.lang.String} object.
     */
    public void setCategory(String category) {
        m_category = category;
    }

    /**
     * <p>getCopyright</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCopyright() {
        return m_copyright;
    }

    /**
     * <p>setCopyright</p>
     *
     * @param copyright a {@link java.lang.String} object.
     */
    public void setCopyright(String copyright) {
        m_copyright = copyright;
    }

    /**
     * <p>getDescr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescr() {
        return m_descr;
    }

    /**
     * <p>setDescr</p>
     *
     * @param descr a {@link java.lang.String} object.
     */
    public void setDescr(String descr) {
        m_descr = descr;
    }

    /**
     * <p>getSummary</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSummary() {
        return m_summary;
    }

    /**
     * <p>setSummary</p>
     *
     * @param summary a {@link java.lang.String} object.
     */
    public void setSummary(String summary) {
        m_summary = summary;
    }

    /**
     * <p>getFamily</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFamily() {
        return m_family;
    }

    /**
     * <p>setFamily</p>
     *
     * @param family a {@link java.lang.String} object.
     */
    public void setFamily(String family) {
        m_family = family;
    }

    /**
     * <p>getVersion</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getVersion() {
        return m_version;
    }

    /**
     * <p>setVersion</p>
     *
     * @param version a {@link java.lang.String} object.
     */
    public void setVersion(String version) {
        m_version = version;
    }

    /**
     * <p>getCveEntry</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCveEntry() {
        return m_cveEntry;
    }

    /**
     * <p>setCveEntry</p>
     *
     * @param cveEntry a {@link java.lang.String} object.
     */
    public void setCveEntry(String cveEntry) {
        m_cveEntry = cveEntry;
    }

    /**
     * <p>getMd5</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMd5() {
        return m_md5;
    }

    /**
     * <p>setMd5</p>
     *
     * @param md5 a {@link java.lang.String} object.
     */
    public void setMd5(String md5) {
        m_md5 = md5;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
            .append("pluginId", getPluginId())
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

	/** {@inheritDoc} */
    @Override
	public void visit(EntityVisitor visitor) {
		// TODO Auto-generated method stub
		throw new RuntimeException("visitor method not implemented");

	}

}
