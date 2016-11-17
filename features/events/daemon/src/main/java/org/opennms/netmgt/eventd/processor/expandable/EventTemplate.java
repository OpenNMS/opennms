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

package org.opennms.netmgt.eventd.processor.expandable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * The {@link EventTemplate} represents the event input, containing place-holders, e.g. 'nodeLabel',
 * and support to expand the template.
 *
 * For each placeholder in the {@link EventTemplate#input} a {@link ExpandableToken} is created.
 * The expandable state of the {@link EventTemplate} is the expanded state of all tokens ({@link EventTemplate#tokens}.
 */
public class EventTemplate implements ExpandableToken {

    private static final Logger LOG = LoggerFactory.getLogger(EventTemplate.class);

    private static final char PERCENT = '%';

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile(".*\\s(?s).*");

    private final String input;

    private final List<ExpandableToken> tokens = Lists.newArrayList();

    private final EventUtil eventUtil;

    private final boolean requiresTransaction;

    public EventTemplate(String input, EventUtil eventUtil) {
        this.input = Objects.requireNonNull(input);
        this.eventUtil = Objects.requireNonNull(eventUtil);
        parse();
        this.requiresTransaction = tokens.stream().filter(t -> t.requiresTransaction()).findAny().isPresent();
    }

    /**
     * Parses the input and creates {@link ExpandableToken} to expand it.
     */
    private void parse() {
        tokens.clear();
        String tempInp = input;
        int inpLen = input.length();

        int index1 = -1;
        int index2 = -1;

        // check input string to see if it has any %xxx% substring
        while ((tempInp != null) && ((index1 = tempInp.indexOf(PERCENT)) != -1)) {

            LOG.debug("checking input {}", tempInp);
            // copy till first %
            tokens.add(new ExpandableConstant(tempInp.substring(0, index1)));
            tempInp = tempInp.substring(index1);

            index2 = tempInp.indexOf(PERCENT, 1);
            if (index2 != -1) {
                // Get the value between the %s
                String parm = tempInp.substring(1, index2);
                LOG.debug("parm: {} found in value", parm);

                // If there's any whitespace in between the % signs, then do not try to
                // expand it with a parameter value
                if (WHITESPACE_PATTERN.matcher(parm).matches()) {
                    tokens.add(new ExpandableConstant(PERCENT));
                    tempInp = tempInp.substring(1);
                    LOG.debug("skipping parm: {} because whitespace found in value", parm);
                    continue;
                }

                tokens.add(new ExpandableParameter(parm, eventUtil));

                if (index2 < (inpLen - 1)) {
                    tempInp = tempInp.substring(index2 + 1);
                } else {
                    tempInp = null;
                }
            } else {
                break;
            }
        }
        if ((index1 == -1 || index2 == -1) && (tempInp != null)) {
            tokens.add(new ExpandableConstant(tempInp));
        }
    }

    @Override
    public String expand(Event event, Map<String, Map<String, String>> decode) {
        final String collect = tokens.stream()
                .map(t -> t.expand(event, decode))
                .collect(Collectors.joining());
        return collect;
    }

    // If we find any token which requires a transaction, the template itself requires a transaction as well
    @Override
    public boolean requiresTransaction() {
        return requiresTransaction;
    }
}
