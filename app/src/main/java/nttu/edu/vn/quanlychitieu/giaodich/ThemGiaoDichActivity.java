package nttu.edu.vn.quanlychitieu.giaodich;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.hu.Hu;
import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;
import nttu.edu.vn.quanlychitieu.tienich.ThongBaoHelper;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Thêm mới hoặc sửa một giao dịch (nếu Intent có kèm "id" thì là sửa). */
public class ThemGiaoDichActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int uid;
    private RadioGroup rgLoai;
    private RadioButton rbChi, rbThu;
    private Spinner spDanhMuc, spHu;
    private EditText etSoTien, etGhiChu;
    private Button btnNgay, btnLuu, btnHuy;

    private String ngay = TienIch.homNay();
    private int idSua = -1; // -1: thêm mới

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_giao_dich);
        TienIch.apDungInsets(findViewById(R.id.rootThem));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DatabaseHelper(this);
        uid = PhienDangNhap.getUserId(this);

        rgLoai = findViewById(R.id.rgLoai);
        rbChi = findViewById(R.id.rbChi);
        rbThu = findViewById(R.id.rbThu);
        spDanhMuc = findViewById(R.id.spDanhMuc);
        spHu = findViewById(R.id.spHu);
        etSoTien = findViewById(R.id.etSoTien);
        etGhiChu = findViewById(R.id.etGhiChu);
        btnNgay = findViewById(R.id.btnNgay);
        btnLuu = findViewById(R.id.btnLuu);
        btnHuy = findViewById(R.id.btnHuy);

        idSua = getIntent().getIntExtra("id", -1);
        if (idSua != -1) {
            setTitle(R.string.tieu_de_sua);
            napDuLieuCu();
        } else {
            setTitle(R.string.tieu_de_them);
            napDanhMuc(GiaoDich.CHI, -1);
            napHu(Hu.VI_CHINH);
        }
        btnNgay.setText(TienIch.ngayHienThi(ngay));

        // Đổi Chi tiêu / Thu nhập thì nạp lại danh sách danh mục tương ứng
        rgLoai.setOnCheckedChangeListener((group, checkedId) ->
                napDanhMuc(checkedId == R.id.rbThu ? GiaoDich.THU : GiaoDich.CHI, -1));

        btnNgay.setOnClickListener(v -> chonNgay());
        btnLuu.setOnClickListener(v -> luu());
        btnHuy.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** Nạp danh mục lên Spinner; chonId là danh mục cần chọn sẵn (-1 nếu không có). */
    private void napDanhMuc(int loai, int chonId) {
        List<DanhMuc> ds = db.layDanhMuc(uid, loai);
        ArrayAdapter<DanhMuc> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ds);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDanhMuc.setAdapter(ad);
        if (chonId != -1) {
            for (int i = 0; i < ds.size(); i++) {
                if (ds.get(i).getId() == chonId) {
                    spDanhMuc.setSelection(i);
                    break;
                }
            }
        }
    }

    /** Nạp Ví chính và các hũ lên Spinner; chonId là Ví / hũ cần chọn sẵn. */
    private void napHu(int chonId) {
        List<Hu> ds = db.layDanhSachHu(uid, true);
        ArrayAdapter<Hu> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ds);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spHu.setAdapter(ad);
        for (int i = 0; i < ds.size(); i++) {
            if (ds.get(i).getId() == chonId) {
                spHu.setSelection(i);
                break;
            }
        }
    }

    /** Chế độ sửa: đọc giao dịch cũ từ CSDL rồi đổ lên giao diện. */
    private void napDuLieuCu() {
        GiaoDich g = db.layGiaoDichTheoId(uid, idSua);
        if (g == null) {
            finish();
            return;
        }
        ngay = g.getNgay();
        if (g.getLoai() == GiaoDich.THU) rbThu.setChecked(true);
        else rbChi.setChecked(true);
        napDanhMuc(g.getLoai(), g.getDanhMucId());
        napHu(g.getHuId());
        etSoTien.setText(String.valueOf(g.getSoTien()));
        etGhiChu.setText(g.getGhiChu());
    }

    private void chonNgay() {
        String[] p = ngay.split("-");
        int nam = Integer.parseInt(p[0]);
        int thang = Integer.parseInt(p[1]) - 1;
        int ngayTrongThang = Integer.parseInt(p[2]);
        new DatePickerDialog(this, (view, y, m, d) -> {
            ngay = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            btnNgay.setText(TienIch.ngayHienThi(ngay));
        }, nam, thang, ngayTrongThang).show();
    }

    private void luu() {
        String s = etSoTien.getText().toString().trim();
        if (s.isEmpty()) {
            etSoTien.setError(getString(R.string.loi_nhap_so_tien));
            return;
        }
        long soTien;
        try {
            soTien = Long.parseLong(s);
        } catch (NumberFormatException e) {
            etSoTien.setError(getString(R.string.loi_so_tien_khong_hop_le));
            return;
        }
        if (soTien <= 0) {
            etSoTien.setError(getString(R.string.loi_so_tien_khong_hop_le));
            return;
        }

        DanhMuc dm = (DanhMuc) spDanhMuc.getSelectedItem();
        if (dm == null) {
            Toast.makeText(this, R.string.loi_chua_co_danh_muc, Toast.LENGTH_LONG).show();
            return;
        }
        Hu hu = (Hu) spHu.getSelectedItem();
        int huId = hu == null ? Hu.VI_CHINH : hu.getId();
        int loai = rbThu.isChecked() ? GiaoDich.THU : GiaoDich.CHI;

        GiaoDich g = new GiaoDich(soTien, loai, dm.getId(), huId,
                etGhiChu.getText().toString().trim(), ngay);
        if (idSua == -1) {
            db.themGiaoDich(uid, g);
        } else {
            g.setId(idSua);
            db.suaGiaoDich(uid, g);
        }

        // Nếu là khoản chi: kiểm tra ngân sách và gửi thông báo cảnh báo (nếu cần)
        if (loai == GiaoDich.CHI) {
            ThongBaoHelper.kiemTraNganSach(this, db, uid);
        }

        // Chi vượt quá số dư của ví / hũ thì nhắc người dùng (vẫn cho lưu)
        long soDuSau = db.tinhSoDu(uid, huId);
        if (loai == GiaoDich.CHI && soDuSau < 0) {
            String ten = hu == null ? getString(R.string.vi_chinh) : hu.getTen();
            Toast.makeText(this, getString(R.string.canh_bao_am_hu, ten,
                    TienIch.dinhDangTien(soDuSau)), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.da_luu, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
