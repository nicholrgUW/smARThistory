package com.kell.android.smarthistory;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kell.android.smarthistory.data.CardInfoDB;
import com.kell.android.smarthistory.model.ArtCard;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * The AddCard {@link Fragment} subclass. This fragment allows the user to add a card to a list.
 * The card info is stored in an online db. Card attributes such as the user and list are handled
 * automatically through SharedPrefences.
 */
public class
        AddCardFragment extends Fragment {
    private static final String THE_URL = "http://cssgate.insttech.washington.edu/~eibbor08/addCard.php";
    private EditText mTitle;
    private EditText mYear;
    private EditText mArtist;
    private EditText mInfo;
    private EditText mURL;
    private String mUser;
    private String mList;

    /**
     * Required empty public constructor
     */
    public AddCardFragment() {
    }

    /**
     * onCreateView
     * <p/>
     * Inflates the add card fragment. Sets the fragments components.
     *
     * @param inflater           The Android inflater.
     * @param container          The main container for the app.
     * @param savedInstanceState Saved Instance.
     * @return The View.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {    // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_card, container, false);
        ((MainActivity) getActivity()).showActionBar();

        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(
                getString(R.string.SHARED_PREFS), Context.MODE_PRIVATE);
        mUser = mSharedPreferences.getString("user", "");
        mList = mSharedPreferences.getString("list", "");

        mTitle = (EditText) v.findViewById(R.id.title_text);
        mYear = (EditText) v.findViewById(R.id.year_text);
        mArtist = (EditText) v.findViewById(R.id.artist_text);
        mInfo = (EditText) v.findViewById(R.id.info_text);
        mURL = (EditText) v.findViewById(R.id.url_text);

        Bundle args = this.getArguments();
        final boolean isEdit = args.getBoolean("isEdit", false);
        final ArtCard.CardInfo cardToEdit;
        String oldArtist = "";
        String oldTitle = "";
        if (isEdit) {
            cardToEdit = MainActivity.getCurrentCard();
            mTitle.setText(cardToEdit.getTitle(), TextView.BufferType.EDITABLE);
            mYear.setText(cardToEdit.getYear(), TextView.BufferType.EDITABLE);
            mArtist.setText(cardToEdit.getArtist(), TextView.BufferType.EDITABLE);
            mInfo.setText(cardToEdit.getInfo(), TextView.BufferType.EDITABLE);
            mURL.setText(cardToEdit.getUrl(), TextView.BufferType.EDITABLE);

            try {
                oldArtist = URLEncoder.encode(cardToEdit.getArtist(), "utf-8");
                oldTitle = URLEncoder.encode(cardToEdit.getTitle(), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        FloatingActionButton addBtn = (FloatingActionButton) v.findViewById(R.id.add_card_button);
        final String finalOldTitle = oldTitle;
        final String finalOldArtist = oldArtist;
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTitle.getText().length() != 0 && mYear.getText().length() != 0
                        && mArtist.getText().length() != 0 && mInfo.getText().length() != 0 && mURL.getText().length() != 0) {
                    String addCardURL = null;
                    try {
                        addCardURL = createAddCardURL(isEdit);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    addCardURL = addCardURL + "&otitle=" + finalOldTitle + "&oartist=" + finalOldArtist;
                    Log.d("params ", addCardURL);
                    new AddCardWebTask().execute(addCardURL);
                } else {
                    Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return v;
    }

    /**
     * createAddCardURL
     *
     * @param isEdit If the card is being edited.
     * @return String of the url for AddCardWebTask.
     * @throws UnsupportedEncodingException
     */
    private String createAddCardURL(boolean isEdit) throws UnsupportedEncodingException {
        String title = URLEncoder.encode(mTitle.getText().toString(), "utf-8");
        String artist = URLEncoder.encode(mArtist.getText().toString(), "utf-8");
        String year = URLEncoder.encode(mYear.getText().toString(), "utf-8");
        String info = URLEncoder.encode(mInfo.getText().toString(), "utf-8");
        String url = URLEncoder.encode(mURL.getText().toString(), "utf-8");
        String list = URLEncoder.encode(mList, "utf-8");
        String user = URLEncoder.encode(mUser, "utf-8");

        return THE_URL + "?title=" + title + "&artist=" + artist
                + "&year=" + year + "&info=" + info + "&url=" + url
                + "&list=" + list + "&user=" + user + "&edit=" + isEdit;
    }

    /**
     * Removes the current card from the local db.
     */
    private void removeLocalCard() {
        CardInfoDB cardInfoDB = new CardInfoDB(getActivity());
        ArtCard.CardInfo cardToRemove = MainActivity.getCurrentCard();
        cardInfoDB.deleteCardByArtistTitle(cardToRemove.getArtist(), cardToRemove.getTitle());
        cardInfoDB.closeDB();
    }

    //This inner class adds a card to the Card database.
    private class AddCardWebTask extends AsyncTask<String, Void, String> {

        private static final String TAG = "AddCardWebTask";

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. THE_URL may be invalid.";
            }
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                Log.d(TAG, "The string is: " + contentAsString);
                return contentAsString;
            } catch (Exception e) {
                Log.d(TAG, "Something happened" + e.getMessage());
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            return null;
        }

        // Reads an InputStream and converts it to a String.
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
                    Bundle args = getArguments();
                    if (args.getBoolean("isEdit", false)) {
                        removeLocalCard();
                    }
                    Log.d(TAG, "Card insert success");
                } else {
                    String reason = jsonObject.getString("error");
                    Log.d(TAG, "Card insert failure " + reason);
                }
                MainActivity.hideKeyboard(getContext());
                getFragmentManager().popBackStackImmediate();
            } catch (Exception e) {
                Log.d(TAG, "Parsing JSON Exception " + e.getMessage());
            }
        }
    }
}
