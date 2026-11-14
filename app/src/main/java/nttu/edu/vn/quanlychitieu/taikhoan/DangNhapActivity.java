package nttu.edu.vn.quanlychitieu.taikhoan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import nttu.edu.vn.quanlychitieu.MainActivity;
import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Màn hình đầu tiên của ứng dụng. Nếu đã đăng nhập từ trước thì vào thẳng màn hình chính. */
public class DangNhapActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EditText etTenDangNhap, etMatKhau;
    private TextView tvLoi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PhienDangNhap.daDangNhap(this)) {
            moManHinhChinh();
            return;
        }
        setContentView(R.layout.activity_dang_nhap);
        TienIch.apDungInsets(findViewById(R.id.rootDangNhap));
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = new DatabaseHelper(this);
        etTenDangNhap = findViewById(R.id.etTenDangNhap);
        etMatKhau = findViewById(R.id.etMatKhau);
        tvLoi = findViewById(R.id.tvLoiDangNhap);
        Button btnDangNhap = findViewById(R.id.btnDangNhap);
        TextView tvDangKy = findViewById(R.id.tvChuyenDangKy);

        btnDangNhap.setOnClickListener(v -> dangNhap());
        tvDangKy.setOnClickListener(v -> startActivity(new Intent(this, DangKyActivity.class)));
    }

    private void dangNhap() {
        String ten = etTenDangNhap.getText().toString().trim();
        String matKhau = etMatKhau.getText().toString();

        if (ten.isEmpty()) {
            etTenDangNhap.setError(getString(R.string.loi_nhap_ten_dang_nhap));
            return;
        }
        if (matKhau.isEmpty()) {
            etMatKhau.setError(getString(R.string.loi_nhap_mat_khau));
            return;
        }

        NguoiDung u = db.dangNhap(ten, matKhau);
        if (u == null) {
            tvLoi.setVisibility(View.VISIBLE);
            tvLoi.setText(R.string.loi_dang_nhap_sai);
            return;
        }
        PhienDangNhap.dangNhap(this, u);
        moManHinhChinh();
    }

    private void moManHinhChinh() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
