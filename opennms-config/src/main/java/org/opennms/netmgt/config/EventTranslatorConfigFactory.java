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
package org.opennms.netmgt.config;

import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.MatchTable;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.SingleResultQuerier;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.translator.Assignment;
import org.opennms.netmgt.config.translator.EventTranslationSpec;
import org.opennms.netmgt.config.translator.EventTranslatorConfiguration;
import org.opennms.netmgt.config.translator.Mapping;
import org.opennms.netmgt.config.translator.Value;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * This is the singleton class used to load the configuration from the
 * passive-status-configuration.xml. This provides convenience methods to get the configured
 * categories and their information, add/delete categories from category groups.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class EventTranslatorConfigFactory implements EventTranslatorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(EventTranslatorConfigFactory.class);
    /**
     * The singleton instance of this factory
     */
    private static EventTranslatorConfig m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private EventTranslatorConfiguration m_config;

    /** Volatile because update() clears it while translateEvent() reads it unsynchronized. */
    private volatile List<TranslationSpec> m_translationSpecs;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * connection factory for use with sql-value
     */
    private DataSource m_dbConnFactory = null;


    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * 
     */
    private EventTranslatorConfigFactory(String configFile, DataSource dbConnFactory) throws IOException{
        InputStream stream = null;
        try {
            stream = new FileInputStream(configFile);
            unmarshall(stream, dbConnFactory);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    /**
     * <p>Constructor for EventTranslatorConfigFactory.</p>
     *
     * @param rdr a {@link java.io.Reader} object.
     * @param dbConnFactory a {@link javax.sql.DataSource} object.
     * @throws IOException 
     */
    public EventTranslatorConfigFactory(InputStream rdr, DataSource dbConnFactory) throws IOException {
        unmarshall(rdr, dbConnFactory);
    }

    private synchronized void unmarshall(InputStream stream, DataSource dbConnFactory) throws IOException {
        try(final Reader reader = new InputStreamReader(stream)) {
            m_config = JaxbUtils.unmarshal(EventTranslatorConfiguration.class, reader);
            m_dbConnFactory = dbConnFactory;
        }
    }

    private synchronized void unmarshall(InputStream stream) throws IOException {
        unmarshall(stream, null);
    }

    /**
     * Simply marshals the config without messing with the singletons.
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void update() throws Exception  {

        synchronized (this) {

            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.TRANSLATOR_CONFIG_FILE_NAME);
            InputStream stream = null;

            try {
                stream = new FileInputStream(cfgFile);
                unmarshall(stream);
                // After reloading m_config we must invalidate the cached specs so new
                // events are translated using the updated configuration.
                m_translationSpecs = null;
            } finally {
                if (stream != null) {
                    IOUtils.closeQuietly(stream);
                }
            }

        }
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     * @throws java.beans.PropertyVetoException if any.
     */
    public static synchronized void init() throws IOException, ClassNotFoundException, SQLException, PropertyVetoException  {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.TRANSLATOR_CONFIG_FILE_NAME);

        m_singleton = new EventTranslatorConfigFactory(cfgFile.getPath(), DataSourceFactory.getInstance());

        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     * @throws java.beans.PropertyVetoException if any.
     */
    public static synchronized void reload() throws IOException, ClassNotFoundException, SQLException, PropertyVetoException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized EventTranslatorConfig getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("getInstance: The factory has not been initialized");

        return m_singleton;
    }

    /**
     * <p>setInstance</p>
     *
     * @param singleton a {@link org.opennms.netmgt.config.EventTranslatorConfig} object.
     */
    public static void setInstance(EventTranslatorConfig singleton) {
        m_singleton=singleton;
        m_loaded=true;
    }

    /**
     * Return the PassiveStatus configuration.
     * 
     * @return the PassiveStatus configuration
     */
    private synchronized EventTranslatorConfiguration getConfig() {
        return m_config;
    }


    /*
     *  (non-Javadoc)
     * @see org.opennms.netmgt.config.PassiveStatusConfig#getUEIList()
     */
    /**
     * <p>getUEIList</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<String> getUEIList() {
        return getTranslationUEIs();
    }

    private List<String> getTranslationUEIs() {
        return getConfig().getEventTranslationSpecs().parallelStream()
            .map(EventTranslationSpec::getUei)
            .distinct().collect(Collectors.toList());
    }

    static class TranslationFailedException extends RuntimeException {
        private static final long serialVersionUID = -7219413891842193464L;

        TranslationFailedException(String msg) {
            super(msg);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Event> translateEvent(Event e) {
        ArrayList<Event> events = new ArrayList<>();
        for (TranslationSpec spec : getTranslationSpecs()) {
            events.addAll(spec.translate(e));
        }
        return events;
    }

    private List<TranslationSpec> getTranslationSpecs() {
        // Read the field once. Returning it directly would hand back null when update()
        // clears it after the check, and translateEvent() iterates the result.
        List<TranslationSpec> specs = m_translationSpecs;
        if (specs == null) {
            specs = constructTranslationSpecs();
            m_translationSpecs = specs;
        }

        return specs;
    }

    private List<TranslationSpec> constructTranslationSpecs() {
        return getConfig().getEventTranslationSpecs().parallelStream()
            .map(TranslationSpec::new)
            .collect(Collectors.toList());
    }

    class TranslationSpec {
        private EventTranslationSpec m_spec;
        private List<TranslationMapping> m_translationMappings;
        TranslationSpec(EventTranslationSpec spec) {
            m_spec = spec;
            m_translationMappings = null; // lazy init
        }
        public List<Event> translate(Event e) {
            // short circuit here is the uei doesn't match
            if (!ueiMatches(e)) return Collections.emptyList();

            // uei matches now go thru the mappings
            ArrayList<Event> events = new ArrayList<>();
            for (TranslationMapping mapping : getTranslationMappings()) {
                Event translatedEvent = mapping.translate(e);
                if (translatedEvent != null)
                    events.add(translatedEvent);
            }

            return events;
        }
        String getUei() { return m_spec.getUei(); }
        public EventTranslationSpec getEventTranslationSpec() {
            return m_spec;
        }

        private List<TranslationMapping> constructTranslationMappings() {
            if (m_spec.getMappings() == null) return Collections.emptyList();

            final List<Mapping> mappings = m_spec.getMappings();

            List<TranslationMapping> transMaps = new ArrayList<TranslationMapping>(mappings.size());
            for (final Mapping mapping : mappings) {
                TranslationMapping transMap = new TranslationMapping(mapping);
                transMaps.add(transMap);
            }

            return Collections.unmodifiableList(transMaps);
        }

        List<TranslationMapping> getTranslationMappings() {
            if (m_translationMappings == null)
                m_translationMappings = constructTranslationMappings();
            return Collections.unmodifiableList(m_translationMappings);
        }

        private boolean ueiMatches(Event e) {
            return e.getUei().equals(m_spec.getUei())
                    || m_spec.getUei().endsWith("/")
                    && e.getUei().startsWith(m_spec.getUei());
        }
    }

    class TranslationMapping {
        Mapping m_mapping;
        List<AssignmentSpec> m_assignments;
        TranslationMapping(Mapping mapping) { 
            m_mapping = mapping;
            m_assignments = null; // lazy init
        }

        public Event translate(Event srcEvent) {
            final List<AssignmentSpec> assignmentSpecs = getAssignmentSpecs();

            /* Resolving before cloning is safe because value specs only ever read the source
             * event, and it keeps a mapping that rejects the event from paying for a clone. */
            final String[] values = new String[assignmentSpecs.size()];
            for (int i = 0; i < values.length; i++) {
                final AssignmentSpec assignSpec = assignmentSpecs.get(i);
                final EvaluationResult result = assignSpec.evaluate(srcEvent);
                if (result.matched()) {
                    values[i] = result.value();
                } else if (assignSpec.getAssignment().hasDefault()) {
                    values[i] = assignSpec.getAssignment().getDefault();
                } else {
                    return null;
                }
            }

            final Event targetEvent = cloneEvent(srcEvent);
            for (int i = 0; i < values.length; i++) {
                assignmentSpecs.get(i).setValue(targetEvent, values[i]);
            }

            targetEvent.setSource(TRANSLATOR_NAME);
            return targetEvent;
        }

        private Event cloneEvent(Event srcEvent) {
            Event clonedEvent = EventTranslatorConfigFactory.cloneEvent(srcEvent);
            if (clonedEvent == null) {
                throw new IllegalStateException("unable to clone event: " + srcEvent);
            }
            /* since alarmData and severity are computed based on translated information in 
             * eventd using the data from eventconf, we unset it here to eventd
             * can reset to the proper new settings.
             */ 
            clonedEvent.setAlarmData(null);
            clonedEvent.setSeverity(null);
            /* the reasoning for alarmData and severity also applies to description (see NMS-4038). */
            clonedEvent.setDescr(null);
            if (!m_mapping.getPreserveSnmpData()) { // NMS-8374
                clonedEvent.setSnmp(null);
            }
            return clonedEvent;
        }

        public Mapping getMapping() {
            return m_mapping;
        }

        private List<AssignmentSpec> getAssignmentSpecs() {
            if (m_assignments == null)
                m_assignments = constructAssignmentSpecs();
            return m_assignments;
        }

        private List<AssignmentSpec> constructAssignmentSpecs() {
            Mapping mapping = getMapping();
            List<AssignmentSpec> assignments = new ArrayList<>();
            for (Assignment assign : mapping.getAssignments()) {
                AssignmentSpec assignSpec = 
                        ("parameter".equals(assign.getType()) ? 
                            (AssignmentSpec)new ParameterAssignmentSpec(assign) :
                                (AssignmentSpec)new FieldAssignmentSpec(assign)
                                );
                assignments.add(assignSpec);
            }
            return assignments;
        }
    }

    abstract class AssignmentSpec {
        private Assignment m_assignment;
        private ValueSpec m_valueSpec;
        AssignmentSpec(Assignment assignment) {
            m_assignment = assignment;
            m_valueSpec = null; // lazy init
        }

        private Assignment getAssignment() { return m_assignment; }

        protected String getAttributeName() { return getAssignment().getName(); }

        private ValueSpec constructValueSpec() {
            Value val = getAssignment().getValue();

            return EventTranslatorConfigFactory.this.getValueSpec(val);
        }

        protected abstract void setValue(Event targetEvent, String value);

        private ValueSpec getValueSpec() {
            if (m_valueSpec == null)
                m_valueSpec = constructValueSpec();
            return m_valueSpec;
        }

        EvaluationResult evaluate(Event srcEvent) {
            return getValueSpec().evaluate(srcEvent);
        }
    }

    class FieldAssignmentSpec extends AssignmentSpec {
        FieldAssignmentSpec(Assignment field) { super(field); }

        @Override
        protected void setValue(Event targetEvent, String value) {
            try {
                BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(targetEvent);
                bean.setPropertyValue(getAttributeName(), value);
            } catch(FatalBeanException e) {
                LOG.error("Unable to set value for attribute {}to value {} Exception: {}", e, getAttributeName(), value);
                throw new TranslationFailedException("Unable to set value for attribute "+getAttributeName()+" to value "+value);
            }
        }

    }

    class ParameterAssignmentSpec extends AssignmentSpec {
        ParameterAssignmentSpec(Assignment assign) {
            super(assign);
        }

        @Override
        protected void setValue(Event targetEvent, String value) {
            if (value == null) {
                LOG.debug("Value of parameter is null setting to blank");
                value="";
            }

            for (final Parm parm : targetEvent.getParmCollection()) {
                if (parm.getParmName().equals(getAttributeName())) {
                    org.opennms.netmgt.xml.event.Value val = parm.getValue();
                    if (val == null) {
                        val = new org.opennms.netmgt.xml.event.Value();
                        parm.setValue(val);
                    }
                    LOG.debug("Overriding value of parameter {}. Setting it to {}", value, getAttributeName());
                    val.setContent(value);
                    return;
                }
            }

            // if we got here then we didn't find the existing parameter
            Parm newParm = new Parm();
            newParm.setParmName(getAttributeName());
            org.opennms.netmgt.xml.event.Value val = new org.opennms.netmgt.xml.event.Value();
            newParm.setValue(val);
            LOG.debug("Setting value of parameter {} to {}", value, getAttributeName());
            val.setContent(value);
            targetEvent.addParm(newParm);
        }
    }

    ValueSpec getValueSpec(Value val) {
        if ("field".equals(val.getType()))
            return new FieldValueSpec(val);
        else if ("parameter".equals(val.getType()))
            return new ParameterValueSpec(val);
        else if ("constant".equals(val.getType()))
            return new ConstantValueSpec(val);
        else if ("sql".equals(val.getType()))
            return new SqlValueSpec(val);
        else
            return new ValueSpecUnspecified();
    }

    /**
     * Outcome of evaluating a {@link ValueSpec} against an event: either no match, or a match
     * carrying a value. A matched value may legitimately be null (a SQL lookup that found a row
     * with a null column), so this cannot collapse into an Optional.
     */
    static final class EvaluationResult {
        private static final EvaluationResult NO_MATCH = new EvaluationResult(null, false);

        private final String m_value;
        private final boolean m_matched;

        private EvaluationResult(String value, boolean matched) {
            m_value = value;
            m_matched = matched;
        }

        static EvaluationResult noMatch() {
            return NO_MATCH;
        }

        static EvaluationResult of(String value) {
            return new EvaluationResult(value, true);
        }

        boolean matched() {
            return m_matched;
        }

        String value() {
            return m_value;
        }
    }

    abstract class ValueSpec {

        /**
         * Evaluates this value against the source event exactly once, yielding both whether it
         * matched and the value to assign. Callers must not re-evaluate to obtain the value:
         * for sql values each evaluation is a database round trip.
         */
        public abstract EvaluationResult evaluate(Event srcEvent);
    }

    class ConstantValueSpec extends ValueSpec {
        Value m_constant;

        public ConstantValueSpec(Value constant) {
            m_constant = constant;
        }

        @Override
        public EvaluationResult evaluate(Event srcEvent) {
            if (m_constant.getMatches().isPresent()) {
                LOG.warn("ConstantValueSpec.evaluate: matches not allowed for constant value.");
                throw new IllegalStateException("Illegal to use matches with constant type values");
            }
            return EvaluationResult.of(m_constant.getResult());
        }

    }

    class ValueSpecUnspecified extends ValueSpec {

        @Override
        public EvaluationResult evaluate(Event srcEvent) {
            // TODO: this should probably throw an exception since it makes no sense
            return EvaluationResult.of("value unspecified");
        }

    }

    class SqlValueSpec extends ValueSpec {
        Value m_val;
        List<ValueSpec> m_nestedValues;
        public SqlValueSpec(Value val) {
            m_val = val;
            m_nestedValues = null; // lazy init
        }

        public List<ValueSpec> getNestedValues() {
            if (m_nestedValues == null)
                m_nestedValues = constructNestedValues();
            return m_nestedValues;
        }

        private List<ValueSpec> constructNestedValues() {
            List<ValueSpec> nestedValues = new ArrayList<>();
            for (Value val : m_val.getValues()) {
                nestedValues.add(EventTranslatorConfigFactory.this.getValueSpec(val));
            }
            return nestedValues;
        }

        @Override
        public EvaluationResult evaluate(Event srcEvent) {
            final List<ValueSpec> nestedValues = getNestedValues();
            final Object[] args = new Object[nestedValues.size()];
            for (int i = 0; i < args.length; i++) {
                final EvaluationResult nested = nestedValues.get(i).evaluate(srcEvent);
                if (!nested.matched()) {
                    return EvaluationResult.noMatch();
                }
                args[i] = nested.value();
            }

            final SingleResultQuerier querier = new SingleResultQuerier(m_dbConnFactory, m_val.getResult());
            querier.execute(args);

            if (querier.getCount() < 1) {
                LOG.info("No results found for query {}. No match.", querier.reproduceStatement(args));
                return EvaluationResult.noMatch();
            }

            final Object result = querier.getResult();
            LOG.debug("evaluate: result of single result querier is: {}", result);
            return EvaluationResult.of(result == null ? null : result.toString());
        }

    }

    abstract class AttributeValueSpec extends ValueSpec {
        Value m_val;
        /** Compiled once at construction; null when the value has no 'matches' attribute. */
        private final Pattern m_pattern;

        AttributeValueSpec(Value val) {
            m_val = val;
            m_pattern = val.getMatches().map(Pattern::compile).orElse(null);
        }

        @Override
        public EvaluationResult evaluate(Event srcEvent) {
            final String attributeValue = getAttributeValue(srcEvent);
            if (attributeValue == null) {
                LOG.debug("AttributeValueSpec.evaluate: no match because attribute {} is null", getAttributeName());
                return EvaluationResult.noMatch();
            }

            if (m_pattern == null) {
                LOG.debug("AttributeValueSpec.evaluate: Event attributeValue: {} matches because pattern is null", attributeValue);
                return EvaluationResult.of(m_val.getResult());
            }

            final Matcher m = m_pattern.matcher(attributeValue);
            if (!m.matches()) {
                LOG.debug("AttributeValueSpec.evaluate: Event attributeValue: {} doesn't match pattern: {}", attributeValue, m_pattern);
                return EvaluationResult.noMatch();
            }

            LOG.debug("AttributeValueSpec.evaluate: Event attributeValue: {} matches pattern: {}", attributeValue, m_pattern);
            return EvaluationResult.of(PropertiesUtils.substitute(m_val.getResult(), new MatchTable(m)));
        }

        public String getAttributeName() { return m_val.getName().orElse(null); }


        abstract public String getAttributeValue(Event e);
    }

    // XXX: This is here because Spring converting to a String appears
    // to be broken.  It if probably a Hack and we probably need to have
    // a better way to access the Spring property editors and convert
    // to a string more correctly.
    static class StringPropertyEditor extends PropertyEditorSupport {

        @Override
        public void setValue(Object value) {
            if (value == null || value instanceof String)
                super.setValue(value);
            else
                super.setValue(value.toString());
        }

        @Override
        public String getAsText() {
            return (String)super.getValue();
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            super.setValue(text);
        }



    }

    class FieldValueSpec extends AttributeValueSpec {
        public FieldValueSpec(Value val) {
            super(val);
        }

        @Override
        public String getAttributeValue(Event e) {
            try {
                BeanWrapper bean = getBeanWrapper(e);

                return (String)bean.convertIfNecessary(bean.getPropertyValue(getAttributeName()), String.class);
            } catch (FatalBeanException ex) {
                LOG.error("Property {} does not exist on Event", ex, getAttributeName());
                throw new TranslationFailedException("Property "+getAttributeName()+" does not exist on Event");
            }
        }

        private BeanWrapper getBeanWrapper(Event e) {
            BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(e);
            bean.registerCustomEditor(String.class, new StringPropertyEditor());
            return bean;
        }
    }

    class ParameterValueSpec extends AttributeValueSpec {
        /** A '~' prefix makes the name a regex matched against each parm name; null otherwise. */
        private final Pattern m_namePattern;

        ParameterValueSpec(Value val) {
            super(val);
            final String attrName = val.getName().orElse(null);
            m_namePattern = (attrName != null && attrName.startsWith("~"))
                    ? Pattern.compile(StringUtils.removeStart(attrName, "~"))
                    : null;
        }

        @Override
        public String getAttributeValue(Event e) {

            String attrName = getAttributeName();
            for (Parm parm : e.getParmCollection()) {

                if (parm.getParmName().equals(attrName)) {
                    LOG.debug("getAttributeValue: eventParm name: '{}' equals translation parameter name: '{}'", attrName, parm.getParmName());
                    return (parm.getValue() == null ? "" : parm.getValue().getContent());
                }

                if (m_namePattern != null && m_namePattern.matcher(parm.getParmName()).matches()) {
                    LOG.debug("getAttributeValue: eventParm name: '{}' matches translation parameter name expression: '{}'", parm.getParmName(), m_namePattern);
                    return (parm.getValue() == null ? "" : parm.getValue().getContent());
                }
            }
            return null;
        }
    }


    /**
     * <p>cloneEvent</p>
     *
     * @param orig a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event cloneEvent(Event orig) {
        // Event doesn't implement IEvent, so the deep copy has to go via the immutable model.
        return Event.copyFrom(ImmutableMapper.fromMutableEvent(orig));
    }

}
