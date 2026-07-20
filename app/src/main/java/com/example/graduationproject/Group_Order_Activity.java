package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class Group_Order_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText etCoordinatorName, etCoordinatorPhone, etLocationDescription;
    private TextView tvTotalLitersBadge;
    private RecyclerView rvNeighborsList;
    private MapView mapViewLocation;
    private GoogleMap googleMap;
    
    private NeighborAdapter adapter;
    private List<Neighbor> neighborList = new ArrayList<>();
    private int totalLiters = 0;
    private LatLng selectedLocation = new LatLng(31.5, 34.4); 

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_order);

        ImageView btnBack = findViewById(R.id.btnBack);
        etCoordinatorName = findViewById(R.id.etCoordinatorName);
        etCoordinatorPhone = findViewById(R.id.etCoordinatorPhone);
        tvTotalLitersBadge = findViewById(R.id.tvTotalLitersBadge);
        rvNeighborsList = findViewById(R.id.rvNeighborsList);
        LinearLayout btnAddNeighbor = findViewById(R.id.btnAddNeighborClick);
        mapViewLocation = findViewById(R.id.mapViewLocation);
        etLocationDescription = findViewById(R.id.etLocationDescription);
        android.view.View btnSubmit = findViewById(R.id.btnSubmitOrder);

        btnBack.setOnClickListener(v -> finish());

        adapter = new NeighborAdapter(neighborList, position -> {
            totalLiters -= neighborList.get(position).getQuantity();
            neighborList.remove(position);
            adapter.notifyItemRemoved(position);
            updateTotalBadge();
        });
        rvNeighborsList.setLayoutManager(new LinearLayoutManager(this));
        rvNeighborsList.setAdapter(adapter);

        btnAddNeighbor.setOnClickListener(v -> showAddNeighborDialog());

        mapViewLocation.onCreate(savedInstanceState);
        mapViewLocation.getMapAsync(this);

        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                Intent intent = new Intent(this, Review_Order_Activity.class);
                intent.putExtra("service_id", 3);
                intent.putExtra("quantity", totalLiters);
                intent.putExtra("unit", "لتر (مبادرة جماعية)");
                intent.putExtra("address", etLocationDescription.getText().toString());
                
                StringBuilder notesBuilder = new StringBuilder();
                notesBuilder.append("المنسق: ").append(etCoordinatorName.getText().toString())
                            .append("\nالهاتف: ").append(etCoordinatorPhone.getText().toString())
                            .append("\nالجيران: ");
                for(Neighbor n : neighborList) {
                    notesBuilder.append(n.getName()).append(" (").append(n.getQuantity()).append("لتر)، ");
                }
                
                intent.putExtra("notes", notesBuilder.toString());
                intent.putExtra("lat", selectedLocation.latitude);
                intent.putExtra("lng", selectedLocation.longitude);
                intent.putExtra("scheduledTime", "طلب فوري");
                startActivity(intent);
            }
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapExplorerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        findViewById(R.id.navWallet).setOnClickListener(v -> startActivity(new Intent(this, WalletActivity.class)));
        findViewById(R.id.navOrders).setOnClickListener(v -> startActivity(new Intent(this, My_Orders_Activity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
    }

    private void showAddNeighborDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("إضافة جار للمبادرة");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        final EditText inputName = new EditText(this);
        inputName.setHint("اسم الجار");
        layout.addView(inputName);
        final EditText inputQty = new EditText(this);
        inputQty.setHint("الكمية المطلوبة (لتر)");
        inputQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputQty);
        builder.setView(layout);
        builder.setPositiveButton("إضافة", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String qtyStr = inputQty.getText().toString().trim();
            if (!name.isEmpty() && !qtyStr.isEmpty()) {
                int qty = Integer.parseInt(qtyStr);
                neighborList.add(new Neighbor(name, qty));
                totalLiters += qty;
                adapter.notifyItemInserted(neighborList.size() - 1);
                updateTotalBadge();
            }
        });
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    private void updateTotalBadge() {
        tvTotalLitersBadge.setText(totalLiters + " لتر إجمالي");
    }

    private boolean validateInputs() {
        if (etCoordinatorName.getText().toString().trim().isEmpty() || neighborList.isEmpty()) {
            Toast.makeText(this, "يرجى إكمال بيانات المبادرة", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
        googleMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("نقطة التجمع"));
        });
    }

    @Override protected void onResume() { super.onResume(); mapViewLocation.onResume(); }
    @Override protected void onPause() { super.onPause(); mapViewLocation.onPause(); }
}
