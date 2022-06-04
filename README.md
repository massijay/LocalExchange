# Local Exchange
Repository for the Android module of the "Sitemi integrati e mobili" course's project.

## What is Local Exchange App
Local Exchange App is a platform for exchanging goods or services primarily focused on the user current location.
It features a straightforward way to see what people in the neighborhood have to offer or need thanks to the main view, which consists mainly of a map with pictures of what others have to offer.

## Structure of the project
The project uses mainly the MVVM and the observer patterns, with a clear distinction of the roles of each component.

Since the main way to navigate through the platform content is using the map that is always available on the screen, other views like content details, search or add announce are built with Fragments and displayed in the bottom sheet that can expand or collapse.

The app adapts to the system theme and language, with Italian and English localizations available.

## Some screenshots
### Main map view
In the main view the map is entirely visible and shows a preview of the items directly on it.

<img src="https://github.com/massijay/LocalExchange/blob/1c98163b779f3761d7f814fa880667e2411cdc28/screenshot/main_view.png" height="500">

### List view
Expanding the bottom sheet there is a list with the same items currently shown in the map, useful when there are many items.

<img src="https://github.com/massijay/LocalExchange/blob/1c98163b779f3761d7f814fa880667e2411cdc28/screenshot/list_view.png" height="500">

### Item details view
Tapping on a item either on the map or in the list opens the item's details view with all informations as well as a button to view the item location on Google Maps to show directions.

<img src="https://github.com/massijay/LocalExchange/blob/1c98163b779f3761d7f814fa880667e2411cdc28/screenshot/details_view.png" height="500">

### Add announce view
Users can add their announce with all details with the ability to take a photo with the camera or choose it from the gallery

<img src="https://github.com/massijay/LocalExchange/blob/1c98163b779f3761d7f814fa880667e2411cdc28/screenshot/upload_view.png" height="500">

