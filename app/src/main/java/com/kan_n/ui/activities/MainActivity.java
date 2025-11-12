package com.kan_n.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView; // Đảm bảo import TextView

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

// --- CÓ THỂ BẠN CẦN THÊM IMPORT NÀY ---
// (Mặc dù binding.fabCreateBoard có thể đã xử lý)
// import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

        FirebaseApp.initializeApp(this);

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

        // 6. [ĐÃ SỬA] Chỉ cập nhật tiêu đề Toolbar.
        // Logic ẩn/hiện FAB đã được xóa bỏ vì nó không còn thuộc về Activity này.
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

            // --- Phần 1: Cập nhật tiêu đề (giữ nguyên) ---
            CharSequence label = destination.getLabel();
            if (toolbarTitle != null && label != null) {
                toolbarTitle.setText(label);
            }

            // --- Phần 2: [SỬA ĐỔI] Ẩn/hiện NavView và đổi Icon ---
            int destId = destination.getId();

            if (destId == R.id.thanhDieuHuong_Bang ||
                    destId == R.id.thanhDieuHuong_HoatDong ||
                    destId == R.id.thanhDieuHuong_ThongTin)
            {
                // Đây là 3 fragment chính (cấp cao nhất)
                binding.navView.setVisibility(View.VISIBLE);

                // Xóa icon 'X' nếu có (để NavController tự quản lý)
                binding.toolbar.getRoot().setNavigationIcon(null);

            } else if (destId == R.id.taoBangMoiFragment) {
                // Đây là fragment Tạo Bảng Mới
                binding.navView.setVisibility(View.GONE); // Ẩn NavView

                // Đặt icon 'X' (Hủy) cho nút "Up"
                // (Bạn đã có file ic_huy.xml)
                binding.toolbar.getRoot().setNavigationIcon(R.drawable.ic_huy);

            } else {
                // Đây là các fragment "con" khác (nếu có)
                binding.navView.setVisibility(View.GONE); // Ẩn NavView

                // Xóa icon 'X' để dùng icon "mũi tên quay lại" (<-) mặc định
                binding.toolbar.getRoot().setNavigationIcon(null);
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