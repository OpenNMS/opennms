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
