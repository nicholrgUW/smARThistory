package com.kell.android.smarthistory;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.kell.android.smarthistory.data.CardInfoDB;
import com.kell.android.smarthistory.model.ArtCard;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * The CardList {@link Fragment} subclass. This fragment displays a list of the cards from a
 * selected List.
 */
public class CardListFragment extends Fragment {

    private static final String GET_CARDS_URL = "http://cssgate.insttech.washington.edu/~eibbor08/cards.php";
    private static final String DELETE_CARD_URL = "http://cssgate.insttech.washington.edu/~eibbor08/delCard.php";
    private String mUser;
    private String mUserList;
    private List<ArtCard.CardInfo> mList;
    private ListView mListView;
    private ArrayAdapter<ArtCard.CardInfo> mAdapter;
    private String mGetCardsURL;

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(getString(R.string.SHARED_PREFS), Context.MODE_PRIVATE);

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            mUserList = mSharedPreferences.getString("list", "");
            mUser = mSharedPreferences.getString("user", "");

            String user = null;
            String list = null;
            try {
                user = URLEncoder.encode(mUser, "utf-8");
                list = URLEncoder.encode(mUserList, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            mGetCardsURL = (GET_CARDS_URL + "?user=" + user + "&list=" + list);

            new GetCardWebTask().execute(mGetCardsURL);

        } else {
            Toast.makeText(getActivity()
                    , "No network connection available.", Toast.LENGTH_SHORT)
                    .show();
        }

        mList = ArtCard.ITEMS;
//        storeCards();
        mAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, mList);
        mListView = (ListView) getActivity().findViewById(R.id.card_list);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            //popup menu
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
                final ArtCard.CardInfo card = (ArtCard.CardInfo) adapter.getItemAtPosition(position);
                MainActivity.setCurrentCard(card);
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == R.id.edit_card) {
                            editCard();
                        } else if (item.getItemId() == R.id.delete_card) {
                            deleteCard();
                        } else if (item.getItemId() == R.id.content_sharing) {
                            Intent sendIntent = new Intent();
                            String tinyUrl = "";
                            try {
                                tinyUrl = new GetShortenedURL().execute(card.getUrl()).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }

                            sendIntent.setAction(Intent.ACTION_SEND);
                            String content = card.getTitle() + " by " + card.getArtist() + " from " + card.getYear() + " is great! " + tinyUrl;
                            sendIntent.putExtra(Intent.EXTRA_TEXT, content);
                            sendIntent.setType("text/plain");
                            startActivity(sendIntent);
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.menu_card);
                popupMenu.show();
                return true;
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                ArtCard.CardInfo card = (ArtCard.CardInfo) adapter.getItemAtPosition(position);
                MainActivity.setCurrentCard(card);
                CardFragment cardFragment = new CardFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, cardFragment)
                        .addToBackStack(null).commit();

            }
        });
    }

    /**
     * @param menu     The menu to inflate for this fragment.
     * @param v        The View.
     * @param menuInfo The ContextMenuInfo.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_card, menu);
    }

    /**
     * Creates the view for this fragment.
     *
     * @param inflater           The view inflated for this fragment.
     * @param container          The ViewGroup this fragment is displayed in.
     * @param savedInstanceState The instance this fragment gets saved to.
     * @return the View inflater.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ArtCard.ITEMS.clear();
        View v = inflater.inflate(R.layout.fragment_card_list, container, false);
        mListView = (ListView) v.findViewById(R.id.card_list);
        setHasOptionsMenu(true);
        registerForContextMenu(mListView);
        FloatingActionButton addCardBtn = (FloatingActionButton) v.findViewById(R.id.add_card);
        addCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddCardFragment addCardFragment = new AddCardFragment();
                Bundle args = new Bundle();
                args.putBoolean("isEdit", false);
                addCardFragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, addCardFragment)
                        .addToBackStack(null).commit();
            }
        });


        return v;
    }

    /**
     * onCreateOptionsMenu
     * <p/>
     * Inflates the menu for this fragment.
     *
     * @param menu     The menu to inflate for this fragment.
     * @param inflater The Android inflater.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_switch_view, menu);
    }

    /**
     * onOptionsItemSelected
     * <p/>
     * Does an action for the selected menu item.
     *
     * @param item The menu item selected.
     * @return Returns true if an item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_switch_view:
                CardFlipperFragment cardFlipperFragment = new CardFlipperFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, cardFlipperFragment).addToBackStack(null).commit();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Helper method for editing the selected card.
     */
    private void editCard() {
        AddCardFragment addCardFragment = new AddCardFragment();
        Bundle args = new Bundle();
        args.putBoolean("isEdit", true);
        addCardFragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, addCardFragment)
                .addToBackStack(null).commit();
    }

    /**
     * Helper method for deleting the selected card.
     */
    private void deleteCard() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Delete this card?");
        dialog.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new DeleteCardWebTask().execute(mUser, mUserList);
                        CardInfoDB cardInfoDB = new CardInfoDB(getActivity());
                        ArtCard.CardInfo cardToRemove = MainActivity.getCurrentCard();
                        cardInfoDB.deleteCardByArtistTitle(cardToRemove.getArtist(), cardToRemove.getTitle());
                        ArtCard.ITEMS.remove(MainActivity.getCurrentCard());
                        cardInfoDB.closeDB();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = dialog.create();
        alert.show();
    }


    private class DeleteCardWebTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            try {
                return downloadUrl(urls[0], urls[1]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        private String downloadUrl(String user, String list) throws IOException {
            InputStream is = null;

            int len = 500;
            user = URLEncoder.encode(user, "utf-8");
            list = URLEncoder.encode(list, "utf-8");
            String title = URLEncoder.encode(MainActivity.getCurrentCard().getTitle(), "utf-8");
            String artist = URLEncoder.encode(MainActivity.getCurrentCard().getArtist(), "utf-8");
            try {
                URL url = new URL(DELETE_CARD_URL + "?user=" + user + "&list=" + list + "&artist="
                        + artist + "&title=" + title);
                Log.d(MainActivity.APP_TAG, url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(MainActivity.APP_TAG, "The response is: " + response);
                is = conn.getInputStream();
                return readIt(is, len);
            } catch (Exception e) {
                Log.d(MainActivity.APP_TAG, "Something happened" + e.getMessage());
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
                    Log.d(MainActivity.APP_TAG, "delete card success");
                } else {
//                    String reason = jsonObject.getString("error");
                    Log.d(MainActivity.APP_TAG, "delete card failure");
                }
            } catch (Exception e) {
                Log.d(MainActivity.APP_TAG, "Parsing JSON Exception " + e.getMessage());
            }
            new GetCardWebTask().execute(mGetCardsURL);
        }
    }

    private class GetShortenedURL extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    String tinyURL = "http://tinyurl.com/api-create.php?url=";
                    URL urlObject = new URL(tinyURL + url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                    Log.d(MainActivity.APP_TAG, "The string is: " + response);
                } catch (Exception e) {
                    response = "Unable to download the list of courses, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    //This private class gets all the Cards for the List open.
    private class GetCardWebTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();

                    InputStream content = urlConnection.getInputStream();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                    Log.d(MainActivity.APP_TAG, "The string is: " + response);
                } catch (Exception e) {
                    response = "Unable to download the list of courses, Reason: "
                            + e.getMessage();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Parse JSON
            try {
                mList.clear();
                ArtCard.ITEMS.clear();
                JSONArray jsonarray;
                if (!s.contains("emptyList!")) {
                    jsonarray = new JSONArray(s);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonarray.get(i);
                        String title = (String) jsonObject.get("title");
                        String artist = (String) jsonObject.get("artist");
                        String year = (String) jsonObject.get("year");
                        String info = (String) jsonObject.get("info");
                        String url = (String) jsonObject.get("url");
                        title = URLDecoder.decode(title, "utf-8");
                        artist = URLDecoder.decode(artist, "utf-8");
                        year = URLDecoder.decode(year, "utf-8");
                        info = URLDecoder.decode(info, "utf-8");
                        url = URLDecoder.decode(url, "utf-8");
                        title = title.replaceAll("@@", "'");
                        year = year.replaceAll("@@", "'");
                        info = info.replaceAll("@@", "'");
                        url = url.replaceAll("@@", "'");
                        artist = artist.replaceAll("@@", "'");

                        ArtCard.ITEMS.add(new ArtCard.CardInfo(title, artist, year, info, url));
                    }
                } else {
                    Toast.makeText(getActivity(), "List is empty", Toast.LENGTH_SHORT)
                            .show();
                }
                mList = ArtCard.ITEMS;
                mListView.setAdapter(mAdapter);
            } catch (Exception e) {
                Log.d(MainActivity.APP_TAG, "Parsing JSON Exception " + e.getMessage());
            }
        }
    }


}


