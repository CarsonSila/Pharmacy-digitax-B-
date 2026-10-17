package com.example.hethongbanthuoc.feature_shopping.cart.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Map;

public class Model_ChiTietGioHang implements Serializable {
    @SerializedName("maCTGH")
    private int maCTGH;

    @SerializedName("maGioHang")
    private int maGioHang;

    @SerializedName("maSanPham")
    private int maSanPham;

    @SerializedName("tenSanPham")
    private String tenSanPham;

    @SerializedName("soLuong")
    private int soLuong;

    @SerializedName("donGia")
    private double donGia;

    @SerializedName("thanhTien")
    private double thanhTien;

    @SerializedName("duongDanAnh")
    private String hinhAnh;

    @SerializedName("donViTinh")
    private String donViTinh;

    private boolean selected = false;

    // --- Getter / Setter ---
    public int getMaCTGH() { return maCTGH; }
    public void setMaCTGH(int maCTGH) { this.maCTGH = maCTGH; }

    public int getMaGioHang() { return maGioHang; }
    public void setMaGioHang(int maGioHang) { this.maGioHang = maGioHang; }

    public int getMaSanPham() { return maSanPham; }
    public void setMaSanPham(int maSanPham) { this.maSanPham = maSanPham; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTien() { return thanhTien; }
    public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    // Tạo đối tượng từ Map<String, Object> (khi backend trả về kiểu Map)
    public static Model_ChiTietGioHang fromMap(Map<String, Object> map) {
        Model_ChiTietGioHang ct = new Model_ChiTietGioHang();
        if (map == null) return ct;

        try {
            Object o;

            o = map.get("maCTGH");
            if (o instanceof Number) ct.setMaCTGH(((Number) o).intValue());
            else if (o != null) ct.setMaCTGH(Integer.parseInt(o.toString()));

            o = map.get("maGioHang");
            if (o instanceof Number) ct.setMaGioHang(((Number) o).intValue());
            else if (o != null) ct.setMaGioHang(Integer.parseInt(o.toString()));

            o = map.get("maSanPham");
            if (o instanceof Number) ct.setMaSanPham(((Number) o).intValue());
            else if (o != null) ct.setMaSanPham(Integer.parseInt(o.toString()));

            o = map.get("tenSanPham");
            if (o != null) ct.setTenSanPham(o.toString());

            o = map.get("soLuong");
            if (o instanceof Number) ct.setSoLuong(((Number) o).intValue());
            else if (o != null) ct.setSoLuong(Integer.parseInt(o.toString()));

            o = map.get("donGia");
            if (o instanceof Number) ct.setDonGia(((Number) o).doubleValue());
            else if (o != null) ct.setDonGia(Double.parseDouble(o.toString()));

            o = map.get("thanhTien");
            if (o instanceof Number) ct.setThanhTien(((Number) o).doubleValue());
            else if (o != null) ct.setThanhTien(Double.parseDouble(o.toString()));

            o = map.get("duongDanAnh");
            if (o != null) ct.setHinhAnh(o.toString());

            o = map.get("donViTinh");
            if (o != null) ct.setDonViTinh(o.toString());
            else ct.setDonViTinh("Đơn vị");

        } catch (Exception e) {
            e.printStackTrace();
        }

        ct.setSelected(false);
        return ct;
    }

}
