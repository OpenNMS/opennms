/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.microblog.MicroblogProfile;
import org.opennms.netmgt.dao.MicroblogConfigurationDao;
import org.opennms.netmgt.dao.castor.DefaultMicroblogConfigurationDao;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Send notifications to a TwitterAPI-compatible microblog service.
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:http://www.opennms.org">OpenNMS</a>
 */
public class MicroblogNotificationStrategy implements NotificationStrategy {
    private static final String UBLOG_PROFILE_NAME = "notifd";
    protected MicroblogConfigurationDao m_microblogConfigurationDao;
    protected MicroblogConfigurationDao m_configDao;
    
    /**
     * <p>Constructor for MicroblogNotificationStrategy.</p>
     *
     * @throws java.io.IOException if any.
     */
    public MicroblogNotificationStrategy() throws IOException {
        this(findDefaultConfigResource());
    }
    
    /**
     * <p>Constructor for MicroblogNotificationStrategy.</p>
     *
     * @param configResource a {@link org.springframework.core.io.Resource} object.
     */
    public MicroblogNotificationStrategy(Resource configResource) {
        m_configDao = new DefaultMicroblogConfigurationDao();
        ((DefaultMicroblogConfigurationDao)m_configDao).setConfigResource(configResource);
        ((DefaultMicroblogConfigurationDao)m_configDao).afterPropertiesSet();
        setMicroblogConfigurationDao(m_configDao);
    }

    /** {@inheritDoc} */
    public int send(List<Argument> arguments) {
        Twitter svc = buildUblogService(arguments);
        String messageBody = buildMessageBody(arguments);
        Status response;
        
        if (log().isDebugEnabled()) {
            log().debug("Dispatching microblog notification for user '" + svc.getUserId() + "' at base URL '" + svc.getBaseURL() + "' with message '" + messageBody + "'");
        }
        try {
            response = svc.updateStatus(messageBody);
        } catch (TwitterException e) {
            log().error("Microblog notification failed");
            log().info("Failed to update status for user '" + svc.getUserId() + "' at service URL '" + svc.getBaseURL() + "', caught exception: " + e.getMessage());
            return 1;
        }
        
        log().info("Microblog notification succeeded: update posted with ID " + response.getId());
        return 0;
    }
    
    /**
     * <p>buildUblogService</p>
     *
     * @param arguments a {@link java.util.List} object.
     * @return a {@link twitter4j.Twitter} object.
     */
    protected Twitter buildUblogService(List<Argument> arguments) {
        MicroblogProfile profile = null;
        String serviceUrl = "";
        String authenUser = "";
        String authenPass = "";
        
        // First try to get a microblog profile called "notifd", falling back to the default if that fails
        profile = m_microblogConfigurationDao.getProfile("notifd");
        if (profile == null)
            profile = m_microblogConfigurationDao.getDefaultProfile();

        if (profile == null) {
            log().fatal("Unable to find a microblog profile called '" + UBLOG_PROFILE_NAME + "', and default profile does not exist; we cannot send microblog notifications!");
            throw new RuntimeException("Could not find a usable microblog profile.");
        }
        
        log().info("Using microblog profile with name '" + profile.getName() + "'");
        
        serviceUrl = profile.getServiceUrl();
        authenUser = profile.getAuthenUsername();
        authenPass = profile.getAuthenPassword();

        if (authenUser == null || "".equals(authenUser))
            log().warn("Working with a blank username, perhaps you forgot to set this in the microblog configuration?");
        if (authenPass == null || "".equals(authenPass))
            log().warn("Working with a blank password, perhaps you forgot to set this in the microblog configuration?");
        if (serviceUrl == null || "".equals(serviceUrl))
            throw new IllegalArgumentException("Cannot use a blank microblog service URL, perhaps you forgot to set this in the microblog configuration?");
        
        Twitter svc = new Twitter();
        svc.setBaseURL(serviceUrl);
        svc.setSource("OpenNMS");
        svc.setUserId(authenUser);
        svc.setPassword(authenPass);
        return svc;
    }
    
    /**
     * <p>buildMessageBody</p>
     *
     * @param arguments a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    protected String buildMessageBody(List<Argument> arguments) {
        String messageBody = null;
        
        // Support PARAM_TEXT_MSG and PARAM_NUM_MSG but prefer PARAM_TEXT_MSG
        for (Argument arg : arguments) {
            if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) {
                messageBody = arg.getValue();
            } else if (NotificationManager.PARAM_NUM_MSG.equals(arg.getSwitch())) {
                if (messageBody == null) messageBody = arg.getValue();
            }
        }
        
        if (messageBody == null) {
            // FIXME We should have a better Exception to use here for configuration problems
            throw new IllegalArgumentException("No message specified, but is required");
        }
        
        // Collapse whitespace in final message
        messageBody = messageBody.replaceAll("\\s+", " ");
        if (log().isDebugEnabled()) {
            log().debug("Final message body after collapsing whitespace is: '" + messageBody + "'");
        }

        return messageBody;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    /**
     * <p>findDestName</p>
     *
     * @param arguments a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    protected String findDestName(List<Argument> arguments) {
        for (Argument arg : arguments) {
            if (NotificationManager.PARAM_MICROBLOG_USERNAME.equals(arg.getSwitch())) {
                log().debug("Found destination microblog name: " + arg.getSwitch());
                return arg.getValue();
            }
        }
        log().debug("No destination microblog name found");
        return null;
    }
    
    /**
     * <p>findDefaultConfigResource</p>
     *
     * @return a {@link org.springframework.core.io.Resource} object.
     * @throws java.io.IOException if any.
     */
    protected static Resource findDefaultConfigResource() throws IOException {
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.MICROBLOG_CONFIG_FILE_NAME);
        return new FileSystemResource(configFile);        
    }

    /**
     * <p>setMicroblogConfigurationDao</p>
     *
     * @param dao a {@link org.opennms.netmgt.dao.MicroblogConfigurationDao} object.
     */
    public void setMicroblogConfigurationDao(MicroblogConfigurationDao dao) {
        m_microblogConfigurationDao = dao;
    }
    
    /**
     * <p>getMicroblogConfigurationDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.MicroblogConfigurationDao} object.
     */
    public MicroblogConfigurationDao getMicroblogConfigurationDao() {
        return m_microblogConfigurationDao;
    }
}
