package nttu.edu.vn.quanlychitieu.taikhoan;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Băm mật khẩu bằng PBKDF2 + salt ngẫu nhiên.
 * Mật khẩu gốc không bao giờ được lưu, chỉ lưu salt và chuỗi băm.
 */
public class MatKhauUtil {

    private static final int SO_LAN_LAP = 10000;
    private static final int DO_DAI_KHOA = 256; // bit

    /** Tạo salt ngẫu nhiên 16 byte, mã hóa Base64. */
    public static String taoMuoi() {
        byte[] muoi = new byte[16];
        new SecureRandom().nextBytes(muoi);
        return Base64.encodeToString(muoi, Base64.NO_WRAP);
    }

    public static String bam(String matKhau, String muoi) {
        try {
            byte[] m = Base64.decode(muoi, Base64.NO_WRAP);
            PBEKeySpec spec = new PBEKeySpec(matKhau.toCharArray(), m, SO_LAN_LAP, DO_DAI_KHOA);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return Base64.encodeToString(f.generateSecret(spec).getEncoded(), Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    /** So sánh mật khẩu người dùng nhập với chuỗi băm đã lưu. */
    public static boolean kiemTra(String matKhau, String muoi, String bamDaLuu) {
        return MessageDigest.isEqual(
                bam(matKhau, muoi).getBytes(StandardCharsets.UTF_8),
                bamDaLuu.getBytes(StandardCharsets.UTF_8));
    }
}
