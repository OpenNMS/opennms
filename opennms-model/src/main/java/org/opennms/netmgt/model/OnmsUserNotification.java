package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="usersnotified"
 *     
*/
public class OnmsUserNotification extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1750912427062821742L;
    
    /** identifier field */
    private String m_userId;

    /** identifier field */
    private Date m_notifyTime;

    /** identifier field */
    private String m_media;

    /** identifier field */
    private String m_contactInfo;

    /** identifier field */
    private String m_autoNotify;

    /** persistent field */
    private OnmsNotification m_notification;

    /** full constructor */
    public OnmsUserNotification(String userId, Date notifyTime, String media, String contactInfo, String autoNotify, OnmsNotification notification) {
        m_userId = userId;
        m_notifyTime = notifyTime;
        m_media = media;
        m_contactInfo = contactInfo;
        m_autoNotify = autoNotify;
        m_notification = notification;
    }

    /** default constructor */
    public OnmsUserNotification() {
    }
    
    /** 
     *                @hibernate.property
     *                 column="userId"
     *                 length="256"
     *             
     */
    public String getUserId() {
        return m_userId;
    }

    public void setUserId(String userId) {
        m_userId = userId;
    }

    /** 
     *                @hibernate.property
     *                 column="notifyTime"
     *                 length="8"
     *             
     */
    public Date getNotifyTime() {
        return m_notifyTime;
    }

    public void setNotifyTime(Date notifyTime) {
        m_notifyTime = notifyTime;
    }

    /** 
     *                @hibernate.property
     *                 column="media"
     *                 length="32"
     *             
     */
    public String getMedia() {
        return m_media;
    }

    public void setMedia(String media) {
        m_media = media;
    }

    /** 
     *                @hibernate.property
     *                 column="contactInfo"
     *                 length="64"
     *             
     */
    public String getContactInfo() {
        return m_contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        m_contactInfo = contactInfo;
    }

    /** 
     *                @hibernate.property
     *                 column="autoNotify"
     *                 length="1"
     *             
     */
    public String getAutoNotify() {
        return m_autoNotify;
    }

    public void setAutoNotify(String autoNotify) {
        m_autoNotify = autoNotify;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="notifyid"         
     *         
     */
    public OnmsNotification getNotification() {
        return m_notification;
    }

    public void setNotification(OnmsNotification notification) {
        m_notification = notification;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("userId", getUserId())
            .append("notifyTime", getNotifyTime())
            .append("media", getMedia())
            .append("contactInfo", getContactInfo())
            .append("autoNotify", getAutoNotify())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		// TODO Auto-generated method stub
		throw new RuntimeException("visitor method not implemented");

	}

}
