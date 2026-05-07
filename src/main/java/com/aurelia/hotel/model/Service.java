package com.aurelia.hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🏷 Service Name
    private String name;

    // 💰 Price
    private double price;

    // 🧾 Description
    @Column(length = 1000)
    private String description;

    // 🖼 Image
    private String image;

    // 🎯 Category (Spa, Dining, Gym, etc.)
    private String category;

    // 🟢 Status
    // AVAILABLE / NOT_AVAILABLE
    private String status;

    // 📅 Created Date
    private LocalDate createdDate;

    // 🔥 DEFAULT VALUES
    @PrePersist
    public void prePersist(){
        if(this.status == null){
            this.status = "AVAILABLE";
        }
        this.createdDate = LocalDate.now();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}
}