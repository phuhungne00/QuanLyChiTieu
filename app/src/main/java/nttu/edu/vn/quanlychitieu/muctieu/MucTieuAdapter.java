package nttu.edu.vn.quanlychitieu.muctieu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Adapter hiển thị danh sách mục tiêu tiết kiệm kèm thanh tiến độ. */
public class MucTieuAdapter extends ArrayAdapter<MucTieu> {

    public MucTieuAdapter(Context context, List<MucTieu> ds) {
        super(context, 0, ds);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_muc_tieu, parent, false);
        }
        MucTieu m = getItem(position);

        TextView tvTen = convertView.findViewById(R.id.tvTenMucTieu);
        TextView tvTienDo = convertView.findViewById(R.id.tvTienDo);
        ProgressBar pb = convertView.findViewById(R.id.pbMucTieu);
        TextView tvHan = convertView.findViewById(R.id.tvHanMucTieu);
        TextView tvGoiY = convertView.findViewById(R.id.tvGoiY);

        tvTen.setText(m.getTen());
        tvTienDo.setText(getContext().getString(R.string.tien_do_muc_tieu,
                TienIch.dinhDangTien(m.getDaTietKiem()),
                TienIch.dinhDangTien(m.getSoTienMucTieu()),
                m.getPhanTram()));
        pb.setProgress(m.getPhanTram());

        long conThieu = m.getSoTienMucTieu() - m.getDaTietKiem();
        long ngayCon = TienIch.soNgayConLai(m.getHan());

        if (ngayCon >= 0) {
            tvHan.setText(getContext().getString(R.string.han_con_ngay,
                    TienIch.ngayHienThi(m.getHan()), ngayCon));
        } else {
            tvHan.setText(getContext().getString(R.string.han_qua_han,
                    TienIch.ngayHienThi(m.getHan())));
        }

        // Gợi ý thông minh: mỗi tháng cần tiết kiệm bao nhiêu để kịp hạn
        if (conThieu <= 0) {
            tvGoiY.setText(R.string.muc_tieu_hoan_thanh);
        } else if (ngayCon <= 0) {
            tvGoiY.setText(getContext().getString(R.string.muc_tieu_con_thieu,
                    TienIch.dinhDangTien(conThieu)));
        } else {
            long soThang = Math.max(1, (long) Math.ceil(ngayCon / 30.0));
            long moiThang = (conThieu + soThang - 1) / soThang;
            tvGoiY.setText(getContext().getString(R.string.goi_y_moi_thang,
                    TienIch.dinhDangTien(moiThang)));
        }
        return convertView;
    }
}
