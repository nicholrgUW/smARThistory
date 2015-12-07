package com.kell.android.smarthistory;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import java.util.Random;

/**
 * Created by Robbie on 12/6/2015.
 */
public class LoginFragmentsTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;

    public LoginFragmentsTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
        solo.clickOnActionBarItem(0);
        solo.clickOnMenuItem("Logout", true);
    }

    @Override
    public void tearDown() throws Exception {
        //tearDown() is run after a test case has finished.
        //finishOpenedActivities() will finish all the activities that have been opened during the test execution.
        solo.clickOnActionBarItem(0);
        solo.clickOnMenuItem("Logout", true);
        solo.finishOpenedActivities();
    }

    public void testLoginFragment() {
        solo.enterText(0, "test@test.com");
        solo.enterText(1, "qwerty");
        solo.clickOnButton("Login");
        boolean textFound = solo.searchText("Login success");
        assertTrue("User login passed", textFound);
    }

    public void testAddUserFragment() {
        solo.clickOnView(solo.getView(R.id.new_user_button));
        Random random = new Random();
        int number = random.nextInt(10000);
        solo.enterText(0, "test@test" + number + ".org");
        solo.enterText(1, "somepassword");
        solo.enterText(2, "somepassword");

        solo.clickOnButton("Add User");
        boolean textFound = solo.searchText("User successfully added");
        assertTrue("User add passed", textFound);

    }

    public void testAddDuplicateUserFragment() {
        solo.clickOnView(solo.getView(R.id.new_user_button));
        solo.enterText(0, "test@test.com");
        solo.enterText(1, "qwerty");
        solo.enterText(2, "qwerty");
        solo.clickOnButton("Add User");
        boolean textFound = solo.searchText("Failed :");
        assertTrue("User add failed", textFound);

    }
}
