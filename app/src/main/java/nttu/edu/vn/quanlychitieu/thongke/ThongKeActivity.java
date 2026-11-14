package nttu.edu.vn.quanlychitieu.thongke;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.caidat.CaiDat;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.giaodich.GiaoDich;
import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Thống kê: biểu đồ tròn chi theo danh mục, biểu đồ cột thu - chi 6 tháng và phân tích thông minh. */
public class ThongKeActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int uid;
    private String thang; // dạng yyyy-MM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);
        TienIch.apDungInsets(findViewById(R.id.rootThongKe));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.thong_ke);

        db = new DatabaseHelper(this);
        uid = PhienDangNhap.getUserId(this);

        // Nhận tháng cần xem từ màn hình chính qua Intent
        thang = getIntent().getStringExtra("thang");
        if (thang == null) thang = TienIch.thangHienTai();

        TextView tvTieuDe = findViewById(R.id.tvTieuDeThang);
        tvTieuDe.setText(getString(R.string.thang_nam, TienIch.thangHienThi(TienIch.calTuKey(thang))));

        veBieuDoTron();
        veBieuDoCot();
        phanTich();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ------------------------------------------------------------------
    // Biểu đồ tròn: chi tiêu theo danh mục
    // ------------------------------------------------------------------
    private void veBieuDoTron() {
        PieChart chart = findViewById(R.id.pieChart);
        chart.setNoDataText(getString(R.string.chua_co_du_lieu));
        Map<String, Long> duLieu = db.chiTheoDanhMuc(uid, thang);
        if (duLieu.isEmpty()) {
            chart.clear();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Long> e : duLieu.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
        }

        List<Integer> mau = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) mau.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) mau.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) mau.add(c);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(mau);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());

        chart.setUsePercentValues(true);
        chart.setData(data);
        chart.setDrawEntryLabels(false);          // tên danh mục hiển thị ở chú thích bên dưới
        chart.getDescription().setEnabled(false);
        chart.setCenterText(getString(R.string.chi_tieu));
        chart.getLegend().setWordWrapEnabled(true);
        chart.animateY(700);
        chart.invalidate();
    }

    // ------------------------------------------------------------------
    // Biểu đồ cột: thu và chi 6 tháng gần nhất
    // ------------------------------------------------------------------
    private void veBieuDoCot() {
        final int soThang = 6;
        BarChart chart = findViewById(R.id.barChart);
        chart.setNoDataText(getString(R.string.chua_co_du_lieu));

        List<BarEntry> dsThu = new ArrayList<>();
        List<BarEntry> dsChi = new ArrayList<>();
        final String[] nhan = new String[soThang];

        Calendar cal = TienIch.calTuKey(thang);
        cal.add(Calendar.MONTH, -(soThang - 1));
        for (int i = 0; i < soThang; i++) {
            String key = TienIch.thangKey(cal);
            dsThu.add(new BarEntry(i, db.tongTheoThang(uid, key, GiaoDich.THU)));
            dsChi.add(new BarEntry(i, db.tongTheoThang(uid, key, GiaoDich.CHI)));
            nhan[i] = "T" + (cal.get(Calendar.MONTH) + 1);
            cal.add(Calendar.MONTH, 1);
        }

        BarDataSet setThu = new BarDataSet(dsThu, getString(R.string.thu_nhap));
        setThu.setColor(ContextCompat.getColor(this, R.color.ct_thu));
        setThu.setDrawValues(false);
        BarDataSet setChi = new BarDataSet(dsChi, getString(R.string.chi_tieu));
        setChi.setColor(ContextCompat.getColor(this, R.color.ct_chi));
        setChi.setDrawValues(false);

        // Mỗi nhóm cột rộng 1 đơn vị: 0.2 + 2 x (0.35 + 0.05) = 1.0
        float khoangCachNhom = 0.2f, khoangCachCot = 0.05f, beRongCot = 0.35f;
        BarData data = new BarData(setThu, setChi);
        data.setBarWidth(beRongCot);

        chart.setData(data);
        chart.groupBars(0f, khoangCachNhom, khoangCachCot);

        XAxis trucX = chart.getXAxis();
        trucX.setPosition(XAxis.XAxisPosition.BOTTOM);
        trucX.setGranularity(1f);
        trucX.setCenterAxisLabels(true);   // nhãn nằm giữa mỗi nhóm cột
        trucX.setAxisMinimum(0f);
        trucX.setAxisMaximum(soThang);     // 6 nhóm, mỗi nhóm rộng 1 đơn vị
        trucX.setDrawGridLines(false);
        trucX.setValueFormatter(new IndexAxisValueFormatter(nhan));

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return rutGonTien(value);
            }
        });
        chart.getDescription().setEnabled(false);
        chart.setScaleEnabled(false);
        chart.animateY(700);
        chart.invalidate();
    }

    private String rutGonTien(float v) {
        if (v >= 1000000f) return String.format(Locale.US, "%.1ftr", v / 1000000f);
        if (v >= 1000f) return String.format(Locale.US, "%.0fk", v / 1000f);
        return String.format(Locale.US, "%.0f", v);
    }

    // ------------------------------------------------------------------
    // Phân tích và gợi ý
    // ------------------------------------------------------------------
    private void phanTich() {
        long thu = db.tongTheoThang(uid, thang, GiaoDich.THU);
        long chi = db.tongTheoThang(uid, thang, GiaoDich.CHI);

        TextView tvTongQuan = findViewById(R.id.tvTongQuan);
        tvTongQuan.setText(getString(R.string.tong_quan,
                TienIch.dinhDangTien(thu), TienIch.dinhDangTien(chi), TienIch.dinhDangTien(thu - chi)));

        TextView tvPhanTich = findViewById(R.id.tvPhanTich);
        if (thu == 0 && chi == 0) {
            tvPhanTich.setText(R.string.chua_co_du_lieu);
            return;
        }

        StringBuilder sb = new StringBuilder();

        // 1. So sánh với tháng trước
        Calendar calTruoc = TienIch.calTuKey(thang);
        calTruoc.add(Calendar.MONTH, -1);
        long chiTruoc = db.tongTheoThang(uid, TienIch.thangKey(calTruoc), GiaoDich.CHI);
        if (chiTruoc > 0) {
            long pt = Math.round((chi - chiTruoc) * 100.0 / chiTruoc);
            if (pt > 0) {
                dong(sb, getString(R.string.pt_tang, pt));
            } else if (pt < 0) {
                dong(sb, getString(R.string.pt_giam, -pt));
            } else {
                dong(sb, getString(R.string.pt_bang));
            }
        }

        // 2. Danh mục chi nhiều nhất
        Map<String, Long> theoDanhMuc = db.chiTheoDanhMuc(uid, thang);
        if (!theoDanhMuc.isEmpty() && chi > 0) {
            Map.Entry<String, Long> dau = theoDanhMuc.entrySet().iterator().next();
            long pt = Math.round(dau.getValue() * 100.0 / chi);
            dong(sb, getString(R.string.pt_nhieu_nhat, dau.getKey(), pt));
        }

        // 3. Chi trung bình mỗi ngày + dự báo cuối tháng
        boolean laThangHienTai = thang.equals(TienIch.thangHienTai());
        int soNgayTrongThang = TienIch.calTuKey(thang).getActualMaximum(Calendar.DAY_OF_MONTH);
        int soNgayDaQua = laThangHienTai
                ? Calendar.getInstance().get(Calendar.DAY_OF_MONTH) : soNgayTrongThang;
        if (chi > 0) {
            dong(sb, getString(R.string.pt_trung_binh_ngay, TienIch.dinhDangTien(chi / soNgayDaQua)));
        }
        if (laThangHienTai && chi > 0 && soNgayDaQua < soNgayTrongThang) {
            long duBao = chi * soNgayTrongThang / soNgayDaQua;
            dong(sb, getString(R.string.pt_du_bao, TienIch.dinhDangTien(duBao)));

            long nganSach = new CaiDat(this).getNganSach();
            if (nganSach > 0) {
                if (duBao > nganSach) {
                    dong(sb, getString(R.string.pt_du_bao_vuot, TienIch.dinhDangTien(duBao - nganSach)));
                } else {
                    dong(sb, getString(R.string.pt_du_bao_on));
                }
            }
        }

        // 4. Tỷ lệ tiết kiệm (quy tắc 50/30/20 khuyên tiết kiệm ít nhất 20% thu nhập)
        if (thu > 0) {
            long tyLe = Math.round((thu - chi) * 100.0 / thu);
            if (tyLe >= 20) {
                dong(sb, getString(R.string.pt_tiet_kiem_tot, tyLe));
            } else {
                dong(sb, getString(R.string.pt_tiet_kiem_thap, tyLe));
            }
        }

        tvPhanTich.setText(sb.toString().trim());
    }

    private void dong(StringBuilder sb, String noiDung) {
        sb.append("• ").append(noiDung).append("\n\n");
    }
}
