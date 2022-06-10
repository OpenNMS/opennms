/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.search.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each {@link SearchResult} contains 0 to n {@link SearchResultItem}s which represent an item for the context,
 * e.g. a Node, or Action.
 *
 * @author mvrueden
 */
public class SearchResultItem {

    private static final Logger LOG = LoggerFactory.getLogger(SearchResultItem.class);

    private String identifier;
    private String icon;
    private String label;
    private String url;
    private final Map<String, String> properties = new HashMap<>();
    private final List<Match> matches = new ArrayList<>();
    private int weight;
    private String info = "";

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<Match> getMatches() {
        return matches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof SearchResultItem) {
            final SearchResultItem that = (SearchResultItem) o;
            return Objects.equals(identifier, that.identifier)
                    && Objects.equals(label, that.label)
                    && Objects.equals(url, that.url)
                    && Objects.equals(properties, that.properties)
                    && Objects.equals(matches, that.matches)
                    && Objects.equals(icon, that.icon)
                    && Objects.equals(weight, that.weight);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, label, properties, matches, url, icon, weight);
    }

    public void addMatch(Match match) {
        final Optional<Match> existingMatch = matches.stream()
                .filter(m -> match.getId().equals(m.getId()))
                .findAny();
        if (existingMatch.isPresent()) {
            LOG.warn("A match with id {} already exists: {}. Will ignore Match {}", match.getId(), existingMatch.get(), match);
        } else {
            matches.add(match);
        }
    }

    public void addMatches(List<Matcher> matcherList, String input) {
        Objects.requireNonNull(matcherList);
        Objects.requireNonNull(input);
        matcherList.stream()
                .filter(m -> !"".equals(m.getValue()) && m.getValue() != null)
                .filter(m -> m.matches(input))
                .map(m -> new Match(m.getLabel(), m.getLabel(), m.getValue()))
                .forEach(m -> addMatch(m));
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public void merge(SearchResultItem mergeMe) {
        // Merge attributes
        getProperties().putAll(mergeMe.getProperties());

        // Merge Matches
        mergeMe.getMatches().forEach(m -> addMatch(m));

        // Adjust weight
        setWeight(Math.max(getWeight(), mergeMe.getWeight()));
    }

    public void setInfo(final String info) {
        this.info = info;
    }

    public String getInfo() {
        return this.info;
    }
}
