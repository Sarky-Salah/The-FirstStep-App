package com.example.thefirststep;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BeneficiaryAdapter extends RecyclerView.Adapter<BeneficiaryAdapter.ViewHolder> implements Filterable {

    private List<Beneficiary> beneficiaryList;
    private List<Beneficiary> fullList;
    private Context context;

    public BeneficiaryAdapter(List<Beneficiary> beneficiaryList, Context context) {
        this.beneficiaryList = beneficiaryList;
        this.fullList = new ArrayList<>(beneficiaryList);
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
        holder.textStatus.setText(b.getStatus());
        holder.textContact.setText(b.getContact());
        holder.textHistory.setText(b.getHistory());
        holder.textGender.setText(b.getGender());
        holder.textAge.setText(b.getBirthDate());
        holder.textOccupation.setText(b.getOccupation());
        holder.textAddress.setText(b.getAddress());
        holder.textLevel.setText(b.getLevel());
        holder.textSide.setText(b.getSide());
        holder.textYear.setText(b.getYearOfAmputation());
        holder.kinName.setText(b.getNameKin());
        holder.kinContact.setText(b.getContactKin());

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
            intent.putExtra("SIDE", b.getSide());
            intent.putExtra("LEVEL", b.getLevel());
            intent.putExtra("STATUS", b.getStatus());
            intent.putExtra("HISTORY", b.getHistory());
            intent.putExtra("PHOTO_URL", b.getPhotoUrl());
            intent.putExtra("GENDER", b.getGender());
            intent.putExtra("AGE", b.getBirthDate());
            intent.putExtra("OCCUPATION", b.getOccupation());
            intent.putExtra("ADDRESS", b.getAddress());
            intent.putExtra("YEAR_OF_AMPUTATION", b.getYearOfAmputation());
            intent.putExtra("KIN_NAME", b.getNameKin());
            intent.putExtra("KIN_CONTACT", b.getContactKin());

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

    public void updateFullList(List<Beneficiary> newList) {
        fullList.clear();
        fullList.addAll(newList);
    }


    @Override
    public int getItemCount() {
        return beneficiaryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textKey, textAge, textGender, textOccupation, textName, textHistory, textStatus, textContact;
        TextView textAddress, kinContact, kinName, textLevel, textSide, textYear;
        ImageView imageViewPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textKey = itemView.findViewById(R.id.textKey);
            textStatus = itemView.findViewById(R.id.textStatus);
            textName = itemView.findViewById(R.id.textName);
            textAge = itemView.findViewById(R.id.textBirthdate);
            textGender = itemView.findViewById(R.id.textGender);
            textOccupation = itemView.findViewById(R.id.textOccupation);
            textContact = itemView.findViewById(R.id.textContact);
            textAddress = itemView.findViewById(R.id.textAddress);
            kinName = itemView.findViewById(R.id.nextOfKinName);
            kinContact = itemView.findViewById(R.id.nextOfKinContact);
            textSide = itemView.findViewById(R.id.sideOfAmputation);
            textLevel = itemView.findViewById(R.id.levelOfAmputation);
            textYear = itemView.findViewById(R.id.yearOfAmputation);
            textHistory = itemView.findViewById(R.id.textHistory);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
        }
    }

    @Override
    public Filter getFilter() {
        return beneficiaryFilter;
    }
    @SuppressWarnings("unchecked")
    private Filter beneficiaryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Beneficiary> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(fullList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Beneficiary item : fullList) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            // ✅ OK to declare here
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            beneficiaryList.clear();
            if (results.values != null) {
                beneficiaryList.addAll((List<Beneficiary>) results.values);
            }
            notifyDataSetChanged();
        }
    };
}