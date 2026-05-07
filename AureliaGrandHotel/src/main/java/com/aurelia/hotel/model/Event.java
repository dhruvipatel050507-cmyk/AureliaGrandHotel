package com.aurelia.hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🏷 Event Name
    private String name;

    // 🧾 Description
    @Column(length = 1000)
    private String description;

    // 💰 Price
    private double price;

    // 📍 Location
    private String location;

    // 🖼 Image
    private String image;

    // 🎯 Event Type (Wedding, Party, Conference)
    private String type;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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