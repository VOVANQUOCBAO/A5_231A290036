package vn.edu.vhu.ltdd.a5intent;
 
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
 
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
 
public class MainActivity extends AppCompatActivity {
 
    // TODO: thay 231A290036 b ng MSSV c a b n
    private static final String TAG = "A5_231A290036";
 
    /** Khóa dùng chung cho hai màn hình — luôn khai báo h ng s , không gõ chu i hai l n. */
    public static final String EXTRA_CONTACT = "extra_contact";
    public static final String EXTRA_NGUOI_GUI = "extra_nguoi_gui";
 
    private EditText edtHoTen, edtDienThoai, edtEmail;
    private TextView tvKetQuaTraVe;
 
    /** B  nh n k t qu  tr  v  t  DetailActivity (thay cho onActivityResult đã l i th i). */
    private final ActivityResultLauncher<Intent> chiTietLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Contact daSua = IntentCompat.getParcelableExtra(
                            result.getData(), EXTRA_CONTACT, Contact.class);
                    if (daSua != null) {
                        edtHoTen.setText(daSua.getHoTen());
                        tvKetQuaTraVe.setText(getString(R.string.returned, daSua.getHoTen()));
                        Log.d(TAG, "Nh n k t qu  tr  v : " + daSua.getHoTen());
                    }
                } else {
                    tvKetQuaTraVe.setText(R.string.returned_cancel);
                }
            });
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
 
        edtHoTen = findViewById(R.id.edtHoTen);
        edtDienThoai = findViewById(R.id.edtDienThoai);
        edtEmail = findViewById(R.id.edtEmail);
        tvKetQuaTraVe = findViewById(R.id.tvKetQuaTraVe);
 
        Button btnChiTiet = findViewById(R.id.btnChiTiet);
        Button btnGoi = findViewById(R.id.btnGoi);
        Button btnWeb = findViewById(R.id.btnWeb);
        Button btnChiaSe = findViewById(R.id.btnChiaSe);
 
        btnChiTiet.setOnClickListener(v -> moManHinhChiTiet());
        btnGoi.setOnClickListener(v -> goiDien());
        btnWeb.setOnClickListener(v -> moTrangWeb());
        btnChiaSe.setOnClickListener(v -> chiaSe());
    }
 
    // ============ INTENT T NG MINH (explicit) ============
 
    private void moManHinhChiTiet() {
        String hoTen = edtHoTen.getText().toString().trim();
        if (hoTen.isEmpty()) {
            edtHoTen.setError(getString(R.string.err_empty));
            return;
        }
        Contact contact = new Contact(hoTen,
                edtDienThoai.getText().toString().trim(),
                edtEmail.getText().toString().trim());
 
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(EXTRA_CONTACT, contact);          // đóng gói c  đ i t ng
        intent.putExtra(EXTRA_NGUOI_GUI, TAG);            // d  li u đ n gi n kèm theo
        chiTietLauncher.launch(intent);                   // m  và CH  k t qu  tr  v
    }
 
    // ============ INTENT NG M Đ NH (implicit) ============
 
    private void goiDien() {
        String sdt = edtDienThoai.getText().toString().trim();
        if (sdt.isEmpty()) {
            edtDienThoai.setError(getString(R.string.err_empty));
            return;
        }
        // ACTION_DIAL ch  m  màn hình g i v i s  đã đi n s n → KHÔNG c n xin quy n
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + sdt));
        moAnToan(intent);
    }
 
    private void moTrangWeb() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.school_url)));
        moAnToan(intent);
    }
 
    private void chiaSe() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text,
                edtHoTen.getText().toString(), edtDienThoai.getText().toString()));
        // B c trong b  ch n đ  ng i dùng t  ch n ng d ng
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
    }
 
    /**
     * T  Android 11, resolveActivity() th ng tr  v  null do c  ch  gi i h n hi n th  gói
     * (package visibility). Cách an toàn nh t là b t ngo i l  ActivityNotFoundException.
     */
    private void moAnToan(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.err_no_app, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Không có ng d ng nào x  lý: " + intent.getAction(), e);
        }
    }
}