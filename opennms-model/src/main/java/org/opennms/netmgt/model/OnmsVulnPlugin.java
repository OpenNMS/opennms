package org.opennms.netmgt.model;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="vulnplugins"
 *     
*/
public class OnmsVulnPlugin implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6665164524726559523L;

    /** identifier field */
    private Integer pluginid;

    /** identifier field */
    private Integer pluginsubid;

    /** identifier field */
    private String name;

    /** identifier field */
    private String category;

    /** identifier field */
    private String copyright;

    /** identifier field */
    private String descr;

    /** identifier field */
    private String summary;

    /** identifier field */
    private String family;

    /** identifier field */
    private String version;

    /** identifier field */
    private String cveentry;

    /** identifier field */
    private String md5;

    private Integer id;

    /** full constructor */
    public OnmsVulnPlugin(Integer pluginid, Integer pluginsubid, String name, String category, String copyright, String descr, String summary, String family, String version, String cveentry, String md5) {
        this.pluginid = pluginid;
        this.pluginsubid = pluginsubid;
        this.name = name;
        this.category = category;
        this.copyright = copyright;
        this.descr = descr;
        this.summary = summary;
        this.family = family;
        this.version = version;
        this.cveentry = cveentry;
        this.md5 = md5;
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
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    

    /** 
     *                @hibernate.property
     *                 column="pluginid"
     *                 length="4"
     *             
     */
    public Integer getPluginid() {
        return this.pluginid;
    }

    public void setPluginid(Integer pluginid) {
        this.pluginid = pluginid;
    }

    /** 
     *                @hibernate.property
     *                 column="pluginsubid"
     *                 length="4"
     *             
     */
    public Integer getPluginsubid() {
        return this.pluginsubid;
    }

    public void setPluginsubid(Integer pluginsubid) {
        this.pluginsubid = pluginsubid;
    }

    /** 
     *                @hibernate.property
     *                 column="name"
     *                 length="128"
     *             
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** 
     *                @hibernate.property
     *                 column="category"
     *                 length="32"
     *             
     */
    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /** 
     *                @hibernate.property
     *                 column="copyright"
     *                 length="128"
     *             
     */
    public String getCopyright() {
        return this.copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    /** 
     *                @hibernate.property
     *                 column="descr"
     *                 length="1024"
     *             
     */
    public String getDescr() {
        return this.descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    /** 
     *                @hibernate.property
     *                 column="summary"
     *                 length="256"
     *             
     */
    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    /** 
     *                @hibernate.property
     *                 column="family"
     *                 length="32"
     *             
     */
    public String getFamily() {
        return this.family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    /** 
     *                @hibernate.property
     *                 column="version"
     *                 length="32"
     *             
     */
    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /** 
     *                @hibernate.property
     *                 column="cveentry"
     *                 length="14"
     *             
     */
    public String getCveentry() {
        return this.cveentry;
    }

    public void setCveentry(String cveentry) {
        this.cveentry = cveentry;
    }

    /** 
     *                @hibernate.property
     *                 column="md5"
     *                 length="32"
     *             
     */
    public String getMd5() {
        return this.md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("pluginid", getPluginid())
            .append("pluginsubid", getPluginsubid())
            .append("name", getName())
            .append("category", getCategory())
            .append("copyright", getCopyright())
            .append("descr", getDescr())
            .append("summary", getSummary())
            .append("family", getFamily())
            .append("version", getVersion())
            .append("cveentry", getCveentry())
            .append("md5", getMd5())
            .toString();
    }

}
