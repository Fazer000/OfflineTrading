package com.example.offlinetrading.ui.notifications;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.offlinetrading.R;

import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private CryptoPriceAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(this::fetchPrices);

        fetchPrices();

        return rootView;
    }

    private void fetchPrices() {
        BinanceApi binanceApi = new BinanceApi();
        binanceApi.getPrices(new BinanceApi.Callback() {
            @Override
            public void onSuccess(List<CryptoPrice> prices) {
                if (isAdded() && !isDetached()) {
                    requireActivity().runOnUiThread(() -> {
                        if (adapter == null) {
                            adapter = new CryptoPriceAdapter(prices);
                            recyclerView.setAdapter(adapter);
                        } else {
                            adapter.updateData(prices);
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded() && !isDetached()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Нет соединения с интернетом", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }
            }
        });
    }
}

