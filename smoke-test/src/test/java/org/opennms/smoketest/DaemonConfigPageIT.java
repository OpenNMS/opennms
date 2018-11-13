/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static com.jayway.awaitility.Awaitility.await;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class DaemonConfigPageIT extends OpenNMSSeleniumTestCase {
    @Rule
    public Timeout timeout = new Timeout(10, TimeUnit.MINUTES);

    @Before
    public void setUp() {
        RestAssured.baseURI = getBaseUrl();
        RestAssured.port = getServerHttpPort();
        RestAssured.basePath = "/opennms/admin/daemons/index.jsp";
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);
    }

    @After
    public void tearDown() {
        RestAssured.reset();
    }

    @Test
    public void verifyDaemonListIsShown() {
        given().get().then()
                .assertThat().statusCode(200)
                .assertThat().contentType(ContentType.HTML);

        DaemonReloadPage page = new DaemonReloadPage().open();
        Assert.assertThat(page.getDaemonRows(), hasSize(30));

        Daemon eventd = new Daemon(page, "eventd");
        Daemon snmppoller = new Daemon(page, "snmppoller");
        Daemon ticketer = new Daemon(page, "ticketer");

        Assert.assertThat(eventd.getName(), is("eventd"));
        Assert.assertThat(eventd.getStatus(), is("running"));
        Assert.assertThat(eventd.isReloadable(), is(true));

        Assert.assertThat(snmppoller.getName(), is("snmppoller"));
        Assert.assertThat(snmppoller.getStatus(), is("not running"));
        Assert.assertThat(snmppoller.isReloadable(), is(false));

        Assert.assertThat(ticketer.getName(), is("ticketer"));
        Assert.assertThat(ticketer.getStatus(), is("running"));
        Assert.assertThat(ticketer.isReloadable(), is(false));


        eventd.reload(5);
    }

    private class Daemon {
        private final DaemonReloadPage page;
        private final String daemonName;

        public Daemon(DaemonReloadPage page, String daemonName) {
            this.page = page;
            this.daemonName = daemonName;
        }

        public String getName() {
            // Assert That Right Name is shown
            WebElement nameCell = getElement().findElements(By.xpath("./td[1]")).get(0);
            return nameCell.getText();
        }

        public String getStatus() {
            WebElement label = getElement().findElements(By.xpath("./td[2]/label")).get(0);
            return label.getText();
        }

        public boolean isReloadable() {
            WebElement reloadCell = getElement().findElements(By.xpath("./td[3]")).get(0);
            return !reloadCell.getText().equals("Daemon not reloadable");
        }

        public void reload(int maxResultTimeInSeconds) {
            if (!this.isReloadable()) {
                throw new IllegalStateException("This daemon is not reloadable");
            }
            WebElement reloadCell = getElement().findElements(By.xpath("./td[3]")).get(0);
            List<WebElement> buttons = reloadCell.findElements(By.xpath(".//button"));
            Assert.assertThat(buttons, hasSize(2));
            buttons.get(0).click();

            //Give the Gui Time to react
            sleep(500);

            reloadCell = getElement().findElements(By.xpath("./td[3]")).get(0);
            Assert.assertThat(reloadCell.getText(), containsString("Reloading..."));

            await().atMost(maxResultTimeInSeconds, TimeUnit.SECONDS)
                    .pollDelay(1, TimeUnit.SECONDS)
                    .until(() -> {
                        WebElement successReloadCell = getElement().findElements(By.xpath("./td[3]")).get(0);
                        Assert.assertThat(successReloadCell.getText(), containsString("Success"));
                    });

        }

        private WebElement getElement() {
            return this.page.getRowForDaemonName(this.daemonName);
        }

        //public DaemonData toData() {

        //}
    }

    private class DaemonReloadPage {

        private final int expectedDaemonCount = 30;

        public DaemonReloadPage open() {
            m_driver.get(baseURI + "opennms/admin/daemons/index.jsp");
            new WebDriverWait(m_driver, 5).until((Predicate<WebDriver>) (driver) -> getDaemonRows().size() == expectedDaemonCount);
            return this;
        }

        public List<WebElement> getDaemonRows() {
            return m_driver.findElements(By.xpath("//table/tbody/tr"));
        }

        public WebElement getRowForDaemonName(String name) {
            final List<WebElement> listRows = getDaemonRows();

            for (WebElement row : listRows) {
                if (row.findElements(By.xpath("./td[1]")).get(0).getText().equals(name)) {
                    return row;
                }
            }
            throw new NoSuchElementException("There is no Table Row for a Daemon by the name of: " + name);
        }
    }
}
