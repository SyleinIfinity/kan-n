package com.kan_n.ui.activities;

import android.os.Bundle;
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


        // --- BẮT ĐẦU PHẦN CÀI ĐẶT NAVIGATION ---

        // 1. Thiết lập Toolbar làm ActionBar
        setSupportActionBar(binding.toolbar);

        // 2. Ẩn tiêu đề mặc định
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 3. Lấy NavController (Code của bạn đã đúng)
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment); // Đảm bảo ID này là "nav_host_fragment"
        this.navController = navHostFragment.getNavController();

        // 4. Cấu hình AppBarConfiguration (Code của bạn đã đúng)
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.thanhDieuHuong_Bang, R.id.thanhDieuHuong_HoatDong, R.id.thanhDieuHuong_ThongTin)
                .build();

        // 5. Liên kết ActionBar (Toolbar) với NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // 6. Tìm TextView tùy chỉnh (toolbar_title)
        TextView toolbarTitle = findViewById(R.id.toolbar_title);

        // 7. [SỬA ĐỔI] Thêm Listener để cập nhật tiêu đề VÀ ẩn/hiện FAB
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

            // --- PHẦN 1: Cập nhật tiêu đề Toolbar (Code cũ của bạn) ---
            CharSequence label = destination.getLabel();
            if (toolbarTitle != null && label != null) {
                toolbarTitle.setText(label);
            }

            // --- PHẦN 2: [THÊM MỚI] Ẩn/Hiện nút FAB ---

            // Lấy ID của fragment đích
            int destinationId = destination.getId();

            // Kiểm tra xem ID có khớp với fragment "Bảng" không
            // (Sử dụng ID từ tệp navigation của bạn)
            if (destinationId == R.id.thanhDieuHuong_Bang) {
                // Nếu ĐÚNG, hiện FAB
                binding.fabCreateBoard.show();
            } else {
                // Nếu là bất kỳ fragment nào khác (Hoạt động, Thông tin), ẩn FAB
                binding.fabCreateBoard.hide();
            }
        });

        // 8. [XÓA] Bỏ dòng code cũ này đi (Code của bạn đã xóa, rất tốt)

        // 9. Liên kết BottomNavigationView với NavController
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    // 10. Hàm xử lý nút "Back" trên Toolbar (Giữ nguyên)
    @Override
    public boolean onSupportNavigateUp() {
        // Đảm bảo ID "nav_host_fragment" khớp với ID ở mục 3
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(this.navController, this.appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}