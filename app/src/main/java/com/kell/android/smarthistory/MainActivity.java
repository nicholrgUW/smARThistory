package com.kell.android.smarthistory;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.kell.android.smarthistory.model.ArtCard;

public class MainActivity extends AppCompatActivity implements UserLoginFragment.MyMenuListener {
    public static final String APP_TAG = "smarthistory logged";
    private static ArtCard.CardInfo mCurrentCard;
    private SharedPreferences mSharedPreferences;

    public static ArtCard.CardInfo getCurrentCard() {
        return mCurrentCard;
    }

    public static void setCurrentCard(ArtCard.CardInfo currentCard) {
        mCurrentCard = currentCard;
    }

    /**
     * hideKeyboard
     *
     * Hides the soft keyboard.
     * @param context The Context
     */
    public static void hideKeyboard(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = ((Activity) context).getCurrentFocus();
        if (v == null)
            return;
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /**
     * onStart.
     *
     * Either starts UserLoginFragment or SelectListFragment depending on if a user is still logged
     * in.
     *
     * @param savedInstanceState The saved state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences(getString(R.string.SHARED_PREFS), MODE_PRIVATE);
        if (savedInstanceState != null) return;

        boolean loggedIn = mSharedPreferences.getBoolean(getString(R.string.LOGGEDIN), false);


        if (findViewById(R.id.fragment_container) != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if (!loggedIn) {
                UserLoginFragment loginFragment = new UserLoginFragment();
                fragmentTransaction.add(R.id.fragment_container, loginFragment).commit();
            } else {
                SelectListFragment selectListFragment = new SelectListFragment();
                fragmentTransaction.add(R.id.fragment_container, selectListFragment).commit();
            }
        }
        getSupportActionBar().hide();
    }

    @Override
    protected void onStart(){
        super.onStart();
        getSupportActionBar().hide();
    }

    public void showActionBar(){
        getSupportActionBar().show();
    }

    public void hideActionBar(){
        getSupportActionBar().hide();
    }

    /**
     * Inflates the Options Menu.
     *
     * @param menu The menu.
     * @return Returns the menu inflater.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Does an action when an option from the options menu is selected.
     *
     * @param item The item selected.
     * @return returns true if the option has been selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //This action logs the currently logged in user out.
        if (id == R.id.action_logout) {
            mSharedPreferences = getSharedPreferences(
                    getString(R.string.SHARED_PREFS), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(getString(R.string.LOGGEDIN), false);
            editor.putString("user", "");
            editor.putString("list", "");
            editor.apply();
            UserLoginFragment loginFragment = new UserLoginFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, loginFragment)
                    .commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Launches the SelectListFragment if a user is logged in on startup.
     */
    public void startListMenu() {
        SelectListFragment selectListFragment = new SelectListFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectListFragment).commit();
    }
}
