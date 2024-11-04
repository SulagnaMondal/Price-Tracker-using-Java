//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.exception.ApiException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class PriceTrackerGUI {

    // Twilio Account SID and Auth Token (Replace with your credentials)
    public static final String ACCOUNT_SID = "";
    public static final String AUTH_TOKEN = "";
    private String userPhoneNumber = ""; // Replace with your phone number
    private String twilioPhoneNumber = ""; // Replace with your Twilio number

    private JFrame frame;
    private JTextField productUrlField, cssQueryField, priceThresholdField;
    private JTextArea logArea;
    private JButton startButton, stopButton;
    private ScheduledExecutorService scheduler;

    public PriceTrackerGUI() {
        // Initialize Twilio
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        // Initialize the GUI components
        frame = new JFrame("Price Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2));

        // Product URL input
        panel.add(new JLabel("Product URL:"));
        productUrlField = new JTextField(30);
        panel.add(productUrlField);

        // CSS Query input
        panel.add(new JLabel("CSS Query:"));
        cssQueryField = new JTextField(".a-price-whole"); // Default for Amazon prices
        panel.add(cssQueryField);

        // Price threshold input
        panel.add(new JLabel("Price Threshold:"));
        priceThresholdField = new JTextField();
        panel.add(priceThresholdField);

        // Buttons
        startButton = new JButton("Start Tracking");
        startButton.addActionListener(new StartButtonListener());
        panel.add(startButton);
        stopButton = new JButton("Stop Tracking");
        stopButton.addActionListener(new StopButtonListener());
        panel.add(stopButton);

        // Text area for logging price updates
        logArea = new JTextArea(10, 40);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane);

        // Add the panel to the frame and display
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);

        // Initialize the scheduler for tracking
        scheduler = Executors.newScheduledThreadPool(1);
    }

    // Start button listener
    private class StartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String productUrl = productUrlField.getText();
            String cssQuery = cssQueryField.getText();
            double priceThreshold;
            try {
                // Parse the price threshold input
                priceThreshold = Double.parseDouble(priceThresholdField.getText());
                // Schedule the price tracking task
                scheduler.scheduleAtFixedRate(new PriceCheckTask(productUrl, cssQuery,
                        priceThreshold), 0, 24, TimeUnit.HOURS);
                logArea.append("Started tracking for product: " + productUrl + "\n");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid price threshold.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Stop button listener
    private class StopButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            scheduler.shutdownNow(); // Stop all scheduled tasks
            logArea.append("Stopped tracking.\n");
        }
    }

    // Task for checking price periodically
    private class PriceCheckTask implements Runnable {
        private String productUrl;
        private String cssQuery;
        private double priceThreshold;

        public PriceCheckTask(String productUrl, String cssQuery, double priceThreshold) {
            this.productUrl = productUrl;
            this.cssQuery = cssQuery;
            this.priceThreshold = priceThreshold;
        }

        @Override
        public void run() {
            try {
                double currentPrice = PriceTracker.getProductPrice(productUrl, cssQuery);
                logArea.append("Checked Price: " + currentPrice + "\n");

                // Check if the current price is below the threshold
                if (currentPrice <= priceThreshold) {
                    logArea.append("Price has dropped to " + currentPrice + " or below.\n");

                    // Send SMS Notification
                    sendSmsNotification(currentPrice);
                }
            } catch (Exception ex) {
                logArea.append("Error checking price: " + ex.getMessage() + "\n");
            }
        }

        private void sendSmsNotification(double currentPrice) {
            logArea.append("Attempting to send SMS notification...\n");
            try {
                Message message = Message.creator(
                        new PhoneNumber(userPhoneNumber), // To number
                        new PhoneNumber(twilioPhoneNumber), // From Twilio number
                        "Price has dropped to " + currentPrice + " or below!"
                ).create();

                logArea.append("SMS sent: " + message.getSid() + "\n");
            } catch (ApiException apiException) {
                logArea.append("API Error: " + apiException.getMessage() + "\n");
                logArea.append("Status Code: " + apiException.getStatusCode() + "\n");
            } catch (Exception e) {
                logArea.append("Error sending SMS: " + e.getMessage() + "\n");
                e.printStackTrace(); // Print stack trace for detailed debugging
            }
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PriceTrackerGUI());
    }
}

// The PriceTracker class that fetches the product price using Jsoup
class PriceTracker {
    // Method to fetch the current product price using JSoup
    public static double getProductPrice(String productUrl, String cssQuery) throws Exception {
        // Connect to the website and fetch the product page HTML
        Document doc = Jsoup.connect(productUrl).get();
        // Find the price element using the provided CSS selector
        Element priceElement = doc.selectFirst(cssQuery);

        if (priceElement != null) {
            // Extract the price text and remove any non-numeric characters
            String priceText = priceElement.text().replaceAll("[^0-9.]", "");
            return Double.parseDouble(priceText); // Convert the price to a double and return it
        } else {
            throw new Exception("Price element not found");
        }
    }
}