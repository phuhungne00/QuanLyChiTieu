package nttu.edu.vn.quanlychitieu.giaodich;

/** Danh mục thu / chi (Ăn uống, Đi lại, Lương...). */
public class DanhMuc {
    private int id;
    private String ten;
    private int loai; // GiaoDich.CHI hoặc GiaoDich.THU

    public DanhMuc(int id, String ten, int loai) {
        this.id = id;
        this.ten = ten;
        this.loai = loai;
    }

    public int getId() { return id; }
    public String getTen() { return ten; }
    public int getLoai() { return loai; }

    /** Spinner dùng toString() để hiển thị tên danh mục. */
    @Override
    public String toString() {
        return ten;
    }
}
