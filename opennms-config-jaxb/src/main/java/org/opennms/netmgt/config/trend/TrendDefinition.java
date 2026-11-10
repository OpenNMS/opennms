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
package org.opennms.netmgt.config.trend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="trend-definition")
@XmlAccessorType(XmlAccessType.NONE)
public class TrendDefinition implements Serializable {
    private static final long serialVersionUID = 1005268629840127148L;

    @XmlAttribute(name="name")
    private String name;

    @XmlElement(name="title")
    private String title;

    @XmlElement(name="subtitle")
    private String subtitle;

    @XmlElement(name="visible")
    private boolean visible;

    @XmlElement(name="icon")
    private String icon;

    @XmlElementWrapper(name="trend-attributes")
    @XmlElement(name="trend-attribute")
    private List<TrendAttribute> trendAttributes = new ArrayList<>();

    @XmlElement(name="descriptionLink")
    private String descriptionLink;

    @XmlElement(name="description")
    private String description;

    @XmlElement(name="query")
    private String query;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<TrendAttribute> getTrendAttributes() {
        return trendAttributes;
    }

    public void setTrendAttributes(List<TrendAttribute> trendAttributes) {
        this.trendAttributes = trendAttributes;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionLink() {
        return descriptionLink;
    }

    public void setDescriptionLink(String descriptionLink) {
        this.descriptionLink = descriptionLink;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrendDefinition that = (TrendDefinition) o;

        if (visible != that.visible) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (subtitle != null ? !subtitle.equals(that.subtitle) : that.subtitle != null) return false;
        if (icon != null ? !icon.equals(that.icon) : that.icon != null) return false;
        if (trendAttributes != null ? !trendAttributes.equals(that.trendAttributes) : that.trendAttributes != null)
            return false;
        if (descriptionLink != null ? !descriptionLink.equals(that.descriptionLink) : that.descriptionLink != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        return query != null ? query.equals(that.query) : that.query == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (subtitle != null ? subtitle.hashCode() : 0);
        result = 31 * result + (visible ? 1 : 0);
        result = 31 * result + (icon != null ? icon.hashCode() : 0);
        result = 31 * result + (trendAttributes != null ? trendAttributes.hashCode() : 0);
        result = 31 * result + (descriptionLink != null ? descriptionLink.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (query != null ? query.hashCode() : 0);
        return result;
    }
}
