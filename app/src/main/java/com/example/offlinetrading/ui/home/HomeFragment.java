package com.example.offlinetrading.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.offlinetrading.R;

public class HomeFragment extends Fragment {
    private TextView cryptocurrenciesTextView;
    private HomeViewModel homeViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView usernameTextView = view.findViewById(R.id.username_text_view);
        cryptocurrenciesTextView = view.findViewById(R.id.cryptocurrencies_text_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        // Отображение имени на странице аккаунта
        Bundle args = getArguments();
        if (args != null && args.containsKey("username")) {
            String username = args.getString("username");
            if (username != null) {
                usernameTextView.setText(username);
            } else {
                usernameTextView.setText("No username provided.");
            }
        } else {
            usernameTextView.setText("No username provided.");
        }

        // Инициализируем ViewModel и LiveData
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getAllUniqueCryptocurrencies().observe(getViewLifecycleOwner(), cryptocurrencies -> {
            if (cryptocurrencies != null) {
                cryptocurrenciesTextView.setText(String.join(", ", cryptocurrencies));
            }
            swipeRefreshLayout.setRefreshing(false);  // Скрываем индикатор после обновления данных
        });

        // SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            homeViewModel.refreshCryptocurrencies();
        });
        return view;
    }
}
