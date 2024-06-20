package com.example.offlinetrading.ui.dashboard;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.offlinetrading.R;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class DashboardFragment extends Fragment {

    public CandleStickChart candleStickChart;
    private AppDatabase db;
    private Call<List<Object[]>> call;

    public String selectedCryptocurrency = "BTCUSDT";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        db = AppDatabase.getInstance(getContext());

        // Подключение спиннера
        candleStickChart = view.findViewById(R.id.candleStickChart);
        Spinner cryptoSpinner = view.findViewById(R.id.crypto_spinner);

        // Подключение массива спиннера с валютами
        String[] cryptocurrencyNames = getResources().getStringArray(R.array.cryptocurrency_names);
        String[] cryptocurrencyValues = getResources().getStringArray(R.array.cryptocurrency_values);

        // Установка значений для спиннера
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, cryptocurrencyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cryptoSpinner.setAdapter(adapter);

        // Присваивание значения переменной "selectedCryptocurrency" исходя из выбранной валюты в спиннере
        cryptoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCryptocurrency = cryptocurrencyValues[position];
                fetchChartData();  // Fetch data for the selected cryptocurrency
                loadChartDataFromDatabase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (isNetworkAvailable()) {
            fetchChartData();
        } else {
            loadChartDataFromDatabase();
        }

        return view;
    }

    // Отмена сетевых запросов при выходе из фрагмента, чтобы исключить вылеты
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (call != null) {
            call.cancel();
        }

    }

    // Проверка подключение к интернету
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        Network activeNetwork = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            activeNetwork = cm.getActiveNetwork();
        }
        if (activeNetwork == null) {
            return false;
        }
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    // Подключение к Binance API и создание списков для MPAndroidChart
    public void fetchChartData() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.binance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BinanceService service = retrofit.create(BinanceService.class);

        String selectedTimeFrame = "1d";
        Call<List<Object[]>> call = service.getKlines(selectedCryptocurrency, selectedTimeFrame, 300);
        call.enqueue(new Callback<List<Object[]>>() {
            @Override
            public void onResponse(@NonNull Call<List<Object[]>> call, @NonNull Response<List<Object[]>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    List<CandleEntry> entries = new ArrayList<>();
                    List<Candle> candles = new ArrayList<>();
                    assert response.body() != null;
                    for (Object[] kline : response.body()) {
                        long timestamp = ((Double) kline[0]).longValue();
                        double open = Double.parseDouble((String) kline[1]);
                        double high = Double.parseDouble((String) kline[2]);
                        double low = Double.parseDouble((String) kline[3]);
                        double close = Double.parseDouble((String) kline[4]);

                        // Список для вывода на график когда есть интернет
                        entries.add(new CandleEntry(
                                timestamp,
                                (float) high,
                                (float) low,
                                (float) open,
                                (float) close
                        ));

                        // Список для отправки в БД когда нет интернета
                        candles.add(new Candle(
                                timestamp,
                                (float) high,
                                (float) low,
                                (float) open,
                                (float) close,
                                selectedCryptocurrency
                        ));

                    }

                    new Thread(() -> {
                        try {
                            db.runInTransaction(() -> {
                                db.candleDao().insertAll(candles);
                                db.query("PRAGMA wal_checkpoint(FULL);", null);
                                Log.d("Database", "Checkpoint completed.");
                            });
                        } catch (Exception e) {
                            Log.e("Database", "Error during transaction", e);
                        }
                    }).start();

                    setChartData(entries);
                } else {
                    Toast.makeText(requireContext(), "Не удалось сохранить данные графика", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Object[]>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // Загрузка сохраненных данных из БД и вывод их на график
    public void loadChartDataFromDatabase() {
        new Thread(() -> {
            List<Candle> candles = db.candleDao().getAllCandles(selectedCryptocurrency);
            List<CandleEntry> entries = new ArrayList<>();

            for (Candle candle : candles) {
                entries.add(new CandleEntry(
                        candle.timestamp,
                        candle.open,
                        candle.close,
                        candle.high,
                        candle.low
                ));
            }

            if (candles.isEmpty()) {
                // Вывести сообщение "График выбранной валюты не загружен" если в БД нет данных о валюте
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "График выбранной валюты не загружен", Toast.LENGTH_LONG).show();
                    candleStickChart.invalidate();
                    candleStickChart.clear();
                });
            } else {
                // Если данные есть, то выводим на график
                requireActivity().runOnUiThread(() -> setChartData(entries));
            }
        }).start();
    }

    // Оформление графика
    public void setChartData(List<CandleEntry> entries) {
        CandleDataSet dataSet = new CandleDataSet(entries, selectedCryptocurrency);
        styleDataSet(dataSet, Color.rgb(80, 80, 80), Color.rgb(122, 242, 84));

        candleStickChart.getDescription().setTextColor(Color.TRANSPARENT);
        candleStickChart.getXAxis().setTextColor(Color.TRANSPARENT);
        candleStickChart.getAxisLeft().setTextColor(Color.WHITE);
        candleStickChart.getAxisRight().setTextColor(Color.WHITE);

        CandleData candleData = new CandleData(dataSet);
        candleStickChart.setData(candleData);

        Legend legend = candleStickChart.getLegend();
        legend.setTextColor(Color.WHITE);

        XAxis xAxis = candleStickChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setAvoidFirstLastClipping(true);

        candleStickChart.moveViewToX(entries.size() - 1);

        xAxis.setAvoidFirstLastClipping(true);

        CustomMarkerView markerView = new CustomMarkerView(requireContext(), R.layout.marker_view);
        candleStickChart.setMarker(markerView);

        candleStickChart.invalidate();
    }

    // Оформление графика
    private void styleDataSet(CandleDataSet dataSet, int shadowColor, int increasingColor) {
        dataSet.setColor(shadowColor);
        dataSet.setShadowColor(Color.GRAY);
        dataSet.setShadowWidth(1f);
        dataSet.setDecreasingColor(Color.RED);
        dataSet.setDecreasingPaintStyle(Paint.Style.STROKE);
        dataSet.setIncreasingColor(increasingColor);
        dataSet.setIncreasingPaintStyle(Paint.Style.STROKE);
        dataSet.setValueTextColor(Color.TRANSPARENT);
    }

}