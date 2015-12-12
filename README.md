# smARThistory
To use smARThistory at this phase of development, you must be connected to the web. After the initial
launch of the app, you will land on a login screen. Click the register text to launch the registration fragment. After logging in or creating an account, 
you will see an empty or partially filled listView of Card Lists. These lists are how lists of Cards will
be chosen and identified. A List can be added with the floating action button at the bottom of the screen. 
To edit or delete the List, click and hold the List you wish to change. This will display a popup menu with 
options to edit or delete this List.

After successfully creating or entering a List by clicking it, you will be greeted with a nearly identical 
screen displaying Cards, which are displayed in a list of title-artist pairs. To edit, delete, or share 
(Content Sharing! — this sends the title, artist, and shortened link to the image) click and hold the Card
you wish to modify or share.

From here you can either click on a Card, which will then display the Card in Single Card Mode or click 
SWITCH VIEW. If you click and hold the display the display will switch views to display the Card info. Click
and hold again to switch back to the image. If wish to return to the List of Cards, hit back.

If you clicked SWITCH VIEW you will be greeted by a stack of all the Cards in the selected List. It might take 
a lit bit if the images are being retrieved for the the first time. Like Single Card Mode, if you click and hold
 a Card, it will change its view. To move between the Cards swipe up or down.

To logout, click the vertical ellipsis options, then click Logout. 

TODO <br>
Implement Wikipedia autocomplete option for cards.

I've used <a href="https://github.com/blipinsk/FlippableStackView">Bartosz Lipiński's FlippableStackView</a> for the flippable fragment stack.
