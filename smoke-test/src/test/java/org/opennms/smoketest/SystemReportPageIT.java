package org.opennms.smoketest;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;


public class SystemReportPageIT extends UiPageTest  {


    private Page uiPage;

    @Before
    public void setUp() throws Exception {
        uiPage = new SystemReportPageIT.Page(getBaseUrlInternal());
        uiPage.open();
    }

    @Test
    public void systemReport() throws IOException, InterruptedException {


        WebElement filenameInput = driver.findElement(By.id("filename"));
        filenameInput.sendKeys("abc@!321.txt");
        WebElement generateButton = driver.findElement(By.xpath("//input[@class='btn btn-secondary' and @value='Generate System Report']"));
        generateButton.click();
        execute(() -> {
            pageContainsText("abc321.txt");
            return null;
        });

    }

    private class Page {
        private final String url;

        public Page(String baseUrl) {
            this.url = Objects.requireNonNull(baseUrl) + "opennms/admin/support/systemReport.htm";
        }
        public Page open() {
            driver.get(url);
            System.out.println(url);
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(pageContainsText("Choose which plugins to enable:"));
            return this;
        }



    }
}
