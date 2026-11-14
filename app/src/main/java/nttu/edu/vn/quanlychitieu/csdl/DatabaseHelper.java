package nttu.edu.vn.quanlychitieu.csdl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nttu.edu.vn.quanlychitieu.R;
import nttu.edu.vn.quanlychitieu.giaodich.DanhMuc;
import nttu.edu.vn.quanlychitieu.giaodich.GiaoDich;
import nttu.edu.vn.quanlychitieu.hu.ChuyenTien;
import nttu.edu.vn.quanlychitieu.hu.Hu;
import nttu.edu.vn.quanlychitieu.muctieu.MucTieu;
import nttu.edu.vn.quanlychitieu.taikhoan.MatKhauUtil;
import nttu.edu.vn.quanlychitieu.taikhoan.NguoiDung;
import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/**
 * Quản lý CSDL SQLite (Chương 7).
 * Mọi bảng dữ liệu đều có cột user_id để dữ liệu của các tài khoản tách biệt nhau.
 *
 * Số dư của Ví chính / mỗi hũ KHÔNG lưu cố định mà được tính từ lịch sử:
 *   số dư = thu vào hũ - chi từ hũ + tiền nạp/chuyển vào - tiền chuyển ra
 * Cách này tránh sai lệch khi sửa hoặc xóa giao dịch.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TEN_DB = "chitieu.db";
    private static final int PHIEN_BAN = 2;

    private static final String SELECT_GIAO_DICH =
            "SELECT g.id, g.so_tien, g.loai, g.danh_muc_id, d.ten, g.ghi_chu, g.ngay, g.hu_id, h.ten "
                    + "FROM giao_dich g JOIN danh_muc d ON g.danh_muc_id = d.id "
                    + "LEFT JOIN hu h ON g.hu_id = h.id ";

    private static final String[] DANH_MUC_CHI = {"Ăn uống", "Đi lại", "Nhà ở", "Hóa đơn", "Mua sắm",
            "Giải trí", "Sức khỏe", "Học tập", "Khác"};
    private static final String[] DANH_MUC_THU = {"Lương", "Thưởng", "Tiền được cho", "Thu nhập khác"};

    // 6 chiếc hũ mặc định và tỷ lệ phân bổ gợi ý
    private static final String[] TEN_HU_MAU = {"Thiết yếu", "Tự do tài chính", "Giáo dục",
            "Tiết kiệm dài hạn", "Hưởng thụ", "Cho đi"};
    private static final int[] TY_LE_HU_MAU = {55, 10, 10, 10, 10, 5};

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, TEN_DB, null, PHIEN_BAN);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE nguoi_dung ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "ten_dang_nhap TEXT NOT NULL UNIQUE, "
                + "ho_ten TEXT NOT NULL, "
                + "muoi TEXT NOT NULL, "
                + "mat_khau TEXT NOT NULL, "
                + "ngay_tao TEXT NOT NULL)");

        db.execSQL("CREATE TABLE danh_muc ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "ten TEXT NOT NULL, "
                + "loai INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE hu ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "ten TEXT NOT NULL, "
                + "ty_le INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE giao_dich ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "so_tien INTEGER NOT NULL, "
                + "loai INTEGER NOT NULL, "
                + "danh_muc_id INTEGER NOT NULL, "
                + "hu_id INTEGER NOT NULL DEFAULT 0, "
                + "ghi_chu TEXT, "
                + "ngay TEXT NOT NULL)");

        db.execSQL("CREATE TABLE chuyen_tien ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "loai INTEGER NOT NULL, "
                + "tu_hu INTEGER NOT NULL, "
                + "den_hu INTEGER NOT NULL, "
                + "so_tien INTEGER NOT NULL, "
                + "nguon TEXT, "
                + "ghi_chu TEXT, "
                + "ngay TEXT NOT NULL)");

        db.execSQL("CREATE TABLE muc_tieu ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "ten TEXT NOT NULL, "
                + "so_tien_muc_tieu INTEGER NOT NULL, "
                + "da_tiet_kiem INTEGER NOT NULL DEFAULT 0, "
                + "han TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int cu, int moi) {
        db.execSQL("DROP TABLE IF EXISTS muc_tieu");
        db.execSQL("DROP TABLE IF EXISTS chuyen_tien");
        db.execSQL("DROP TABLE IF EXISTS giao_dich");
        db.execSQL("DROP TABLE IF EXISTS hu");
        db.execSQL("DROP TABLE IF EXISTS danh_muc");
        db.execSQL("DROP TABLE IF EXISTS nguoi_dung");
        onCreate(db);
    }

    /** Danh mục và 6 hũ mặc định cho tài khoản mới. */
    private void taoDuLieuMau(SQLiteDatabase db, int uid) {
        for (String ten : DANH_MUC_CHI) themDanhMucMau(db, uid, ten, GiaoDich.CHI);
        for (String ten : DANH_MUC_THU) themDanhMucMau(db, uid, ten, GiaoDich.THU);
        for (int i = 0; i < TEN_HU_MAU.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put("user_id", uid);
            cv.put("ten", TEN_HU_MAU[i]);
            cv.put("ty_le", TY_LE_HU_MAU[i]);
            db.insert("hu", null, cv);
        }
    }

    private void themDanhMucMau(SQLiteDatabase db, int uid, String ten, int loai) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", uid);
        cv.put("ten", ten);
        cv.put("loai", loai);
        db.insert("danh_muc", null, cv);
    }

    private static String s(int n) {
        return String.valueOf(n);
    }

    // ------------------------------------------------------------------
    // TÀI KHOẢN
    // ------------------------------------------------------------------
    private static String chuanHoa(String ten) {
        return ten.trim().toLowerCase(Locale.ROOT);
    }

    public boolean tonTaiTenDangNhap(String ten) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT 1 FROM nguoi_dung WHERE ten_dang_nhap = ?", new String[]{chuanHoa(ten)});
        boolean co = c.moveToFirst();
        c.close();
        return co;
    }

    /** Đăng ký tài khoản mới. Trả về id tài khoản, hoặc -1 nếu tên đăng nhập đã tồn tại. */
    public long dangKy(String tenDangNhap, String hoTen, String matKhau) {
        if (tonTaiTenDangNhap(tenDangNhap)) return -1;

        String muoi = MatKhauUtil.taoMuoi();
        ContentValues cv = new ContentValues();
        cv.put("ten_dang_nhap", chuanHoa(tenDangNhap));
        cv.put("ho_ten", hoTen.trim());
        cv.put("muoi", muoi);
        cv.put("mat_khau", MatKhauUtil.bam(matKhau, muoi));
        cv.put("ngay_tao", TienIch.homNay());

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long id = db.insert("nguoi_dung", null, cv);
            if (id == -1) return -1;
            taoDuLieuMau(db, (int) id);
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    /** Trả về tài khoản nếu đúng tên đăng nhập và mật khẩu, ngược lại trả về null. */
    public NguoiDung dangNhap(String tenDangNhap, String matKhau) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, ten_dang_nhap, ho_ten, ngay_tao, muoi, mat_khau "
                        + "FROM nguoi_dung WHERE ten_dang_nhap = ?",
                new String[]{chuanHoa(tenDangNhap)});
        NguoiDung u = null;
        if (c.moveToFirst() && MatKhauUtil.kiemTra(matKhau, c.getString(4), c.getString(5))) {
            u = new NguoiDung(c.getInt(0), c.getString(1), c.getString(2), c.getString(3));
        }
        c.close();
        return u;
    }

    public NguoiDung layNguoiDung(int uid) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, ten_dang_nhap, ho_ten, ngay_tao FROM nguoi_dung WHERE id = ?",
                new String[]{s(uid)});
        NguoiDung u = null;
        if (c.moveToFirst()) {
            u = new NguoiDung(c.getInt(0), c.getString(1), c.getString(2), c.getString(3));
        }
        c.close();
        return u;
    }

    public boolean kiemTraMatKhau(int uid, String matKhau) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT muoi, mat_khau FROM nguoi_dung WHERE id = ?", new String[]{s(uid)});
        boolean dung = c.moveToFirst() && MatKhauUtil.kiemTra(matKhau, c.getString(0), c.getString(1));
        c.close();
        return dung;
    }

    public void doiMatKhau(int uid, String matKhauMoi) {
        String muoi = MatKhauUtil.taoMuoi();
        ContentValues cv = new ContentValues();
        cv.put("muoi", muoi);
        cv.put("mat_khau", MatKhauUtil.bam(matKhauMoi, muoi));
        getWritableDatabase().update("nguoi_dung", cv, "id = ?", new String[]{s(uid)});
    }

    public void capNhatHoTen(int uid, String hoTen) {
        ContentValues cv = new ContentValues();
        cv.put("ho_ten", hoTen.trim());
        getWritableDatabase().update("nguoi_dung", cv, "id = ?", new String[]{s(uid)});
    }

    /** Xóa tài khoản cùng toàn bộ dữ liệu của tài khoản đó. */
    public void xoaTaiKhoan(int uid) {
        SQLiteDatabase db = getWritableDatabase();
        String[] a = {s(uid)};
        db.beginTransaction();
        try {
            for (String bang : new String[]{"chuyen_tien", "giao_dich", "muc_tieu", "hu", "danh_muc"}) {
                db.delete(bang, "user_id = ?", a);
            }
            db.delete("nguoi_dung", "id = ?", a);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ------------------------------------------------------------------
    // DANH MỤC
    // ------------------------------------------------------------------
    public List<DanhMuc> layDanhMuc(int uid, int loai) {
        List<DanhMuc> ds = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, ten, loai FROM danh_muc WHERE user_id = ? AND loai = ? ORDER BY id",
                new String[]{s(uid), s(loai)});
        while (c.moveToNext()) {
            ds.add(new DanhMuc(c.getInt(0), c.getString(1), c.getInt(2)));
        }
        c.close();
        return ds;
    }

    /** Kiểm tra trùng tên danh mục (cùng loại). boQuaId dùng khi đổi tên, truyền -1 nếu thêm mới. */
    public boolean tonTaiDanhMuc(int uid, int loai, String ten, int boQuaId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT 1 FROM danh_muc WHERE user_id = ? AND loai = ? AND ten = ? COLLATE NOCASE AND id <> ?",
                new String[]{s(uid), s(loai), ten.trim(), s(boQuaId)});
        boolean co = c.moveToFirst();
        c.close();
        return co;
    }

    public long themDanhMuc(int uid, String ten, int loai) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", uid);
        cv.put("ten", ten.trim());
        cv.put("loai", loai);
        return getWritableDatabase().insert("danh_muc", null, cv);
    }

    public int doiTenDanhMuc(int uid, int id, String tenMoi) {
        ContentValues cv = new ContentValues();
        cv.put("ten", tenMoi.trim());
        return getWritableDatabase().update("danh_muc", cv,
                "id = ? AND user_id = ?", new String[]{s(id), s(uid)});
    }

    public int demGiaoDichTheoDanhMuc(int uid, int id) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM giao_dich WHERE user_id = ? AND danh_muc_id = ?",
                new String[]{s(uid), s(id)});
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }

    public int xoaDanhMuc(int uid, int id) {
        return getWritableDatabase().delete("danh_muc",
                "id = ? AND user_id = ?", new String[]{s(id), s(uid)});
    }

    // ------------------------------------------------------------------
    // GIAO DỊCH THU - CHI
    // ------------------------------------------------------------------
    private ContentValues taoValues(int uid, GiaoDich g) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", uid);
        cv.put("so_tien", g.getSoTien());
        cv.put("loai", g.getLoai());
        cv.put("danh_muc_id", g.getDanhMucId());
        cv.put("hu_id", g.getHuId());
        cv.put("ghi_chu", g.getGhiChu());
        cv.put("ngay", g.getNgay());
        return cv;
    }

    private GiaoDich docGiaoDich(Cursor c) {
        GiaoDich g = new GiaoDich();
        g.setId(c.getInt(0));
        g.setSoTien(c.getLong(1));
        g.setLoai(c.getInt(2));
        g.setDanhMucId(c.getInt(3));
        g.setTenDanhMuc(c.getString(4));
        g.setGhiChu(c.getString(5));
        g.setNgay(c.getString(6));
        g.setHuId(c.getInt(7));
        g.setTenHu(c.getString(8));
        return g;
    }

    public long themGiaoDich(int uid, GiaoDich g) {
        return getWritableDatabase().insert("giao_dich", null, taoValues(uid, g));
    }

    public int suaGiaoDich(int uid, GiaoDich g) {
        return getWritableDatabase().update("giao_dich", taoValues(uid, g),
                "id = ? AND user_id = ?", new String[]{s(g.getId()), s(uid)});
    }

    public int xoaGiaoDich(int uid, int id) {
        return getWritableDatabase().delete("giao_dich",
                "id = ? AND user_id = ?", new String[]{s(id), s(uid)});
    }

    public GiaoDich layGiaoDichTheoId(int uid, int id) {
        Cursor c = getReadableDatabase().rawQuery(
                SELECT_GIAO_DICH + "WHERE g.user_id = ? AND g.id = ?", new String[]{s(uid), s(id)});
        GiaoDich g = null;
        if (c.moveToFirst()) g = docGiaoDich(c);
        c.close();
        return g;
    }

    /** Lấy giao dịch của một tháng. thang có dạng "yyyy-MM", ví dụ "2026-11". */
    public List<GiaoDich> layGiaoDichTheoThang(int uid, String thang) {
        List<GiaoDich> ds = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                SELECT_GIAO_DICH + "WHERE g.user_id = ? AND g.ngay LIKE ? ORDER BY g.ngay DESC, g.id DESC",
                new String[]{s(uid), thang + "%"});
        while (c.moveToNext()) {
            ds.add(docGiaoDich(c));
        }
        c.close();
        return ds;
    }

    /** Toàn bộ giao dịch (dùng để xuất dữ liệu), cũ nhất trước. */
    public List<GiaoDich> layTatCaGiaoDich(int uid) {
        List<GiaoDich> ds = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                SELECT_GIAO_DICH + "WHERE g.user_id = ? ORDER BY g.ngay ASC, g.id ASC",
                new String[]{s(uid)});
        while (c.moveToNext()) {
            ds.add(docGiaoDich(c));
        }
        c.close();
        return ds;
    }

    /** Tổng thu hoặc tổng chi của một tháng. */
    public long tongTheoThang(int uid, String thang, int loai) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(so_tien), 0) FROM giao_dich "
                        + "WHERE user_id = ? AND ngay LIKE ? AND loai = ?",
                new String[]{s(uid), thang + "%", s(loai)});
        long tong = 0;
        if (c.moveToFirst()) tong = c.getLong(0);
        c.close();
        return tong;
    }

    /** Tổng chi theo từng danh mục trong tháng, giảm dần (dùng cho biểu đồ tròn). */
    public Map<String, Long> chiTheoDanhMuc(int uid, String thang) {
        Map<String, Long> kq = new LinkedHashMap<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT d.ten, SUM(g.so_tien) AS tong "
                        + "FROM giao_dich g JOIN danh_muc d ON g.danh_muc_id = d.id "
                        + "WHERE g.user_id = ? AND g.loai = 0 AND g.ngay LIKE ? "
                        + "GROUP BY d.id ORDER BY tong DESC",
                new String[]{s(uid), thang + "%"});
        while (c.moveToNext()) {
            kq.put(c.getString(0), c.getLong(1));
        }
        c.close();
        return kq;
    }

    // ------------------------------------------------------------------
    // VÍ CHÍNH VÀ HŨ
    // ------------------------------------------------------------------

    /** Số dư của Ví chính (huId = 0) hoặc của một hũ. */
    public long tinhSoDu(int uid, int huId) {
        String u = s(uid);
        String h = s(huId);
        // loai = 1 là THU, loai = 0 là CHI
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT "
                        + "(SELECT COALESCE(SUM(so_tien),0) FROM giao_dich WHERE user_id=? AND hu_id=? AND loai=1) "
                        + "- (SELECT COALESCE(SUM(so_tien),0) FROM giao_dich WHERE user_id=? AND hu_id=? AND loai=0) "
                        + "+ (SELECT COALESCE(SUM(so_tien),0) FROM chuyen_tien WHERE user_id=? AND den_hu=?) "
                        + "- (SELECT COALESCE(SUM(so_tien),0) FROM chuyen_tien WHERE user_id=? AND tu_hu=?)",
                new String[]{u, h, u, h, u, h, u, h});
        long kq = 0;
        if (c.moveToFirst()) kq = c.getLong(0);
        c.close();
        return kq;
    }

    /** Danh sách hũ kèm số dư. kemViChinh = true thì Ví chính nằm ở đầu danh sách. */
    public List<Hu> layDanhSachHu(int uid, boolean kemViChinh) {
        List<Hu> ds = new ArrayList<>();
        if (kemViChinh) {
            ds.add(new Hu(Hu.VI_CHINH, context.getString(R.string.vi_chinh), 0,
                    tinhSoDu(uid, Hu.VI_CHINH)));
        }
        List<Hu> hu = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, ten, ty_le FROM hu WHERE user_id = ? ORDER BY id", new String[]{s(uid)});
        while (c.moveToNext()) {
            hu.add(new Hu(c.getInt(0), c.getString(1), c.getInt(2), 0));
        }
        c.close();
        for (Hu h : hu) {
            h.setSoDu(tinhSoDu(uid, h.getId()));
            ds.add(h);
        }
        return ds;
    }

    public boolean tonTaiHu(int uid, String ten, int boQuaId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT 1 FROM hu WHERE user_id = ? AND ten = ? COLLATE NOCASE AND id <> ?",
                new String[]{s(uid), ten.trim(), s(boQuaId)});
        boolean co = c.moveToFirst();
        c.close();
        return co;
    }

    public long themHu(int uid, String ten, int tyLe) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", uid);
        cv.put("ten", ten.trim());
        cv.put("ty_le", tyLe);
        return getWritableDatabase().insert("hu", null, cv);
    }

    public int suaHu(int uid, int id, String ten, int tyLe) {
        ContentValues cv = new ContentValues();
        cv.put("ten", ten.trim());
        cv.put("ty_le", tyLe);
        return getWritableDatabase().update("hu", cv, "id = ? AND user_id = ?", new String[]{s(id), s(uid)});
    }

    /** Hũ đã có giao dịch hoặc lần nạp/chuyển tiền nào chưa (nếu có thì không cho xóa). */
    public boolean huCoGiaoDich(int uid, int id) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT (SELECT COUNT(*) FROM giao_dich WHERE user_id = ? AND hu_id = ?) "
                        + "+ (SELECT COUNT(*) FROM chuyen_tien WHERE user_id = ? AND (tu_hu = ? OR den_hu = ?))",
                new String[]{s(uid), s(id), s(uid), s(id), s(id)});
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n > 0;
    }

    public int xoaHu(int uid, int id) {
        return getWritableDatabase().delete("hu", "id = ? AND user_id = ?", new String[]{s(id), s(uid)});
    }

    /** Tổng tỷ lệ % của tất cả các hũ (nên bằng 100). */
    public int tongTyLe(int uid) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(ty_le), 0) FROM hu WHERE user_id = ?", new String[]{s(uid)});
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }

    // ------------------------------------------------------------------
    // NẠP TIỀN / CHUYỂN TIỀN
    // ------------------------------------------------------------------
    private ContentValues taoValuesChuyenTien(int uid, ChuyenTien ct) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", uid);
        cv.put("loai", ct.getLoai());
        cv.put("tu_hu", ct.getTuHu());
        cv.put("den_hu", ct.getDenHu());
        cv.put("so_tien", ct.getSoTien());
        cv.put("nguon", ct.getNguon());
        cv.put("ghi_chu", ct.getGhiChu());
        cv.put("ngay", ct.getNgay());
        return cv;
    }

    public long themChuyenTien(int uid, ChuyenTien ct) {
        return getWritableDatabase().insert("chuyen_tien", null, taoValuesChuyenTien(uid, ct));
    }

    public List<ChuyenTien> layLichSuChuyenTien(int uid) {
        List<ChuyenTien> ds = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, loai, tu_hu, den_hu, so_tien, nguon, ghi_chu, ngay FROM chuyen_tien "
                        + "WHERE user_id = ? ORDER BY ngay DESC, id DESC",
                new String[]{s(uid)});
        while (c.moveToNext()) {
            ChuyenTien ct = new ChuyenTien();
            ct.setId(c.getInt(0));
            ct.setLoai(c.getInt(1));
            ct.setTuHu(c.getInt(2));
            ct.setDenHu(c.getInt(3));
            ct.setSoTien(c.getLong(4));
            ct.setNguon(c.getString(5));
            ct.setGhiChu(c.getString(6));
            ct.setNgay(c.getString(7));
            ds.add(ct);
        }
        c.close();
        return ds;
    }

    /**
     * Chia soTien từ Ví chính vào các hũ theo tỷ lệ. Hũ cuối nhận phần còn lại để tổng khớp tuyệt đối.
     * Trả về false nếu không có hũ nào có tỷ lệ > 0.
     */
    public boolean phanBoTheoTyLe(int uid, long soTien, String ngay, String ghiChu) {
        List<Hu> hu = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, ten, ty_le FROM hu WHERE user_id = ? AND ty_le > 0 ORDER BY id",
                new String[]{s(uid)});
        while (c.moveToNext()) {
            hu.add(new Hu(c.getInt(0), c.getString(1), c.getInt(2), 0));
        }
        c.close();
        if (hu.isEmpty() || soTien <= 0) return false;

        long tong = 0;
        for (Hu h : hu) tong += h.getTyLe();

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long daChia = 0;
            for (int i = 0; i < hu.size(); i++) {
                Hu h = hu.get(i);
                long phan = (i == hu.size() - 1) ? soTien - daChia : soTien * h.getTyLe() / tong;
                daChia += phan;
                if (phan <= 0) continue;

                ChuyenTien ct = new ChuyenTien();
                ct.setLoai(ChuyenTien.CHUYEN);
                ct.setTuHu(Hu.VI_CHINH);
                ct.setDenHu(h.getId());
                ct.setSoTien(phan);
                ct.setNguon("");
                ct.setGhiChu(ghiChu);
                ct.setNgay(ngay);
                db.insert("chuyen_tien", null, taoValuesChuyenTien(uid, ct));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return true;
    }

    // ------------------------------------------------------------------
    // MỤC TIÊU TIẾT KIỆM
    // ------------------------------------------------------------------
    public long themMucTieu(int uid, MucTieu m) {
        ContentValues cv = new ContentValues();
        cv.put("user_id", uid);
        cv.put("ten", m.getTen());
        cv.put("so_tien_muc_tieu", m.getSoTienMucTieu());
        cv.put("da_tiet_kiem", m.getDaTietKiem());
        cv.put("han", m.getHan());
        return getWritableDatabase().insert("muc_tieu", null, cv);
    }

    public List<MucTieu> layTatCaMucTieu(int uid) {
        List<MucTieu> ds = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, ten, so_tien_muc_tieu, da_tiet_kiem, han FROM muc_tieu "
                        + "WHERE user_id = ? ORDER BY han ASC",
                new String[]{s(uid)});
        while (c.moveToNext()) {
            MucTieu m = new MucTieu(c.getString(1), c.getLong(2), c.getLong(3), c.getString(4));
            m.setId(c.getInt(0));
            ds.add(m);
        }
        c.close();
        return ds;
    }

    /** Nạp thêm tiền vào mục tiêu: da_tiet_kiem = da_tiet_kiem + soTien. */
    public void napTienMucTieu(int uid, int id, long soTien) {
        getWritableDatabase().execSQL(
                "UPDATE muc_tieu SET da_tiet_kiem = da_tiet_kiem + ? WHERE id = ? AND user_id = ?",
                new Object[]{soTien, id, uid});
    }

    public int xoaMucTieu(int uid, int id) {
        return getWritableDatabase().delete("muc_tieu",
                "id = ? AND user_id = ?", new String[]{s(id), s(uid)});
    }
}
