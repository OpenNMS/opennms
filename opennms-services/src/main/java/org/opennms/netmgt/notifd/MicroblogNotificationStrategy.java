/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.notifd;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.dao.api.MicroblogConfigurationDao;
import org.opennms.netmgt.dao.jaxb.DefaultMicroblogConfigurationDao;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger LOG = LoggerFactory.getLogger(MicroblogNotificationStrategy.class);
    
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
    @Override
    public int send(List<Argument> arguments) {
        Twitter svc = buildUblogService(arguments);
        String messageBody = buildMessageBody(arguments);
        Status response;

        final String baseURL = svc.getConfiguration().getClientURL();
        LOG.debug("Dispatching microblog notification at base URL '{}' with message '{}'", baseURL, messageBody);
        try {
            response = svc.updateStatus(messageBody);
        } catch (TwitterException e) {
            LOG.error("Microblog notification failed at service URL '{}'", baseURL, e);
            return 1;
        }

        LOG.info("Microblog notification succeeded: update posted with ID {}", response.getId());
        return 0;
    }

    /**
     * <p>buildUblogService</p>
     *
     * @param arguments a {@link java.util.List} object.
     * @return a {@link twitter4j.Twitter} object.
     */
    protected Twitter buildUblogService(final List<Argument> arguments) {
        final MicroblogClient client = new MicroblogClient(m_microblogConfigurationDao);

        if (!client.isOAuthUsable(UBLOG_PROFILE_NAME)) {
            if (!client.hasBasicAuth(UBLOG_PROFILE_NAME)) {
                throw new RuntimeException("No profile with OAuth or password authentication configured!  Edit your microblog-configuration.xml!");
            }
        }

        return client.getTwitter(UBLOG_PROFILE_NAME);
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
        LOG.debug("Final message body after collapsing whitespace is: '{}'", messageBody);

        return messageBody;
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
                LOG.debug("Found destination microblog name: {}", arg.getSwitch());
                return arg.getValue();
            }
        }
        LOG.debug("No destination microblog name found");
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
     * @param dao a {@link org.opennms.netmgt.dao.api.MicroblogConfigurationDao} object.
     */
    public void setMicroblogConfigurationDao(MicroblogConfigurationDao dao) {
        m_microblogConfigurationDao = dao;
    }

    /**
     * <p>getMicroblogConfigurationDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.MicroblogConfigurationDao} object.
     */
    public MicroblogConfigurationDao getMicroblogConfigurationDao() {
        return m_microblogConfigurationDao;
    }
}
