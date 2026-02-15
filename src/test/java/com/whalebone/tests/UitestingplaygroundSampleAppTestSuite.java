package com.whalebone.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;

public class UitestingplaygroundSampleAppTestSuite extends UitestingplaygroundTestSuite {
    private static final String TEST_USER = "user";
    private static final String TEST_PASSWORD = "pwd";

    private static final String USER_LOGGED_OUT = "User logged out.";
    private static final String INVALID_USERNAME_PASSWORD = "Invalid username/password";
    private static final String LOG_IN = "Log In";
    private static final String LOG_OUT = "Log Out";

    private static final String USERNAME_PLACEHOLDER = "User Name";
    private static final String PASSWORD_PLACEHOLDER = "********";
    private static final String LOGIN_BUTTON_ID = "#login";
    private static final String LOGIN_STATUS_ID = "#loginstatus";
    
    @Override
    @BeforeMethod
    public void createPage() {
        super.createPage();
        page.getByText("Sample App").click();
        page.waitForLoadState(LoadState.LOAD);
    }

    @Test
    public void testSampleAppLoginSuccessful() {
        assertLoginStatus(USER_LOGGED_OUT);

        login(TEST_USER, TEST_PASSWORD);

        assertLoginButtonText(LOG_OUT);
        assertLoginStatus("Welcome, " + TEST_USER + "!");
    }

    @Test
    public void testSampleAppLogoutSuccessful() {
        login(TEST_USER, TEST_PASSWORD);
        assertLoginButtonText(LOG_OUT);

        clickLoginButton();

        assertLoginStatus(USER_LOGGED_OUT);
    }

    @Test
    public void testSampleAppLoginWrongPassword() {
        login(TEST_USER, TEST_USER);
        assertInvalidLogin();
    }

    @Test
    public void testSampleAppLoginUserNoPassword() {
        login(TEST_USER, "");
        assertInvalidLogin();
    }
    
    @Test
    public void testSampleAppLoginNoUserCorrectPassword() {
        login("", TEST_PASSWORD);
        assertInvalidLogin();
    }
    
    @Test
    public void testSampleAppLoginNoUserNoPassword() {
        clickLoginButton();
        assertInvalidLogin();
    }

    @Override
    @Test(enabled = false)
    public void testLoadDelay() {
        // disable inherited test
    }

    @Override
    @Test(enabled = false)
    public void testProgressBar75Percent() {
        // disable inherited test
    }

    // Helpers

    private void login(String username, String password) {
        if (!username.isEmpty()) {
            page.getByPlaceholder(USERNAME_PLACEHOLDER).fill(username);
        }
        if (!password.isEmpty()) {
            page.getByPlaceholder(PASSWORD_PLACEHOLDER).fill(password);
        }
        clickLoginButton();
    }

    private void clickLoginButton() {
        getLoginButton().click();
    }

    private void assertInvalidLogin() {
        assertLoginButtonText(LOG_IN);
        assertLoginStatus(INVALID_USERNAME_PASSWORD);
    }
    
    private void assertLoginButtonText(String expectedText) {
        String actualText = getLoginButton().textContent();
        assertEquals(actualText, expectedText);
    }
    
    private void assertLoginStatus(String expectedPrefix) {
        String actualStatus = getLoginStatus().textContent();
        assertTrue(actualStatus.startsWith(expectedPrefix));
    }

    private Locator getLoginButton() {
        return page.locator(LOGIN_BUTTON_ID);
    }
    
    private Locator getLoginStatus() {
        return page.locator(LOGIN_STATUS_ID);
    }
}
