    package com.example.hethongbanthuoc.feature_onboarding.models;

    public class Model_Onboarding {
        private int imageResId;
        private String title;
        private String description;

        public Model_Onboarding(int imageResId, String title, String description) {
            this.imageResId = imageResId;
            this.title = title;
            this.description = description;
        }

        public int getImageResId() {
            return imageResId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }
    }
