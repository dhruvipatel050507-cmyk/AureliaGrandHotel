package com.aurelia.hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👤 User Info
    private String name;
    private String email;

    // 🧾 Message
    @Column(length = 1000)
    private String message;

    // 💬 Admin Reply
    @Column(length = 1000)
    private String reply;

    // 🟢 Status
    // NEW / READ / REPLIED
    private String status;

    // 📅 Created Date
    private LocalDate createdDate;

    // 📅 Reply Date
    private LocalDate replyDate;

    // 🔥 DEFAULT VALUES
    @PrePersist
    public void prePersist(){
        if(this.status == null){
            this.status = "NEW";
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
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

	public LocalDate getReplyDate() {
		return replyDate;
	}

	public void setReplyDate(LocalDate replyDate) {
		this.replyDate = replyDate;
	}
}