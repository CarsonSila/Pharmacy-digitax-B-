package com.example.hethongbanthuoc.feature_rewards;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.hethongbanthuoc.core.network.API_RetrofitClient;
import com.example.hethongbanthuoc.core.network.API_AuthService;
import com.example.hethongbanthuoc.R;
import com.example.hethongbanthuoc.core.utils.TokenManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Button_sheet_doi_quaActivity extends BottomSheetDialogFragment {

    private ImageView imgQuaTang, btnTang, btnGiam;
    private TextView tvTenQuaTang, tvSoLuong, tvDiemSuDung, tvDiemHienCo, tvDiemConLai;
    private LinearLayout layoutWarning;
    private MaterialButton btnXacNhan;

    private String tenQua;
    private String hinhAnh;
    private int diemCanDoi;
    private int tongDiem;
    private int soLuong = 1;
    private int maQuaTang;
    private int maNguoiDung;

    public static Button_sheet_doi_quaActivity newInstance(String tenQua, String hinhAnh,
                                                           int diemCanDoi, int tongDiem,
                                                           int maQuaTang, int maNguoiDung) {
        Button_sheet_doi_quaActivity sheet = new Button_sheet_doi_quaActivity();
        Bundle args = new Bundle();
        args.putString("TEN_QUA", tenQua);
        args.putString("HINH_ANH", hinhAnh);
        args.putInt("DIEM_CAN_DOI", diemCanDoi);
        args.putInt("TONG_DIEM", tongDiem);
        args.putInt("MA_QUA_TANG", maQuaTang);
        args.putInt("MA_NGUOI_DUNG", maNguoiDung);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_button_sheet_doi_qua, container, false);
        initViews(view);
        getData();
        bindData();
        setupEvents();
        return view;
    }

    private void initViews(View v) {
        imgQuaTang = v.findViewById(R.id.imgQuaTang);
        btnTang = v.findViewById(R.id.btnTangSoLuong);
        btnGiam = v.findViewById(R.id.btnGiamSoLuong);
        tvTenQuaTang = v.findViewById(R.id.tvTenQuaTang);
        tvSoLuong = v.findViewById(R.id.tvSoLuong);
        tvDiemSuDung = v.findViewById(R.id.tvDiemSuDung);
        tvDiemHienCo = v.findViewById(R.id.tvDiemHienCo);
        tvDiemConLai = v.findViewById(R.id.tvDiemConLai);
        layoutWarning = v.findViewById(R.id.layoutWarning);
        btnXacNhan = v.findViewById(R.id.btnXacNhan);
    }

    private void getData() {
        if (getArguments() != null) {
            tenQua = getArguments().getString("TEN_QUA");
            hinhAnh = getArguments().getString("HINH_ANH");
            diemCanDoi = getArguments().getInt("DIEM_CAN_DOI");
            tongDiem = getArguments().getInt("TONG_DIEM");
            maQuaTang = getArguments().getInt("MA_QUA_TANG");
            maNguoiDung = getArguments().getInt("MA_NGUOI_DUNG");
        }
    }

    private void bindData() {
        tvTenQuaTang.setText(tenQua);
        tvDiemHienCo.setText(String.valueOf(tongDiem));
        Glide.with(requireContext())
                .load(hinhAnh)
                .placeholder(R.drawable.gift)
                .into(imgQuaTang);
        updateTinhToan();
    }

    private void setupEvents() {
        btnTang.setOnClickListener(v -> {
            soLuong++;
            updateTinhToan();
        });

        btnGiam.setOnClickListener(v -> {
            if (soLuong > 1) {
                soLuong--;
                updateTinhToan();
            }
        });

        btnXacNhan.setOnClickListener(v -> {
            if (tongDiem - (diemCanDoi * soLuong) < 0) {
                Toast.makeText(requireContext(), "Không đủ điểm để đổi quà!", Toast.LENGTH_SHORT).show();
                return;
            }
            doiQuaTangAPI(requireContext());
        });
    }

    private void updateTinhToan() {
        tvSoLuong.setText(String.valueOf(soLuong));
        int tongSuDung = diemCanDoi * soLuong;
        int diemConLai = tongDiem - tongSuDung;
        tvDiemSuDung.setText("-" + tongSuDung);
        tvDiemConLai.setText(diemConLai + " điểm");

        if (diemConLai < 0) {
            layoutWarning.setVisibility(View.VISIBLE);
            btnXacNhan.setEnabled(false);
            btnXacNhan.setAlpha(0.5f);
        } else {
            layoutWarning.setVisibility(View.GONE);
            btnXacNhan.setEnabled(true);
            btnXacNhan.setAlpha(1f);
        }
    }

    private void doiQuaTangAPI(Context context) {
        // Disable button để tránh click nhiều lần
        btnXacNhan.setEnabled(false);
        btnXacNhan.setText("Đang xử lý...");

        API_AuthService apiService = API_RetrofitClient.getRetrofitInstance(context).create(API_AuthService.class);
        String token = TokenManager.getToken(context);

        // Tạo request body theo format API backend
        Map<String, Object> request = new HashMap<>();
        request.put("maNguoiDung", maNguoiDung);
        request.put("maQuaTang", maQuaTang);
        request.put("soLuong", soLuong);
        request.put("ghiChu", null); // Có thể thêm input cho ghi chú nếu cần

        Call<Map<String, Object>> call = apiService.doiQuaTang("Bearer " + token, request);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    boolean success = body.get("success") != null && (boolean) body.get("success");

                    if (success) {
                        Toast.makeText(context, "Đổi quà thành công!", Toast.LENGTH_SHORT).show();
                        dismiss();

                        // Mở màn hình "Quà của tôi" sau khi đổi quà thành công
                        Intent intent = new Intent(context, QuaCuaToiActivity.class);
                        intent.putExtra("MA_NGUOI_DUNG", maNguoiDung);
                        startActivity(intent);
                    } else {
                        String message = body.get("message") != null ?
                                body.get("message").toString() : "Đổi quà thất bại";
                        Toast.makeText(context, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                        btnXacNhan.setEnabled(true);
                        btnXacNhan.setText("Xác nhận");
                    }
                } else {
                    Toast.makeText(context, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    btnXacNhan.setEnabled(true);
                    btnXacNhan.setText("Xác nhận");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Toast.makeText(context, "Không thể kết nối server!", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
                btnXacNhan.setEnabled(true);
                btnXacNhan.setText("Xác nhận");
            }
        });
    }
}