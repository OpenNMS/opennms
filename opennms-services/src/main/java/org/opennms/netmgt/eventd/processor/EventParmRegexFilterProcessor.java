/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.processor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.config.EventExpander;
import org.opennms.netmgt.model.events.EventProcessor;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author Seth
 * @author <a href="mailto:tfalzone@doubleclick.com">Tim Falzone</a>
 */
public final class EventParmRegexFilterProcessor implements EventProcessor, InitializingBean {

    private EventConfDao m_eventConfDao;
    private Map<String, org.opennms.netmgt.xml.eventconf.Filter> m_filterMap = new HashMap<String, org.opennms.netmgt.xml.eventconf.Filter>();

    @Override
    public void process(Header eventHeader, Event event) throws SQLException {
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        org.opennms.netmgt.xml.eventconf.Event econf = EventExpander.lookup(m_eventConfDao, event);
        if (econf.getFilters() != null) {

            for (org.opennms.netmgt.xml.eventconf.Filter fConf : econf.getFilters().getFilterCollection()) {
                if (!m_filterMap.containsKey(fConf.getEventparm() + "|" + event.getUei())) {
                    m_filterMap.put(fConf.getEventparm() + "|" + event.getUei(), fConf);
                    if (log.isDebugEnabled()) {
                        log.debug("adding [" + fConf.getEventparm() + "|" + event.getUei() + "] to filter map");
                    }
                }
            }

            for (Parm parm : event.getParmCollection()) {
                if ((parm.getParmName() != null)
                        && (parm.getValue().getContent() != null)
                        && (m_filterMap.containsKey(parm.getParmName() + "|" + event.getUei()))
                ) {
                    org.opennms.netmgt.xml.eventconf.Filter f = m_filterMap.get(parm.getParmName() + "|" + event.getUei());
                    if (log.isDebugEnabled()) {
                        log.debug("filtering " + parm.getParmName() + " with " + f.getPattern());
                    }
                    final Pattern pattern = Pattern.compile( f.getPattern() );
                    Matcher matcher = pattern.matcher( parm.getValue().getContent().trim() );
                    parm.getValue().setContent( matcher.replaceAll(f.getReplacement()) );
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_eventConfDao != null, "property eventConfDao must be set");
    }

    /**
     * <p>getEventConfDao</p>
     *
     * @return a {@link org.opennms.netmgt.config.EventConfDao} object.
     */
    public EventConfDao getEventConfDao() {
        return m_eventConfDao;
    }

    /**
     * <p>setEventConfDao</p>
     *
     * @param eventConfDao a {@link org.opennms.netmgt.config.EventConfDao} object.
     */
    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }
}
