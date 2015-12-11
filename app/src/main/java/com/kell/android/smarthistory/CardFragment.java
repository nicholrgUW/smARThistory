package com.kell.android.smarthistory;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kell.android.smarthistory.data.CardInfoDB;
import com.kell.android.smarthistory.model.ArtCard;

import java.io.InputStream;

/**
 * The CardFrament {@link Fragment} subclass. This fragment displays all the data stored in a CardInfo
 * object. This includes Work title, artist, year, information, and displays an image from the url
 * stored in the CardInfo object.
 */
public class CardFragment extends Fragment {
    private ArtCard.CardInfo mCard;
    private String mUser;
    private String mList;
    private View mInfoView;
    private View mImageView;
    private boolean mShowImage;
    private int mShortAnimationDuration;


    public CardFragment() {
    }

    /**
     * newInstance
     * <p/>
     * Receives arguments, constructs, and returns a CardFragment.
     *
     * @param i     The index of the cardInfo in ArtCard.ITEMS.
     * @param total The total number of cards.
     * @return A card fragment with data corresponding to the CardInfo item specified by i.
     */
    public static CardFragment newInstance(int i, int total) {
        CardFragment fragment = new CardFragment();
        Bundle bdl = new Bundle();
        bdl.putInt("cardIndex", i);
        bdl.putInt("total", total);
        bdl.putBoolean("flipMode", true);
        fragment.setArguments(bdl);
        return fragment;
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
        View v = inflater.inflate(R.layout.fragment_card, container, false);
        ((MainActivity) getActivity()).showActionBar();

        Bundle bdl = getArguments();
        if (bdl != null) {
            int index = bdl.getInt("cardIndex");
            int total = bdl.getInt("total");
            mCard = ArtCard.ITEMS.get(index);
            mShortAnimationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);
            String indexText = "Card " + (index + 1) + " of " + total;
            ((TextView) v.findViewById(R.id.card_number)).setText(indexText);
        } else {
            mCard = MainActivity.getCurrentCard();
        }
        mInfoView = v.findViewById(R.id.info);
        mImageView = v.findViewById(R.id.image_card);
        mInfoView.setVisibility(View.GONE);

        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mShowImage = !mShowImage;
                showImageOrInfo(mShowImage);
                return true;
            }
        });

        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(
                getString(R.string.SHARED_PREFS), Context.MODE_PRIVATE);
        mList = mSharedPreferences.getString("list", "");
        mUser = mSharedPreferences.getString("user", "");

        loadCardInfo(v);
        storeCard();
        attemptDBLoad(v);

        return v;
    }

    /**
     * Sets the TextViews for the this CardFragment.
     *
     * @param v The View.
     */
    private void loadCardInfo(View v) {
        TextView title = (TextView) v.findViewById(R.id.title_card);
        title.setText(title.getText() + ": " + mCard.getTitle());
        TextView artist = (TextView) v.findViewById(R.id.artist_card);
        artist.setText(artist.getText() + ": " + mCard.getArtist());
        TextView year = (TextView) v.findViewById(R.id.year_card);
        year.setText(year.getText() + ": " + mCard.getYear());
        TextView facts = (TextView) v.findViewById(R.id.facts_card);
        facts.setText(facts.getText() + ": " + mCard.getInfo());

    }

    /**
     * attemptDBLoad
     * <p/>
     * Attempts to load the image for this CardFragment from the local db. If there is no stored
     * image it is downloaded and stored.
     *
     * @param v The View
     */
    private void attemptDBLoad(View v) {
        CardInfoDB cardInfoDB = new CardInfoDB(v.getContext());
        byte[] imageAsByte = cardInfoDB.getImage(mUser, mList, mCard.getArtist(), mCard.getTitle());

        Log.d(MainActivity.APP_TAG, mUser + " " + mList + " " + mCard.getArtist() + " " + mCard.getTitle());
        if (imageAsByte == null) {
            new DownloadImage((ImageView) v.findViewById(R.id.image_card)).execute(mCard.getUrl());
            Log.d(MainActivity.APP_TAG, "null image");
        } else {
            ImageView imageView = (ImageView) v.findViewById(R.id.image_card);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageAsByte, 0, imageAsByte.length));
            Log.d(MainActivity.APP_TAG, "loaded image");
        }
        cardInfoDB.closeDB();
    }

    /**
     * storeCard
     * <p/>
     * Stores the card in the local db.
     */
    private void storeCard() {
        Activity activity = getActivity();
        CardInfoDB cardInfoDB = new CardInfoDB(activity);
        Log.d(MainActivity.APP_TAG, mUser + " " + mList + " " + mCard.getArtist() + " " + mCard.getTitle());

        if (cardInfoDB.insertCard(mUser, mList, mCard.getArtist(), mCard.getTitle(),
                mCard.getYear(), mCard.getInfo(), mCard.getUrl()))
            Log.d(MainActivity.APP_TAG, "card stored locally");
        else
            Log.d(MainActivity.APP_TAG, "failed to store card locally");

        cardInfoDB.closeDB();
    }

    /**
     * Switches the view to either show the card info, or the card image.
     *
     * @param showImage Show the image.
     */
    private void showImageOrInfo(boolean showImage) {
        final View showView = showImage ? mInfoView : mImageView;
        final View hideView = showImage ? mImageView : mInfoView;

        showView.setAlpha(0f);
        showView.setVisibility(View.VISIBLE);

        showView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        hideView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideView.setVisibility(View.GONE);
                    }
                });
    }

    //This private class downloads the image from the URL for the current Card.
    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        final ImageView image;

        //add code to store image in SQLite, too
        public DownloadImage(ImageView image) {
            this.image = image;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap theImage = null;
            try {
                Log.d("CardFragment", "DownloadingImage");
                InputStream in = new java.net.URL(url).openStream();
                theImage = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return theImage;
        }

        protected void onPostExecute(Bitmap result) {
            Activity activity = getActivity();
            CardInfoDB cardInfoDB = new CardInfoDB(activity);
            cardInfoDB.updateImage(mUser, mList, mCard.getArtist(), mCard.getTitle(), result);
            image.setImageBitmap(result);
        }
    }

}
