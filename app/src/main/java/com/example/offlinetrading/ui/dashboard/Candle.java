package com.example.offlinetrading.ui.dashboard;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "candles",
        indices = {@Index(value = {"timestamp", "open", "close", "high", "low", "cryptocurrency"}, unique = true)})
public class Candle {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp;
    public float open;
    public float close;
    public float high;
    public float low;
    public String cryptocurrency;

    public Candle(long timestamp, float open, float close, float high, float low, String cryptocurrency) {
        this.timestamp = timestamp;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.cryptocurrency = cryptocurrency;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCryptocurrency() {
        return cryptocurrency;
    }

    public void setCryptocurrency(String cryptocurrency) {
        this.cryptocurrency = cryptocurrency;
    }

}