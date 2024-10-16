/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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
