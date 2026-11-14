package nttu.edu.vn.quanlychitieu.tienich;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.taikhoan.PhienDangNhap;

/** Các hàm tiện ích dùng chung: định dạng tiền, xử lý ngày tháng. */
public class TienIch {

    private static final Locale VN = Locale.forLanguageTag("vi-VN");

    /** 1500000 -> "1.500.000 đ" */
    public static String dinhDangTien(long soTien) {
        return NumberFormat.getInstance(VN).format(soTien) + " đ";
    }

    /** Ngày hôm nay dạng yyyy-MM-dd. */
    public static String homNay() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    /** Calendar -> "yyyy-MM" (khóa tháng dùng để truy vấn CSDL). */
    public static String thangKey(Calendar cal) {
        return String.format(Locale.US, "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
    }

    public static String thangHienTai() {
        return thangKey(Calendar.getInstance());
    }

    /** "yyyy-MM" -> Calendar (ngày mùng 1 của tháng đó). */
    public static Calendar calTuKey(String key) {
        String[] p = key.split("-");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1); // đặt ngày trước để tránh tràn tháng
        c.set(Calendar.YEAR, Integer.parseInt(p[0]));
        c.set(Calendar.MONTH, Integer.parseInt(p[1]) - 1);
        return c;
    }

    /** Calendar -> "11/2026". */
    public static String thangHienThi(Calendar cal) {
        return String.format(Locale.US, "%02d/%04d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
    }

    /** "2026-11-05" -> "05/11/2026". */
    public static String ngayHienThi(String iso) {
        String[] p = iso.split("-");
        if (p.length != 3) return iso;
        return p[2] + "/" + p[1] + "/" + p[0];
    }

    /** Số ngày còn lại tính từ hôm nay đến ngày iso (âm nếu đã quá hạn). */
    public static long soNgayConLai(String iso) {
        try {
            Date han = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso);
            Calendar hn = Calendar.getInstance();
            hn.set(Calendar.HOUR_OF_DAY, 0);
            hn.set(Calendar.MINUTE, 0);
            hn.set(Calendar.SECOND, 0);
            hn.set(Calendar.MILLISECOND, 0);
            long chenhLech = han.getTime() - hn.getTimeInMillis();
            return Math.round(chenhLech / 86400000.0);
        } catch (ParseException e) {
            return 0;
        }
    }

    /**
     * Từ Android 15 ứng dụng được vẽ tràn viền (edge-to-edge), nên cần chừa lề
     * cho thanh điều hướng phía dưới để các nút không bị che.
     */
    public static void apDungInsets(View goc) {
        final int trai = goc.getPaddingLeft();
        final int tren = goc.getPaddingTop();
        final int phai = goc.getPaddingRight();
        final int duoi = goc.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(goc, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(trai + bars.left, tren, phai + bars.right, duoi + bars.bottom);
            return windowInsets;
        });
    }

    /** Đọc số tiền từ chuỗi; trả về 0 nếu rỗng hoặc không hợp lệ. */
    public static long docSoTien(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Bọc một view (thường là EditText) vào khung có lề để đặt trong hộp thoại. */
    public static FrameLayout khungNhap(Context ctx, View v) {
        FrameLayout khung = new FrameLayout(ctx);
        int le = (int) (20 * ctx.getResources().getDisplayMetrics().density);
        khung.setPadding(le, le / 2, le, 0);
        khung.addView(v);
        return khung;
    }

    /** Hỏi xác nhận rồi đăng xuất. */
    public static void hoiDangXuat(final Activity a) {
        new AlertDialog.Builder(a)
                .setTitle(R.string.dang_xuat)
                .setMessage(R.string.hoi_dang_xuat)
                .setPositiveButton(R.string.dang_xuat, (d, w) -> PhienDangNhap.dangXuat(a))
                .setNegativeButton(R.string.huy, null)
                .show();
    }
}
