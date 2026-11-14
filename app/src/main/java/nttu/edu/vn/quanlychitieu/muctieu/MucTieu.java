package nttu.edu.vn.quanlychitieu.muctieu;

/** Mục tiêu tiết kiệm (ví dụ: mua laptop 15.000.000 đ trước 31/12). */
public class MucTieu {
    private int id;
    private String ten;
    private long soTienMucTieu;
    private long daTietKiem;
    private String han; // yyyy-MM-dd

    public MucTieu() {
    }

    public MucTieu(String ten, long soTienMucTieu, long daTietKiem, String han) {
        this.ten = ten;
        this.soTienMucTieu = soTienMucTieu;
        this.daTietKiem = daTietKiem;
        this.han = han;
    }

    /** Phần trăm hoàn thành (0 - 100). */
    public int getPhanTram() {
        if (soTienMucTieu <= 0) return 0;
        return (int) Math.min(100, daTietKiem * 100 / soTienMucTieu);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public long getSoTienMucTieu() { return soTienMucTieu; }
    public void setSoTienMucTieu(long soTienMucTieu) { this.soTienMucTieu = soTienMucTieu; }
    public long getDaTietKiem() { return daTietKiem; }
    public void setDaTietKiem(long daTietKiem) { this.daTietKiem = daTietKiem; }
    public String getHan() { return han; }
    public void setHan(String han) { this.han = han; }
}
