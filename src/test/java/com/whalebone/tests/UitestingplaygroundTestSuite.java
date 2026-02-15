package com.whalebone.tests;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.testng.annotations.*;

import static org.testng.Assert.assertTrue;

public class UitestingplaygroundTestSuite {
    protected static final String TEST_URL = "http://uitestingplayground.com/";
    private static final long REASONABLE_TIME = 6000;
    private static final int PROGRESS_BAR_PERCENT = 75;

    protected Playwright playwright;
    protected Browser browser;
    protected Page page;

    @BeforeClass
    public void setup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @BeforeMethod
    public void createPage() {
        page = browser.newPage();
        page.navigate(TEST_URL);
        page.waitForLoadState(LoadState.LOAD);
    }

    @AfterMethod
    public void closePage() {
        if (page != null) {
            page.close();
        }
    }

    @AfterClass
    public void teardown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    public void testLoadDelay() {
        long startTime = System.currentTimeMillis();

        page.getByText("Load Delay").click();
        page.waitForURL("**/loaddelay", new Page.WaitForURLOptions().setTimeout(REASONABLE_TIME));

        long duration = System.currentTimeMillis() - startTime;

        assertTrue(duration <= REASONABLE_TIME);
    }

    @Test
    public void testProgressBar75Percent() {
        page.getByText("Progress Bar").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);

        page.locator("#startButton").click();
        page.waitForFunction("() => parseInt(document.querySelector('[role=progressbar]').getAttribute('aria-valuenow')) >= " + PROGRESS_BAR_PERCENT);
        
        page.locator("#stopButton").click();

        double percentageValue = Double.parseDouble(page.locator("[role=progressbar]").textContent().replace("%", ""));
        assertTrue(percentageValue >= PROGRESS_BAR_PERCENT);
        assertTrue(page.locator("#result").textContent().startsWith("Result: " + (int) (percentageValue - PROGRESS_BAR_PERCENT)));
    }
}
