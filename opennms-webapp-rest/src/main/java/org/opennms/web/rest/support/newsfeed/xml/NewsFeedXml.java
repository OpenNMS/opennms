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
