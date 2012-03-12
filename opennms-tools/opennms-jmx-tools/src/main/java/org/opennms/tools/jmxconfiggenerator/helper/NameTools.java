/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.tools.jmxconfiggenerator.helper;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Markus Neumann <markus@opennms.com>
 */
public class NameTools {

    private final static Logger logger = LoggerFactory.getLogger(NameTools.class);
    private static Map<String, String> dictionary = new HashMap<String, String>();

    public static String trimByCamelCase(String name, Integer maxLength) {
        String result = "";
        String[] nameParts = StringUtils.splitByCharacterTypeCamelCase(name);
        Integer charsOver = name.length() - maxLength;
        for (int i = 0; i < charsOver; i++) {
            Integer largest = 0;
            Integer index = 0;
            for (int j = 0; j < nameParts.length; j++) {
                if (nameParts[j].length() > largest) {
                    largest = nameParts[j].length();
                    index = j;
                }
            }
            nameParts[index] = StringUtils.chop(nameParts[index]);
        }
        for (String namePart : nameParts) {
            result = result + namePart;
        }
        return result;
    }

    public static String trimByDictionary(String name) {
        String result = "";

        String[] nameParts = StringUtils.splitByCharacterTypeCamelCase(name);
        for (int i = 0;
                i < nameParts.length;
                i++) {
            String namePart = nameParts[i];

            for (String word : dictionary.keySet()) {
                if (namePart.equalsIgnoreCase(word)) {
                    logger.debug("dictionary Hit at '{}' result '{}'", name, name.replaceAll(word, dictionary.get(word)));
                    nameParts[i] = dictionary.get(word);
                }
            }
        }
        for (String namePart : nameParts) {
            result = result + namePart;
        }
        return result;
    }

    public static void loadExtermalDictionary(String dictionaryFile) {
        Properties properties = new Properties();
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(dictionaryFile));
            properties.load(stream);
            stream.close();
        } catch (FileNotFoundException ex) {
            logger.error("'{}'", ex.getMessage());
        } catch (IOException ex) {
            logger.error("'{}'", ex.getMessage());
        }
        logger.info("Loaded '{}' external dictionary entiers from '{}'", properties.size(), dictionaryFile);
        for (Object key : properties.keySet()) {
            dictionary.put(key.toString(), properties.get(key).toString());
        }
        logger.info("Dictionary entries loaded: '{}'", dictionary.size());
    }

    public static void loadInternalDictionary() {
        Properties properties = new Properties();
        try {
            BufferedInputStream stream = new BufferedInputStream(NameTools.class.getClassLoader().getResourceAsStream("dictionary.properties"));
            properties.load(stream);
            stream.close();
        } catch (IOException ex) {
            logger.error("Load dictionary entires from internal properties files error: '{}'", ex.getMessage());
        }
        logger.info("Loaded '{}' internal dictionary entiers", properties.size());
        for (Object key : properties.keySet()) {
            dictionary.put(key.toString(), properties.get(key).toString());
        }
        logger.info("Dictionary entries loaded: '{}'", dictionary.size());
    }

}
