[View the Software Architecture](https://github.com/I-Kirck/FairSplit/blob/master/Fairsplit%20-%20Architecture.pdf)

# Read before testing
In order for the deepLink to work you first must activate the link in the apps settings: 

App-Info → Open by default → Add link → choose fairsplit-dbf6e.wep.app
(If there is no option for this, it should work without setting the link)
The App needs to be completly closed before you click on the link.
The link can also be opened through the browser, if you use the "Medium Phone API 36" as the emulator. It does not work with the "Pixel 9 Pro" emulator.

Our pictures (GroupPicture, ProfilePicture) are only default pictures. You can choose a GroupPicture from your mobile gallery but it won’t be saved to firebase.
This is because in order to upload pictures to firebase you need to activate firebase storage. Although the first 5GB are free to use you still need to create a billing account and fill in your real credit card information. We did not feel comfortable doing so, so our pictures are still the default ones. However we have a function to upload the pictures as bitmaps, we just don’t use it.

The app was not tested for support on characters outside the standard ASCII range.

It’s possible that umlauts and ß are only available on a real phone, sometimes the emulator does not support it even if you choose a german keyboard layout.

### Dummy user login credentials:

| Email         | Password      |
| ------------- | ------------- |
| elma@live.de  | Elma123!      |
| jerry@live.de | Jery123!      |
| tom@live.de   | Tom123!       |
| nils@live.de  | Nils123!      |

### Known Bugs: 
- Tapping the backArrow  fast twice results in navigating back to the loading screen: the App needs to be restarted
- AddSpending: When distributing individual amounts you can give the creditor 0,00 Euro which results in multiple spendings being visible in the group details screen for them (one for each member). You can also make a deselected user the creditor resulting in the same bug, since they have 0,00 Euro to pay. 
- Activities sometimes don’t show the amount you have to pay or get.
