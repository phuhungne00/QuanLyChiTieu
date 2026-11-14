package nttu.edu.vn.quanlychitieu.taikhoan;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.caidat.CaiDat;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Quản lý tài khoản: xem thông tin, sửa họ tên, đăng xuất, xóa tài khoản. */
public class TaiKhoanActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int uid;
    private EditText etHoTen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tai_khoan);
        TienIch.apDungInsets(findViewById(R.id.rootTaiKhoan));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.quan_ly_tai_khoan);

        db = new DatabaseHelper(this);
        uid = PhienDangNhap.getUserId(this);

        NguoiDung u = db.layNguoiDung(uid);
        if (u == null) { // tài khoản không còn tồn tại
            PhienDangNhap.dangXuat(this);
            return;
        }

        TextView tvTen = findViewById(R.id.tvTenDangNhapTk);
        TextView tvNgayTao = findViewById(R.id.tvNgayTaoTk);
        etHoTen = findViewById(R.id.etHoTenTk);
        Button btnLuu = findViewById(R.id.btnLuuHoTen);
        Button btnDangXuat = findViewById(R.id.btnDangXuatTk);
        Button btnXoa = findViewById(R.id.btnXoaTaiKhoan);

        tvTen.setText(getString(R.string.nhan_ten_dang_nhap, u.getTenDangNhap()));
        tvNgayTao.setText(getString(R.string.nhan_ngay_tao, TienIch.ngayHienThi(u.getNgayTao())));
        etHoTen.setText(u.getHoTen());

        btnLuu.setOnClickListener(v -> luuHoTen());
        btnDangXuat.setOnClickListener(v -> TienIch.hoiDangXuat(this));
        btnXoa.setOnClickListener(v -> hopThoaiXoaTaiKhoan());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void luuHoTen() {
        String hoTen = etHoTen.getText().toString().trim();
        if (hoTen.isEmpty()) {
            etHoTen.setError(getString(R.string.loi_nhap_ho_ten));
            return;
        }
        db.capNhatHoTen(uid, hoTen);
        PhienDangNhap.capNhatHoTen(this, hoTen);
        Toast.makeText(this, R.string.da_luu, Toast.LENGTH_SHORT).show();
    }

    /** Xóa tài khoản: bắt nhập lại mật khẩu để xác nhận vì thao tác này không thể hoàn tác. */
    private void hopThoaiXoaTaiKhoan() {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        et.setHint(R.string.nhap_mat_khau_xac_nhan);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.xoa_tai_khoan)
                .setMessage(R.string.canh_bao_xoa_tai_khoan)
                .setView(TienIch.khungNhap(this, et))
                .setPositiveButton(R.string.xoa, null)
                .setNegativeButton(R.string.huy, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (!db.kiemTraMatKhau(uid, et.getText().toString())) {
                et.setError(getString(R.string.loi_mat_khau_sai));
                return;
            }
            new CaiDat(this).xoaTatCa(); // phải xóa trước khi đăng xuất (cần id tài khoản)
            db.xoaTaiKhoan(uid);
            Toast.makeText(this, R.string.da_xoa_tai_khoan, Toast.LENGTH_LONG).show();
            dialog.dismiss();
            PhienDangNhap.dangXuat(this);
        });
    }
}
