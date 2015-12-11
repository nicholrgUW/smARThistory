package com.kell.android.smarthistory;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The UserLoginFragment {@link Fragment} subclass. Users can login or navigate to the register
 * {@link AddUserFragment} class.
 */
public class UserLoginFragment extends Fragment {
    private static final String url = "http://cssgate.insttech.washington.edu/~eibbor08/login.php";
    private EditText mEmailText;
    private EditText mPwdText;
    private boolean mIsConnected;

    public UserLoginFragment() {
    }



    /**
     * onStart. Calls super.onStart() and checks for web connectivity.
     */
    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).hideActionBar();

        mIsConnected = false;
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            mIsConnected = true;
        } else {
            Toast.makeText(getActivity(), "No network connection", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * onCreateView
     * <p/>
     *
     * @param inflater           The Android inflater.
     * @param container          The main container for the app.
     * @param savedInstanceState Saved Instance.
     * @return The View.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_login, container, false);
        mEmailText = (EditText) v.findViewById(R.id.login_email);
        mPwdText = (EditText) v.findViewById(R.id.login_pwd);
        Button loginButton = (Button) v.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsConnected) {
                    String email = mEmailText.getText().toString();
                    if (email.length() != 0 && mPwdText.getText().length() != 0 && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        String urlWArgs = url + "?email=" + mEmailText.getText().toString()
                                + "&password=" + mPwdText.getText().toString();
                        MainActivity.hideKeyboard(getContext());
                        new LoginUserWebTask().execute(urlWArgs);
                    } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        mEmailText.setError("enter valid email");
                        Toast.makeText(getActivity(), "Enter your email and " +
                                "password or register", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        TextView addNewUser = (TextView) v.findViewById(R.id.new_user_button);
        addNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddUserFragment addUserFragment = new AddUserFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, addUserFragment)
                        .addToBackStack(null).commit();
            }
        });
        return v;
    }

    public interface MyMenuListener {
        void startListMenu();
    }

    private class LoginUserWebTask extends AsyncTask<String, Void, String> {
        private static final String TAG = "LoginUserWebTask";

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            int len = 500;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /*milliseconds*/);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);
                is = conn.getInputStream();

                String contentAsString = readIt(is, len);
                Log.d(TAG, "The string is: " + contentAsString);
                return contentAsString;
            } catch (Exception e) {
                Log.d(TAG, "Something happened " + e.getMessage());
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            return null;
        }

        public String readIt(InputStream stream, int len) throws IOException {
            Reader reader;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String status = jsonObject.getString("result");

                if (status.equalsIgnoreCase("success")) {
                    Toast.makeText(getActivity(), "Login success", Toast.LENGTH_SHORT).show();
                    SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(
                            getString(R.string.SHARED_PREFS), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putBoolean(getString(R.string.LOGGEDIN), true);
                    editor.putString("user", mEmailText.getText().toString());
                    editor.apply();
                    ((MyMenuListener) getActivity()).startListMenu();
                } else if (status.equalsIgnoreCase("failed")) {
                    Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
                } else if (status.equalsIgnoreCase("no user")) {
                    Toast.makeText(getActivity(), "User does not exist", Toast.LENGTH_SHORT).show();
                } else {
                    String reason = jsonObject.getString("error");
                    Toast.makeText(getActivity(), "Login failed " + reason,
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.d(TAG, "Parsing JSON Exception " + e.getMessage());
            }
        }
    }
}
