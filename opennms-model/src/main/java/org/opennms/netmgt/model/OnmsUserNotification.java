package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="usersnotified"
 *     
*/
public class OnmsUserNotification implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1750912427062821742L;
    
    private Integer id;

    /** identifier field */
    private String userid;

    /** identifier field */
    private Date notifytime;

    /** identifier field */
    private String media;

    /** identifier field */
    private String contactinfo;

    /** identifier field */
    private String autonotify;

    /** persistent field */
    private OnmsNotification notification;

    /** full constructor */
    public OnmsUserNotification(String userid, Date notifytime, String media, String contactinfo, String autonotify, org.opennms.netmgt.model.OnmsNotification notification) {
        this.userid = userid;
        this.notifytime = notifytime;
        this.media = media;
        this.contactinfo = contactinfo;
        this.autonotify = autonotify;
        this.notification = notification;
    }

    /** default constructor */
    public OnmsUserNotification() {
    }
    
    /**
     * Unique identifier for ipInterface.
     * 
     * @hibernate.id generator-class="native" column="id"
     * @hibernate.generator-param name="sequence" value="usrNotifNxtId"
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
     *                 column="userid"
     *                 length="256"
     *             
     */
    public String getUserid() {
        return this.userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    /** 
     *                @hibernate.property
     *                 column="notifytime"
     *                 length="8"
     *             
     */
    public Date getNotifytime() {
        return this.notifytime;
    }

    public void setNotifytime(Date notifytime) {
        this.notifytime = notifytime;
    }

    /** 
     *                @hibernate.property
     *                 column="media"
     *                 length="32"
     *             
     */
    public String getMedia() {
        return this.media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    /** 
     *                @hibernate.property
     *                 column="contactinfo"
     *                 length="64"
     *             
     */
    public String getContactinfo() {
        return this.contactinfo;
    }

    public void setContactinfo(String contactinfo) {
        this.contactinfo = contactinfo;
    }

    /** 
     *                @hibernate.property
     *                 column="autonotify"
     *                 length="1"
     *             
     */
    public String getAutonotify() {
        return this.autonotify;
    }

    public void setAutonotify(String autonotify) {
        this.autonotify = autonotify;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="notifyid"         
     *         
     */
    public org.opennms.netmgt.model.OnmsNotification getNotification() {
        return this.notification;
    }

    public void setNotification(org.opennms.netmgt.model.OnmsNotification notification) {
        this.notification = notification;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("userid", getUserid())
            .append("notifytime", getNotifytime())
            .append("media", getMedia())
            .append("contactinfo", getContactinfo())
            .append("autonotify", getAutonotify())
            .toString();
    }

}
