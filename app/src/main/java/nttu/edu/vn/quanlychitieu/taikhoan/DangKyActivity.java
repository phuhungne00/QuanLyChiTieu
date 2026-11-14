package nttu.edu.vn.quanlychitieu.taikhoan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import nttu.edu.vn.quanlychitieu.MainActivity;
import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Tạo tài khoản mới. Tài khoản mới có sẵn danh mục và 6 hũ chi tiêu mặc định. */
public class DangKyActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EditText etHoTen, etTenDangNhap, etMatKhau, etNhapLai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);
        TienIch.apDungInsets(findViewById(R.id.rootDangKy));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.dang_ky);

        db = new DatabaseHelper(this);
        etHoTen = findViewById(R.id.etHoTenDk);
        etTenDangNhap = findViewById(R.id.etTenDangNhapDk);
        etMatKhau = findViewById(R.id.etMatKhauDk);
        etNhapLai = findViewById(R.id.etNhapLaiDk);
        Button btnDangKy = findViewById(R.id.btnDangKy);

        btnDangKy.setOnClickListener(v -> dangKy());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void dangKy() {
        String hoTen = etHoTen.getText().toString().trim();
        String ten = etTenDangNhap.getText().toString().trim();
        String matKhau = etMatKhau.getText().toString();
        String nhapLai = etNhapLai.getText().toString();

        if (hoTen.isEmpty()) {
            etHoTen.setError(getString(R.string.loi_nhap_ho_ten));
            return;
        }
        if (!ten.matches("[A-Za-z0-9_]{3,20}")) {
            etTenDangNhap.setError(getString(R.string.loi_ten_dang_nhap_khong_hop_le));
            return;
        }
        if (matKhau.length() < 6) {
            etMatKhau.setError(getString(R.string.loi_mat_khau_ngan));
            return;
        }
        if (!matKhau.equals(nhapLai)) {
            etNhapLai.setError(getString(R.string.loi_mat_khau_khong_khop));
            return;
        }

        long id = db.dangKy(ten, hoTen, matKhau);
        if (id == -1) {
            etTenDangNhap.setError(getString(R.string.loi_ten_dang_nhap_ton_tai));
            return;
        }

        // Đăng ký xong thì đăng nhập luôn
        NguoiDung u = db.layNguoiDung((int) id);
        if (u == null) return;
        PhienDangNhap.dangNhap(this, u);
        Toast.makeText(this, R.string.dang_ky_thanh_cong, Toast.LENGTH_SHORT).show();

        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
