package com.example.offlinetrading.ui.home;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;

import com.example.offlinetrading.ui.dashboard.AppDatabase;
import com.example.offlinetrading.ui.dashboard.CandleDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Принятие данных из БД о загруженных валютах
public class CandleRepository {
    private CandleDao candleDao;
    private ExecutorService executorService;

    public CandleRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        candleDao = db.candleDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public MutableLiveData<List<String>> getAllUniqueCryptocurrencies() {
        MutableLiveData<List<String>> liveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<String> data = candleDao.getAllUniqueCryptocurrencies();
            liveData.postValue(data);
        });
        return liveData;
    }

}

