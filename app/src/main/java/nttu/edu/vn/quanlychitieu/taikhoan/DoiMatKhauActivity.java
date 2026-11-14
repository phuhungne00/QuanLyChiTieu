package nttu.edu.vn.quanlychitieu.taikhoan;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Đổi mật khẩu: phải nhập đúng mật khẩu cũ. */
public class DoiMatKhauActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int uid;
    private EditText etCu, etMoi, etNhapLai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doi_mat_khau);
        TienIch.apDungInsets(findViewById(R.id.rootDoiMk));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.doi_mat_khau);

        db = new DatabaseHelper(this);
        uid = PhienDangNhap.getUserId(this);
        etCu = findViewById(R.id.etMkCu);
        etMoi = findViewById(R.id.etMkMoi);
        etNhapLai = findViewById(R.id.etMkNhapLai);
        Button btnDoi = findViewById(R.id.btnDoiMk);

        btnDoi.setOnClickListener(v -> doiMatKhau());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void doiMatKhau() {
        String cu = etCu.getText().toString();
        String moi = etMoi.getText().toString();
        String nhapLai = etNhapLai.getText().toString();

        if (!db.kiemTraMatKhau(uid, cu)) {
            etCu.setError(getString(R.string.loi_mat_khau_cu_sai));
            return;
        }
        if (moi.length() < 6) {
            etMoi.setError(getString(R.string.loi_mat_khau_ngan));
            return;
        }
        if (moi.equals(cu)) {
            etMoi.setError(getString(R.string.loi_mat_khau_trung_cu));
            return;
        }
        if (!moi.equals(nhapLai)) {
            etNhapLai.setError(getString(R.string.loi_mat_khau_khong_khop));
            return;
        }
        db.doiMatKhau(uid, moi);
        Toast.makeText(this, R.string.doi_mat_khau_thanh_cong, Toast.LENGTH_LONG).show();
        finish();
    }
}
