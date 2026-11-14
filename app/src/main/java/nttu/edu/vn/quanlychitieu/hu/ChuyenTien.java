package nttu.edu.vn.quanlychitieu.hu;

/** Một lần nạp tiền hoặc chuyển tiền giữa ví chính và các hũ. */
public class ChuyenTien {
    public static final int NAP = 0;      // tiền từ bên ngoài đưa vào
    public static final int CHUYEN = 1;   // chuyển nội bộ giữa ví chính và các hũ
    public static final int NGUON_NGOAI = -1; // giá trị tuHu khi nạp tiền từ bên ngoài

    private int id;
    private int loai;
    private int tuHu;
    private int denHu;
    private long soTien;
    private String nguon;   // chỉ dùng khi nạp: Tiền mặt, Ngân hàng...
    private String ghiChu;
    private String ngay;    // yyyy-MM-dd

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getLoai() { return loai; }
    public void setLoai(int loai) { this.loai = loai; }
    public int getTuHu() { return tuHu; }
    public void setTuHu(int tuHu) { this.tuHu = tuHu; }
    public int getDenHu() { return denHu; }
    public void setDenHu(int denHu) { this.denHu = denHu; }
    public long getSoTien() { return soTien; }
    public void setSoTien(long soTien) { this.soTien = soTien; }
    public String getNguon() { return nguon; }
    public void setNguon(String nguon) { this.nguon = nguon; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }
}
