package com.kell.android.smarthistory;


import android.content.Context;
import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;


import com.kell.android.smarthistory.data.CardInfoDB;
import com.kell.android.smarthistory.model.CardList;

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


/**
 * The SelectListFragment {@link Fragment} subclass. This class displays a clickable list of all the
 * user defined Lists (of ArtCards, should probably be renamed). Lists can be added here. If a List
 * is clicked it launches the CardListFragment (should also be renamed), which displays all the cards
 * themselves in a list.
 */
public class SelectListFragment extends Fragment {
    private static final String getListUrl = "http://cssgate.insttech.washington.edu/~eibbor08/lists.php";
    private static final String ADD_LIST_URL = "http://cssgate.insttech.washington.edu/~eibbor08/addList.php";
    private static final String DELETE_CARD_URL = "http://cssgate.insttech.washington.edu/~eibbor08/delCard.php";

    private ArrayAdapter<CardList.ListInfo> mAdapter;
    private List<CardList.ListInfo> mList;
    private ListView mListView;
    private String mUser;
    private SharedPreferences mSharedPreferences;

    public SelectListFragment() {
    }

    /**
     * Initial fragment setup
     */
    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).showActionBar();
        mSharedPreferences = getActivity().getSharedPreferences(
                getString(R.string.SHARED_PREFS), Context.MODE_PRIVATE);
        mUser = mSharedPreferences.getString("user", "");
        MainActivity.setCurrentCard(null);

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetListsWebTask().execute(getListUrl + "?user=" + mUser);//change to CardList
        } else {
            Toast.makeText(getActivity()
                    , "No network connection available. You will not be able to add lists or cards."
                    , Toast.LENGTH_LONG).show();
        }
        mListView = (ListView) getActivity().findViewById(R.id.listView);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                MainActivity.setCurrentCard(null);
                CardList.ListInfo list = (CardList.ListInfo) adapter.getItemAtPosition(position);
                mSharedPreferences.edit().putString("list", list.name).apply();

                CardListFragment cardListFragment = new CardListFragment();
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, cardListFragment, "flipper")
                        .addToBackStack(null).commit();
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            //popup menu
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                final CardList.ListInfo list = (CardList.ListInfo) adapter.getItemAtPosition(position);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.edit_list) {
                            showEditListDialog(list.name);
                            //edit name
                        } else if (item.getItemId() == R.id.delete_list) {
                            //delete list
                            deleteList(list.name);
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.menu_list);
                popupMenu.show();
                return true;
            }
        });

        mList = CardList.ITEMS;
        mAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mList);
    }

    /**
     * Helper method to delete the selected list.
     *
     * @param list the list to delete.
     */
    private void deleteList(String list) {
        CardInfoDB cardInfoDB = new CardInfoDB(getActivity());
        cardInfoDB.deleteCardByArtistTitle(mUser, list);
        new DeleteList().execute(list);
        cardInfoDB.closeDB();
        Toast.makeText(getActivity(), "List Deleted", Toast.LENGTH_SHORT).show();

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
        View v = inflater.inflate(R.layout.fragment_add_list, container, false);
        MainActivity.setCurrentCard(null);
        FloatingActionButton addList = (FloatingActionButton) v.findViewById(R.id.add_list_button);
        addList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showListDialog();
                new GetListsWebTask().execute(getListUrl + "?user=" + mUser);
            }
        });

        return v;
    }

    /**
     * Shows a dialog for creating a new list.
     */
    private void showListDialog() {
        final LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.dialog_new_list, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);

        final EditText newListEditText = (EditText) promptView.findViewById(R.id.new_list);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String listName = newListEditText.getText().toString();
                        String user = mUser;
                        try {
                            listName = URLEncoder.encode(listName, "utf-8");
                            user = URLEncoder.encode(user, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        String params = "?list=" + listName + "&user=" + user;
                        new AddListWebTask().execute(ADD_LIST_URL + params);
                        new GetListsWebTask().execute(getListUrl + "?user=" + mUser);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

    }

    /**
     * Displays a dialog populated with the selected list's information.
     *
     * @param list The list name to edit.
     */
    private void showEditListDialog(final String list) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.dialog_new_list, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);

        final EditText newListEditText = (EditText) promptView.findViewById(R.id.new_list);
        newListEditText.setText(list);
        newListEditText.setSelection(newListEditText.getText().length());
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String listName = newListEditText.getText().toString();
                        String user = mUser;
                        String oldList = list;
                        try {
                            listName = URLEncoder.encode(listName, "utf-8");
                            user = URLEncoder.encode(user, "utf-8");
                            oldList = URLEncoder.encode(oldList, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        String params = "?list=" + listName + "&user=" + user + "&olist=" + oldList;
                        new AddListWebTask().execute(ADD_LIST_URL + params);
                        new GetListsWebTask().execute(getListUrl + "?user=" + mUser);
                        Toast.makeText(getActivity(), "List edited", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

    }

    private class AddListWebTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                return downloadUrl(params[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        private String downloadUrl(String theUrl) throws IOException {
            InputStream is = null;
            try {
                Log.d("AddList", theUrl);

                URL url = new URL(theUrl);
                int len = theUrl.length();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /*milliseconds*/);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(MainActivity.APP_TAG, "The response is: " + response);
                is = conn.getInputStream();
                String contentAsString = readIt(is, len);
                Log.d(MainActivity.APP_TAG, "The string is: " + contentAsString);
                return contentAsString;
            } catch (Exception e) {
                Log.d(MainActivity.APP_TAG, "Something happened " + e.getMessage());
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
                    Toast.makeText(getActivity(), "List successfully created", Toast.LENGTH_SHORT).show();
                } else {
                    String reason = jsonObject.getString("error");
                    Toast.makeText(getActivity(), "Failed :" + reason, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.d(MainActivity.APP_TAG, "Parsing JSON Exception " + e.getMessage());
            }
        }
    }

    private class GetListsWebTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the getListUrl.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
                Log.d("GetList", myurl);

                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(MainActivity.APP_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                Log.d(MainActivity.APP_TAG, "The string is: " + contentAsString);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } catch (Exception e) {
                Log.d(MainActivity.APP_TAG, "Something happened" + e.getMessage());
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
                mList.clear();
                CardList.ITEMS.clear();
                JSONArray jsonarray;
                jsonarray = new JSONArray(s);
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonarray.get(i);
                    String name = (String) jsonObject.get("list_name");
                    name = URLDecoder.decode(name, "utf-8");
                    CardList.ITEMS.add(new CardList.ListInfo(name));
                }
                mList = CardList.ITEMS;
                mListView.setAdapter(mAdapter);
            } catch (Exception e) {
                Log.d(MainActivity.APP_TAG, "Parsing JSON Exception " + e.getMessage());
            }
        }
    }

    private class DeleteList extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    String user = URLEncoder.encode(mUser, "utf-8");
                    String list = URLEncoder.encode(url, "utf-8");
                    String strurl = DELETE_CARD_URL + "?user=" + user + "&list=" + list + "&delAll=true";
                    Log.d("DeleteList", strurl);
                    URL urlObject = new URL(strurl);
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
            new GetListsWebTask().execute(getListUrl + "?user=" + mUser);
        }
    }
}
