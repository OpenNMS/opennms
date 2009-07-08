/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao.castor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.dao.AckdConfigurationDao;

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
        String expression = getContainer().getObject().getAckExpression();
        return matcher(messageText, expression);
    }

    public Boolean clearMatch(List<String> messageText) {
        String expression = getContainer().getObject().getClearExpression();
        return matcher(messageText, expression);
    }

    public Boolean escalationMatch(List<String> messageText) {
        String expression = getContainer().getObject().getEscalateExpression();
        return matcher(messageText, expression);
    }

    public Boolean unAcknowledgmentMatch(List<String> messageText) {
        String expression = getContainer().getObject().getUnackExpression();
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
    
}
