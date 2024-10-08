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
package org.opennms.features.vaadin.dashboard.config.ui.editors;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Primitives;

/**
 * This class is used to construct a criteria model based on the OpenNMS' model classes.
 *
 * @author Christian Pape
 */
public class CriteriaBuilderHelper {
    private static final Logger LOG = LoggerFactory.getLogger(CriteriaBuilderHelper.class);

    /**
     * the map of properties
     */
    private Map<String, Class<?>> m_entities = new LinkedHashMap<String, Class<?>>();
    /**
     * the map of parsers
     */
    private Map<Class<?>, CriteriaParser<?>> m_parsers = new HashMap<Class<?>, CriteriaParser<?>>();

    /**
     * Constructor used to instantiate new objects.
     *
     * @param entityType the base entity class
     * @param aliasTypes the remaining "joined" model classes
     */
    public CriteriaBuilderHelper(Class<?> entityType, Class<?>... aliasTypes) {
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
            final DateTimeFormatter parserFormatter = new DateTimeFormatterBuilder()
                    .append(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .toFormatter();

            final String DELTA_PATTERN = "^[+-]\\d+$";

            private ZonedDateTime parseZonedDateTinme(final String string) {
                if ("0".equals(string)) {
                    return ZonedDateTime.now();
                } else {
                    if (string.matches(DELTA_PATTERN)) {
                        final long delta = Long.parseLong(string.substring(1));
                        if (string.charAt(0) == '+') {
                            return ZonedDateTime.now().plus(delta, ChronoUnit.SECONDS);
                        } else {
                            return ZonedDateTime.now().minus(delta, ChronoUnit.SECONDS);
                        }
                    } else {
                        try {
                            return ZonedDateTime.parse(string, parserFormatter);
                        } catch (DateTimeParseException ex) {
                            return null;
                        }
                    }
                }
            }

            @Override
            public Date parse(String string) {
                final ZonedDateTime zonedDateTime = parseZonedDateTinme(string);

                if (zonedDateTime == null) {
                    return null;
                } else {
                    return Date.from(zonedDateTime.toInstant());
                }
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

        TreeMap<String, Class<?>> sortedMap = new TreeMap<String, Class<?>>();

        for(Class<?> clazz : aliasTypes) {
            sortedMap.put(clazz.getSimpleName(), clazz);
        }
        for (Map.Entry<String, Class<?>> entry : sortedMap.entrySet()) {
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
    public Object parseCriteriaValue(Class<?> clazz, String value) {
        CriteriaParser<?> criteriaParser = m_parsers.get(clazz);

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
    public Class<?> getTypeOfProperty(String property) {
        return m_entities.get(property);
    }

    /**
     * Sets a {@link CriteriaParser} for a given class.
     *
     * @param clazz          the class to be used
     * @param criteriaParser the {@link CriteriaParser} to handle data for the class
     */
    public void setCriteriaParser(Class<?> clazz, CriteriaParser<?> criteriaParser) {
        m_parsers.put(clazz, criteriaParser);
    }

    /**
     * This method parses a criteria configuration and adds the given restrictions to the {@link CriteriaBuilder} instance.
     *
     * @param criteriaBuilder     the {@link CriteriaBuilder} to be used
     * @param configurationString the criteria configuration string
     */
    public void parseConfiguration(CriteriaBuilder criteriaBuilder, String configurationString) {
        String[] entries = configurationString.split("(?<=[\\)])\\.");

        for (String entry : entries) {
            String[] entryParts = entry.split("(?<!\\\\)[\\(\\),]", -1);
            for (int i = 0; i < entryParts.length; i++) {
                entryParts[i] = decode(entryParts[i]);
            }
            CriteriaRestriction criteriaRestriction = CriteriaRestriction.valueOfIgnoreCase(entryParts[0]);
            criteriaRestriction.addRestrictionToCriteriaBuilder(this, criteriaBuilder, Arrays.copyOfRange(entryParts, 1, entryParts.length));
        }
    }

    public static String decode(final String string) {
        return URLDecoder.decode(string);
    }

    public static String encode(final String string) {
        return URLEncoder.encode(string).replace("%7C", "|");
    }

    /**
     * Dumps all the entities data to System.out.
     */
    public void dump() {
        for (final Map.Entry<String, Class<?>> entry : m_entities.entrySet()) {
            LOG.debug("{} {}", entry.getKey(), entry.getValue().getSimpleName());
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
    private void populateProperties(Class<?> entityClass, boolean alias) {
        TreeMap<String, Class<?>> sortedMap = new TreeMap<String, Class<?>>(new Comparator<String>() {
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

                    Class<?> clazz = Primitives.wrap(method.getReturnType());

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
        for (Map.Entry<String, Class<?>> entry : sortedMap.entrySet()) {
            m_entities.put(entry.getKey(), entry.getValue());
        }
    }
}
