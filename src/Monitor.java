import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.io.OutputStream;

public class Monitor {
    public static int activeApplication = 7;
    public static String lastBody = "";
    // TODO make a HashSet<Integer>
    public HashMap<Integer, Boolean> active = new HashMap<>();

    public Integer getActiveApplication() {
        for (Map.Entry<Integer, Boolean> entry : active.entrySet()) {
            if (entry.getValue()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void run(String[] args) throws InterruptedException {
        Thread heartbeatReceiver = new Thread(() -> {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
                server.createContext("/", new MyHandler());
                server.setExecutor(null); // creates a default executor
                server.start();
            } catch (Exception e) {
                System.out.println(e);
            }
        });
        Thread timer = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(1100);
                    System.out.println("Coordinates: " + lastBody);
                    if (active.containsKey(activeApplication) && active.get(activeApplication)) {
                        // OK
                        System.out.println("Active application " + activeApplication + " is alive");
                    } else {
                        Integer newActiveApplication = getActiveApplication();
                        if (newActiveApplication == null) {
                            System.out.println("No applications are active");
                        } else {
                            System.out.println(
                                    "Switching from application " + activeApplication + " to " + newActiveApplication);
                            activeApplication = newActiveApplication;
                        }
                    }
                    // Spawn new applications if existing ones fail:
                    // if(active.size() < 2) {
                    //     int newApplicationCode;
                    //     do {
                    //         newApplicationCode = (new Random()).nextInt(100);
                    //     } while(active.containsKey(newApplicationCode)); // Repeat until we get an unused application code
                    //     System.out.println("Starting new application " + newApplicationCode);
                    //     Runtime.getRuntime().exec("java Main " + newApplicationCode + "&");
                    // }
                    // Reset active list
                    active.clear();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        });
        heartbeatReceiver.start();
        timer.start();
        heartbeatReceiver.join();
        timer.join();
    }

    public static void main(String[] args) throws InterruptedException {
        (new Monitor()).run(args);
    }

    class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!t.getRequestHeaders().containsKey("AppNumber")) {
                t.sendResponseHeaders(400, 0);
                t.getResponseBody().close();
                return; // Ignore non-authenticated
            }
            int appNumber = Integer.parseInt(t.getRequestHeaders().get("AppNumber").get(0));
            String body = new String(t.getRequestBody().readAllBytes());
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.flush();
            os.close();
            if (activeApplication == appNumber) {
                lastBody = body;
            }
            active.put(appNumber, true);
        }
    }
}
