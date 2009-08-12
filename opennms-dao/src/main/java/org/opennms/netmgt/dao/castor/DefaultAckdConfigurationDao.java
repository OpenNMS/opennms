/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 27, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.dao.castor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.config.ackd.Reader;
import org.opennms.netmgt.config.ackd.ReaderSchedule;
import org.opennms.netmgt.dao.AckdConfigurationDao;

/**
 * Default implementation of <code>AckdConfiguration</code> containing utility methods for manipulating
 * the <code>Ackd</code> and <code>AckdReader</code>s.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class DefaultAckdConfigurationDao extends AbstractCastorConfigDao<AckdConfiguration, AckdConfiguration> implements AckdConfigurationDao {

    public DefaultAckdConfigurationDao() {
        super(AckdConfiguration.class, "Ackd Configuration");
    }
    
    public AckdConfiguration getConfig() {
        return getContainer().getObject();
    }

    @Override
    public AckdConfiguration translateConfig(AckdConfiguration castorConfig) {
        return castorConfig;
    }

    public Boolean acknowledgmentMatch(List<String> messageText) {
        String expression = getConfig().getAckExpression();
        return matcher(messageText, expression);
    }

    public Boolean clearMatch(List<String> messageText) {
        String expression = getConfig().getClearExpression();
        return matcher(messageText, expression);
    }

    public Boolean escalationMatch(List<String> messageText) {
        String expression = getConfig().getEscalateExpression();
        return matcher(messageText, expression);
    }

    public Boolean unAcknowledgmentMatch(List<String> messageText) {
        String expression = getConfig().getUnackExpression();
        return matcher(messageText, expression);
    }

    private Boolean matcher(List<String> messageText, String expression) {
        Boolean matches = new Boolean(false);
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

    public Reader getReader(String readerName) {
        Reader readerByName = null;
        List<Reader> readers = getConfig().getReaders().getReaderCollection();
        for (Reader reader : readers) {
            if (readerName.equals(reader.getReaderName())) {
                readerByName = reader;
            }
        }
        return readerByName;
    }
    
    public ReaderSchedule getReaderSchedule(String readerName) {
        ReaderSchedule schedule = null;
        Reader reader = getReader(readerName);
        if (reader != null) {
            schedule = reader.getReaderSchedule();
        }
        return schedule;
    }
    
    public boolean isReaderEnabled(String readerName) {
        boolean enabled = false;
        Reader reader = getReader(readerName);
        if (reader != null) {
            enabled = reader.isEnabled();
        }
        return enabled;
    }

    public void reloadConfiguration() {
        getContainer().reload();
    }
    
}
