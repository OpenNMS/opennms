/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
/*
 Copyright 2012 Software Freedom Conservancy.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.openqa.selenium.remote;

import org.openqa.selenium.WebDriverException;

/**
 * TODO This would not be necessary if the HtmlUnitDriver project was updated to work correctly with selenium 3 and 4.
 * 
 * taken from https://github.com/SeleniumHQ/selenium/blob/selenium-2.53.1/java/client/src/org/openqa/selenium/remote/SessionNotFoundException.java
 * 
 * this has been removed from selenium 3.0 which causes the HtmlUnitDriver to fail in the groovy test
 * see https://github.com/SeleniumHQ/selenium/blob/selenium-3.14.0/java/CHANGELOG v3.0.0-beta3
 * 
 * Removed deprecated SessionNotFoundException in favour of the NoSuchSessionException.
 * Indicates that the session is not found (either terminated or not started).
 */
@Deprecated
public class SessionNotFoundException extends WebDriverException {

  public SessionNotFoundException() {
    super();
  }

  public SessionNotFoundException(String message) {
    super(message);
  }

}