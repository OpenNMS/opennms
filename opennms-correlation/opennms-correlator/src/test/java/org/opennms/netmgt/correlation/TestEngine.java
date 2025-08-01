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
package org.opennms.netmgt.correlation;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;

public class TestEngine extends AbstractCorrelationEngine implements InitializingBean {
    
    Integer m_timerId = null;
    CorrelationEngineRegistrar m_correlator;

    @Override
	public void correlate(Event e) {
		if ("testDown".equals(e.getUei())) {
            EventBuilder bldr = new EventBuilder("testDownReceived", "TestEngine");
            sendEvent(bldr.getEvent());
		}
		else if ("testUp".equals(e.getUei())) {
            EventBuilder bldr = new EventBuilder("testUpReceived", "TestEngine");
            sendEvent(bldr.getEvent());
		}
        else if ("timed".equals(e.getUei())) {
            m_timerId = setTimer(1000);
        }
        else if ("cancelTimer".equals(e.getUei())) {
            cancelTimer(m_timerId);
        }
		else {
			throw new IllegalArgumentException("Unexpected event with uei = "+e.getUei());
		}
		
	}
    
    @Override
	public List<String> getInterestingEvents() {
		List<String> ueis = new ArrayList<>();
		ueis.add("testDown");
		ueis.add("testUp");
        ueis.add("timed");
        ueis.add("cancelTimer");
		return ueis;
	}

    @Override
    protected void timerExpired(Integer timerId) {
        EventBuilder bldr = new EventBuilder("timerExpired", "TestEngine");
        sendEvent(bldr.getEvent());
    }

    @Override
    public String getName() {
        return "TestEngine";
    }

    @Override
    public void tearDown() {
        // pass
    }

    @Override
    public void reloadConfig(boolean persistState) {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        m_correlator.addCorrelationEngine(this);
    }

    public void setCorrelator(CorrelationEngineRegistrar correlator) {
        m_correlator = correlator;
    }

}
