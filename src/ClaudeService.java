import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

public class ClaudeService {

    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String MODEL = "gemini-2.5-flash";

    private static final String API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/" 
        + MODEL + ":generateContent?key=" + API_KEY;

    private static final MediaType JSON_TYPE =
        MediaType.get("application/json; charset=utf-8");

    public static String ask(String userMessage) throws Exception {

        // Escape message
        String safeMsg = userMessage
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "");

        // JSON body (Gemini format)
        String body = "{"
            + "\"contents\":[{"
            + "\"parts\":[{"
            + "\"text\":\"" + safeMsg + "\""
            + "}]"
            + "}]"
            + "}";

        // OkHttp client
        OkHttpClient client = new OkHttpClient();

        // Request
        Request request = new Request.Builder()
            .url(API_URL)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(JSON_TYPE, body))
            .build();

        // Response
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        if (!response.isSuccessful()) {
            throw new Exception("API Error "
                + response.code() + ": " + responseBody);
        }

        // Parse response (Gemini format)
        JsonElement root = JsonParser.parseString(responseBody);

        return root.getAsJsonObject()
            .getAsJsonArray("candidates")
            .get(0).getAsJsonObject()
            .getAsJsonObject("content")
            .getAsJsonArray("parts")
            .get(0).getAsJsonObject()
            .get("text")
            .getAsString();
    }
}