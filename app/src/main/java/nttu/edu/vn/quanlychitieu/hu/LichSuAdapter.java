package nttu.edu.vn.quanlychitieu.hu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Map;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Adapter hiển thị lịch sử nạp tiền và chuyển tiền. */
public class LichSuAdapter extends ArrayAdapter<ChuyenTien> {

    private final Map<Integer, String> tenHu; // id hũ -> tên hũ

    public LichSuAdapter(Context context, List<ChuyenTien> ds, Map<Integer, String> tenHu) {
        super(context, 0, ds);
        this.tenHu = tenHu;
    }

    private String ten(int id) {
        String t = tenHu.get(id);
        return t == null ? "?" : t;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_lich_su, parent, false);
        }
        ChuyenTien ct = getItem(position);

        TextView tvLoai = convertView.findViewById(R.id.tvLoaiLs);
        TextView tvChiTiet = convertView.findViewById(R.id.tvChiTietLs);
        TextView tvNgay = convertView.findViewById(R.id.tvNgayLs);
        TextView tvSoTien = convertView.findViewById(R.id.tvSoTienLs);

        boolean laNap = ct.getLoai() == ChuyenTien.NAP;
        String tu = laNap ? ct.getNguon() : ten(ct.getTuHu());
        String chiTiet = tu + " → " + ten(ct.getDenHu());
        if (ct.getGhiChu() != null && !ct.getGhiChu().isEmpty()) {
            chiTiet += "\n" + ct.getGhiChu();
        }

        tvLoai.setText(laNap ? R.string.nap_tien_vi : R.string.chuyen_tien);
        tvChiTiet.setText(chiTiet);
        tvNgay.setText(TienIch.ngayHienThi(ct.getNgay()));
        tvSoTien.setText((laNap ? "+" : "") + TienIch.dinhDangTien(ct.getSoTien()));
        tvSoTien.setTextColor(ContextCompat.getColor(getContext(),
                laNap ? R.color.ct_thu : R.color.ct_primary));
        return convertView;
    }
}
