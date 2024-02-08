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
package org.opennms.netmgt.alarmd.northbounder.drools;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.alarmd.api.Destination;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Configuration for the Drools engine.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "engine")
@XmlAccessorType(XmlAccessType.FIELD)
public class DroolsEngine implements Destination {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6351398964945801319L;

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DroolsEngine.class);

    /** The engine name. */
    @XmlElement(name = "name", required = true)
    private String m_name;

    /** The filter. */
    @XmlElement(name = "filter", required = false)
    private String m_filter;

    /** The assert behaviour. */
    @XmlElement(name = "assertBehaviour", required = false, defaultValue = "identity")
    private String m_assertBehaviour;

    /** The globals. */
    @XmlElement(name = "global", required = false)
    private List<Global> m_globals = new ArrayList<>();

    /** The rule files. */
    @XmlElement(name = "ruleFile", required = true)
    private List<String> m_ruleFiles = new ArrayList<>();

    /** The application context. */
    @XmlElement(name = "appContext", required = false)
    private String m_appContext;

    /**
     * Instantiates a new Drools engine handler.
     */
    public DroolsEngine() {}

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#isFirstOccurrenceOnly()
     */
    @Override
    public boolean isFirstOccurrenceOnly() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#getName()
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.m_name = name;
    }

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public String getFilter() {
        return m_filter;
    }

    /**
     * Sets the filter.
     *
     * @param filter the new filter
     */
    public void setFilter(String filter) {
        this.m_filter = filter;
    }

    /**
     * Gets the assert behaviour.
     *
     * @return the assert behaviour
     */
    public String getAssertBehaviour() {
        return m_assertBehaviour == null ? "identity" : m_assertBehaviour;
    }

    /**
     * Sets the assert behaviour.
     *
     * @param assertBehaviour the new assert behaviour
     */
    public void setAssertBehaviour(String assertBehaviour) {
        this.m_assertBehaviour = assertBehaviour;
    }


    /**
     * Gets the application context.
     *
     * @return the application context
     */
    public String getAppContext() {
        return m_appContext;
    }

    /**
     * Sets the application context.
     *
     * @param appContext the new application context
     */
    public void setAppContext(String appContext) {
        this.m_appContext = appContext;
    }

    /**
     * Gets the globals.
     *
     * @return the globals
     */
    public List<Global> getGlobals() {
        return m_globals;
    }

    /**
     * Sets the globals.
     *
     * @param globals the new globals
     */
    public void setGlobals(List<Global> globals) {
        this.m_globals = globals;
    }

    /**
     * Gets the rule files.
     *
     * @return the rule files
     */
    public List<String> getRuleFiles() {
        return m_ruleFiles;
    }

    /**
     * Sets the rule files.
     *
     * @param ruleFiles the new rule files
     */
    public void setRuleFiles(List<String> ruleFiles) {
        this.m_ruleFiles = ruleFiles;
    }

    /**
     * Accepts.
     * <p>If the engine doesn't have filter, the method will return true.</p>
     * <p>If the method has a filter, it will be evaluated.</p>
     *
     * @param alarm the alarm
     * @return true, if successful
     */
    public boolean accepts(NorthboundAlarm alarm) {
        if (getFilter() != null) {
            StandardEvaluationContext context = new StandardEvaluationContext(alarm);
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(m_filter);
            boolean passed = false;
            try {
                passed = (Boolean)exp.getValue(context, Boolean.class);
            } catch (Exception e) {
                LOG.warn("accepts: can't evaluate expression {} for alarm {} because: {}", getFilter(), alarm.getUei(), e.getMessage());
            }
            LOG.debug("accepts: checking {} ? {}", m_filter, passed);
            return passed;
        }
        return true;
    }

}
