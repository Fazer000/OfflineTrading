package com.example.offlinetrading.ui.home;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

// Принятие данных из CandleRepository и вывод на фрагмент
public class HomeViewModel extends AndroidViewModel {
    private final CandleRepository repository;
    private final MutableLiveData<List<String>> allUniqueCryptocurrencies;

    public HomeViewModel(Application application) {
        super(application);
        repository = new CandleRepository(application);
        allUniqueCryptocurrencies = repository.getAllUniqueCryptocurrencies();
        refreshCryptocurrencies();
    }

    public MutableLiveData<List<String>> getAllUniqueCryptocurrencies() {
        return allUniqueCryptocurrencies;
    }

    public void refreshCryptocurrencies() {
        repository.getAllUniqueCryptocurrencies().observeForever(allUniqueCryptocurrencies::setValue);
    }
}
