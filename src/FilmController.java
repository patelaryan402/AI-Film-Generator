import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class FilmController {

    public static void start() throws Exception {

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        // Route: /chat — develop story idea
        server.createContext("/chat", new HttpHandler() {
            public void handle(HttpExchange ex)
                    throws IOException {
                String idea = readBody(ex);
                String prompt = "You are a film development partner. "
                    + "The user says: " + idea
                    + ". Ask one helpful question to develop their story.";
                respond(ex, prompt);
            }
        });

        // Route: /script — generate film script
        server.createContext("/script", new HttpHandler() {
            public void handle(HttpExchange ex)
                    throws IOException {
                String idea = readBody(ex);
                String prompt =
                    "Write a short film script with title, logline, "
                    + "characters, and 3 acts (Setup, Confrontation, "
                    + "Resolution) with dialogue for this idea: "
                    + idea;
                respond(ex, prompt);
            }
        });

        // Route: /scenes — break into scenes
        server.createContext("/scenes", new HttpHandler() {
            public void handle(HttpExchange ex)
                    throws IOException {
                String idea = readBody(ex);
                String prompt =
                    "Break this short film into 5 scenes. "
                    + "For each scene write: SCENE NUMBER, TITLE, "
                    + "LOCATION, WHAT HAPPENS, KEY VISUAL. Story: "
                    + idea;
                respond(ex, prompt);
            }
        });

        // Route: /narration — write voiceover
        server.createContext("/narration", new HttpHandler() {
            public void handle(HttpExchange ex)
                    throws IOException {
                String idea = readBody(ex);
                String prompt =
                    "Write cinematic voiceover narration in 3 parts "
                    + "labeled OPENING, MIDDLE, CLOSING for: "
                    + idea;
                respond(ex, prompt);
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://localhost:8080");
        System.out.println("Open web/index.html in your browser!");
    }

    // Read text from the request body
    private static String readBody(HttpExchange ex)
            throws IOException {
        InputStream is = ex.getRequestBody();
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        return new String(bytes, "UTF-8");
    }

    // Call Claude AI and send result back to browser
    private static void respond(HttpExchange ex, String prompt)
            throws IOException {
        String result;
        try {
            result = ClaudeService.ask(prompt);
        } catch (Exception e) {
            result = "Error: " + e.getMessage();
        }
        try {
            sendResponse(ex, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Write HTTP response with CORS header
    private static void sendResponse(HttpExchange ex, String text)
            throws Exception {
        ex.getResponseHeaders()
          .add("Access-Control-Allow-Origin", "*");
        byte[] bytes = text.getBytes("UTF-8");
        ex.sendResponseHeaders(200, bytes.length);
        OutputStream out = ex.getResponseBody();
        out.write(bytes);
        out.close();
    }
}