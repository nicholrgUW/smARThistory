package com.kell.android.smarthistory;

/**
 * Created by Robbie on 12/6/2015.
 */


import com.kell.android.smarthistory.model.ArtCard;

import junit.framework.TestCase;


/**
 * Created by Kell on 12/6/2015.
 */


public class CardTest extends TestCase {
    String mTitle;
    String mArtist;
    String mYear;
    String mInfo;
    String mUrl;
    ArtCard.CardInfo mCard;

    public void setUp() {
        mTitle = "test";
        mArtist = "tester";
        mYear = "2015";
        mInfo = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sit amet " +
                "urna at velit porttitor dictum. Donec nec orci blandit, tempor enim vel, " +
                "euismod tortor. Nulla nec cursus turpis. Maecenas hendrerit eget enim " +
                "elementum sollicitudin. Vivamus lacinia.";
        mUrl = "www.test.com/someImage.jpg";
        mCard = new ArtCard.CardInfo(mTitle, mArtist, mYear, mInfo, mUrl);
    }

    public void testConstructor() {

        assertNotNull(mCard);
    }

    public void testGetters() {
        String info = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean sit amet " +
                "urna at velit porttitor dictum. Donec nec orci blandit, tempor enim vel, " +
                "euismod tortor. Nulla nec cursus turpis. Maecenas hendrerit eget enim " +
                "elementum sollicitudin. Vivamus lacinia.";

        assertEquals("test", mCard.getTitle());
        assertEquals("tester", mCard.getArtist());
        assertEquals("2015", mCard.getYear());
        assertEquals(info, mCard.getInfo());
        assertEquals("www.test.com/someImage.jpg", mCard.getUrl());
    }

    public void testList(){
        ArtCard.CardInfo card2 = new ArtCard.CardInfo("test2", mArtist, mYear, mInfo, mUrl);

        ArtCard.ITEMS.add(mCard);
        assertEquals(ArtCard.ITEMS.size(), 1);

        ArtCard.ITEMS.add(card2);
        assertEquals(ArtCard.ITEMS.size(), 2);

        ArtCard.ITEMS.clear();
        assertEquals(ArtCard.ITEMS.size(), 0);
        }
}
