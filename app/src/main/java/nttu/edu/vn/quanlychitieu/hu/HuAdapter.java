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

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Adapter hiển thị Ví chính và các hũ kèm số dư. */
public class HuAdapter extends ArrayAdapter<Hu> {

    public HuAdapter(Context context, List<Hu> ds) {
        super(context, 0, ds);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_hu, parent, false);
        }
        Hu h = getItem(position);

        TextView tvTen = convertView.findViewById(R.id.tvTenHu);
        TextView tvPhu = convertView.findViewById(R.id.tvPhuHu);
        TextView tvSoDu = convertView.findViewById(R.id.tvSoDuHu);

        tvTen.setText(h.getTen());
        if (h.getId() == Hu.VI_CHINH) {
            tvPhu.setText(R.string.mo_ta_vi_chinh);
        } else {
            tvPhu.setText(getContext().getString(R.string.ty_le_hu, h.getTyLe()));
        }
        tvSoDu.setText(TienIch.dinhDangTien(h.getSoDu()));
        tvSoDu.setTextColor(ContextCompat.getColor(getContext(),
                h.getSoDu() < 0 ? R.color.ct_chi : R.color.ct_primary));
        return convertView;
    }
}
