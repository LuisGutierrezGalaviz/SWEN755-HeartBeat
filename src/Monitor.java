import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashSet;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Monitor {
    public static int activeApplication = 7;
    public static String lastBody = "";
    public HashSet<Integer> active = new HashSet<>();

    public Integer getActiveApplication() {
        for (Integer entry : active) {
            return entry;
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
                    if (active.contains(activeApplication)) {
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
            active.add(appNumber);
        }
    }
}
