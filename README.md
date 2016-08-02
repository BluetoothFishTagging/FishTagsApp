# FishTagsApplication
Mobile application built to work in conjunction with an RFID reader that reads information from a tagged fish and transfers it to the phone. The application parses the data, asks the user for some information, and sends the data to the research center database.

# Structure:
- Camera
	- Empty Activity Interfacing with Camera
- Client
	- Interaction with the Server
	- GET/POST Data 
	- Capable of Sending Multipart Data (image,text...)
- GPS
	- Fetches GPS Info
	- Supports Network Provider & GPS Provider
- ~~Linkage~~
	- Editing Photo-Tag association
	- Removed, as it was deemed unnecessary
- ParseFile
	- Bundling File-Parsing for Tag Data
- Signup
	- Handles 
- Storage
	- Provides Interface to In-App Storage
- Wifi
	- Checks Wi-Fi Connection
