package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "complaint_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false, columnDefinition = "CHAR(36)")
    private Complaint complaint;

    @Column(name = "sender_id", nullable = false, columnDefinition = "CHAR(36)")
    private String senderId;

    @Column(name = "sender_type", nullable = false, length = 20)
    private String senderType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String attachments;
}


