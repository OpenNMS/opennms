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

package org.opennms.smoketest.ui.framework.search;

import org.opennms.smoketest.ui.framework.TextInput;
import org.opennms.smoketest.ui.framework.UiElement;
import org.opennms.smoketest.ui.framework.search.result.SearchResult;
import org.openqa.selenium.WebDriver;

public class CentralSearch extends UiElement {
    public CentralSearch(WebDriver driver) {
        super(driver, "onms-search-query");
    }

    public SearchResult search(String input) {
        if (input.length() < 3) {
            throw new IllegalArgumentException("Search must be longer than 3, but only got: " + input.length());
        }
        new TextInput(getDriver(), this.elementId).setInput(input);
        return new SearchResult(getDriver(), input);
    }
}
