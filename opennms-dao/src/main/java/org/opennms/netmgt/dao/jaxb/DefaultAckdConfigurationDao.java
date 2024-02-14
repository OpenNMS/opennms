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
package org.opennms.netmgt.dao.jaxb;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.config.ackd.Parameter;
import org.opennms.netmgt.config.ackd.Reader;
import org.opennms.netmgt.config.ackd.ReaderSchedule;
import org.opennms.netmgt.dao.api.AckdConfigurationDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Default implementation of <code>AckdConfiguration</code> containing utility methods for manipulating
 * the <code>Ackd</code> and <code>AckdReader</code>s.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class DefaultAckdConfigurationDao extends AbstractJaxbConfigDao<AckdConfiguration, AckdConfiguration> implements AckdConfigurationDao {

    /**
     * <p>Constructor for DefaultAckdConfigurationDao.</p>
     */
    public DefaultAckdConfigurationDao() {
        super(AckdConfiguration.class, "Ackd Configuration");
    }
    
    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.ackd.AckdConfiguration} object.
     */
    @Override
    public AckdConfiguration getConfig() {
        return getContainer().getObject();
    }

    /** {@inheritDoc} */
    @Override
    public AckdConfiguration translateConfig(AckdConfiguration config) {
        return config;
    }

    /** {@inheritDoc} */
    @Override
    public Boolean acknowledgmentMatch(List<String> messageText) {
        String expression = getConfig().getAckExpression();
        return matcher(messageText, expression);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean clearMatch(List<String> messageText) {
        String expression = getConfig().getClearExpression();
        return matcher(messageText, expression);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean escalationMatch(List<String> messageText) {
        String expression = getConfig().getEscalateExpression();
        return matcher(messageText, expression);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean unAcknowledgmentMatch(List<String> messageText) {
        String expression = getConfig().getUnackExpression();
        return matcher(messageText, expression);
    }

    private Boolean matcher(List<String> messageText, String expression) {
        Boolean matches = Boolean.FALSE;
        Pattern p;
        
        if (expression.startsWith("~")) {
            expression = (expression.startsWith("~") ? expression.substring(1) : expression); 
            p = Pattern.compile(expression);

            for (String text : messageText) {
                Matcher m = p.matcher(text);
                matches = m.matches();
                if (matches) {
                    break;
                }
            }
        } else {
            for (String text : messageText) {
                matches = expression.equalsIgnoreCase(text);
            }
        }
        return matches;
    }

    /** {@inheritDoc} */
    @Override
    public Reader getReader(String readerName) {
        Reader readerByName = null;
        List<Reader> readers = getConfig().getReaders();
        for (Reader reader : readers) {
            if (readerName.equals(reader.getReaderName())) {
                readerByName = reader;
            }
        }
        return readerByName;
    }
    
    /** {@inheritDoc} */
    @Override
    public ReaderSchedule getReaderSchedule(String readerName) {
        ReaderSchedule schedule = null;
        Reader reader = getReader(readerName);
        if (reader != null) {
            schedule = reader.getReaderSchedule();
        }
        return schedule;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isReaderEnabled(String readerName) {
        boolean enabled = false;
        Reader reader = getReader(readerName);
        if (reader != null) {
            enabled = reader.getEnabled();
        }
        return enabled;
    }

    /**
     * The exception boils up from the container class  The container class should
     * indicate this.
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    @Override
    public void reloadConfiguration() throws DataAccessResourceFailureException {
        getContainer().reload();
    }

    @Override
    public int getEnabledReaderCount() {
        return Long.valueOf(getConfig().getReaders().stream().filter(Reader::getEnabled).count()).intValue();
    }

    /** {@inheritDoc} */
    @Override
    public List<Parameter> getParametersForReader(String name) {
        return getReader(name).getParameters();
    }
    
}
