package nttu.edu.vn.quanlychitieu.caidat;

import android.Manifest;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.tienich.ThongBaoHelper;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Đặt ngân sách chi tiêu tháng và cảnh báo (lưu bằng SharedPreferences). */
public class NganSachActivity extends AppCompatActivity {

    private static final int NGUONG_TOI_THIEU = 50; // SeekBar 0..50 tương ứng 50%..100%

    private EditText etNganSach;
    private SwitchCompat swCanhBao;
    private SeekBar skNguong;
    private TextView tvNguong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngan_sach);
        TienIch.apDungInsets(findViewById(R.id.rootNganSach));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.ngan_sach_va_canh_bao);

        etNganSach = findViewById(R.id.etNganSach);
        swCanhBao = findViewById(R.id.swCanhBao);
        skNguong = findViewById(R.id.skNguong);
        tvNguong = findViewById(R.id.tvNguong);
        Button btnLuu = findViewById(R.id.btnLuuNganSach);
        Button btnThu = findViewById(R.id.btnThuThongBao);

        CaiDat caiDat = new CaiDat(this);
        if (caiDat.getNganSach() > 0) etNganSach.setText(String.valueOf(caiDat.getNganSach()));
        swCanhBao.setChecked(caiDat.isBatCanhBao());
        skNguong.setProgress(caiDat.getNguong() - NGUONG_TOI_THIEU);
        capNhatNhanNguong();

        skNguong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                capNhatNhanNguong();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnLuu.setOnClickListener(v -> luu());
        btnThu.setOnClickListener(v -> guiThongBaoThu());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void capNhatNhanNguong() {
        int nguong = skNguong.getProgress() + NGUONG_TOI_THIEU;
        tvNguong.setText(getString(R.string.nhan_nguong, nguong));
    }

    private void luu() {
        long nganSach = 0;
        String s = etNganSach.getText().toString().trim();
        if (!s.isEmpty()) {
            try {
                nganSach = Long.parseLong(s);
            } catch (NumberFormatException e) {
                etNganSach.setError(getString(R.string.loi_so_tien_khong_hop_le));
                return;
            }
        }
        int nguong = skNguong.getProgress() + NGUONG_TOI_THIEU;
        new CaiDat(this).luu(nganSach, swCanhBao.isChecked(), nguong);

        Toast.makeText(this, R.string.da_luu, Toast.LENGTH_SHORT).show();
        finish();
    }

    /** Gửi thử một thông báo để kiểm tra Local Notification hoạt động. */
    private void guiThongBaoThu() {
        if (!ThongBaoHelper.coQuyen(this)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 200);
            Toast.makeText(this, R.string.can_cap_quyen_thong_bao, Toast.LENGTH_LONG).show();
            return;
        }
        ThongBaoHelper.hien(this, ThongBaoHelper.ID_THU_NGHIEM,
                getString(R.string.tb_thu_tieu_de), getString(R.string.tb_thu_noi_dung));
    }
}
