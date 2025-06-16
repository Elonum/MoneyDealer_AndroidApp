package com.example.moneydealer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class CurrencyWindow extends AppCompatActivity {

    private TextInputEditText etSearchCurrency;
    private RecyclerView rvCurrencyList;
    private Button btnSaveCurrency;
    private CurrencyAdapter currencyAdapter;
    private List<Currency> allCurrencies;
    private String selectedCurrencyCode;
    private Map<String, String> currencyCodeToFlagNameMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.currency_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeFlagMap();
        setupCurrencyList();
        setupSearch();
        setupSaveButton();
    }

    private void initializeViews() {
        etSearchCurrency = findViewById(R.id.et_search_currency);
        rvCurrencyList = findViewById(R.id.rv_currency_list);
        btnSaveCurrency = findViewById(R.id.btn_save_currency);
    }

    private void initializeFlagMap() {
        currencyCodeToFlagNameMap = new HashMap<>();
        currencyCodeToFlagNameMap.put("RUB", "ru_ic");
        currencyCodeToFlagNameMap.put("EUR", "eu_ic");
        currencyCodeToFlagNameMap.put("USD", "usa_ic");
        currencyCodeToFlagNameMap.put("BYN", "by_ic");
        currencyCodeToFlagNameMap.put("CNY", "cn_ic");
        currencyCodeToFlagNameMap.put("JPY", "jp_ic");
        currencyCodeToFlagNameMap.put("GBP", "gb_ic");
        currencyCodeToFlagNameMap.put("CHF", "ch_ic");
        currencyCodeToFlagNameMap.put("CAD", "ca_ic");
        currencyCodeToFlagNameMap.put("AUD", "au_ic");
        currencyCodeToFlagNameMap.put("INR", "in_ic");
        currencyCodeToFlagNameMap.put("BRL", "br_ic");
        currencyCodeToFlagNameMap.put("MXN", "mx_ic");
        currencyCodeToFlagNameMap.put("TRY", "tr_ic");
        currencyCodeToFlagNameMap.put("KRW", "kr_ic");
    }

    private void setupCurrencyList() {
        allCurrencies = new ArrayList<>();
        allCurrencies.add(new Currency("Российский рубль", "RUB"));
        allCurrencies.add(new Currency("Евро", "EUR"));
        allCurrencies.add(new Currency("Доллар США", "USD"));
        allCurrencies.add(new Currency("Белорусский рубль", "BYN"));
        allCurrencies.add(new Currency("Китайский юань", "CNY"));
        allCurrencies.add(new Currency("Японская иена", "JPY"));
        allCurrencies.add(new Currency("Британский фунт", "GBP"));
        allCurrencies.add(new Currency("Швейцарский франк", "CHF"));
        allCurrencies.add(new Currency("Канадский доллар", "CAD"));
        allCurrencies.add(new Currency("Австралийский доллар", "AUD"));
        allCurrencies.add(new Currency("Индийская рупия", "INR"));
        allCurrencies.add(new Currency("Бразильский реал", "BRL"));
        allCurrencies.add(new Currency("Мексиканское песо", "MXN"));
        allCurrencies.add(new Currency("Турецкая лира", "TRY"));
        allCurrencies.add(new Currency("Южнокорейская вона", "KRW"));

        currencyAdapter = new CurrencyAdapter(this, allCurrencies);
        rvCurrencyList.setLayoutManager(new LinearLayoutManager(this));
        rvCurrencyList.setAdapter(currencyAdapter);
    }

    private void setupSearch() {
        etSearchCurrency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCurrencies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCurrencies(String query) {
        List<Currency> filteredList = new ArrayList<>();
        for (Currency currency : allCurrencies) {
            if (currency.getName().toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault())) ||
                currency.getCode().toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))) {
                filteredList.add(currency);
            }
        }
        currencyAdapter.filterList(filteredList);
    }

    private void setupSaveButton() {
        btnSaveCurrency.setOnClickListener(v -> {
            if (selectedCurrencyCode != null) {
                // Simulate saving to backend
                saveCurrencyToBackend(selectedCurrencyCode);
                Toast.makeText(this, "Валюта сохранена: " + selectedCurrencyCode, Toast.LENGTH_SHORT).show();
                // Optionally, navigate to MainWindow or previous screen
                // Intent intent = new Intent(CurrencyWindow.this, MainWindow.class);
                // startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Пожалуйста, выберите валюту", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCurrencyToBackend(String currencyCode) {
        // This method would make an API call to your backend to save the user's selected currency.
        // Example (conceptual): ApiService.saveUserCurrency(userId, currencyCode, new Callback() { ... });
        // For now, it's just a placeholder.
        System.out.println("Simulating saving currency to backend: " + currencyCode);
    }

    public static class Currency {
        private String name;
        private String code;

        public Currency(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }
    }

    // Currency Adapter for RecyclerView
    public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {

        private List<Currency> currencies;
        private Context context;
        private int selectedItem = RecyclerView.NO_POSITION;

        public CurrencyAdapter(Context context, List<Currency> currencies) {
            this.context = context;
            this.currencies = currencies;
        }

        public void filterList(List<Currency> filteredList) {
            this.currencies = filteredList;
            notifyDataSetChanged();
        }

        public void setSelectedCurrency(String code) {
            for (int i = 0; i < currencies.size(); i++) {
                if (currencies.get(i).getCode().equals(code)) {
                    selectedItem = i;
                    notifyItemChanged(i);
                    break;
                }
            }
        }

        @NonNull
        @Override
        public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_currency, parent, false);
            return new CurrencyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
            Currency currency = currencies.get(position);
            holder.tvCurrencyName.setText(currency.getName());
            holder.tvCurrencyCode.setText(currency.getCode());

            // Set flag based on currency code using the map
            String flagName = ((CurrencyWindow) context).currencyCodeToFlagNameMap.get(currency.getCode());
            if (flagName != null) {
                int flagResId = context.getResources().getIdentifier(
                        flagName,
                        "drawable",
                        context.getPackageName()
                );
                if (flagResId != 0) {
                    holder.ivCurrencyFlag.setImageResource(flagResId);
                    holder.ivCurrencyFlag.setVisibility(View.VISIBLE);
                } else {
                    holder.ivCurrencyFlag.setVisibility(View.GONE); // Hide if no flag found
                }
            } else {
                holder.ivCurrencyFlag.setVisibility(View.GONE); // Hide if no mapping found
            }

            holder.itemView.setSelected(position == selectedItem);
            holder.itemView.setOnClickListener(v -> {
                int previousSelected = selectedItem;
                selectedItem = holder.getAdapterPosition();
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedItem);
                selectedCurrencyCode = currency.getCode();
            });
        }

        @Override
        public int getItemCount() {
            return currencies.size();
        }

        public class CurrencyViewHolder extends RecyclerView.ViewHolder {
            TextView tvCurrencyName;
            TextView tvCurrencyCode;
            ImageView ivCurrencyFlag;

            public CurrencyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCurrencyName = itemView.findViewById(R.id.tv_currency_name);
                tvCurrencyCode = itemView.findViewById(R.id.tv_currency_code);
                ivCurrencyFlag = itemView.findViewById(R.id.iv_currency_flag);
            }
        }
    }
} 