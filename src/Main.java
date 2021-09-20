import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.io.OutputStream;

/*
    Creating two threads (main and GPSLocation Thread) - Luis
    Calling https://ipwhois.app/json/129.21.145.232 every 2 seconds and storing longitude and latitude in coordinatesHistory
    Diconnecting form wifi --> fault that stops GPSLocation Thread
        Dont catch exception --> should stop thread form executing

    Sending Heartbeat message back to main thread --> Option: use data pipe https://www.oreilly.com/library/view/efficient-android-threading/9781449364120/ch04.html
    When main thread no longer sees "Im alive message" in data pipe, log the failure in a file (fault monitor action)
 */

//Thread whose main task is to fetch the car's longitude and latitude coordinates
class Main {
    private String getResponseContents(HttpURLConnection connection) throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // print result
        return (response.toString());
    }
    public static int AppNumber;

    public static void main(String[] args) throws Exception {
        AppNumber = Integer.parseInt(args[0]);
        // new Thread() // heartbeatMonitor
        (new Main()).run();
    }
    public void run() throws Exception
    {
        //String array to hold coordinates. Limited to 30 entries to simulate limited memory
        String[] coordinatesHistory = new String[AppNumber];
        int coordinatesHistoryIdx = 0;
        final String USER_AGENT = "Mozilla/5.0";
        final String GET_IP_URL = "https://api.ipify.org/";
        final String GET_URL = "https://ipwhois.app/json/";

        while(true){
            // System.out.println("Fetching Car's Coordinates");

            URL getIpUrl = new URL(GET_IP_URL);
            HttpURLConnection getIpCon = (HttpURLConnection) getIpUrl.openConnection();
            String myIp;
            if (getIpCon.getResponseCode() == HttpURLConnection.HTTP_OK) { // success
                myIp = getResponseContents(getIpCon);
                // System.out.println("Your IP: " + myIp);
            } else {
                // System.out.println("GET request not worked");
                return;  // Crash thread
            }
            URL url = new URL(GET_URL + myIp);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = con.getResponseCode();
            // System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                String json = getResponseContents(con);

                String[] list = json.split(",");
                String coordinates = "";
                for(int i=0; i<list.length; i++){
                    if(list[i].contains("latitude")){
                        String[] latitudeAttrValue = list[i].split(":");
                        coordinates += latitudeAttrValue[1] + ", ";
                    }
                    if(list[i].contains("longitude")){
                        String[] longitudeAttrValue = list[i].split(":");
                        coordinates += longitudeAttrValue[1];
                    }
                }
                coordinatesHistory[coordinatesHistoryIdx] = coordinates;
                coordinatesHistoryIdx ++;

                for(int i=0; i<coordinatesHistory.length; i++){
                    // System.out.println(coordinatesHistory[i]);
                }
            } else {
                // System.out.println("GET request not worked");
                return;  // Crash thread
            }


            Thread.sleep(1000);

            // Ping the monitor with the current location
            URL monitorURL = new URL("http://127.0.0.1:8080");
            HttpURLConnection conn = (HttpURLConnection)monitorURL.openConnection();
            // POST request here
            conn.setRequestProperty("AppNumber", Integer.toString(AppNumber));
            conn.setDoOutput(true);
            conn.getOutputStream().write(coordinatesHistory[coordinatesHistoryIdx-1].getBytes());
            conn.getResponseCode();
        }
    }
}
