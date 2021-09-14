import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/*
    Creating two threads (main and GPSLocation Thread) - Luis
    Calling https://ipwhois.app/json/129.21.145.232 every 2 seconds and storing longitude and latitude in coordinatesHistory
    Diconnecting form wifi --> fault that stops GPSLocation Thread
        Dont catch exception --> should stop thread form executing

    Sending Heartbeat message back to main thread --> Option: use data pipe https://www.oreilly.com/library/view/efficient-android-threading/9781449364120/ch04.html
    When main thread no longer sees "Im alive message" in data pipe, log the failure in a file (fault monitor action)
 */

//Thread whose main task is to fetch the car's longitude and latitude coordinates
class GPSLocation extends Thread {
    @Override
    public void run() {
        //String array to hold coordinates. Limited to 30 entries to simulate limited memory
        String[] coordinatesHistory = new String[30];
        final String USER_AGENT = "Mozilla/5.0";
        final String GET_URL = "https://ipwhois.app/json/129.21.145.232";

        while(true){
            System.out.println("Fetching Car's Coordinates");

            try{
                URL url = new URL(GET_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", USER_AGENT);
                int responseCode = con.getResponseCode();
                System.out.println("GET Response Code :: " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) { // success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // print result
                    System.out.println(response.toString());
                } else {
                    System.out.println("GET request not worked");
                }
            } catch(MalformedURLException e) {
                System.out.println(e);
            } catch(IOException e) {
                System.out.println(e);
            }


            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                System.out.println(e);
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        GPSLocation getGPSLocationThread = new GPSLocation();
        getGPSLocationThread.start();

        while(true) {
            System.out.println("Main Method Thread executing!");

            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                System.out.println(e);
            }


        }
    }
}
