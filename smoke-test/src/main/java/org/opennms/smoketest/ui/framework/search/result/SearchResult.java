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
package org.opennms.smoketest.ui.framework.search.result;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.smoketest.ui.framework.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SearchResult extends Element {

    private String input;

    public SearchResult(WebDriver driver, String input) {
        super(driver);
        this.input = Objects.requireNonNull(input);
    }

    public List<SearchResultItem> getItems() {
        final List<WebElement> elements = getDriver().findElements(By.xpath("//div[@id='onms-search-result']//*[contains(@class, 'list-group-item')]"));
        final List<SearchResultItem> items = elements.stream().map(element -> {
            if (element.getTagName().equalsIgnoreCase("div")) {
                return new SearchResultItem(getDriver(), ItemType.Header, element.getAttribute("id"), element.getText());
            }
            if (element.getTagName().equalsIgnoreCase("a")) {
                if (element.getText().contains("more...")) {
                    return new SearchResultItem(getDriver(), ItemType.More, element.getAttribute("id"), element.getText());
                }
                return new SearchResultItem(getDriver(), ItemType.Item, element.getAttribute("id"), element.getText());
            }
            throw new IllegalStateException("Could not determine item type. Bailing");
        }).collect(Collectors.toList());
        return items;
    }

    public SearchResultItem getSingleItem() {
        return getItems().stream()
                .filter(i -> i.getType() == ItemType.Item).findFirst()
                .orElseThrow(() -> new RuntimeException("Not a single item found"));
    }

    public ContextSearchResult forContext(String context) {
        return new ContextSearchResult(getDriver(), input, context);
    }

    public long size() {
        return getItems().stream().filter(item -> item.getType() == ItemType.Item).count();
    }
}
