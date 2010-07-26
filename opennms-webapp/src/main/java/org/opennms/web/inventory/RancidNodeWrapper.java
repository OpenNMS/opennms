package org.opennms.web.inventory;

import java.util.Date;

/**
 * <p>RancidNodeWrapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class RancidNodeWrapper {
    
    private String deviceName;
    private String group;
    private String deviceType;
    private String comment;
    private String headRevision;
    private int totalRevisions;
    private Date creationDate;
    private String rootConfigurationUrl;
    
    /**
     * <p>Constructor for RancidNodeWrapper.</p>
     *
     * @param _deviceName a {@link java.lang.String} object.
     * @param _group a {@link java.lang.String} object.
     * @param _deviceType a {@link java.lang.String} object.
     * @param _comment a {@link java.lang.String} object.
     * @param _headRevision a {@link java.lang.String} object.
     * @param _totalRevision a int.
     * @param _expirationDate a java$util$Date object.
     * @param _rootConfigurationUrl a {@link java.lang.String} object.
     */
    public RancidNodeWrapper(String _deviceName, String _group, String _deviceType, String _comment, String _headRevision,
                      int _totalRevision, Date _expirationDate, String _rootConfigurationUrl) {
         deviceName=_deviceName;
         group=_group;
         deviceType=_deviceType;
         comment=_comment;
         headRevision=_headRevision;
         totalRevisions=_totalRevision;
         creationDate=_expirationDate;
         rootConfigurationUrl=_rootConfigurationUrl;
    }
            
    /**
     * <p>Getter for the field <code>deviceName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDeviceName(){
        return deviceName;
    }
    /**
     * <p>Getter for the field <code>group</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGroup(){
        return group;
    }
    /**
     * <p>Getter for the field <code>deviceType</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDeviceType(){
        return deviceType;
    }
    /**
     * <p>Getter for the field <code>comment</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getComment(){
        return comment;
    }
    /**
     * <p>Getter for the field <code>headRevision</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHeadRevision(){
        return headRevision;
    }
    /**
     * <p>Getter for the field <code>totalRevisions</code>.</p>
     *
     * @return a int.
     */
    public int getTotalRevisions(){
        return totalRevisions;
    }
    /**
     * <p>Getter for the field <code>creationDate</code>.</p>
     *
     * @return a java$util$Date object.
     */
    public Date getCreationDate(){
        return creationDate;
    }
    /**
     * <p>Getter for the field <code>rootConfigurationUrl</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRootConfigurationUrl(){
        return rootConfigurationUrl;
    }

}
