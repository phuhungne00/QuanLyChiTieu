package nttu.edu.vn.quanlychitieu;

import android.Manifest;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nttu.edu.vn.quanlychitieu.caidat.CaiDat;
import nttu.edu.vn.quanlychitieu.caidat.CaiDatActivity;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.giaodich.GiaoDich;
import nttu.edu.vn.quanlychitieu.giaodich.GiaoDichAdapter;
import nttu.edu.vn.quanlychitieu.giaodich.ThemGiaoDichActivity;
import nttu.edu.vn.quanlychitieu.hu.HuActivity;
import nttu.edu.vn.quanlychitieu.muctieu.MucTieuActivity;
import nttu.edu.vn.quanlychitieu.taikhoan.DangNhapActivity;
import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;
import nttu.edu.vn.quanlychitieu.thongke.ThongKeActivity;
import nttu.edu.vn.quanlychitieu.tienich.ThongBaoHelper;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Màn hình chính: tổng quan thu - chi trong tháng và danh sách giao dịch. */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int uid;
    private final Calendar thangDangXem = Calendar.getInstance();
    private final List<GiaoDich> dsGiaoDich = new ArrayList<>();
    private GiaoDichAdapter adapter;

    private TextView tvThang, tvSoDu, tvTongThu, tvTongChi, tvNganSach;
    private ProgressBar pbNganSach;
    private ListView lvGiaoDich;
    private TextView tvTrong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chưa đăng nhập (ví dụ mở từ thông báo sau khi đăng xuất) thì quay về màn hình đăng nhập
        if (!PhienDangNhap.daDangNhap(this)) {
            startActivity(new Intent(this, DangNhapActivity.class));
            finish();
            return;
        }
        uid = PhienDangNhap.getUserId(this);

        setContentView(R.layout.activity_main);
        TienIch.apDungInsets(findViewById(R.id.rootMain));

        db = new DatabaseHelper(this);
        ThongBaoHelper.taoKenh(this);
        xinQuyenThongBao();

        // Ánh xạ view
        tvThang = findViewById(R.id.tvThang);
        tvSoDu = findViewById(R.id.tvSoDu);
        tvTongThu = findViewById(R.id.tvTongThu);
        tvTongChi = findViewById(R.id.tvTongChi);
        tvNganSach = findViewById(R.id.tvNganSach);
        pbNganSach = findViewById(R.id.pbNganSach);
        lvGiaoDich = findViewById(R.id.lvGiaoDich);
        tvTrong = findViewById(R.id.tvTrong);
        Button btnTruoc = findViewById(R.id.btnThangTruoc);
        Button btnSau = findViewById(R.id.btnThangSau);
        Button btnThem = findViewById(R.id.btnThem);
        Button btnThongKe = findViewById(R.id.btnThongKe);
        Button btnViHu = findViewById(R.id.btnViHu);
        Button btnMucTieu = findViewById(R.id.btnMucTieu);
        Button btnCaiDat = findViewById(R.id.btnCaiDat);

        adapter = new GiaoDichAdapter(this, dsGiaoDich);
        lvGiaoDich.setAdapter(adapter);
        lvGiaoDich.setEmptyView(tvTrong);

        // Chuyển tháng
        btnTruoc.setOnClickListener(v -> {
            thangDangXem.add(Calendar.MONTH, -1);
            taiDuLieu();
        });
        btnSau.setOnClickListener(v -> {
            thangDangXem.add(Calendar.MONTH, 1);
            taiDuLieu();
        });

        // Bấm vào 1 giao dịch -> mở màn hình sửa (truyền id bằng Intent)
        lvGiaoDich.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, ThemGiaoDichActivity.class);
            i.putExtra("id", dsGiaoDich.get(position).getId());
            startActivity(i);
        });

        // Nhấn giữ -> hỏi xóa
        lvGiaoDich.setOnItemLongClickListener((parent, view, position, id) -> {
            GiaoDich g = dsGiaoDich.get(position);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.xoa_giao_dich_hoi)
                    .setMessage(g.getTenDanhMuc() + ": " + TienIch.dinhDangTien(g.getSoTien()))
                    .setPositiveButton(R.string.xoa, (dialog, which) -> {
                        db.xoaGiaoDich(uid, g.getId());
                        Toast.makeText(this, R.string.da_xoa, Toast.LENGTH_SHORT).show();
                        taiDuLieu();
                    })
                    .setNegativeButton(R.string.huy, null)
                    .show();
            return true;
        });

        btnThem.setOnClickListener(v -> startActivity(new Intent(this, ThemGiaoDichActivity.class)));

        btnThongKe.setOnClickListener(v -> {
            Intent i = new Intent(this, ThongKeActivity.class);
            i.putExtra("thang", TienIch.thangKey(thangDangXem));
            startActivity(i);
        });
        btnViHu.setOnClickListener(v -> startActivity(new Intent(this, HuActivity.class)));
        btnMucTieu.setOnClickListener(v -> startActivity(new Intent(this, MucTieuActivity.class)));
        btnCaiDat.setOnClickListener(v -> startActivity(new Intent(this, CaiDatActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db == null) return; // chưa đăng nhập, đang chuyển sang màn hình đăng nhập
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(getString(R.string.xin_chao, PhienDangNhap.getHoTen(this)));
        }
        taiDuLieu(); // quay lại từ màn hình khác thì tải lại dữ liệu
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_cai_dat) {
            startActivity(new Intent(this, CaiDatActivity.class));
            return true;
        } else if (id == R.id.menu_dang_xuat) {
            TienIch.hoiDangXuat(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Android 13+ cần xin quyền hiển thị thông báo. */
    private void xinQuyenThongBao() {
        if (!ThongBaoHelper.coQuyen(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

    private void taiDuLieu() {
        String thang = TienIch.thangKey(thangDangXem);
        tvThang.setText(getString(R.string.thang_nam, TienIch.thangHienThi(thangDangXem)));

        dsGiaoDich.clear();
        dsGiaoDich.addAll(db.layGiaoDichTheoThang(uid, thang));
        adapter.notifyDataSetChanged();

        long thu = db.tongTheoThang(uid, thang, GiaoDich.THU);
        long chi = db.tongTheoThang(uid, thang, GiaoDich.CHI);
        long soDu = thu - chi;

        tvSoDu.setText(TienIch.dinhDangTien(soDu));
        tvSoDu.setTextColor(ContextCompat.getColor(this, soDu >= 0 ? R.color.ct_thu : R.color.ct_chi));
        tvTongThu.setText(getString(R.string.tong_thu, TienIch.dinhDangTien(thu)));
        tvTongChi.setText(getString(R.string.tong_chi, TienIch.dinhDangTien(chi)));

        capNhatNganSach(chi);
    }

    /** Hiển thị thanh tiến độ chi tiêu so với ngân sách tháng. */
    private void capNhatNganSach(long chi) {
        CaiDat caiDat = new CaiDat(this);
        long nganSach = caiDat.getNganSach();

        if (nganSach <= 0) {
            tvNganSach.setText(R.string.chua_dat_ngan_sach);
            pbNganSach.setVisibility(View.GONE);
            return;
        }

        pbNganSach.setVisibility(View.VISIBLE);
        int phanTram = (int) (chi * 100 / nganSach);
        pbNganSach.setProgress(Math.min(100, phanTram));
        tvNganSach.setText(getString(R.string.ngan_sach_da_chi,
                TienIch.dinhDangTien(chi), TienIch.dinhDangTien(nganSach), phanTram));

        int mau;
        if (chi >= nganSach) {
            mau = R.color.ct_chi;
        } else if (phanTram >= caiDat.getNguong()) {
            mau = R.color.ct_accent;
        } else {
            mau = R.color.ct_thu;
        }
        pbNganSach.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, mau)));
    }
}
