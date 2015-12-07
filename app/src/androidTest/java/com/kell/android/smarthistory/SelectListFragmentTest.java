package com.kell.android.smarthistory;

import android.test.ActivityInstrumentationTestCase2;

import com.kell.android.smarthistory.model.CardList;
import com.robotium.solo.Solo;

import java.util.Random;

/**
 * Created by Robbie on 12/6/2015.
 */
public class SelectListFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;


    public SelectListFragmentTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
        solo.clickOnActionBarItem(0);
        solo.clickOnMenuItem("Logout", true);
        solo.enterText(0, "test2@test.com");
        solo.enterText(1, "qwerty");
        solo.clickOnButton("Login");
    }

    @Override
    public void tearDown() throws Exception {
        //tearDown() is run after a test case has finished.
        //finishOpenedActivities() will finish all the activities that have been opened during the test execution.
        solo.finishOpenedActivities();
    }

    public void testAddList() {
        solo.clickOnView(solo.getView(R.id.add_list_button));
        Random random = new Random();
        int randomNum = random.nextInt(10000);
        solo.enterText(0, "list " + randomNum);
        solo.clickOnButton("OK");
        boolean textFound = solo.searchText("List successfully created");
        assertTrue("List added", textFound);
    }

    public void testRemoveList() {
        solo.clickLongInList(CardList.ITEMS.size());
        solo.clickOnMenuItem("Delete List");
        boolean textFound = solo.searchText("List Deleted");
        assertTrue("List removed", textFound);
    }

    public void testEditList() {
        solo.clickLongInList(CardList.ITEMS.size());
        solo.clickOnMenuItem("Edit List Name");
        solo.enterText(0, " edit");
        solo.clickOnButton("OK");
        boolean textFound = solo.searchText("List successfully created");
        assertTrue("List edited", textFound);
    }
}
