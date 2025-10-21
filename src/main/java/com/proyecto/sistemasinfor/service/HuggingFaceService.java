package com.proyecto.sistemasinfor.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HuggingFaceService {
    private static final String API_URL = "https://api-inference.huggingface.co/models/facebook/blenderbot-3B";

    @Value("${huggingface.api.token:}")
    private String apiToken;

    public String preguntar(String mensaje) {
        if (apiToken == null || apiToken.isBlank()) {
            return "El servicio de IA no está configurado.";
        }

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject().put("inputs", mensaje);

        Request request = new Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer " + apiToken)
            .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                return "No pude conectar con la IA en este momento.";
            }
            String resp = response.body().string().trim();
            // Si la respuesta es un array (respuesta normal)
            if (resp.startsWith("[")) {
                JSONArray arr = new JSONArray(resp);
                if (arr.length() > 0 && arr.getJSONObject(0).has("generated_text")) {
                    return arr.getJSONObject(0).getString("generated_text");
                }
                return "No entendí tu pregunta.";
            } else {
                // Si la respuesta es un objeto (posible error)
                JSONObject obj = new JSONObject(resp);
                if (obj.has("error")) {
                    return "Error de Hugging Face: "+ obj.getString("error");
                }
                return "No entendí tu pregunta.";
            }
        } catch (Exception e) {
            return "No pude conectar con la IA en este momento.";
        }
    }
}
