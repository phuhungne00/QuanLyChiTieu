package nttu.edu.vn.quanlychitieu.giaodich;

/** Một giao dịch thu hoặc chi (ứng với 1 dòng trong bảng giao_dich). */
public class GiaoDich {
    public static final int CHI = 0;
    public static final int THU = 1;

    private int id;
    private long soTien;        // số tiền (VNĐ)
    private int loai;           // CHI hoặc THU
    private int danhMucId;
    private String tenDanhMuc;  // lấy từ bảng danh_muc khi JOIN
    private int huId;           // 0 = Ví chính, > 0 = một hũ
    private String tenHu;       // null nếu là Ví chính
    private String ghiChu;
    private String ngay;        // định dạng yyyy-MM-dd

    public GiaoDich() {
    }

    public GiaoDich(long soTien, int loai, int danhMucId, int huId, String ghiChu, String ngay) {
        this.soTien = soTien;
        this.loai = loai;
        this.danhMucId = danhMucId;
        this.huId = huId;
        this.ghiChu = ghiChu;
        this.ngay = ngay;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public long getSoTien() { return soTien; }
    public void setSoTien(long soTien) { this.soTien = soTien; }
    public int getLoai() { return loai; }
    public void setLoai(int loai) { this.loai = loai; }
    public int getDanhMucId() { return danhMucId; }
    public void setDanhMucId(int danhMucId) { this.danhMucId = danhMucId; }
    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }
    public int getHuId() { return huId; }
    public void setHuId(int huId) { this.huId = huId; }
    public String getTenHu() { return tenHu; }
    public void setTenHu(String tenHu) { this.tenHu = tenHu; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }
}
