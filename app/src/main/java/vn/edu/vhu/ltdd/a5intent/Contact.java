package vn.edu.vhu.ltdd.a5intent;
 
import android.os.Parcel;
import android.os.Parcelable;
 
/**
 * Đ i t ng đ c đóng gói đ  truy n qua Intent.
 * Parcelable nhanh h n Serializable vì Android t  sinh mã đ c/ghi thay cho c  ch  ph n chi u.
 * M o: gõ "implements Parcelable" r i nh n Alt+Enter, Android Studio sinh s n ph n khung.
 */
public class Contact implements Parcelable {
 
    private String hoTen;
    private String dienThoai;
    private String email;
 
    public Contact(String hoTen, String dienThoai, String email) {
        this.hoTen = hoTen;
        this.dienThoai = dienThoai;
        this.email = email;
    }
 
    /** Đ c l i d  li u ĐÚNG TH  T  đã ghi trong writeToParcel. */
    protected Contact(Parcel in) {
        hoTen = in.readString();
        dienThoai = in.readString();
        email = in.readString();
    }
 
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hoTen);
        dest.writeString(dienThoai);
        dest.writeString(email);
    }
 
    @Override
    public int describeContents() {
        return 0;
    }
 
    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }
 
        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
 
    public String getHoTen() { return hoTen; }
    public String getDienThoai() { return dienThoai; }
    public String getEmail() { return email; }
 
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
}