package com.example.moneydealer;

import android.content.Intent;
import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.*;

public class CurrencyWindow extends AppCompatActivity {

    private TextInputEditText etSearchCurrency;
    private RecyclerView rvCurrencyList;
    private Button btnSaveCurrency;
    private CurrencyAdapter currencyAdapter;
    private List<Currency> allCurrencies;
    private String selectedCurrencyCode;
    private Map<String, String> currencyCodeToFlagNameMap;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.currency_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseUser fUser = auth.getCurrentUser();
        if (fUser == null) {
            startActivity(new Intent(this, LoginWindow.class));
            finish();
            return;
        }

        usersRef.child(fUser.getUid()).child("selectedCurrency")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        String cur = snap.getValue(String.class);
                        if (cur != null && !cur.isEmpty()) {
                            Intent i = new Intent(CurrencyWindow.this, MainWindow.class);
                            i.putExtra("selectedCurrency", cur);
                            startActivity(i);
                            finish();
                        } else {
                            initUI();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError err) {
                        initUI();
                    }
                });
    }

    private void initUI() {
        etSearchCurrency = findViewById(R.id.et_search_currency);
        rvCurrencyList   = findViewById(R.id.rv_currency_list);
        btnSaveCurrency  = findViewById(R.id.btn_save_currency);

        initFlagMap();
        loadCurrencies();
        setupSearch();
        setupSave();
    }

    private void initFlagMap() {
        currencyCodeToFlagNameMap = new HashMap<>();
        currencyCodeToFlagNameMap.put("RUB","ru_ic");
        currencyCodeToFlagNameMap.put("EUR","eu_ic");
        currencyCodeToFlagNameMap.put("USD","usa_ic");
        currencyCodeToFlagNameMap.put("BYN","by_ic");
        currencyCodeToFlagNameMap.put("CNY","cn_ic");
        currencyCodeToFlagNameMap.put("JPY","jp_ic");
        currencyCodeToFlagNameMap.put("GBP","gb_ic");
        currencyCodeToFlagNameMap.put("CHF","ch_ic");
        currencyCodeToFlagNameMap.put("CAD","ca_ic");
        currencyCodeToFlagNameMap.put("AUD","au_ic");
        currencyCodeToFlagNameMap.put("INR","in_ic");
        currencyCodeToFlagNameMap.put("BRL","br_ic");
        currencyCodeToFlagNameMap.put("MXN","mx_ic");
        currencyCodeToFlagNameMap.put("TRY","tr_ic");
        currencyCodeToFlagNameMap.put("KRW","kr_ic");
    }

    private void loadCurrencies() {
        allCurrencies = Arrays.asList(
                new Currency("Российский рубль","RUB"),
                new Currency("Евро","EUR"),
                new Currency("Доллар США","USD"),
                new Currency("Белорусский рубль","BYN"),
                new Currency("Китайский юань","CNY"),
                new Currency("Японская иена","JPY"),
                new Currency("Британский фунт","GBP"),
                new Currency("Швейцарский франк","CHF"),
                new Currency("Канадский доллар","CAD"),
                new Currency("Австралийский доллар","AUD"),
                new Currency("Индийская рупия","INR"),
                new Currency("Бразильский реал","BRL"),
                new Currency("Мексиканское песо","MXN"),
                new Currency("Турецкая лира","TRY"),
                new Currency("Южнокорейская вона","KRW")
        );
        currencyAdapter = new CurrencyAdapter(this, allCurrencies);
        rvCurrencyList.setLayoutManager(new LinearLayoutManager(this));
        rvCurrencyList.setAdapter(currencyAdapter);
    }

    private void setupSearch() {
        etSearchCurrency.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){
                String q = s.toString().toLowerCase();
                List<Currency> filtered = new ArrayList<>();
                for (Currency cur : allCurrencies) {
                    if (cur.getName().toLowerCase().contains(q) ||
                            cur.getCode().toLowerCase().contains(q)) {
                        filtered.add(cur);
                    }
                }
                currencyAdapter.filterList(filtered);
            }
        });
    }

    private void setupSave() {
        btnSaveCurrency.setOnClickListener(v -> {
            if (selectedCurrencyCode == null) {
                Toast.makeText(this, "Выберите валюту", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = auth.getCurrentUser().getUid();
            usersRef.child(uid).child("selectedCurrency")
                    .setValue(selectedCurrencyCode)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Валюта сохранена: "+selectedCurrencyCode,
                                Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(CurrencyWindow.this, MainWindow.class);
                        i.putExtra("selectedCurrency", selectedCurrencyCode);
                        startActivity(i);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,"Ошибка сохранения: "+e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        });
    }

    public static class Currency {
        private String name, code;
        public Currency(String name, String code){this.name=name;this.code=code;}
        public String getName(){return name;}
        public String getCode(){return code;}
    }

    public class CurrencyAdapter
            extends RecyclerView.Adapter<CurrencyAdapter.VH> {

        private List<Currency> list;
        private Context ctx;
        private int sel = RecyclerView.NO_POSITION;

        public CurrencyAdapter(Context c, List<Currency> l){
            ctx=c; list=l;
        }
        public void filterList(List<Currency> l){
            list=l; notifyDataSetChanged();
        }
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p,int v){
            return new VH(LayoutInflater.from(ctx)
                    .inflate(R.layout.item_currency,p,false));
        }
        @Override
        public void onBindViewHolder(@NonNull VH h,int pos){
            Currency cur = list.get(pos);
            h.name.setText(cur.getName());
            h.code.setText(cur.getCode());
            String fn = currencyCodeToFlagNameMap.get(cur.getCode());
            if (fn!=null){
                int rid = ctx.getResources()
                        .getIdentifier(fn,"drawable",ctx.getPackageName());
                if (rid!=0){h.flag.setImageResource(rid);h.flag.setVisibility(View.VISIBLE);}
                else h.flag.setVisibility(View.GONE);
            } else h.flag.setVisibility(View.GONE);

            h.itemView.setSelected(pos==sel);
            h.itemView.setOnClickListener(v -> {
                int prev=sel;
                sel=h.getAdapterPosition();
                notifyItemChanged(prev);
                notifyItemChanged(sel);
                selectedCurrencyCode=cur.getCode();
            });
        }
        @Override public int getItemCount(){return list.size();}

        class VH extends RecyclerView.ViewHolder{
            TextView name, code;
            ImageView flag;
            VH(@NonNull View iv){
                super(iv);
                name=iv.findViewById(R.id.tv_currency_name);
                code=iv.findViewById(R.id.tv_currency_code);
                flag=iv.findViewById(R.id.iv_currency_flag);
            }
        }
    }
}
