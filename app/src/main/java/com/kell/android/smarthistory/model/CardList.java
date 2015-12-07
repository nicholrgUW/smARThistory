package com.kell.android.smarthistory.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robbie on 11/2/2015.
 * <p/>
 * A storage class containing a list/map of Lists(User defined sets of ArtCard.CardInfo). This class
 * can probably be refactored out of existence with a smarter implementation of ArtCard.
 */
public class CardList {
    /**
     * A list containing all the ListInfo objects.
     */
    public static final List<ListInfo> ITEMS = new ArrayList<>();

    /**
     * Inner class of ListInfo objects, the objects CardList contains.
     */
    public static class ListInfo {
        public final String name;

        /**
         * Constructor for ListInfo objects.
         *
         * @param name the ListInfo object's name
         */
        public ListInfo(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
