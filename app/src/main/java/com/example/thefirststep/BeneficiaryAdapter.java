package com.example.thefirststep;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;
import java.util.List;

public class BeneficiaryAdapter extends RecyclerView.Adapter<BeneficiaryAdapter.ViewHolder> {

    private List<Beneficiary> beneficiaryList;
    private Context context;

    public BeneficiaryAdapter(List<Beneficiary> beneficiaryList, Context context) {
        this.beneficiaryList = beneficiaryList;
        this.context = context;
    }

    @NonNull
    @Override
    public BeneficiaryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.beneficiary_list_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull BeneficiaryAdapter.ViewHolder holder, int position) {
        Beneficiary b = beneficiaryList.get(position);
        holder.textKey.setText(b.getKey());
        holder.textName.setText(b.getName());
        holder.textType.setText(b.getProstheticType());
        holder.textStatus.setText(b.getStatus());
        holder.textContact.setText(b.getContact());
        holder.textHistory.setText(b.getHistory());

        if (b.getPhotoUrl() != null && !b.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(b.getPhotoUrl())
                    .placeholder(R.drawable.person_24) // replace with your own placeholder
                    .into(holder.imageViewPhoto);
        } else {
            holder.imageViewPhoto.setImageResource(R.drawable.person_24); // fallback
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EntryActivity.class);
            intent.putExtra("KEY", b.getKey());
            intent.putExtra("NAME", b.getName());
            intent.putExtra("CONTACT", b.getContact());
            intent.putExtra("TYPE", b.getProstheticType());
            intent.putExtra("STATUS", b.getStatus());
            intent.putExtra("HISTORY", b.getHistory());
            intent.putExtra("PHOTO_URL", b.getPhotoUrl());

            context.startActivity(intent);
            // Load image using Glide
            if (b.photoUrl != null && !b.photoUrl.isEmpty()) {
                Glide.with(context)
                        .load(b.photoUrl)
                        .placeholder(R.drawable.person_24)
                        .into(holder.imageViewPhoto);
            } else {
                holder.imageViewPhoto.setImageResource(R.drawable.person_24);
            }
        });


    }

    @Override
    public int getItemCount() {
        return beneficiaryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textKey, textName, textHistory, textType, textStatus, textContact;
        ImageView imageViewPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textKey = itemView.findViewById(R.id.textKey);
            textName = itemView.findViewById(R.id.textName);
            textType = itemView.findViewById(R.id.textType);
            textStatus = itemView.findViewById(R.id.textStatus);
            textContact = itemView.findViewById(R.id.textContact);
            textHistory = itemView.findViewById(R.id.textHistory);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
        }
    }
}
