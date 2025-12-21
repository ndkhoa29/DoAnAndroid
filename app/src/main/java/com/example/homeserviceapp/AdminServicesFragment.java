package com.example.homeserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdminServicesFragment extends Fragment {
    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private AdminServiceAdapter adapter;
    private java.util.List<com.example.homeserviceapp.models.ServiceItem> serviceList;
    private com.google.firebase.firestore.FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_services, container, false);
        
        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewServices);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        
        serviceList = new java.util.ArrayList<>();
        adapter = new AdminServiceAdapter(getContext(), serviceList, new AdminServiceAdapter.OnServiceActionListener() {
            @Override
            public void onEditClick(com.example.homeserviceapp.models.ServiceItem service) {
                android.content.Intent intent = new android.content.Intent(getContext(), AddServiceActivity.class);
                intent.putExtra("IS_EDIT_MODE", true);
                intent.putExtra("SERVICE_ID", service.getServiceId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(com.example.homeserviceapp.models.ServiceItem service) {
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa dịch vụ này?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteService(service.getServiceId()))
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onItemClick(com.example.homeserviceapp.models.ServiceItem service) {
                android.content.Intent intent = new android.content.Intent(getContext(), AdminServiceDetailActivity.class);
                intent.putExtra("SERVICE_ID", service.getServiceId());
                startActivity(intent);
            }
        });
        
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.fabAddService).setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(getContext(), v);
            popup.getMenu().add(0, 1, 0, "Thêm Dịch vụ");
            popup.getMenu().add(0, 2, 1, "Quản lý Danh mục");
            
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    startActivity(new Intent(getActivity(), AddServiceActivity.class));
                    return true;
                } else if (item.getItemId() == 2) {
                    startActivity(new Intent(getActivity(), ManageCategoriesActivity.class));
                    return true;
                }
                return false;
            });
            popup.show();
        });
        
        loadServices();
        
        return view;
    }
    
    private void loadServices() {
        db.collection("services")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        serviceList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            com.example.homeserviceapp.models.ServiceItem item = doc.toObject(com.example.homeserviceapp.models.ServiceItem.class);
                            if (item != null) {
                                serviceList.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
    
    private void deleteService(String serviceId) {
        db.collection("services").document(serviceId).delete()
                .addOnSuccessListener(aVoid -> android.widget.Toast.makeText(getContext(), "Đã xóa dịch vụ", android.widget.Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> android.widget.Toast.makeText(getContext(), "Lỗi xóa: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
    }
}
