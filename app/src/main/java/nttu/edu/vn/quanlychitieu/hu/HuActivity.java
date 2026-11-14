package nttu.edu.vn.quanlychitieu.hu;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Ví chính và các hũ chi tiêu: xem số dư, thêm / sửa / xóa hũ, phân bổ tiền theo tỷ lệ. */
public class HuActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int uid;
    private final List<Hu> dsHu = new ArrayList<>();
    private HuAdapter adapter;
    private TextView tvTongTaiSan, tvTongTyLe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hu);
        TienIch.apDungInsets(findViewById(R.id.rootHu));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.vi_va_hu);

        db = new DatabaseHelper(this);
        uid = PhienDangNhap.getUserId(this);

        tvTongTaiSan = findViewById(R.id.tvTongTaiSan);
        tvTongTyLe = findViewById(R.id.tvTongTyLe);
        ListView lv = findViewById(R.id.lvHu);
        Button btnNap = findViewById(R.id.btnNapTien);
        Button btnChuyen = findViewById(R.id.btnChuyenTien);
        Button btnLichSu = findViewById(R.id.btnLichSu);
        Button btnThemHu = findViewById(R.id.btnThemHu);
        Button btnPhanBo = findViewById(R.id.btnPhanBo);

        adapter = new HuAdapter(this, dsHu);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener((parent, view, position, id) -> {
            Hu h = dsHu.get(position);
            if (h.getId() == Hu.VI_CHINH) {
                Toast.makeText(this, R.string.vi_chinh_khong_sua, Toast.LENGTH_SHORT).show();
            } else {
                hienTuyChonHu(h);
            }
        });

        btnNap.setOnClickListener(v -> moChuyenTien(ChuyenTienActivity.CHE_DO_NAP));
        btnChuyen.setOnClickListener(v -> moChuyenTien(ChuyenTienActivity.CHE_DO_CHUYEN));
        btnLichSu.setOnClickListener(v -> startActivity(new Intent(this, LichSuActivity.class)));
        btnThemHu.setOnClickListener(v -> hopThoaiHu(null));
        btnPhanBo.setOnClickListener(v -> hopThoaiPhanBo());
    }

    @Override
    protected void onResume() {
        super.onResume();
        taiDuLieu(); // quay lại từ màn hình nạp / chuyển tiền thì cập nhật số dư
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void moChuyenTien(int cheDo) {
        Intent i = new Intent(this, ChuyenTienActivity.class);
        i.putExtra(ChuyenTienActivity.EXTRA_CHE_DO, cheDo);
        startActivity(i);
    }

    private void taiDuLieu() {
        dsHu.clear();
        dsHu.addAll(db.layDanhSachHu(uid, true));
        adapter.notifyDataSetChanged();

        long tong = 0;
        for (Hu h : dsHu) tong += h.getSoDu();
        tvTongTaiSan.setText(TienIch.dinhDangTien(tong));

        int tongTyLe = db.tongTyLe(uid);
        tvTongTyLe.setText(getString(R.string.tong_ty_le_hu, tongTyLe));
        tvTongTyLe.setTextColor(ContextCompat.getColor(this,
                tongTyLe == 100 ? R.color.ct_chu_phu : R.color.ct_chi));
    }

    private void hienTuyChonHu(final Hu h) {
        String[] luaChon = {getString(R.string.sua_hu), getString(R.string.xoa_hu)};
        new AlertDialog.Builder(this)
                .setTitle(h.getTen())
                .setItems(luaChon, (dialog, which) -> {
                    if (which == 0) hopThoaiHu(h);
                    else xoaHu(h);
                })
                .show();
    }

    private void xoaHu(final Hu h) {
        if (db.huCoGiaoDich(uid, h.getId())) {
            Toast.makeText(this, R.string.loi_hu_dang_dung, Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.xoa_hu_hoi)
                .setMessage(h.getTen())
                .setPositiveButton(R.string.xoa, (d, w) -> {
                    db.xoaHu(uid, h.getId());
                    Toast.makeText(this, R.string.da_xoa, Toast.LENGTH_SHORT).show();
                    taiDuLieu();
                })
                .setNegativeButton(R.string.huy, null)
                .show();
    }

    /** Hộp thoại thêm hũ (hCu = null) hoặc sửa hũ. */
    private void hopThoaiHu(final Hu hCu) {
        View noiDung = LayoutInflater.from(this).inflate(R.layout.dialog_hu, null);
        final EditText etTen = noiDung.findViewById(R.id.etTenHu);
        final EditText etTyLe = noiDung.findViewById(R.id.etTyLe);
        if (hCu != null) {
            etTen.setText(hCu.getTen());
            etTyLe.setText(String.valueOf(hCu.getTyLe()));
        }

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(hCu == null ? R.string.them_hu : R.string.sua_hu)
                .setView(noiDung)
                .setPositiveButton(R.string.luu, null)
                .setNegativeButton(R.string.huy, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String ten = etTen.getText().toString().trim();
            int idBoQua = hCu == null ? -1 : hCu.getId();
            if (ten.isEmpty()) {
                etTen.setError(getString(R.string.loi_nhap_ten));
                return;
            }
            if (ten.equalsIgnoreCase(getString(R.string.vi_chinh)) || db.tonTaiHu(uid, ten, idBoQua)) {
                etTen.setError(getString(R.string.loi_trung_ten));
                return;
            }
            long tyLe = TienIch.docSoTien(etTyLe.getText().toString());
            if (tyLe < 0 || tyLe > 100) {
                etTyLe.setError(getString(R.string.loi_ty_le));
                return;
            }
            if (hCu == null) db.themHu(uid, ten, (int) tyLe);
            else db.suaHu(uid, hCu.getId(), ten, (int) tyLe);
            taiDuLieu();
            dialog.dismiss();
        });
    }

    /** Chia tiền trong Ví chính vào các hũ theo tỷ lệ. */
    private void hopThoaiPhanBo() {
        long soDuVi = db.tinhSoDu(uid, Hu.VI_CHINH);
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setHint(getString(R.string.hint_phan_bo, TienIch.dinhDangTien(Math.max(0, soDuVi))));

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.phan_bo_theo_ty_le)
                .setMessage(R.string.mo_ta_phan_bo)
                .setView(TienIch.khungNhap(this, et))
                .setPositiveButton(R.string.xac_nhan, null)
                .setNegativeButton(R.string.huy, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            long soTien = TienIch.docSoTien(et.getText().toString());
            long soDu = db.tinhSoDu(uid, Hu.VI_CHINH);
            if (soTien <= 0) {
                et.setError(getString(R.string.loi_so_tien_khong_hop_le));
                return;
            }
            if (soTien > soDu) {
                et.setError(getString(R.string.loi_khong_du_tien, TienIch.dinhDangTien(Math.max(0, soDu))));
                return;
            }
            boolean ok = db.phanBoTheoTyLe(uid, soTien, TienIch.homNay(), getString(R.string.ghi_chu_phan_bo));
            if (!ok) {
                et.setError(getString(R.string.loi_chua_co_ty_le));
                return;
            }
            Toast.makeText(this, R.string.da_phan_bo, Toast.LENGTH_SHORT).show();
            taiDuLieu();
            dialog.dismiss();
        });
    }
}
