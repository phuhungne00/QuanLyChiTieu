package nttu.edu.vn.quanlychitieu.caidat;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.giaodich.DanhMuc;
import nttu.edu.vn.quanlychitieu.giaodich.GiaoDich;
import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Quản lý danh mục thu / chi: thêm, đổi tên, xóa (không xóa được danh mục đang có giao dịch). */
public class DanhMucActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int uid;
    private int loai = GiaoDich.CHI;
    private final List<DanhMuc> ds = new ArrayList<>();
    private ArrayAdapter<DanhMuc> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_muc);
        TienIch.apDungInsets(findViewById(R.id.rootDanhMuc));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.quan_ly_danh_muc);

        db = new DatabaseHelper(this);
        uid = PhienDangNhap.getUserId(this);

        RadioGroup rg = findViewById(R.id.rgLoaiDm);
        ListView lv = findViewById(R.id.lvDanhMuc);
        Button btnThem = findViewById(R.id.btnThemDanhMuc);
        lv.setEmptyView(findViewById(R.id.tvTrongDm));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ds);
        lv.setAdapter(adapter);

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            loai = checkedId == R.id.rbDmThu ? GiaoDich.THU : GiaoDich.CHI;
            taiDuLieu();
        });

        lv.setOnItemClickListener((parent, view, position, id) -> {
            final DanhMuc dm = ds.get(position);
            String[] luaChon = {getString(R.string.doi_ten), getString(R.string.xoa)};
            new AlertDialog.Builder(this)
                    .setTitle(dm.getTen())
                    .setItems(luaChon, (dialog, which) -> {
                        if (which == 0) hopThoaiTen(dm);
                        else xoaDanhMuc(dm);
                    })
                    .show();
        });

        btnThem.setOnClickListener(v -> hopThoaiTen(null));
        taiDuLieu();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void taiDuLieu() {
        ds.clear();
        ds.addAll(db.layDanhMuc(uid, loai));
        adapter.notifyDataSetChanged();
    }

    /** Thêm danh mục (dm = null) hoặc đổi tên danh mục. */
    private void hopThoaiTen(final DanhMuc dm) {
        final EditText et = new EditText(this);
        et.setHint(R.string.ten_danh_muc_hint);
        if (dm != null) {
            et.setText(dm.getTen());
            et.setSelection(et.getText().length());
        }

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(dm == null ? R.string.them_danh_muc : R.string.doi_ten)
                .setView(TienIch.khungNhap(this, et))
                .setPositiveButton(R.string.luu, null)
                .setNegativeButton(R.string.huy, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String ten = et.getText().toString().trim();
            if (ten.isEmpty()) {
                et.setError(getString(R.string.loi_nhap_ten));
                return;
            }
            int idBoQua = dm == null ? -1 : dm.getId();
            if (db.tonTaiDanhMuc(uid, loai, ten, idBoQua)) {
                et.setError(getString(R.string.loi_trung_ten));
                return;
            }
            if (dm == null) db.themDanhMuc(uid, ten, loai);
            else db.doiTenDanhMuc(uid, dm.getId(), ten);
            taiDuLieu();
            dialog.dismiss();
        });
    }

    private void xoaDanhMuc(final DanhMuc dm) {
        if (ds.size() <= 1) {
            Toast.makeText(this, R.string.loi_giu_it_nhat_mot, Toast.LENGTH_LONG).show();
            return;
        }
        int soGiaoDich = db.demGiaoDichTheoDanhMuc(uid, dm.getId());
        if (soGiaoDich > 0) {
            Toast.makeText(this, getString(R.string.loi_danh_muc_dang_dung, soGiaoDich),
                    Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.xoa_danh_muc_hoi)
                .setMessage(dm.getTen())
                .setPositiveButton(R.string.xoa, (d, w) -> {
                    db.xoaDanhMuc(uid, dm.getId());
                    Toast.makeText(this, R.string.da_xoa, Toast.LENGTH_SHORT).show();
                    taiDuLieu();
                })
                .setNegativeButton(R.string.huy, null)
                .show();
    }
}
