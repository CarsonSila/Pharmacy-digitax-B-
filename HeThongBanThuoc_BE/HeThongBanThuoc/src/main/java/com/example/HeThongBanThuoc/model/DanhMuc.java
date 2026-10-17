package com.example.HeThongBanThuoc.model;

public class DanhMuc {
    private Integer  maDanhMuc;
    private String tenDanhMuc;
    private String slug;
    private String icon;
    private Integer maDanhMucCha;
    private Integer soLuongSanPham;

    // Getter & setter
    public Integer getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(Integer maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Integer getMaDanhMucCha() { return maDanhMucCha; }
    public void setMaDanhMucCha(Integer maDanhMucCha) { this.maDanhMucCha = maDanhMucCha; }

    public Integer getSoLuongSanPham() { return soLuongSanPham; }
    public void setSoLuongSanPham(Integer soLuongSanPham) { this.soLuongSanPham = soLuongSanPham; }


}
