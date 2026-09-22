package com.tuckersoft.branchengine.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="reality_logs")
public class RealityLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Decision decision;
    @Column(nullable=false)
    private String recipientEmail;
    @Column(nullable=false)
    private String subject;
    @Column(nullable=false)
    private String logStatus;
    @Column(columnDefinition="TEXT")
    private String errorMessage;
    private Instant sentAt;
    @Column(nullable=false)
    private Instant createdAt;

    public Long getId(){return id;}
    public Decision getDecision(){return decision;}
    public void setDecision(Decision v){decision=v;}
    public String getRecipientEmail(){return recipientEmail;}
    public void setRecipientEmail(String v){recipientEmail=v;}
    public String getSubject(){return subject;}
    public void setSubject(String v){subject=v;}
    public String getLogStatus(){return logStatus;}
    public void setLogStatus(String v){logStatus=v;}
    public String getErrorMessage(){return errorMessage;}
    public void setErrorMessage(String v){errorMessage=v;}
    public Instant getSentAt(){return sentAt;}
    public void setSentAt(Instant v){sentAt=v;}
    public Instant getCreatedAt(){return createdAt;}

    public void setCreatedAt(Instant now) {
        createdAt = now;
    }
}
