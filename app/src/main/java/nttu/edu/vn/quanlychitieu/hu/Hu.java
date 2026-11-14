package nttu.edu.vn.quanlychitieu.hu;

import nttu.edu.vn.quanlychitieu.tienich.TienIch;

/** Hũ chi tiêu. Hũ có id = 0 là "Ví chính" (tiền chưa phân bổ vào hũ nào). */
public class Hu {
    public static final int VI_CHINH = 0;

    private int id;
    private String ten;
    private int tyLe;     // % phân bổ gợi ý (0 - 100)
    private long soDu;    // được tính từ các giao dịch, không lưu trong bảng hu

    public Hu(int id, String ten, int tyLe, long soDu) {
        this.id = id;
        this.ten = ten;
        this.tyLe = tyLe;
        this.soDu = soDu;
    }

    public int getId() { return id; }
    public String getTen() { return ten; }
    public int getTyLe() { return tyLe; }
    public long getSoDu() { return soDu; }
    public void setSoDu(long soDu) { this.soDu = soDu; }

    /** Spinner hiển thị: "Thiết yếu (1.200.000 đ)". */
    @Override
    public String toString() {
        return ten + " (" + TienIch.dinhDangTien(soDu) + ")";
    }
}
