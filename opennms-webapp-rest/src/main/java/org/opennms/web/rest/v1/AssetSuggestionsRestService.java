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

import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Asset", description = "Asset API")
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
