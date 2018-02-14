/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.web.svclayer.support.PropertyUtils;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * The Class AssetSuggestionsRestService.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component("assetSuggestionsRestService")
@Path("assets")
public class AssetSuggestionsRestService extends OnmsRestService implements InitializingBean {

    /** The Constant BLACK_LIST. */
    private static final Set<String> BLACK_LIST = new HashSet<>();

    static {
        BLACK_LIST.add("class");
        BLACK_LIST.add("geolocation");
        BLACK_LIST.add("lastModifiedDate");
        BLACK_LIST.add("lastModifiedBy");
    }

    /** The Asset DAO. */
    @Autowired
    protected AssetRecordDao m_assetDao;

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_assetDao, "AssetRecordDao is required.");
    }

    /**
     * The Class Suggestions.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="suggestions")
    public static class Suggestions extends TreeMap<String, SuggestionList> {

        /**
         * Gets the suggestions.
         *
         * @return the suggestions
         */
        @XmlElementWrapper(name="mappings")
        public Map<String, SuggestionList> getSuggestions() {
            return this;
        }
    }

    /**
     * The Class SuggestionList.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="suggestions")
    public static class SuggestionList extends JaxbListWrapper<String> {

        /**
         * Instantiates a new suggestion list.
         */
        public SuggestionList() {
            super();
        }

        /**
         * Instantiates a new suggestion list.
         *
         * @param c the c
         */
        public SuggestionList(Collection<? extends String> c) {
            super(c);
        }

        /**
         * Gets the suggestions.
         *
         * @return the suggestions
         */
        @XmlElement(name="suggestion")
        public List<String> getSuggestions() {
            final List<String> elements = getObjects();
            Collections.sort(elements);
            return elements;
        }
    }

    /**
     * Gets the asset suggestions.
     *
     * @return the asset suggestions
     */
    @GET
    @Path("suggestions")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Suggestions getAssetSuggestions() {
        final Suggestions suggestions = new Suggestions();
        final List<OnmsAssetRecord> distinctAssetProperties = m_assetDao.getDistinctProperties();
        final List<String> attributes = PropertyUtils.getProperties(new OnmsAssetRecord()).stream().filter(a -> !BLACK_LIST.contains(a)).collect(Collectors.toList());
        distinctAssetProperties.forEach(record -> {
            final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(record);
            attributes.forEach(attribute -> {
                if (! suggestions.containsKey(attribute)) {
                    suggestions.put(attribute, new SuggestionList());
                }
                final Object value = wrapper.getPropertyValue(attribute);
                if (value != null) {
                    final SuggestionList list = suggestions.get(attribute);
                    if (!list.contains(value.toString())) {
                        list.add(value.toString());
                    }
                }
            });
        });
        return suggestions;
    }

}
