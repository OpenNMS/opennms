/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.MatchTable;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.SingleResultQuerier;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.translator.Assignment;
import org.opennms.netmgt.config.translator.EventTranslationSpec;
import org.opennms.netmgt.config.translator.EventTranslatorConfiguration;
import org.opennms.netmgt.config.translator.Mapping;
import org.opennms.netmgt.config.translator.Translation;
import org.opennms.netmgt.config.translator.Value;
import org.opennms.netmgt.eventd.datablock.EventUtil;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
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
    /**
     * The singleton instance of this factory
     */
    private static EventTranslatorConfig m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private EventTranslatorConfiguration m_config;

	private List<TranslationSpec> m_translationSpecs;
	
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
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * 
     */
    private EventTranslatorConfigFactory(String configFile, DataSource dbConnFactory) throws IOException, MarshalException, ValidationException {
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
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public EventTranslatorConfigFactory(InputStream rdr, DataSource dbConnFactory) throws MarshalException, ValidationException {
        unmarshall(rdr, dbConnFactory);
    }

    private synchronized void unmarshall(InputStream stream, DataSource dbConnFactory) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(EventTranslatorConfiguration.class, stream);
        m_dbConnFactory = dbConnFactory;
    }
    
    private synchronized void unmarshall(InputStream stream) throws MarshalException, ValidationException {
        unmarshall(stream, null);
    }

    /**
     * Simply marshals the config without messing with the singletons.
     *
     * @throws java.lang.Exception if any.
     */
    public void update() throws Exception  {
        
        synchronized (this) {
            
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.TRANSLATOR_CONFIG_FILE_NAME);
            InputStream stream = null;
            
            try {
                stream = new FileInputStream(cfgFile);
                unmarshall(stream);
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
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.sql.SQLException if any.
     * @throws java.beans.PropertyVetoException if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException, ClassNotFoundException, SQLException, PropertyVetoException  {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }
        
        DataSourceFactory.init();

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.TRANSLATOR_CONFIG_FILE_NAME);

        m_singleton = new EventTranslatorConfigFactory(cfgFile.getPath(), DataSourceFactory.getInstance());

        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.sql.SQLException if any.
     * @throws java.beans.PropertyVetoException if any.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException, ClassNotFoundException, SQLException, PropertyVetoException {
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
	
	private ThreadCategory log() {
		return ThreadCategory.getInstance(EventTranslatorConfig.class);
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
    public List<String> getUEIList() {
    		return getTranslationUEIs();
    }

    private List<String> getTranslationUEIs() {
		Translation translation = getConfig().getTranslation();
		if (translation == null)
			return Collections.emptyList();
		
		List<String> ueis = new ArrayList<String>();
		for (EventTranslationSpec event : translation.getEventTranslationSpecCollection()) {
            ueis.add(event.getUei());
		}
		return ueis;
	}
    
	static class TranslationFailedException extends RuntimeException {
        private static final long serialVersionUID = -7219413891842193464L;

        TranslationFailedException(String msg) {
    			super(msg);
    		}
    }

    /** {@inheritDoc} */
    public boolean isTranslationEvent(Event e) {
		for (TranslationSpec spec : getTranslationSpecs()) {
			if (spec.matches(e))
				return true;
		}
		return false;
    }
    
	/** {@inheritDoc} */
	public List<Event> translateEvent(Event e) {
		ArrayList<Event> events = new ArrayList<Event>();
		for (TranslationSpec spec : getTranslationSpecs()) {
			events.addAll(spec.translate(e));
		}
		return events;
	}
	
	private List<TranslationSpec> getTranslationSpecs() {
		if (m_translationSpecs == null)
			m_translationSpecs = constructTranslationSpecs();
		
		return m_translationSpecs;
	}

	private List<TranslationSpec> constructTranslationSpecs() {
		List<TranslationSpec> specs = new ArrayList<TranslationSpec>();
		for (EventTranslationSpec eventTrans : m_config.getTranslation().getEventTranslationSpecCollection()) {
			specs.add(new TranslationSpec(eventTrans));
		}
		return specs;
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
			ArrayList<Event> events = new ArrayList<Event>();
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

			List<Mapping> mappings = m_spec.getMappings().getMappingCollection();
			
			List<TranslationMapping> transMaps = new ArrayList<TranslationMapping>(mappings.size());
			for (Mapping mapping : mappings) {
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
		boolean matches(Event e) {
			// short circuit if the eui doesn't match
			if (!ueiMatches(e)) {
			    if (log().isDebugEnabled()) {
			        log().debug("TransSpec.matches: No match comparing spec UEI: "+m_spec.getUei()+" with event UEI: "+e.getUei());
			    }
                return false;
            }
			
			// uei matches to go thru the mappings
            log().debug("TransSpec.matches: checking mappings for spec.");
            for (TranslationMapping transMap : getTranslationMappings()) {
				if (transMap.matches(e)) 
					return true;
			}
			return false;
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
			// if the event doesn't match the mapping then don't apply the translation
			if (!matches(srcEvent)) return null;
			
			Event targetEvent = cloneEvent(srcEvent);
			for (AssignmentSpec assignSpec : getAssignmentSpecs()) {
				assignSpec.apply(srcEvent, targetEvent);
			}
			
			targetEvent.setSource(TRANSLATOR_NAME);
			return targetEvent;
		}

        private Event cloneEvent(Event srcEvent) {
            Event clonedEvent = EventUtil.cloneEvent(srcEvent);
            /* since alarmData and severity are computed based on translated information in 
             * eventd using the data from eventconf, we unset it here to eventd
             * can reset to the proper new settings.
             */ 
            clonedEvent.setAlarmData(null);
            clonedEvent.setSeverity(null);
            /* the reasoning for alarmData and severity also applies to description (see NMS-4038). */
            clonedEvent.setDescr(null);
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
            List<AssignmentSpec> assignments = new ArrayList<AssignmentSpec>();
			for (Assignment assign : mapping.getAssignmentCollection()) {
				AssignmentSpec assignSpec = 
					("parameter".equals(assign.getType()) ? 
							(AssignmentSpec)new ParameterAssignmentSpec(assign) :
							(AssignmentSpec)new FieldAssignmentSpec(assign)
							);
				assignments.add(assignSpec);
			}
			return assignments;
		}
		
		private boolean assignmentsMatch(Event e) {
            AssignmentSpec assignSpec = null;
			for (Iterator<AssignmentSpec> it = getAssignmentSpecs().iterator(); it.hasNext();) {
				assignSpec = it.next();
				if (!assignSpec.matches(e)) {
				    if (log().isDebugEnabled()) {
				        log().debug("TranslationMapping.assignmentsMatch: assignmentSpec: "+assignSpec.getAttributeName()+" doesn't match.");
				    }
					return false;
                }
			}
			if (log().isDebugEnabled()) {
			    log().debug("TranslationMapping.assignmentsMatch: assignmentSpec: "+assignSpec.getAttributeName()+" matches!");
			}
			return true;
		}
		boolean matches(Event e) {
			return assignmentsMatch(e);
		}
	}
	
	abstract class AssignmentSpec {
		private Assignment m_assignment;
		private ValueSpec m_valueSpec;
		AssignmentSpec(Assignment assignment) {
			m_assignment = assignment; 
			m_valueSpec = null; // lazy init
		}
		
		public void apply(Event srcEvent, Event targetEvent) {
			setValue(targetEvent, getValueSpec().getResult(srcEvent));
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
		boolean matches(Event e) {
			return getValueSpec().matches(e);
		}
	}
	
	class FieldAssignmentSpec extends AssignmentSpec {
		FieldAssignmentSpec(Assignment field) { super(field); }
		
		protected void setValue(Event targetEvent, String value) {
			try {
				BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(targetEvent);
				bean.setPropertyValue(getAttributeName(), value);
			} catch(FatalBeanException e) {
				log().error("Unable to set value for attribute "+getAttributeName()+"to value "+value+ " Exception:" +e);
				throw new TranslationFailedException("Unable to set value for attribute "+getAttributeName()+" to value "+value);
			}
		}
		
	}
	
	class ParameterAssignmentSpec extends AssignmentSpec {
		ParameterAssignmentSpec(Assignment assign) {
			super(assign);
		}

		protected void setValue(Event targetEvent, String value) {
			if (value == null) {
			    log().debug("Value of parameter is null setting to blank");
			    value="";
			}

			for (final Parm parm : targetEvent.getParmCollection()) {
				if (parm.getParmName().equals(getAttributeName())) {
					org.opennms.netmgt.xml.event.Value val = parm.getValue();
					if (val == null) {
						val = new org.opennms.netmgt.xml.event.Value();
						parm.setValue(val);
					}
					if (log().isDebugEnabled()) {
					    log().debug("Overriding value of parameter "+getAttributeName()+". Setting it to "+value);
					}
					val.setContent(value);
					return;
				}
			}
			
			// if we got here then we didn't find the existing parameter
			Parm newParm = new Parm();
			newParm.setParmName(getAttributeName());
			org.opennms.netmgt.xml.event.Value val = new org.opennms.netmgt.xml.event.Value();
			newParm.setValue(val);
			if (log().isDebugEnabled()) {
			    log().debug("Setting value of parameter "+getAttributeName()+" to "+value);
			}
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
	

	abstract class ValueSpec {

		public abstract boolean matches(Event e);
		
		public abstract String getResult(Event srcEvent);
	}
	
	class ConstantValueSpec extends ValueSpec {
		
		Value m_constant;

		public ConstantValueSpec(Value constant) {
			m_constant = constant;
		}
		

		public boolean matches(Event e) {
			if (m_constant.getMatches() != null) {
                log().warn("ConstantValueSpec.matches: matches not allowed for constant value.");
				throw new IllegalStateException("Illegal to use matches with constant type values");
            }
			return true;
		}


		public String getResult(Event srcEvent) {
			return m_constant.getResult();
		}

	}

	class ValueSpecUnspecified extends ValueSpec {
		
		public boolean matches(Event e) {
			// TODO: this should probably throw an exception since it makes no sense
			return true;
		}

		public String getResult(Event srcEvent) {
			return "value unspecified";
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
			List<ValueSpec> nestedValues = new ArrayList<ValueSpec>();
			for (Value val : m_val.getValueCollection()) {
				nestedValues.add(EventTranslatorConfigFactory.this.getValueSpec(val));
			}
			return nestedValues;
		}

		public boolean matches(Event e) {
		    for (ValueSpec nestedVal : getNestedValues()) {
				if (!nestedVal.matches(e))
					return false;
			}
		    
		    Query query = createQuery(e);
		    int rowCount = query.execute();

		    if (rowCount < 1) {
                log().info("No results found for query "+query.reproduceStatement()+". No match.");
                return false;
		    }
		    
		    return true;
		}
		
		private class Query {
		    SingleResultQuerier m_querier;
		    Object[] m_args;
		    
		    Query(SingleResultQuerier querier, Object[] args) {
		        m_querier = querier;
		        m_args = args;
		    }

		    public int getRowCount() {
		        return m_querier.getCount();
		    }

		    public int execute() {
		        m_querier.execute(m_args);
		        return getRowCount();
		    }
		    
		    public String reproduceStatement() {
		        return m_querier.reproduceStatement(m_args);
		    }
		    
		    public Object getResult() {
		        return m_querier.getResult();
		    }
		    
		}
		
		public Query createQuery(Event srcEvent) {
            Object[] args = new Object[getNestedValues().size()];
            SingleResultQuerier querier = new SingleResultQuerier(m_dbConnFactory, m_val.getResult());
            for (int i = 0; i < args.length; i++) {
                args[i] = (getNestedValues().get(i)).getResult(srcEvent);
            }
            
            return new Query(querier, args);
		}
		
		public String getResult(Event srcEvent) {
		    Query query = createQuery(srcEvent);
            query.execute();
			if (query.getRowCount() < 1) {
                log().info("No results found for query "+query.reproduceStatement()+". Returning null");
				return null;
			}
			else {
			    Object result = query.getResult();
			    if (log().isDebugEnabled()) {
			        log().debug("getResult: result of single result querier is:"+result);
			    }
			    if (result != null) {
			        return result.toString();
			    } else {
			        return null;
			    }
			}
		}
		
	}

	abstract class AttributeValueSpec extends ValueSpec {
		Value m_val;
		AttributeValueSpec(Value val) { m_val = val; }

		public boolean matches(Event e) {
			
			String attributeValue = getAttributeValue(e);
			if (attributeValue == null) {
                log().debug("AttributeValueSpec.matches: Event attributeValue doesn't match because attributeValue itself is null");
                return false;
            }

			if (m_val.getMatches() == null) {
			    if (log().isDebugEnabled()) {
			        log().debug("AttributeValueSpec.matches: Event attributeValue: "+attributeValue+" matches because pattern is null");
			    }
                return true;
            }

			Pattern p = Pattern.compile(m_val.getMatches());
			Matcher m = p.matcher(attributeValue);

			if (log().isDebugEnabled()) {
			    log().debug("AttributeValueSpec.matches: Event attributeValue: " + attributeValue + " " +
			                (m.matches()? "matches" : "doesn't match") + " pattern: " + m_val.getMatches());
			}
            if (m.matches()) {
                return true;
            } else {
                return false;
            }
		}

		public String getResult(Event srcEvent) {
			if (m_val.getMatches() == null) return m_val.getResult();

			String attributeValue = getAttributeValue(srcEvent);

			if (attributeValue == null) {
				throw new TranslationFailedException("failed to match null against '"+m_val.getMatches()+"' for attribute "+getAttributeName());
			}

			Pattern p = Pattern.compile(m_val.getMatches());
			final Matcher m = p.matcher(attributeValue);
			if (!m.matches())
				throw new TranslationFailedException("failed to match "+attributeValue+" against '"+m_val.getMatches()+"' for attribute "+getAttributeName());
			
			MatchTable matches = new MatchTable(m);

			return PropertiesUtils.substitute(m_val.getResult(), matches);
		}
		
		public String getAttributeName() { return m_val.getName(); }


		abstract public String getAttributeValue(Event e);
	}
    
    // XXX: This is here because Spring converting to a String appears
    // to be broken.  It if probably a Hack and we probably need to have
    // a better way to access the Spring property editors and convert
    // to a string more correctly.
    class StringPropertyEditor extends PropertyEditorSupport {

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

		public String getAttributeValue(Event e) {
			try {
				BeanWrapper bean = getBeanWrapper(e);
                
				return (String)bean.convertIfNecessary(bean.getPropertyValue(getAttributeName()), String.class);
			} catch (FatalBeanException ex) {
				log().error("Property "+getAttributeName()+" does not exist on Event", ex);
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
		ParameterValueSpec(Value val) { super(val); }
		
		public String getAttributeValue(Event e) {
			
			String attrName = getAttributeName();
			for (Parm parm : e.getParmCollection()) {
				
                if (parm.getParmName().equals(attrName)) {
                    if (log().isDebugEnabled()) {
                        log().debug("getAttributeValue: eventParm name: '"+parm.getParmName()+" equals translation parameter name: '"+attrName);
                    }
                    return (parm.getValue() == null ? "" : parm.getValue().getContent());
                }
                
				String trimmedAttrName = StringUtils.removeStart(attrName, "~");
				
                if (attrName.startsWith("~") && (parm.getParmName().matches(trimmedAttrName))) {
                    if (log().isDebugEnabled()) {
                        log().debug("getAttributeValue: eventParm name: '"+parm.getParmName()+" matches translation parameter name expression: '"+trimmedAttrName);
                    }
                    return (parm.getValue() == null ? "" : parm.getValue().getContent());
				}
			}
			return null;
		}
	}
	


}
