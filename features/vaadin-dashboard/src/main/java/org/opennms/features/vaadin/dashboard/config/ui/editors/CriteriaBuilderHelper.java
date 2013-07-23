/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.dashboard.config.ui.editors;

import com.google.gwt.thirdparty.guava.common.primitives.Primitives;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.LoggerFactory;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * This class is used to construct a criteria model based on the OpenNMS' model classes.
 *
 * @author Christian Pape
 */
public class CriteriaBuilderHelper {
    /**
     * the map of properties
     */
    private Map<String, Class> m_entities = new LinkedHashMap<String, Class>();
    /**
     * the map of parsers
     */
    private Map<Class, CriteriaParser> m_parsers = new HashMap<Class, CriteriaParser>();

    /**
     * Constructor used to instantiate new objects.
     *
     * @param entityType the base entity class
     * @param aliasTypes the remaining "joined" model classes
     */
    public CriteriaBuilderHelper(Class entityType, Class... aliasTypes) {
        /**
         * adding criteria parsers
         */
        setCriteriaParser(Integer.class, new CriteriaParser<Integer>() {
            @Override
            public Integer parse(String string) {
                int integer = 0;
                try {
                    integer = Integer.parseInt(string);
                } catch (NumberFormatException numberFormatException) {
                    return null;
                }

                return integer;
            }

            @Override
            public Integer getDefault() {
                return 0;
            }
        });

        setCriteriaParser(String.class, new CriteriaParser<String>() {
            @Override
            public String parse(String string) {
                return string;
            }

            @Override
            public String getDefault() {
                return "foo";
            }
        });

        setCriteriaParser(OnmsSeverity.class, new CriteriaParser<OnmsSeverity>() {
            @Override
            public OnmsSeverity parse(String string) {
                for (OnmsSeverity onmsSeverity : OnmsSeverity.values()) {
                    if (onmsSeverity.name().toLowerCase().equals(string.toLowerCase())) {
                        return onmsSeverity;
                    }
                }
                return null;
            }

            @Override
            public OnmsSeverity getDefault() {
                return OnmsSeverity.CLEARED;
            }
        });

        setCriteriaParser(Date.class, new CriteriaParser<Date>() {
            @Override
            public Date parse(String string) {
                Date date = null;
                try {
                    date = DateFormat.getDateInstance().parse(string);
                } catch (ParseException e) {
                    return null;
                }
                return date;
            }

            @Override
            public Date getDefault() {
                return new Date();
            }
        });

        setCriteriaParser(InetAddress.class, new CriteriaParser<InetAddress>() {
            @Override
            public InetAddress parse(String string) {
                InetAddress inetAddress = null;
                try {
                    inetAddress = InetAddress.getByName(string);
                } catch (UnknownHostException e) {
                    return null;
                } catch (SecurityException e) {
                    return null;
                }
                return inetAddress;
            }

            @Override
            public InetAddress getDefault() {
                InetAddress inetAddress = null;
                try {
                    inetAddress = InetAddress.getByName("127.0.0.1");
                } catch (UnknownHostException e) {
                    return null;
                } catch (SecurityException e) {
                    return null;
                }
                return inetAddress;
            }
        });
        /**
         * now populate the entities map
         */
        populateProperties(entityType, false);

        TreeMap<String, Class> sortedMap = new TreeMap<String, Class>();

        for(Class clazz : aliasTypes) {
            sortedMap.put(clazz.getSimpleName(), clazz);
        }
        for (Map.Entry<String, Class> entry : sortedMap.entrySet()) {
            populateProperties(entry.getValue(), true);
        }
    }

    /**
     * This method is used for parsing a criteria value.
     *
     * @param clazz the type class
     * @param value the value to be parsed
     * @return a new instance representing the value
     */
    public Object parseCriteriaValue(Class clazz, String value) {
        CriteriaParser criteriaParser = m_parsers.get(clazz);

        if (criteriaParser == null) {
            LoggerFactory.getLogger(CriteriaBuilderHelper.class).error("No parser for class " + clazz.getSimpleName() + " found");
            return null;
        } else {
            return criteriaParser.parse(value);
        }
    }

    /**
     * Returns the type of a given property.
     *
     * @param property the property to search for
     * @return the associated type
     */
    public Class getTypeOfProperty(String property) {
        return m_entities.get(property);
    }

    /**
     * Sets a {@link CriteriaParser} for a given class.
     *
     * @param clazz          the class to be used
     * @param criteriaParser the {@link CriteriaParser} to handle data for the class
     */
    public void setCriteriaParser(Class clazz, CriteriaParser criteriaParser) {
        m_parsers.put(clazz, criteriaParser);
    }

    /**
     * This method parses a criteria configuration and adds the given restrictions to the {@link CriteriaBuilder} instance.
     *
     * @param criteriaBuilder     the {@link CriteriaBuilder} to be used
     * @param configurationString the criteria configuration string
     */
    public void parseConfiguration(CriteriaBuilder criteriaBuilder, String configurationString) {
        String entries[] = configurationString.split("(?<=[\\)])\\.");

        for (String entry : entries) {
            String entryParts[] = entry.split("(?<!\\\\)[\\(\\),]", -1);
            CriteriaRestriction criteriaRestriction = CriteriaRestriction.valueOfIgnoreCase(entryParts[0]);
            criteriaRestriction.addRestrictionToCriteriaBuilder(this, criteriaBuilder, Arrays.copyOfRange(entryParts, 1, entryParts.length));
        }
    }

    /**
     * Dumps all the entities data to System.out.
     */
    public void dump() {
        for (Map.Entry<String, Class> entry : m_entities.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue().getSimpleName());
        }
    }

    /**
     * Returns the entities used in this instance.
     *
     * @return the entities
     */
    public Set<String> getEntities() {
        return m_entities.keySet();
    }

    /**
     * Populates all the properties of the given model class into the data structures of this instance.
     *
     * @param entityClass the entity's class
     * @param alias       true, if the properties should be aliased, false otherwise
     */
    private void populateProperties(Class entityClass, boolean alias) {
        TreeMap<String, Class> sortedMap = new TreeMap<String, Class>(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.toLowerCase().compareTo(b.toLowerCase());
            }
        });

        String aliasName = null;

        if (alias) {
            aliasName = entityClass.getSimpleName().replaceAll("Onms", "").toLowerCase();
        }

        for (java.lang.reflect.Method method : entityClass.getDeclaredMethods()) {
            Annotation[] annotations = method.getAnnotations();

            for (Annotation annotation : annotations) {
                if ("javax.persistence.Column".equals(annotation.annotationType().getName())) {
                    String propertyName = Introspector.decapitalize(method.getName().replace("get", ""));

                    Class clazz = Primitives.wrap(method.getReturnType());

                    if (m_parsers.containsKey(clazz)) {
                        if (aliasName != null) {
                            sortedMap.put(aliasName + "." + propertyName, clazz);
                        } else {
                            sortedMap.put(propertyName, clazz);
                        }
                    } else {
                        LoggerFactory.getLogger(CriteriaBuilderHelper.class).warn("No parser for class " + clazz.getSimpleName() + " found, ignoring property " + propertyName);
                    }
                }
            }
        }
        for (Map.Entry<String, Class> entry : sortedMap.entrySet()) {
            m_entities.put(entry.getKey(), entry.getValue());
        }
    }
}
