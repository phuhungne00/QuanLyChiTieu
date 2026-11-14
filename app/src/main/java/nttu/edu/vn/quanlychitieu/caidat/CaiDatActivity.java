package nttu.edu.vn.quanlychitieu.caidat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.taikhoan.DoiMatKhauActivity;
import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;
import nttu.edu.vn.quanlychitieu.taikhoan.TaiKhoanActivity;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Màn hình Cài đặt: cửa vào các chức năng quản lý tài khoản, danh mục, ngân sách, xuất dữ liệu, đổi mật khẩu. */
public class CaiDatActivity extends AppCompatActivity {

    private TextView tvXinChao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cai_dat);
        TienIch.apDungInsets(findViewById(R.id.rootCaiDat));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.cai_dat);

        tvXinChao = findViewById(R.id.tvXinChaoCaiDat);
        Button btnTaiKhoan = findViewById(R.id.btnTaiKhoan);
        Button btnDanhMuc = findViewById(R.id.btnDanhMuc);
        Button btnNganSach = findViewById(R.id.btnNganSach);
        Button btnXuat = findViewById(R.id.btnXuatDuLieu);
        Button btnDoiMk = findViewById(R.id.btnDoiMatKhau);
        Button btnDangXuat = findViewById(R.id.btnDangXuatCaiDat);

        btnTaiKhoan.setOnClickListener(v -> startActivity(new Intent(this, TaiKhoanActivity.class)));
        btnDanhMuc.setOnClickListener(v -> startActivity(new Intent(this, DanhMucActivity.class)));
        btnNganSach.setOnClickListener(v -> startActivity(new Intent(this, NganSachActivity.class)));
        btnXuat.setOnClickListener(v -> startActivity(new Intent(this, XuatDuLieuActivity.class)));
        btnDoiMk.setOnClickListener(v -> startActivity(new Intent(this, DoiMatKhauActivity.class)));
        btnDangXuat.setOnClickListener(v -> TienIch.hoiDangXuat(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvXinChao.setText(getString(R.string.dang_dang_nhap_voi,
                PhienDangNhap.getHoTen(this), PhienDangNhap.getTenDangNhap(this)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
