package com.example.hethongbanthuoc.feature_profile.address;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hethongbanthuoc.feature_profile.models.Model_DiaChi;
import com.example.hethongbanthuoc.R;

import java.util.List;

public class Adapter_Address extends RecyclerView.Adapter<Adapter_Address.AddressViewHolder> {

    private final Context context;
    private List<Model_DiaChi> diaChiList;

    public Adapter_Address(Context context, List<Model_DiaChi> diaChiList) {
        this.context = context;
        this.diaChiList = diaChiList;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_andress, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Model_DiaChi diaChi = diaChiList.get(position);
        if (diaChi == null) return;

        // Hiển thị tên
        holder.tvName.setText(diaChi.getTenNguoiNhan() != null ? diaChi.getTenNguoiNhan() : "Không rõ");

        // Hiển thị số điện thoại với format +84
        String sdt = diaChi.getSoDienThoai() != null ? diaChi.getSoDienThoai() : "";
        if (sdt.startsWith("0")) {
            sdt = "+84" + sdt.substring(1);
        } else if (!sdt.startsWith("+84")) {
            sdt = "+84" + sdt;
        }
        holder.tvPhone.setText(sdt);

        //  DEBUG: In ra log để kiểm tra
        android.util.Log.d("AddressAdapter", "=== DEBUG ===");
        android.util.Log.d("AddressAdapter", "diaChiChiTiet: " + diaChi.getDiaChiChiTiet());
        android.util.Log.d("AddressAdapter", "phuongXa: " + diaChi.getPhuongXa());
        android.util.Log.d("AddressAdapter", "quanHuyen: " + diaChi.getQuanHuyen());
        android.util.Log.d("AddressAdapter", "tinhThanh: " + diaChi.getTinhThanh());

        //  HIỂN THỊ địa chỉ: Kiểm tra nếu diaChiChiTiet đã chứa đầy đủ thì chỉ hiển thị nó
        String fullAddress = buildFullAddressSmart(diaChi);
        holder.tvAddress.setText(fullAddress);

        // Hiển thị loại địa chỉ
        String loai = diaChi.getLoaiDiaChi() != null ? diaChi.getLoaiDiaChi() : "Khác";
        holder.tvAddressType.setText(loai);

        if ("Nhà".equalsIgnoreCase(loai)) {
            holder.ivAddressType.setImageResource(R.drawable.house);
        } else if ("Văn phòng".equalsIgnoreCase(loai)) {
            holder.ivAddressType.setImageResource(R.drawable.office);
        } else {
            holder.ivAddressType.setImageResource(R.drawable.location);
        }

        // Hiển thị tag mặc định
        holder.tvDefaultTag.setVisibility(
                Boolean.TRUE.equals(diaChi.getDiaChiMacDinh()) ? View.VISIBLE : View.GONE
        );

        // Xử lý click: single click → thông báo, double click → InfoAddressActivity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = SystemClock.elapsedRealtime();
                if (currentTime - lastClickTime < 400) {
                    // Double click
                    openInfoActivity(diaChi);
                } else {
                    // Single click
                    if (Boolean.TRUE.equals(diaChi.getDiaChiMacDinh())) {
                        Toast.makeText(context, "Đây đã là địa chỉ mặc định!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Nhấn nút chỉnh sửa để thay đổi địa chỉ mặc định.", Toast.LENGTH_SHORT).show();
                    }
                }
                lastClickTime = currentTime;
            }
        });

        //  Nút sửa → mở AddAndressActivity với dữ liệu TÁCH RIÊNG
        holder.btnEdit.setOnClickListener(v -> openEditActivity(diaChi));
    }

    // Xây dựng địa chỉ đầy đủ - tránh trùng lặp
    private String buildFullAddressSmart(Model_DiaChi diaChi) {
        String diaChiChiTiet = diaChi.getDiaChiChiTiet() != null ? diaChi.getDiaChiChiTiet() : "";
        String phuongXa = diaChi.getPhuongXa() != null ? diaChi.getPhuongXa() : "";
        String quanHuyen = diaChi.getQuanHuyen() != null ? diaChi.getQuanHuyen() : "";
        String tinhThanh = diaChi.getTinhThanh() != null ? diaChi.getTinhThanh() : "";

        // KIỂM TRA: Nếu diaChiChiTiet đã chứa phường/quận/tỉnh thì chỉ hiển thị nó
        if (diaChiChiTiet.contains(phuongXa) &&
                diaChiChiTiet.contains(quanHuyen) &&
                diaChiChiTiet.contains(tinhThanh)) {
            // Backend đã gộp sẵn rồi, chỉ hiển thị diaChiChiTiet
            return diaChiChiTiet;
        }

        // Nếu chưa gộp, thì gộp thủ công
        StringBuilder address = new StringBuilder();

        if (!diaChiChiTiet.isEmpty()) {
            address.append(diaChiChiTiet);
        }

        if (!phuongXa.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(phuongXa);
        }

        if (!quanHuyen.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(quanHuyen);
        }

        if (!tinhThanh.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(tinhThanh);
        }

        return address.toString();
    }

    // Xây dựng địa chỉ đầy đủ để HIỂN THỊ từ các trường riêng biệt
    private String buildFullAddress(Model_DiaChi diaChi) {
        StringBuilder address = new StringBuilder();

        // Thêm địa chỉ chi tiết (số nhà, ngõ)
        if (diaChi.getDiaChiChiTiet() != null && !diaChi.getDiaChiChiTiet().isEmpty()) {
            address.append(diaChi.getDiaChiChiTiet());
        }

        // Thêm phường/xã
        if (diaChi.getPhuongXa() != null && !diaChi.getPhuongXa().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(diaChi.getPhuongXa());
        }

        // Thêm quận/huyện
        if (diaChi.getQuanHuyen() != null && !diaChi.getQuanHuyen().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(diaChi.getQuanHuyen());
        }

        // Thêm tỉnh/thành
        if (diaChi.getTinhThanh() != null && !diaChi.getTinhThanh().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(diaChi.getTinhThanh());
        }

        return address.toString();
    }

    // Mở Activity sửa với dữ liệu TÁCH RIÊNG - XỬ LÝ KHI BACKEND ĐÃ GỘP
    private void openEditActivity(Model_DiaChi diaChi) {
        Intent intent = new Intent(context, AddAndressActivity.class);

        intent.putExtra("maDiaChi", diaChi.getMaDiaChi());
        intent.putExtra("loaiDiaChi", diaChi.getLoaiDiaChi());
        intent.putExtra("tenNguoiNhan", diaChi.getTenNguoiNhan());
        intent.putExtra("soDienThoai", diaChi.getSoDienThoai());

        String diaChiChiTiet = diaChi.getDiaChiChiTiet() != null ? diaChi.getDiaChiChiTiet() : "";
        String phuongXa = diaChi.getPhuongXa() != null ? diaChi.getPhuongXa() : "";
        String quanHuyen = diaChi.getQuanHuyen() != null ? diaChi.getQuanHuyen() : "";
        String tinhThanh = diaChi.getTinhThanh() != null ? diaChi.getTinhThanh() : "";

        // KIỂM TRA: Nếu backend gộp sẵn trong diaChiChiTiet thì phải TÁCH RA
        if (diaChiChiTiet.contains(phuongXa) && !phuongXa.isEmpty() &&
                diaChiChiTiet.contains(quanHuyen) && !quanHuyen.isEmpty() &&
                diaChiChiTiet.contains(tinhThanh) && !tinhThanh.isEmpty()) {

            // Backend đã gộp, cần tách ra
            String khuVucDayDu = phuongXa + ", " + quanHuyen + ", " + tinhThanh;

            // Loại bỏ khu vực khỏi diaChiChiTiet
            diaChiChiTiet = diaChiChiTiet.replace(", " + khuVucDayDu, "")
                    .replace(khuVucDayDu, "")
                    .trim();
        }

        // CHỈ truyền địa chỉ chi tiết (số nhà, ngõ) - ĐÃ TÁCH
        intent.putExtra("diaChiChiTiet", diaChiChiTiet);

        // Truyền khu vực RIÊNG BIỆT
        intent.putExtra("tinhThanh", tinhThanh);
        intent.putExtra("quanHuyen", quanHuyen);
        intent.putExtra("phuongXa", phuongXa);

        intent.putExtra("diaChiMacDinh", diaChi.getDiaChiMacDinh() != null && diaChi.getDiaChiMacDinh());
        intent.putExtra("isEditMode", true);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // Mở Activity xem thông tin chi tiết
    private void openInfoActivity(Model_DiaChi diaChi) {
        Intent intent = new Intent(context, InfoAddressActivity.class);

        intent.putExtra("maDiaChi", diaChi.getMaDiaChi());
        intent.putExtra("loaiDiaChi", diaChi.getLoaiDiaChi());
        intent.putExtra("tenNguoiNhan", diaChi.getTenNguoiNhan());
        intent.putExtra("soDienThoai", diaChi.getSoDienThoai());

        // Truyền dữ liệu TÁCH RIÊNG
        intent.putExtra("diaChiChiTiet", diaChi.getDiaChiChiTiet());
        intent.putExtra("tinhThanh", diaChi.getTinhThanh());
        intent.putExtra("quanHuyen", diaChi.getQuanHuyen());
        intent.putExtra("phuongXa", diaChi.getPhuongXa());

        intent.putExtra("diaChiMacDinh", diaChi.getDiaChiMacDinh() != null && diaChi.getDiaChiMacDinh());
        intent.putExtra("isEditMode", true);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return diaChiList != null ? diaChiList.size() : 0;
    }

    //  Cập nhật danh sách địa chỉ
    public void updateList(List<Model_DiaChi> newList) {
        this.diaChiList = newList;
        notifyDataSetChanged();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAddress, tvAddressType, tvDefaultTag, btnEdit;
        ImageView ivAddressType;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvAddressType = itemView.findViewById(R.id.tvAddressType);
            tvDefaultTag = itemView.findViewById(R.id.tvDefaultTag);
            ivAddressType = itemView.findViewById(R.id.ivAddressType);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}