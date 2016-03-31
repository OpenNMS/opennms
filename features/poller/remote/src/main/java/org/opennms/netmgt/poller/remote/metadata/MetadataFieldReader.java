/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.remote.metadata;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.opennms.netmgt.poller.remote.PollerTheme;
import org.opennms.netmgt.poller.remote.metadata.MetadataField.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataFieldReader {
    private static Logger LOG = LoggerFactory.getLogger(MetadataFieldReader.class);
    private File m_propertyFile;

    public MetadataFieldReader() {
        m_propertyFile = new File(System.getProperty("opennms.home") + File.separator + "etc" + File.separator + "scan-report-metadata.properties");
    }

    public MetadataFieldReader(final File propertyFile) {
        m_propertyFile = propertyFile;
    }

    public Set<MetadataField> getMetadataFields() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final Set<MetadataField> fields = new LinkedHashSet<>();
        if (m_propertyFile.exists() && m_propertyFile.canRead()) {
            final Properties props = new LinkedProperties();
            Reader r = null;
            try {
                r = new FileReader(m_propertyFile);
                props.load(r);

                final Set<String> names = new LinkedHashSet<>();
                for (final Entry<Object,Object> entry : props.entrySet()) {
                    LOG.debug("{}={}", entry.getKey(), entry.getValue());
                    if (entry.getKey() != null) {
                        final String key = entry.getKey().toString().trim();
                        if (key.endsWith(".description")) {
                            names.add(key.replaceAll("\\.description$",  ""));
                        } else if (key.endsWith(".validator")) {
                            names.add(key.replaceAll("\\.validator$",  ""));
                        } else if (key.endsWith(".required")) {
                            names.add(key.replaceAll("\\.required$",  ""));
                        } else {
                            LOG.debug("Unknown metadata entry: {}", key);
                        }
                    }
                }

                for (final String name : names) {
                    final String description = props.getProperty(name + ".description");
                    final String validatorClass = props.getProperty(name + ".validator");
                    final String requiredString = props.getProperty(name + ".required");

                    @SuppressWarnings("unchecked")
                    final Class<Validator> validator = validatorClass == null? null : (Class<Validator>) Class.forName(validatorClass);
                    final Boolean required = Boolean.valueOf(requiredString);

                    fields.add(new MetadataField(name, description, validator == null? null : validator.newInstance(), required));
                }
            } catch (final IOException e) {
                LOG.warn("Failed to get metadata fields.", e);
            } finally {
                IOUtils.closeQuietly(r);
            }
        }
        return fields;
    }

    public String getTitle() {
        final String title = getProperty("gui.title");
        if (title == null) {
            return "On-Demand Scan Report";
        }
        return title;
    }

    public URL getImage() {
        final String image = getProperty("gui.image");
        if (image != null && image.startsWith("http")) {
            try {
                return new URL(image);
            } catch (final MalformedURLException e) {
                LOG.warn("Invalid image URL: {}", image, e);
            }
        }
        return null;
    }

    private static final Pattern HEX_COLOR = Pattern.compile("^\\s*(\\p{XDigit}\\p{XDigit})(\\p{XDigit}\\p{XDigit})(\\p{XDigit}\\p{XDigit})(\\p{XDigit}\\p{XDigit})?\\s*$");

    public Color getColor(final String key) {
        final String color = getProperty(key);
        if (color != null) {
            final Matcher m = HEX_COLOR.matcher(color);
            if (m.matches()) {
                LOG.debug("{} matches", color);
                Integer red = Integer.valueOf(m.group(1), 16);
                Integer green = Integer.valueOf(m.group(2), 16);
                Integer blue = Integer.valueOf(m.group(3), 16);
                Integer alpha = null;
                if (m.group(4) != null) {
                    alpha = Integer.valueOf(m.group(4), 16);
                }
                LOG.debug("red={}, green={}, blue={}, alpha={}", red, green, blue, alpha);
                if (alpha != null) {
                    return new Color(red, green, blue, alpha);
                } else {
                    return new Color(red, green, blue);
                }
            } else {
                LOG.debug("{} does not match", color);
            }
        }
        return null;
    }

    public PollerTheme getTheme() {
        return new PollerTheme(getTitle(), getImage(), getColor("gui.foreground-color"), getColor("gui.background-color"), getColor("gui.detail-color"));
    }

    protected String getProperty(final String key) {
        if (m_propertyFile.exists() && m_propertyFile.canRead()) {
            final Properties p = new Properties();
            Reader r = null;
            try {
                r = new FileReader(m_propertyFile);
                p.load(r);
                final String value = p.getProperty(key);
                if (value != null && !value.trim().isEmpty()) {
                    return value;
                }
            } catch (final IOException e) {
                LOG.warn("Failed to get {} from {}", key, m_propertyFile, e);
            } finally {
                IOUtils.closeQuietly(r);
            }
        }
        return null;
    }
}
