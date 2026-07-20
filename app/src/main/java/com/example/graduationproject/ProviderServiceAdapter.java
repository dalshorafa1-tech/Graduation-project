package com.example.graduationproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.List;

public class ProviderServiceAdapter extends RecyclerView.Adapter<ProviderServiceAdapter.ServiceViewHolder> {

    private Context context;
    private List<ServiceModel> services;

    public ProviderServiceAdapter(Context context, List<ServiceModel> services) {
        this.context = context;
        this.services = services;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_provider_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceModel service = services.get(position);
        holder.tvServiceName.setText(service.getNameAr());
        holder.tvServiceDesc.setText(service.getDescriptionAr());
        
        // Placeholder for price if it's not in ServiceModel, you might need to add it or use a different model
        // holder.tvServicePrice.setText(service.getPrice() + " NIS");

        holder.btnEditService.setOnClickListener(v -> {
            // Intent to edit service
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvServicePrice, tvServiceDesc;
        SwitchMaterial switchServiceStatus;
        View btnEditService;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            tvServiceDesc = itemView.findViewById(R.id.tvServiceDesc);
            switchServiceStatus = itemView.findViewById(R.id.switchServiceStatus);
            btnEditService = itemView.findViewById(R.id.btnEditService);
        }
    }
}
