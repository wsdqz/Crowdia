package com.app.crowdia;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


import classes.Auth;
import classes.Donate;

public class DonationHistoryActivity extends BaseActivity {
    
    private RecyclerView donationsRecyclerView;
    private TextView emptyStateText;
    private DonationAdapter donationAdapter;
    private ArrayList<Donate> donationsList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_history);
        
        setupActionBar(getString(R.string.donation_history_title), true);
        
        donationsRecyclerView = findViewById(R.id.donationsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        
        donationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        donationAdapter = new DonationAdapter(this, donationsList);
        donationsRecyclerView.setAdapter(donationAdapter);
        
        loadDonationHistory();
    }
    
    private void loadDonationHistory() {
        String userId = Auth.signedInUser.getKey();
        
        DatabaseReference donatesRef = FirebaseDatabase.getInstance().getReference("Crowdia").child("Donates");
        Query query = donatesRef.orderByChild("userId").equalTo(userId);
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                donationsList.clear();
                
                for (DataSnapshot donateSnapshot : snapshot.getChildren()) {
                    Donate donate = donateSnapshot.getValue(Donate.class);
                    if (donate != null) {
                        donate.setKey(donateSnapshot.getKey());
                        donationsList.add(donate);
                    }
                }
                
                // сортировка по дате (от новых к старым)
                donationsList.sort((d1, d2) -> Long.compare(d2.getDate(), d1.getDate()));
                
                // обновление адаптера
                donationAdapter.notifyDataSetChanged();
                
                // сообщение если нет пожертвований
                if (donationsList.isEmpty()) {
                    emptyStateText.setVisibility(View.VISIBLE);
                    donationsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    donationsRecyclerView.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 