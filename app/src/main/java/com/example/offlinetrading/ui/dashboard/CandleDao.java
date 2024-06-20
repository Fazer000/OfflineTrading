package com.example.offlinetrading.ui.dashboard;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CandleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Candle> candles);

    @Query("SELECT * FROM candles WHERE cryptocurrency = :selectedCryptocurrency")

    List<Candle> getAllCandles(String selectedCryptocurrency);

    @Query("SELECT DISTINCT cryptocurrency FROM candles")
    List<String> getAllUniqueCryptocurrencies();
}
