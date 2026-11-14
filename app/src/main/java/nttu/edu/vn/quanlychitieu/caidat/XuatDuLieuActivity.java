package nttu.edu.vn.quanlychitieu.caidat;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.giaodich.GiaoDich;
import nttu.edu.vn.quanlychitieu.hu.ChuyenTien;
import nttu.edu.vn.quanlychitieu.hu.Hu;
import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/**
 * Xuất dữ liệu ra file CSV (mở được bằng Excel).
 * Dùng Storage Access Framework: người dùng tự chọn nơi lưu file nên không cần xin quyền bộ nhớ.
 */
public class XuatDuLieuActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int uid;
    private RadioButton rbGiaoDich;

    // Hộp thoại "Lưu file" của hệ thống; khi người dùng chọn xong sẽ trả về Uri của file
    private final ActivityResultLauncher<String> hopThoaiLuuFile = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"),
            uri -> {
                if (uri != null) ghiFile(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xuat_du_lieu);
        TienIch.apDungInsets(findViewById(R.id.rootXuat));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.xuat_du_lieu);

        db = new DatabaseHelper(this);
        uid = PhienDangNhap.getUserId(this);
        rbGiaoDich = findViewById(R.id.rbXuatGiaoDich);
        Button btnXuat = findViewById(R.id.btnXuat);

        btnXuat.setOnClickListener(v -> {
            String ten = rbGiaoDich.isChecked() ? "giao_dich_" : "nap_chuyen_tien_";
            hopThoaiLuuFile.launch(ten + TienIch.homNay().replace("-", "") + ".csv");
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void ghiFile(Uri uri) {
        try {
            OutputStream os = getContentResolver().openOutputStream(uri);
            if (os == null) throw new IOException("Không mở được file");
            try (Writer w = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                w.write('\uFEFF');       // BOM để Excel đọc đúng tiếng Việt
                w.write("sep=,\n");      // báo cho Excel biết dấu phân cách (Excel tiếng Việt mặc định dùng dấu ;)
                if (rbGiaoDich.isChecked()) xuatGiaoDich(w);
                else xuatChuyenTien(w);
            }
            Toast.makeText(this, R.string.xuat_thanh_cong, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, R.string.xuat_that_bai, Toast.LENGTH_LONG).show();
        }
    }

    private void xuatGiaoDich(Writer w) throws IOException {
        w.write(dong(getString(R.string.cot_ngay), getString(R.string.cot_loai), getString(R.string.cot_danh_muc),
                getString(R.string.cot_so_tien), getString(R.string.cot_vi_hu), getString(R.string.cot_ghi_chu)));
        List<GiaoDich> ds = db.layTatCaGiaoDich(uid);
        for (GiaoDich g : ds) {
            w.write(dong(
                    TienIch.ngayHienThi(g.getNgay()),
                    getString(g.getLoai() == GiaoDich.THU ? R.string.thu_nhap : R.string.chi_tieu),
                    g.getTenDanhMuc(),
                    String.valueOf(g.getSoTien()), // số thuần để Excel tính tổng được
                    g.getTenHu() == null ? getString(R.string.vi_chinh) : g.getTenHu(),
                    g.getGhiChu()));
        }
    }

    private void xuatChuyenTien(Writer w) throws IOException {
        w.write(dong(getString(R.string.cot_ngay), getString(R.string.cot_loai), getString(R.string.cot_tu),
                getString(R.string.cot_den), getString(R.string.cot_so_tien), getString(R.string.cot_ghi_chu)));

        Map<Integer, String> tenHu = new HashMap<>();
        for (Hu h : db.layDanhSachHu(uid, true)) tenHu.put(h.getId(), h.getTen());

        for (ChuyenTien ct : db.layLichSuChuyenTien(uid)) {
            boolean laNap = ct.getLoai() == ChuyenTien.NAP;
            String tu = laNap ? ct.getNguon() : tenHu.get(ct.getTuHu());
            w.write(dong(
                    TienIch.ngayHienThi(ct.getNgay()),
                    getString(laNap ? R.string.nap_tien_vi : R.string.chuyen_tien),
                    tu,
                    tenHu.get(ct.getDenHu()),
                    String.valueOf(ct.getSoTien()),
                    ct.getGhiChu()));
        }
    }

    /** Ghép các cột thành một dòng CSV, tự thêm dấu ngoặc kép khi cần. */
    private String dong(String... cot) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cot.length; i++) {
            if (i > 0) sb.append(',');
            String v = cot[i] == null ? "" : cot[i];
            if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
                v = "\"" + v.replace("\"", "\"\"") + "\"";
            }
            sb.append(v);
        }
        return sb.append('\n').toString();
    }
}
