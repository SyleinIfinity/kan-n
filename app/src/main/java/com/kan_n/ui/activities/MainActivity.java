package com.kan_n.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.FirebaseApp;
import com.kan_n.R;
import com.kan_n.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Thiết lập Toolbar
        setSupportActionBar(binding.toolbar.getRoot());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 2. Lấy NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        this.navController = navHostFragment.getNavController();

        // 3. Cấu hình AppBarConfiguration
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.thanhDieuHuong_Bang, R.id.thanhDieuHuong_HoatDong, R.id.thanhDieuHuong_ThongTin)
                .build();

        // 4. Liên kết ActionBar (Toolbar) với NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // 5. Tìm TextView tùy chỉnh (toolbar_title)
        TextView toolbarTitle = findViewById(R.id.toolbar_title);

        // 6. [ĐÃ SỬA] Cập nhật tiêu đề, Ẩn/Hiện Toolbar chính, và NavView.
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

            // --- Phần 1: Cập nhật tiêu đề (giữ nguyên) ---
            CharSequence label = destination.getLabel();
            if (toolbarTitle != null && label != null) {
                toolbarTitle.setText(label);
            }

            // --- Phần 2: [SỬA ĐỔI] Ẩn/hiện Toolbar chính, NavView và đổi Icon ---
            int destId = destination.getId();

            if (destId == R.id.thanhDieuHuong_Bang ||
                    destId == R.id.thanhDieuHuong_HoatDong ||
                    destId == R.id.thanhDieuHuong_ThongTin)
            {
                // Đây là 3 fragment chính (cấp cao nhất)
                binding.toolbar.getRoot().setVisibility(View.VISIBLE); // HIỆN Toolbar chính
                binding.navView.setVisibility(View.VISIBLE); // HIỆN NavView
                binding.toolbar.getRoot().setNavigationIcon(null); // NavController tự quản lý

            } else if (destId == R.id.taoBangMoiFragment) {
                // Đây là fragment Tạo Bảng Mới
                binding.toolbar.getRoot().setVisibility(View.VISIBLE); // HIỆN Toolbar chính
                binding.navView.setVisibility(View.GONE); // ẨN NavView
                binding.toolbar.getRoot().setNavigationIcon(R.drawable.ic_huy); // Đặt icon 'X'

            } else if (destId == R.id.bangSpaceFragment) {
                // Đây là fragment BangSpace (có toolbar riêng)
                binding.toolbar.getRoot().setVisibility(View.GONE); // ẨN Toolbar chính
                binding.navView.setVisibility(View.GONE); // ẨN NavView

            } else if (destId == R.id.TrangCaiDatFragment) {
                binding.toolbar.getRoot().setVisibility(View.VISIBLE); // HIỆN Toolbar chính
                binding.navView.setVisibility(View.GONE); // ẨN NavView
                binding.toolbar.getRoot().setNavigationIcon(R.drawable.ic_quaylai_v1);

            } else if (destId == R.id.infoUserFragment) {
                binding.toolbar.getRoot().setVisibility(View.VISIBLE); // HIỆN Toolbar chính
                binding.navView.setVisibility(View.GONE); // ẨN NavView
                binding.toolbar.getRoot().setNavigationIcon(R.drawable.ic_quaylai_v1);

            } else if (destId == R.id.TrangLichSuDangNhapFragment) {
                binding.toolbar.getRoot().setVisibility(View.VISIBLE); // HIỆN Toolbar chính
                binding.navView.setVisibility(View.GONE); // ẨN NavView
                binding.toolbar.getRoot().setNavigationIcon(R.drawable.ic_quaylai_v1);

            } else if (destId == R.id.TrangBaoMatQuyenRiengTuFragment) {
                binding.toolbar.getRoot().setVisibility(View.GONE); //
                binding.navView.setVisibility(View.VISIBLE); // ẨN NavView

            } else if (destId == R.id.taoBangMoiChonPhongFragment){
                binding.toolbar.getRoot().setVisibility(View.GONE); // HIỆN Toolbar chính
            } else if (destId == R.id.taoBangMoiChonPhongAnhFragment){
                binding.toolbar.getRoot().setVisibility(View.GONE); // HIỆN Toolbar chính
            } else if (destId == R.id.taoBangMoiChonPhongMauFragment){
                binding.toolbar.getRoot().setVisibility(View.GONE); // HIỆN Toolbar chính
            }

            else {
                // Đây là các fragment "con" khác (nếu có)
                binding.toolbar.getRoot().setVisibility(View.VISIBLE); // HIỆN Toolbar chính
                binding.navView.setVisibility(View.GONE); // ẨN NavView
                binding.toolbar.getRoot().setNavigationIcon(null); // Dùng icon "mũi tên quay lại"

            }
        });

        // 7. Liên kết BottomNavigationView với NavController
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(this.navController, this.appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}