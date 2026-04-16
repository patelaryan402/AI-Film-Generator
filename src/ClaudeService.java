import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import java.util.concurrent.TimeUnit;

public class ClaudeService {

    // ✅ FIXED (removed name:)
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private static final String MODEL = "gemini-2.5-flash";

    private static final String API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/"
        + MODEL + ":generateContent?key=" + API_KEY;

    // ✅ FIXED MediaType syntax
    private static final MediaType JSON_TYPE =
        MediaType.get("application/json; charset=utf-8");

    public static String ask(String userMessage) throws Exception {

        // ✅ FIXED escape (removed labels like target:, replacement:)
        String safeMsg = userMessage
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "");

        // JSON body (same)
        String body = "{"
            + "\"contents\":[{"
            + "\"parts\":[{"
            + "\"text\":\"" + safeMsg + "\""
            + "}]"
            + "}]"
            + "}";

        // ✅ ADDED timeout (important)
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

        // Request
        Request request = new Request.Builder()
            .url(API_URL)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(JSON_TYPE, body))
            .build();

        // ✅ ADDED retry logic
        Response response = null;
        int retries = 3;

        for (int i = 0; i < retries; i++) {
            try {
                response = client.newCall(request).execute();
                break;
            } catch (Exception e) {
                if (i == retries - 1) throw e;
                Thread.sleep(2000);
            }
        }

        String responseBody = response.body().string();

        if (!response.isSuccessful()) {
            throw new Exception("API Error " + response.code() + ": " + responseBody);
        }

        // Parse response
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