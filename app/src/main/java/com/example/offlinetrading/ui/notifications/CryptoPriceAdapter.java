package com.example.offlinetrading.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.offlinetrading.R;

import java.util.List;

public class CryptoPriceAdapter extends RecyclerView.Adapter<CryptoPriceAdapter.ViewHolder> {
    private List<CryptoPrice> cryptoPrices;

    // Подключение CryptoPrice
    public CryptoPriceAdapter(List<CryptoPrice> cryptoPrices) {
        this.cryptoPrices = cryptoPrices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crypto_price, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CryptoPrice cryptoPrice = cryptoPrices.get(position);
        holder.symbolTextView.setText(cryptoPrice.getSymbol());
        holder.priceTextView.setText(cryptoPrice.getPrice());
    }

    @Override
    public int getItemCount() {
        return cryptoPrices.size();
    }

    public void updateData(List<CryptoPrice> newCryptoPrices) {
        this.cryptoPrices = newCryptoPrices;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView symbolTextView;
        TextView priceTextView;

        ViewHolder(View itemView) {
            super(itemView);
            symbolTextView = itemView.findViewById(R.id.symbolTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
        }
    }
}

