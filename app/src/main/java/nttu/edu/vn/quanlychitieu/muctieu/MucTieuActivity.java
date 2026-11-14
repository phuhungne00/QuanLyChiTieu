package nttu.edu.vn.quanlychitieu.muctieu;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Quản lý mục tiêu tiết kiệm: thêm mục tiêu, nạp tiền, xóa. */
public class MucTieuActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int uid;
    private final List<MucTieu> dsMucTieu = new ArrayList<>();
    private MucTieuAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muc_tieu);
        TienIch.apDungInsets(findViewById(R.id.rootMucTieu));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.muc_tieu_tiet_kiem);

        db = new DatabaseHelper(this);
        uid = PhienDangNhap.getUserId(this);

        ListView lv = findViewById(R.id.lvMucTieu);
        lv.setEmptyView(findViewById(R.id.tvTrongMucTieu));
        Button btnThem = findViewById(R.id.btnThemMucTieu);

        adapter = new MucTieuAdapter(this, dsMucTieu);
        lv.setAdapter(adapter);

        // Bấm vào mục tiêu: chọn Nạp tiền hoặc Xóa
        lv.setOnItemClickListener((parent, view, position, id) -> {
            MucTieu m = dsMucTieu.get(position);
            String[] luaChon = {getString(R.string.nap_tien), getString(R.string.xoa_muc_tieu)};
            new AlertDialog.Builder(this)
                    .setTitle(m.getTen())
                    .setItems(luaChon, (dialog, which) -> {
                        if (which == 0) hopThoaiNapTien(m);
                        else xacNhanXoa(m);
                    })
                    .show();
        });

        btnThem.setOnClickListener(v -> hopThoaiThemMucTieu());
        taiDuLieu();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void taiDuLieu() {
        dsMucTieu.clear();
        dsMucTieu.addAll(db.layTatCaMucTieu(uid));
        adapter.notifyDataSetChanged();
    }

    // ------------------------------------------------------------------
    // Custom dialog thêm mục tiêu (Chương 2 - Custom Dialog)
    // ------------------------------------------------------------------
    private void hopThoaiThemMucTieu() {
        View noiDung = LayoutInflater.from(this).inflate(R.layout.dialog_muc_tieu, null);
        final EditText etTen = noiDung.findViewById(R.id.etTenMucTieu);
        final EditText etSoTien = noiDung.findViewById(R.id.etSoTienMucTieu);
        final Button btnHan = noiDung.findViewById(R.id.btnHanMucTieu);

        // Hạn mặc định: 6 tháng nữa. Dùng mảng 1 phần tử để sửa được bên trong lambda.
        Calendar macDinh = Calendar.getInstance();
        macDinh.add(Calendar.MONTH, 6);
        final String[] han = {String.format(Locale.US, "%04d-%02d-%02d",
                macDinh.get(Calendar.YEAR), macDinh.get(Calendar.MONTH) + 1,
                macDinh.get(Calendar.DAY_OF_MONTH))};
        btnHan.setText(getString(R.string.han_hoan_thanh, TienIch.ngayHienThi(han[0])));

        btnHan.setOnClickListener(v -> {
            String[] p = han[0].split("-");
            new DatePickerDialog(this, (view, y, m, d) -> {
                han[0] = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                btnHan.setText(getString(R.string.han_hoan_thanh, TienIch.ngayHienThi(han[0])));
            }, Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2])).show();
        });

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.them_muc_tieu)
                .setView(noiDung)
                .setPositiveButton(R.string.luu, null) // gán sự kiện sau để kiểm tra dữ liệu mà không tự đóng
                .setNegativeButton(R.string.huy, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String ten = etTen.getText().toString().trim();
            String sTien = etSoTien.getText().toString().trim();
            if (ten.isEmpty()) {
                etTen.setError(getString(R.string.loi_nhap_ten));
                return;
            }
            long soTien = docSoTien(sTien);
            if (soTien <= 0) {
                etSoTien.setError(getString(R.string.loi_so_tien_khong_hop_le));
                return;
            }
            db.themMucTieu(uid, new MucTieu(ten, soTien, 0, han[0]));
            taiDuLieu();
            dialog.dismiss();
        });
    }

    private void hopThoaiNapTien(final MucTieu m) {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setHint(R.string.so_tien_nap);

        // Bọc EditText trong FrameLayout để có lề hai bên
        FrameLayout khung = new FrameLayout(this);
        int le = (int) (20 * getResources().getDisplayMetrics().density);
        khung.setPadding(le, le / 2, le, 0);
        khung.addView(et);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.nap_tien)
                .setView(khung)
                .setPositiveButton(R.string.luu, null)
                .setNegativeButton(R.string.huy, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            long soTien = docSoTien(et.getText().toString().trim());
            if (soTien <= 0) {
                et.setError(getString(R.string.loi_so_tien_khong_hop_le));
                return;
            }
            db.napTienMucTieu(uid, m.getId(), soTien);
            if (m.getDaTietKiem() + soTien >= m.getSoTienMucTieu()) {
                Toast.makeText(this, R.string.chuc_mung_hoan_thanh, Toast.LENGTH_LONG).show();
            }
            taiDuLieu();
            dialog.dismiss();
        });
    }

    private void xacNhanXoa(final MucTieu m) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.xoa_muc_tieu_hoi)
                .setMessage(m.getTen())
                .setPositiveButton(R.string.xoa, (d, w) -> {
                    db.xoaMucTieu(uid, m.getId());
                    Toast.makeText(this, R.string.da_xoa, Toast.LENGTH_SHORT).show();
                    taiDuLieu();
                })
                .setNegativeButton(R.string.huy, null)
                .show();
    }

    /** Đọc số tiền từ chuỗi, trả về 0 nếu rỗng hoặc không hợp lệ. */
    private long docSoTien(String s) {
        if (s.isEmpty()) return 0;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
