package com.kan_n.ui.activities;

import android.os.Bundle;
import android.widget.TextView; // Đảm bảo import TextView

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.navigation.NavController;
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

        FirebaseApp.initializeApp(this);


        // --- BẮT ĐẦU PHẦN SỬA LỖI ---

        // 1. Thiết lập Toolbar làm ActionBar
        setSupportActionBar(binding.toolbar);

        // 2. [QUAN TRỌNG] Ẩn tiêu đề mặc định (căn lề trái)
        // Vì chúng ta dùng TextView tùy chỉnh (toolbar_title) để căn giữa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 3. Lấy NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        this.navController = navHostFragment.getNavController();

        // 4. Cấu hình AppBarConfiguration
        // Sửa lại ID cho khớp với file mobile_navigation.xml của bạn
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.thanhDieuHuong_Bang, R.id.thanhDieuHuong_HoatDong, R.id.thanhDieuHuong_ThongTin)
                .build();

        // 5. Liên kết ActionBar (Toolbar) với NavController
        // (Vẫn cần dòng này để nút "Back" (mũi tên) tự động hoạt động)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // 6. Tìm TextView tùy chỉnh (toolbar_title)
        // (Đây là TextView bạn đã thêm vào trong Toolbar để căn giữa)
        TextView toolbarTitle = findViewById(R.id.toolbar_title);

        // 7. [QUAN TRỌNG] Thêm Listener để cập nhật tiêu đề khi chuyển Fragment
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Mỗi khi chuyển fragment, lấy label (tiêu đề) của fragment đó
            CharSequence label = destination.getLabel();

            // Cập nhật TextView căn giữa của bạn
            if (toolbarTitle != null && label != null) {
                toolbarTitle.setText(label);
            }
        });

        // 8. [XÓA] Bỏ dòng code cũ này đi, vì nó chỉ chạy 1 lần
        // toolbarTitle.setText(navController.getCurrentDestination().getLabel());

        // 9. Liên kết BottomNavigationView với NavController
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    // 10. Hàm xử lý nút "Back" trên Toolbar (Giữ nguyên)
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(this.navController, this.appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}