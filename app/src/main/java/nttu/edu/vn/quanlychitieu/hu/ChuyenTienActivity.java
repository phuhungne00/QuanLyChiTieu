package nttu.edu.vn.quanlychitieu.hu;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/**
 * Dùng chung cho 2 chức năng (phân biệt bằng Intent extra "che_do"):
 *  - NẠP TIỀN: đưa tiền từ bên ngoài (tiền mặt, ngân hàng...) vào Ví chính hoặc một hũ.
 *  - CHUYỂN TIỀN: chuyển tiền giữa Ví chính và các hũ, không được vượt quá số dư nguồn.
 */
public class ChuyenTienActivity extends AppCompatActivity {

    public static final String EXTRA_CHE_DO = "che_do";
    public static final int CHE_DO_NAP = 0;
    public static final int CHE_DO_CHUYEN = 1;

    private DatabaseHelper db;
    private int uid;
    private int cheDo;

    private Spinner spNguon, spDich;
    private TextView tvSoDu;
    private EditText etSoTien, etGhiChu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chuyen_tien);
        TienIch.apDungInsets(findViewById(R.id.rootChuyenTien));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DatabaseHelper(this);
        uid = PhienDangNhap.getUserId(this);
        cheDo = getIntent().getIntExtra(EXTRA_CHE_DO, CHE_DO_NAP);

        TextView tvNhanNguon = findViewById(R.id.tvNhanNguon);
        TextView tvNhanDich = findViewById(R.id.tvNhanDich);
        spNguon = findViewById(R.id.spNguon);
        spDich = findViewById(R.id.spDich);
        tvSoDu = findViewById(R.id.tvSoDuKhaDung);
        etSoTien = findViewById(R.id.etSoTienCt);
        etGhiChu = findViewById(R.id.etGhiChuCt);
        Button btnHuy = findViewById(R.id.btnHuyCt);
        Button btnXacNhan = findViewById(R.id.btnXacNhanCt);

        List<Hu> dsHu = db.layDanhSachHu(uid, true);
        ArrayAdapter<Hu> adDich = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dsHu);
        adDich.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDich.setAdapter(adDich);

        if (cheDo == CHE_DO_NAP) {
            setTitle(R.string.nap_tien_vi);
            tvNhanNguon.setText(R.string.nap_tu_nguon);
            tvNhanDich.setText(R.string.nap_vao);
            String[] nguon = {getString(R.string.nguon_tien_mat), getString(R.string.nguon_ngan_hang),
                    getString(R.string.nguon_vi_dien_tu), getString(R.string.nguon_khac)};
            ArrayAdapter<String> adNguon = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nguon);
            adNguon.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spNguon.setAdapter(adNguon);
        } else {
            setTitle(R.string.chuyen_tien);
            tvNhanNguon.setText(R.string.chuyen_tu);
            tvNhanDich.setText(R.string.chuyen_den);
            ArrayAdapter<Hu> adNguon = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dsHu);
            adNguon.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spNguon.setAdapter(adNguon);
            if (dsHu.size() > 1) spDich.setSelection(1); // mặc định đích khác nguồn
        }

        AdapterView.OnItemSelectedListener boLang = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                capNhatSoDu();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spNguon.setOnItemSelectedListener(boLang);
        spDich.setOnItemSelectedListener(boLang);

        btnHuy.setOnClickListener(v -> finish());
        btnXacNhan.setOnClickListener(v -> xacNhan());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /** Nạp tiền: hiện số dư nơi nhận. Chuyển tiền: hiện số dư khả dụng của nguồn. */
    private void capNhatSoDu() {
        Object chon = cheDo == CHE_DO_NAP ? spDich.getSelectedItem() : spNguon.getSelectedItem();
        if (!(chon instanceof Hu)) return;
        long soDu = db.tinhSoDu(uid, ((Hu) chon).getId());
        tvSoDu.setText(getString(
                cheDo == CHE_DO_NAP ? R.string.so_du_hien_tai : R.string.so_du_kha_dung,
                TienIch.dinhDangTien(soDu)));
    }

    private void xacNhan() {
        long soTien = TienIch.docSoTien(etSoTien.getText().toString());
        if (soTien <= 0) {
            etSoTien.setError(getString(R.string.loi_so_tien_khong_hop_le));
            return;
        }
        Hu dich = (Hu) spDich.getSelectedItem();
        if (dich == null) return;

        ChuyenTien ct = new ChuyenTien();
        ct.setSoTien(soTien);
        ct.setDenHu(dich.getId());
        ct.setGhiChu(etGhiChu.getText().toString().trim());
        ct.setNgay(TienIch.homNay());

        if (cheDo == CHE_DO_NAP) {
            ct.setLoai(ChuyenTien.NAP);
            ct.setTuHu(ChuyenTien.NGUON_NGOAI);
            ct.setNguon((String) spNguon.getSelectedItem());
        } else {
            Hu nguon = (Hu) spNguon.getSelectedItem();
            if (nguon == null) return;
            if (nguon.getId() == dich.getId()) {
                Toast.makeText(this, R.string.loi_cung_hu, Toast.LENGTH_SHORT).show();
                return;
            }
            // Đọc số dư mới nhất từ CSDL rồi mới so sánh
            long soDu = db.tinhSoDu(uid, nguon.getId());
            if (soTien > soDu) {
                etSoTien.setError(getString(R.string.loi_khong_du_tien,
                        TienIch.dinhDangTien(Math.max(0, soDu))));
                return;
            }
            ct.setLoai(ChuyenTien.CHUYEN);
            ct.setTuHu(nguon.getId());
            ct.setNguon("");
        }

        db.themChuyenTien(uid, ct);
        Toast.makeText(this, cheDo == CHE_DO_NAP ? R.string.nap_thanh_cong : R.string.chuyen_thanh_cong,
                Toast.LENGTH_SHORT).show();
        finish();
    }
}
