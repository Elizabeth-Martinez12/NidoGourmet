package com.example.comederomvil;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager {

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    private static WebSocketManager instance;
    private WebSocket webSocket;
    private final OkHttpClient client;
    private final Handler mainHandler;
    private String SERVER_URL;

    private MessageListener messageListener;

    private WebSocketManager() {
        client = new OkHttpClient();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public void removeMessageListener() {
        this.messageListener = null;
    }

    public void connect(Context context, String token) {
        if (webSocket != null) return;

        SERVER_URL = context.getString(R.string.websocket);

        Request request = new Request.Builder().url(SERVER_URL).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull okhttp3.Response response) {
                mainHandler.post(() ->
                        Toast.makeText(context, "Conectado al WebSocket", Toast.LENGTH_SHORT).show()
                );

                JSONObject json = new JSONObject();
                try {
                    json.put("type", "user_connect");
                    json.put("token", token);
                    json.put("movil", "si");
                    webSocket.send(json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                mainHandler.post(() -> {
                    if (messageListener != null) {
                        messageListener.onMessageReceived(text);
                    } else {
                        Toast.makeText(context, "Mensaje recibido (sin listener): " + text, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, okhttp3.Response response) {
                mainHandler.post(() ->
                        Toast.makeText(context, "WebSocket error: " + t.getMessage(), Toast.LENGTH_SHORT).show()
                );
                WebSocketManager.this.webSocket = null;
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                webSocket.close(1000, null);
                WebSocketManager.this.webSocket = null;
            }
        });
    }

    public void send(String json) {
        if (webSocket != null) {
            webSocket.send(json);
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, null);
            webSocket = null;
        }
    }
}
