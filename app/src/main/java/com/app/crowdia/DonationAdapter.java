package com.app.crowdia;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import classes.Donate;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationViewHolder> {
    
    private Context context;
    private ArrayList<Donate> donations;
    private SimpleDateFormat dateFormat;
    
    public DonationAdapter(Context context, ArrayList<Donate> donations) {
        this.context = context;
        this.donations = donations;
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donation, parent, false);
        return new DonationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder holder, int position) {
        Donate donation = donations.get(position);
        
        // название проекта
        holder.projectTitle.setText(donation.getProjectTitle());
        
        // сумма пожертвования
        holder.donationAmount.setText(String.format(Locale.getDefault(), "%,.0f ₸", donation.getAmount()));
        
        // дата пожертвования
        String formattedDate = dateFormat.format(new Date(donation.getDate()));
        holder.donationDate.setText(formattedDate);
        
        // обработчик для перехода к деталям проекта
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProjectDetailsActivity.class);
            intent.putExtra("projectKey", donation.getProjectId());
            context.startActivity(intent);
        });
    }
    
    @Override
    public int getItemCount() {
        return donations.size();
    }
    
    static class DonationViewHolder extends RecyclerView.ViewHolder {
        TextView projectTitle;
        TextView donationAmount;
        TextView donationDate;
        
        public DonationViewHolder(@NonNull View itemView) {
            super(itemView);
            projectTitle = itemView.findViewById(R.id.projectTitle);
            donationAmount = itemView.findViewById(R.id.donationAmount);
            donationDate = itemView.findViewById(R.id.donationDate);
        }
    }
} 