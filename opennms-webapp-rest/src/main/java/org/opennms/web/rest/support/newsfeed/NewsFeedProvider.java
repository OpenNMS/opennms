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
package org.opennms.web.rest.support.newsfeed;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.opennms.web.rest.support.newsfeed.xml.NewsFeedXml;

public class NewsFeedProvider {
    public NewsFeedProvider() {

    }

    public NewsFeedXml.RssElement parseXml(InputStream inputStream) throws javax.xml.bind.JAXBException {
        NewsFeedXml.RssElement xRssElem = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(NewsFeedXml.RssElement.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            xRssElem = (NewsFeedXml.RssElement) jaxbUnmarshaller.unmarshal(inputStream);
        } catch (JAXBException e) {
            throw e;
        }

        return xRssElem;
    }

    public NewsFeed parseXmlToNewsFeed(NewsFeedXml.RssElement xRssElem) throws Exception {
        final NewsFeed newsFeed = new NewsFeed();

        var channelElem = xRssElem.getChannelElement();
        newsFeed.channelTitle = channelElem.getTitle();

        var items = new ArrayList<NewsFeedItem>();

        for (var xItem : channelElem.getItems()) {
            var item = new NewsFeedItem();

            item.title = xItem.getTitle();
            item.description = xItem.getDescription();
            item.shortDescription = getShortDescription(xItem.getDescription());
            item.link = xItem.getLink();

            item.setCategories(parseCategories(xItem.getCategories()));
            item.setTags(parseTags(xItem.getCategories()));

            items.add(item);
        }

        newsFeed.setItems(items);

        return newsFeed;
    }

    private String getShortDescription(String description) {
        final int TRUNCATE_LENGTH = 200;

        final int pStart = description.indexOf("<p>");
        final int pEnd = description.indexOf("</p>");
        final String innerText = pStart >= 0 && pEnd > (pStart + 3) ? description.substring(pStart + 3, pEnd) : "";
        final String shortDescription = innerText.length() > TRUNCATE_LENGTH ? (innerText.substring(0, TRUNCATE_LENGTH) + "...") : innerText;

        return shortDescription;
    }

    private List<String> parseCategories(List<String> categories) {
        final List<String> cats =
            categories.stream()
                .filter(x -> x.length() > 0 && Character.isUpperCase(x.charAt(0)))
                .sorted()
                .collect(Collectors.toList());

        return cats;
    }

    private List<String> parseTags(List<String> categories) {
        final List<String> tags =
            categories.stream()
                .filter(x -> x.length() > 0 && Character.isLowerCase(x.charAt(0)))
                .sorted()
                .collect(Collectors.toList());

        return tags;
    }
}
