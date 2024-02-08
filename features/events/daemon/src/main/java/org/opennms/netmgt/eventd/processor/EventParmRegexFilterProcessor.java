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
package org.opennms.netmgt.eventd.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.eventd.EventExpander;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author Seth
 * @author <a href="mailto:tfalzone@doubleclick.com">Tim Falzone</a>
 */
public final class EventParmRegexFilterProcessor implements EventProcessor, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(EventParmRegexFilterProcessor.class);

    private EventConfDao m_eventConfDao;
    private Map<String, org.opennms.netmgt.xml.eventconf.Filter> m_filterMap = new HashMap<String, org.opennms.netmgt.xml.eventconf.Filter>();

    /**
     * This processor is always synchronous so this method just 
     * delegates to {@link #process(Log)}.
     */
    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        process(eventLog);
    }

    @Override
    public void process(Log eventLog) throws EventProcessorException {
        if (eventLog != null && eventLog.getEvents() != null && eventLog.getEvents().getEvent() != null) {
            for (Event eachEvent : eventLog.getEvents().getEvent()) {
                process(eachEvent);
            }
        }
    }

    private void process(Event event) throws EventProcessorException {

        org.opennms.netmgt.xml.eventconf.Event econf = EventExpander.lookup(m_eventConfDao, event);
        if (econf.getFilters() != null) {

            for (org.opennms.netmgt.xml.eventconf.Filter fConf : econf.getFilters()) {
                if (!m_filterMap.containsKey(fConf.getEventparm() + "|" + event.getUei())) {
                    m_filterMap.put(fConf.getEventparm() + "|" + event.getUei(), fConf);
                    LOG.debug("adding [{}|{}] to filter map", fConf.getEventparm(), event.getUei());
                }
            }

            for (Parm parm : event.getParmCollection()) {
                if ((parm.getParmName() != null)
                        && (parm.getValue().getContent() != null)
                        && (m_filterMap.containsKey(parm.getParmName() + "|" + event.getUei()))
                ) {
                    org.opennms.netmgt.xml.eventconf.Filter f = m_filterMap.get(parm.getParmName() + "|" + event.getUei());
                    LOG.debug("filtering {} with {}", parm.getParmName(), f.getPattern());
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
     * @return a {@link org.opennms.netmgt.config.api.EventConfDao} object.
     */
    public EventConfDao getEventConfDao() {
        return m_eventConfDao;
    }

    /**
     * <p>setEventConfDao</p>
     *
     * @param eventConfDao a {@link org.opennms.netmgt.config.api.EventConfDao} object.
     */
    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }
}
