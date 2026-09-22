package com.tuckersoft.branchengine.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="decisions")
public class Decision {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Playthrough playthrough;
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private StoryNode node;
    @Column(nullable=false, columnDefinition="TEXT")
    private String rawInput;
    @Column(nullable=false)
    private String branchType;
    @Column(nullable=false)
    private String impactLevel;
    @Column(nullable=false)
    private String handlerUnit;
    @Column(nullable=false)
    private String outcomeCode;
    private String resolvedNodeCode;
    @Column(nullable=false)
    private String status;
    @Column(nullable=false)
    private Instant createdAt;
    @Column(nullable=false)
    private Instant updatedAt;

    public Long getId(){return id;}
    public Playthrough getPlaythrough(){return playthrough;}
    public void setPlaythrough(Playthrough v){playthrough=v;}
    public StoryNode getNode(){return node;}
    public void setNode(StoryNode v){node=v;}
    public String getRawInput(){return rawInput;}
    public void setRawInput(String v){rawInput=v;}
    public String getBranchType(){return branchType;}
    public void setBranchType(String v){branchType=v;}
    public String getImpactLevel(){return impactLevel;}
    public void setImpactLevel(String v){impactLevel=v;}
    public String getHandlerUnit(){return handlerUnit;}
    public void setHandlerUnit(String v){handlerUnit=v;}
    public String getOutcomeCode(){return outcomeCode;}
    public void setOutcomeCode(String v){outcomeCode=v;}
    public String getResolvedNodeCode(){return resolvedNodeCode;}
    public void setResolvedNodeCode(String v){resolvedNodeCode=v;}
    public String getStatus(){return status;}
    public void setStatus(String v){status=v;}
    public Instant getCreatedAt(){return createdAt;}
    public void setCreatedAt(Instant v){createdAt=v;}
    public Instant getUpdatedAt(){return updatedAt;}
    public void setUpdatedAt(Instant v){updatedAt=v;}
}
