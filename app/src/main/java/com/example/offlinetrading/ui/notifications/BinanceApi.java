package com.example.offlinetrading.ui.notifications;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Подключение к Binance API
public class BinanceApi {
    private static final String BASE_URL = "https://api.binance.com/api/v3/ticker/price";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void getPrices(Callback callback) {
        executor.execute(() -> {
            Request request = new Request.Builder().url(BASE_URL).build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Type listType = new TypeToken<List<CryptoPrice>>() {}.getType();
                    List<CryptoPrice> prices = gson.fromJson(json, listType);

                    // Фильтруем только пары к USDT
                    List<CryptoPrice> usdtPrices = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        usdtPrices = prices.stream()
                                .filter(price -> price.getSymbol().endsWith("USDT"))
                                .collect(Collectors.toList());
                    }

                    callback.onSuccess(usdtPrices);
                } else {
                    callback.onError(new IOException("Ошибка при получении данных: " + response.message()));
                }
            } catch (IOException e) {
                callback.onError(e);
            }
        });
    }

    public interface Callback {
        void onSuccess(List<CryptoPrice> prices);
        void onError(Exception e);
    }
}
