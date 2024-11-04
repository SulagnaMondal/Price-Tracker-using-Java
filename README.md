# Price-Tracker-using-Java
Price Tracker Application with Twilio SMS Notification
This application allows users to track product prices online and receive SMS notifications when prices drop below a specified threshold. The app uses a Swing GUI for easy user interaction and Twilio to send SMS notifications.

Requirements
Ensure the following are installed:

Java Development Kit (JDK) 8 or higher
Maven (recommended for managing dependencies)
External Libraries
The following external .jar files are required for the application to run:

Twilio SDK: For sending SMS messages.
Jsoup: For scraping product prices from websites.
Download Links:

Twilio Java SDK: twilio-8.0.0.jar <br>
Jsoup Library: jsoup-1.14.3.jar
Getting Started
Clone the Repository


Add External Libraries<br> Ensure the downloaded .jar files (Twilio and Jsoup) are added to your classpath or placed in the lib/ folder within your project.

Configure the Twilio Credentials<br> In the PriceTrackerGUI.java file, replace the placeholder values for ACCOUNT_SID and AUTH_TOKEN with your actual Twilio account credentials.

java
public static final String ACCOUNT_SID = "your_account_sid";
public static final String AUTH_TOKEN = "your_auth_token";
Setting up Twilio for SMS Notifications  <br> 

java
Copy code
private String userPhoneNumber = "your_number"; // Your verified phone number
private String twilioPhoneNumber = "twilio_number"; // Your Twilio registered number
Running the Application
Compile and Run<br> Use the following commands to compile and run the program:

bash
Copy code
javac -cp ".:lib/*" PriceTrackerGUI.java
java -cp ".:lib/*" PriceTrackerGUI
Using the Application<br>

Product URL: Enter the URL of the product you wish to track.
CSS Query: Provide the CSS selector for the price element on the page (default is set for Amazon).
Price Threshold: Enter the price below which you wish to be notified.
Click Start Tracking to begin tracking and Stop Tracking to halt.
