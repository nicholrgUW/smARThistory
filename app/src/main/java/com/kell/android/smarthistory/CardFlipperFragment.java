package com.kell.android.smarthistory;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bartoszlipinski.flippablestackview.FlippableStackView;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;
import com.kell.android.smarthistory.model.ArtCard;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment creates a flippable stack of CardFragments to display all the cards in a selected list.
 */
public class CardFlipperFragment extends Fragment {
    private List<Fragment> mViewPagerFragments;
    private CardFragmentAdapter mPageAdapter;

    public CardFlipperFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();

        createCardFragments();

        mPageAdapter = new CardFragmentAdapter(getChildFragmentManager(), mViewPagerFragments);
        boolean portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        FlippableStackView flippableStack = (FlippableStackView) getActivity().findViewById(R.id.flippable_stack_view);
        flippableStack.initStack(4, portrait ?
                StackPageTransformer.Orientation.VERTICAL :
                StackPageTransformer.Orientation.HORIZONTAL);
        flippableStack.setAdapter(mPageAdapter);

    }

    /**
     * onResume
     * <p/>
     * Notifies mPageAdapter of any changes made.
     */
    @Override
    public void onResume() {
        super.onResume();
        mPageAdapter.notifyDataSetChanged();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_card_flipper, container, false);
        setHasOptionsMenu(true);

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
                getFragmentManager().popBackStackImmediate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Helper method to create card fragments for the card flipper.
     */
    private void createCardFragments() {
        int numberOfCards = ArtCard.ITEMS.size();
        mViewPagerFragments = new ArrayList<>();

        for (int i = numberOfCards - 1; i >= 0; i--) {
            mViewPagerFragments.add(CardFragment.newInstance(i, numberOfCards));
            Log.d(MainActivity.APP_TAG, "creating card " + i);
        }
    }

    private class CardFragmentAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments;

        public CardFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(MainActivity.APP_TAG, "fragment flipper @ " + position);
            if (position != -1 && position < getCount())
                return this.fragments.get(position);
            else
                return this.fragments.get(0);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }

}
