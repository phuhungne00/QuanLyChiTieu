package nttu.edu.vn.quanlychitieu.caidat;

import android.content.Context;
import android.content.SharedPreferences;

import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;

/** Cài đặt ngân sách và cảnh báo (SharedPreferences, Chương 6). Mỗi tài khoản có cài đặt riêng. */
public class CaiDat {
    private static final String TEN_FILE = "cai_dat_chi_tieu";
    private static final String KEY_NGAN_SACH = "ngan_sach_thang";
    private static final String KEY_BAT_CANH_BAO = "bat_canh_bao";
    private static final String KEY_NGUONG = "nguong_canh_bao";

    private final SharedPreferences sp;
    private final String tienTo; // ví dụ "u3_" để tách cài đặt theo tài khoản

    public CaiDat(Context context) {
        sp = context.getSharedPreferences(TEN_FILE, Context.MODE_PRIVATE);
        tienTo = "u" + PhienDangNhap.getUserId(context) + "_";
    }

    /** Hạn mức chi tiêu mỗi tháng (0 = chưa đặt). */
    public long getNganSach() {
        return sp.getLong(tienTo + KEY_NGAN_SACH, 0);
    }

    public boolean isBatCanhBao() {
        return sp.getBoolean(tienTo + KEY_BAT_CANH_BAO, true);
    }

    /** Ngưỡng cảnh báo, tính theo % ngân sách (mặc định 80%). */
    public int getNguong() {
        return sp.getInt(tienTo + KEY_NGUONG, 80);
    }

    public void luu(long nganSach, boolean batCanhBao, int nguong) {
        sp.edit()
                .putLong(tienTo + KEY_NGAN_SACH, nganSach)
                .putBoolean(tienTo + KEY_BAT_CANH_BAO, batCanhBao)
                .putInt(tienTo + KEY_NGUONG, nguong)
                .apply();
    }

    /** Xóa cài đặt của tài khoản hiện tại (dùng khi xóa tài khoản). */
    public void xoaTatCa() {
        sp.edit()
                .remove(tienTo + KEY_NGAN_SACH)
                .remove(tienTo + KEY_BAT_CANH_BAO)
                .remove(tienTo + KEY_NGUONG)
                .apply();
    }
}
