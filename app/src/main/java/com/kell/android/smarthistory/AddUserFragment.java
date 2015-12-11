package com.kell.android.smarthistory;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * The AddUser {@link Fragment} subclass. Adds a user and password to an online db. If the user
 * already exists an error Toast is displayed. If the fields' requirements are not met, or the
 * passwords do not match then a Toast indicating as much is displayed.
 */
public class AddUserFragment extends Fragment {

    private final String url = "http://cssgate.insttech.washington.edu/~eibbor08/addUser.php";
    private EditText mEmailText;
    private EditText mPwdText;
    private EditText mPwdConfirmText;

    public AddUserFragment() {
    }


    /**
     * Creates the view for this fragment.
     *
     * @param inflater           The view inflated for this fragment.
     * @param container          The ViewGroup this fragment is displayed in.
     * @param savedInstanceState The instance this fragment gets saved to.
     * @return the View inflater.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_user, container, false);

        mEmailText = (EditText) v.findViewById(R.id.email_text);
        mPwdText = (EditText) v.findViewById(R.id.pwd_text);
        mPwdConfirmText = (EditText) v.findViewById(R.id.pwdConfirm_text);
        Button addButton = (Button) v.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEmailText.getText().length() != 0 && mPwdText.getText().length() != 0 &&
                        mPwdText.getText().toString().equals(mPwdConfirmText.getText().toString())) {
                    String newUrl = url + "?email=" + mEmailText.getText().toString()
                            + "&password=" + mPwdText.getText().toString();
                    MainActivity.hideKeyboard(getContext());
                    new AddUserWebTask().execute(newUrl);
                } else if (mPwdText.getText().length() != 0 &&
                        !mPwdText.getText().toString().equals(mPwdConfirmText.getText().toString())) {
                    Toast.makeText(getActivity(), "Passwords do not match",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        return v;
    }

    /**
     * Private inner asynctask class to add users.
     */
    private class AddUserWebTask extends AsyncTask<String, Void, String> {
        private static final String TAG = "AddUserWebTask";

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
            // Parse JSON
            try {
                JSONObject jsonObject = new JSONObject(s);
                String status = jsonObject.getString("result");
                if (status.equalsIgnoreCase("success")) {
                    Toast.makeText(getActivity(), "User successfully added",
                            Toast.LENGTH_SHORT)
                            .show();
                    SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(
                            getString(R.string.SHARED_PREFS), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putBoolean(getString(R.string.LOGGEDIN), true);
                    editor.putString("user", mEmailText.getText().toString());
                    editor.apply();
                    ((UserLoginFragment.MyMenuListener) getActivity()).startListMenu();
                } else {
                    String reason = jsonObject.getString("error");
                    Toast.makeText(getActivity(), "Failed :" + reason,
                            Toast.LENGTH_SHORT)
                            .show();
                }

            } catch (Exception e) {
                Log.d(TAG, "Parsing JSON Exception " + e.getMessage());
            }
        }
    }
}
