package nttu.edu.vn.quanlychitieu.hu;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Lịch sử nạp tiền và chuyển tiền (chỉ xem). */
public class LichSuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su);
        TienIch.apDungInsets(findViewById(R.id.rootLichSu));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.lich_su);

        DatabaseHelper db = new DatabaseHelper(this);
        int uid = PhienDangNhap.getUserId(this);

        Map<Integer, String> tenHu = new HashMap<>();
        for (Hu h : db.layDanhSachHu(uid, true)) tenHu.put(h.getId(), h.getTen());

        List<ChuyenTien> ds = db.layLichSuChuyenTien(uid);
        ListView lv = findViewById(R.id.lvLichSu);
        lv.setEmptyView(findViewById(R.id.tvTrongLichSu));
        lv.setAdapter(new LichSuAdapter(this, ds, tenHu));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
