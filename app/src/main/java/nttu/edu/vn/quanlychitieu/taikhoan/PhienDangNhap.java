package nttu.edu.vn.quanlychitieu.taikhoan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/** Ghi nhớ tài khoản đang đăng nhập bằng SharedPreferences. */
public class PhienDangNhap {

    private static final String TEN_FILE = "phien_dang_nhap";
    private static final String KEY_ID = "user_id";
    private static final String KEY_TEN = "ten_dang_nhap";
    private static final String KEY_HO_TEN = "ho_ten";

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(TEN_FILE, Context.MODE_PRIVATE);
    }

    public static void dangNhap(Context c, NguoiDung u) {
        sp(c).edit()
                .putInt(KEY_ID, u.getId())
                .putString(KEY_TEN, u.getTenDangNhap())
                .putString(KEY_HO_TEN, u.getHoTen())
                .apply();
    }

    /** Trả về -1 nếu chưa đăng nhập. */
    public static int getUserId(Context c) {
        return sp(c).getInt(KEY_ID, -1);
    }

    public static boolean daDangNhap(Context c) {
        return getUserId(c) != -1;
    }

    public static String getTenDangNhap(Context c) {
        return sp(c).getString(KEY_TEN, "");
    }

    public static String getHoTen(Context c) {
        return sp(c).getString(KEY_HO_TEN, "");
    }

    public static void capNhatHoTen(Context c, String hoTen) {
        sp(c).edit().putString(KEY_HO_TEN, hoTen).apply();
    }

    /** Đăng xuất, quay về màn hình đăng nhập và đóng mọi màn hình đang mở. */
    public static void dangXuat(Activity a) {
        sp(a).edit().clear().apply();
        Intent i = new Intent(a, DangNhapActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        a.startActivity(i);
        a.finish();
    }
}
