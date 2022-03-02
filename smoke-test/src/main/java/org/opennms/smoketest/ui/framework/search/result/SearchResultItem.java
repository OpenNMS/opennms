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

import java.util.Objects;

import org.opennms.smoketest.ui.framework.Element;
import org.openqa.selenium.WebDriver;

public class SearchResultItem extends Element {
    private final ItemType type;
    private final String id;
    private final String label;


    public SearchResultItem(WebDriver driver, ItemType type, String elementId, String label) {
        super(driver);
        this.type = Objects.requireNonNull(type);
        this.id = Objects.requireNonNull(elementId);
        this.label = Objects.requireNonNull(label);
    }

    public String getLabel() {
        return this.label;
    }

    public ItemType getType() {
        return type;
    }

    public void click() {
        findElementById(id).click();
    }
}
