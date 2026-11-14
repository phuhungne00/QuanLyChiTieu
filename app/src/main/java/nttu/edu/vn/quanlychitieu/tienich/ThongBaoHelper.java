package nttu.edu.vn.quanlychitieu.tienich;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import nttu.edu.vn.quanlychitieu.MainActivity;
import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.caidat.CaiDat;
import nttu.edu.vn.quanlychitieu.csdl.DatabaseHelper;
import nttu.edu.vn.quanlychitieu.giaodich.GiaoDich;

/** Tạo và hiển thị Local Notification để cảnh báo chi tiêu. */
public class ThongBaoHelper {

    private static final String KENH_ID = "kenh_canh_bao_chi_tieu";
    private static final int ID_CANH_BAO = 1001;
    public static final int ID_THU_NGHIEM = 1002;

    /** Từ Android 8 bắt buộc phải có Notification Channel. */
    public static void taoKenh(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel kenh = new NotificationChannel(
                    KENH_ID, ctx.getString(R.string.ten_kenh), NotificationManager.IMPORTANCE_DEFAULT);
            kenh.setDescription(ctx.getString(R.string.mo_ta_kenh));
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(kenh);
        }
    }

    /** Từ Android 13 phải được người dùng cho phép POST_NOTIFICATIONS. */
    public static boolean coQuyen(Context ctx) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public static void hien(Context ctx, int id, String tieuDe, String noiDung) {
        if (!coQuyen(ctx)) return;

        // Bấm vào thông báo sẽ mở màn hình chính
        Intent mo = new Intent(ctx, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, mo,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, KENH_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(tieuDe)
                .setContentText(noiDung)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(noiDung))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManagerCompat.from(ctx).notify(id, builder.build());
    }

    /**
     * Kiểm tra chi tiêu tháng này so với ngân sách và gửi cảnh báo nếu cần.
     * Gọi sau mỗi lần thêm / sửa một khoản chi.
     */
    public static void kiemTraNganSach(Context ctx, DatabaseHelper db, int uid) {
        CaiDat caiDat = new CaiDat(ctx);
        long nganSach = caiDat.getNganSach();
        if (!caiDat.isBatCanhBao() || nganSach <= 0) return;

        long chi = db.tongTheoThang(uid, TienIch.thangHienTai(), GiaoDich.CHI);
        int phanTram = (int) (chi * 100 / nganSach);

        if (chi >= nganSach) {
            hien(ctx, ID_CANH_BAO,
                    ctx.getString(R.string.tb_vuot_tieu_de),
                    ctx.getString(R.string.tb_vuot_noi_dung,
                            TienIch.dinhDangTien(chi), TienIch.dinhDangTien(nganSach)));
        } else if (phanTram >= caiDat.getNguong()) {
            hien(ctx, ID_CANH_BAO,
                    ctx.getString(R.string.tb_sap_cham_tieu_de),
                    ctx.getString(R.string.tb_sap_cham_noi_dung,
                            phanTram, TienIch.dinhDangTien(nganSach - chi)));
        }
    }
}
