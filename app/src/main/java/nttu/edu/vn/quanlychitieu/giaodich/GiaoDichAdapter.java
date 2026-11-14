package nttu.edu.vn.quanlychitieu.giaodich;

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

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Adapter hiển thị danh sách giao dịch lên ListView (Chương 3). */
public class GiaoDichAdapter extends ArrayAdapter<GiaoDich> {

    public GiaoDichAdapter(Context context, List<GiaoDich> ds) {
        super(context, 0, ds);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_giao_dich, parent, false);
        }
        GiaoDich g = getItem(position);

        TextView tvDanhMuc = convertView.findViewById(R.id.tvDanhMuc);
        TextView tvGhiChu = convertView.findViewById(R.id.tvGhiChu);
        TextView tvNgay = convertView.findViewById(R.id.tvNgay);
        TextView tvSoTien = convertView.findViewById(R.id.tvSoTien);

        tvDanhMuc.setText(g.getTenDanhMuc());
        tvGhiChu.setText(g.getGhiChu());
        tvGhiChu.setVisibility(g.getGhiChu() == null || g.getGhiChu().isEmpty() ? View.GONE : View.VISIBLE);
        String nguonTien = g.getTenHu() == null ? getContext().getString(R.string.vi_chinh) : g.getTenHu();
        tvNgay.setText(TienIch.ngayHienThi(g.getNgay()) + " · " + nguonTien);

        if (g.getLoai() == GiaoDich.THU) {
            tvSoTien.setText("+" + TienIch.dinhDangTien(g.getSoTien()));
            tvSoTien.setTextColor(ContextCompat.getColor(getContext(), R.color.ct_thu));
        } else {
            tvSoTien.setText("-" + TienIch.dinhDangTien(g.getSoTien()));
            tvSoTien.setTextColor(ContextCompat.getColor(getContext(), R.color.ct_chi));
        }
        return convertView;
    }
}
