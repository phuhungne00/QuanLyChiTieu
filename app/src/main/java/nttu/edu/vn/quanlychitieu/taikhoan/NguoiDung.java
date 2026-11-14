package nttu.edu.vn.quanlychitieu.taikhoan;

/** Một tài khoản người dùng. */
public class NguoiDung {
    private final int id;
    private final String tenDangNhap;
    private final String hoTen;
    private final String ngayTao; // yyyy-MM-dd

    public NguoiDung(int id, String tenDangNhap, String hoTen, String ngayTao) {
        this.id = id;
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.ngayTao = ngayTao;
    }

    public int getId() { return id; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getHoTen() { return hoTen; }
    public String getNgayTao() { return ngayTao; }
}
