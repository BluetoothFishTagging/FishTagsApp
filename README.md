# FishTagsApplication
Mobile application built to work in conjunction with an RFID reader that reads information from a tagged fish and transfers it to the phone. The application parses the data, asks the user for some information, and sends the data to the research center database.

# Structure:
- ~~Camera~~
	- Not Implemented
	- Intended to be Camera Object dealing with interface
	- May be unnecessary
- Client
	- Interaction with the Server
	- Capable of Sending Multipart Data (image,text...)
- GPS
	- Fetches GPS Info
- Linkage
	- Editing Photo-Tag association
- ParseFile
	- Bundling File-Parsing for Tag Data
- Storage
	- Provides Interface to In-App Storage
- Wifi
	- Checks Wi-Fi Connection
