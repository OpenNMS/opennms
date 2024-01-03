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

package org.opennms.web.rest.support.newsfeed.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class NewsFeedXml {
    public static class ItemElement {
        private String title;
        private String link;
        private String description;

        private List<String> categories = new ArrayList<>();

        @XmlElement(name="title")
        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @XmlElement(name="link")
        public String getLink() {
            return this.link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        @XmlElement(name="description")
        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @XmlElement(name="category")
        public List<String> getCategories() {
            return this.categories;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories;
        }
    }

    public static class ChannelElement {
        private String title;

        private List<ItemElement> items =  new ArrayList<>();

        @XmlElement(name="title")
        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @XmlElement(name="item", type=NewsFeedXml.ItemElement.class)
        public List<ItemElement> getItems() {
            return this.items;
        }

        public void setItem(List<ItemElement> items) {
            this.items = items;
        }
    }

    @XmlRootElement(name="rss", namespace="")
    public static class RssElement {
        private ChannelElement channelElement;

        @XmlElement(name="channel", type= NewsFeedXml.ChannelElement.class)
        public NewsFeedXml.ChannelElement getChannelElement() {
            return this.channelElement;
        }

        public void setChannelElement(NewsFeedXml.ChannelElement c) {
            this.channelElement = c;
        }
    }
}
