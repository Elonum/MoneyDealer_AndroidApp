package com.example.moneydealer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.example.moneydealer.models.RegularPayment;
import com.example.moneydealer.models.Category;
import com.example.moneydealer.adapters.TransactionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegularPaymentWindow extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private TextView tvTitle;
    private RecyclerView rvRegularPayments;
    private Button btnAddRegularPayment;
    private List<RegularPayment> regularPayments = new ArrayList<>();
    //private RegularPaymentAdapter adapter;
    private DatabaseReference regularPaymentsRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.regular_payment_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menuIcon);
        tvTitle = findViewById(R.id.tvTitle);
        rvRegularPayments = findViewById(R.id.rvRegularPayments);
        btnAddRegularPayment = findViewById(R.id.btnAddRegularPayment);
        DrawerHelper.setupDrawer(this, drawerLayout, navigationView, menuIcon);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        regularPaymentsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("regular_payments");
        //adapter = new RegularPaymentAdapter(regularPayments);
        rvRegularPayments.setLayoutManager(new LinearLayoutManager(this));
        //rvRegularPayments.setAdapter(adapter);
        loadRegularPayments();
        btnAddRegularPayment.setOnClickListener(v -> showAddRegularPaymentDialog());
    }

    private void loadRegularPayments() {
        regularPaymentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                regularPayments.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    RegularPayment rp = snap.getValue(RegularPayment.class);
                    if (rp != null) regularPayments.add(rp);
                }
                //adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showAddRegularPaymentDialog() {
        // TODO: Реализовать диалог добавления регулярного платежа
    }
} 