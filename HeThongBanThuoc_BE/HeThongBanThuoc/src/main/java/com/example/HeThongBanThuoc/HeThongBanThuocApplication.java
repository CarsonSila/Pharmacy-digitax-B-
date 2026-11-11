package com.example.HeThongBanThuoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // FIX #10: cho phép @Scheduled trong các service
public class HeThongBanThuocApplication {

	public static void main(String[] args) {
		SpringApplication.run(HeThongBanThuocApplication.class, args);
	}

}
