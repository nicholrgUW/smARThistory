package com.kell.android.smarthistory.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robbie on 10/27/2015.
 */
public class ArtCard {
    /**
     * A list of all the CardInfo objects in this list.
     */
    public static final List<CardInfo> ITEMS = new ArrayList<>();


    /**
     * Inner class containing all the info for a card.
     */
    public static class CardInfo {
        private final byte[] image;
        private final String artist;
        private final String year;
        private final String info;
        private final String title;
        private String url;

        /**
         * Constructor for CardInfo.
         *
         * @param title  The title of the work.
         * @param artist The artist who created the work.
         * @param year   The year or period the work was created in.
         * @param info   Information regarding the work.
         * @param url    A url for an image of the work.
         */
        public CardInfo(String title, String artist, String year, String info, String url) {
            this.title = title;
            this.artist = artist;
            this.year = year;
            this.url = url;
            this.image = null;
            this.info = info;
        }

        public CardInfo(String title, String artist, String year, String info, byte[] image) {
            this.title = title;
            this.artist = artist;
            this.year = year;
            this.image = image;
            this.info = info;
        }

        public String getUrl() {
            return url;
        }

        public byte[] getImage() {
            return image;
        }

        /**
         * Returns the info string.
         *
         * @return the info string.
         */
        public String getInfo() {
            return info;
        }

        /**
         * Returns the artist string.
         *
         * @return the artist string
         */
        public String getArtist() {
            return artist;
        }


        /**
         * Returns the year string.
         *
         * @return the year string
         */
        public String getYear() {
            return year;
        }


        /**
         * Returns the title string.
         *
         * @return the title string
         */
        public String getTitle() {
            return title;
        }

        @Override
        public String toString() {
            return "Title: " + title + " Artist: " + artist;
        }
    }
}
