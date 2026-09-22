package com.tuckersoft.branchengine.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="playthroughs", uniqueConstraints=@UniqueConstraint(columnNames="playerTag"))
public class Playthrough {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=40)
    private String playerTag;
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private User user;
    @Column(nullable=false)
    private String startNodeCode;
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private StoryNode currentNode;
    @Column(nullable=false)
    private Integer lucidity;
    @Column(nullable=false)
    private Integer controlLevel;
    @Column(nullable=false)
    private String status;
    private String endingCode;
    @Column(nullable=false)
    private Instant createdAt;
    @Column(nullable=false)
    private Instant updatedAt;

    public Long getId(){return id;}
    public String getPlayerTag(){return playerTag;}
    public void setPlayerTag(String v){playerTag=v;}
    public User getUser(){return user;}
    public void setUser(User v){user=v;}
    public String getStartNodeCode(){return startNodeCode;}
    public void setStartNodeCode(String v){startNodeCode=v;}
    public StoryNode getCurrentNode(){return currentNode;}
    public void setCurrentNode(StoryNode v){currentNode=v;}
    public Integer getLucidity(){return lucidity;}
    public void setLucidity(Integer v){lucidity=v;}
    public Integer getControlLevel(){return controlLevel;}
    public void setControlLevel(Integer v){controlLevel=v;}
    public String getStatus(){return status;}
    public void setStatus(String v){status=v;}
    public String getEndingCode(){return endingCode;}
    public void setEndingCode(String v){endingCode=v;}
    public Instant getCreatedAt(){return createdAt;}
    public void setCreatedAt(Instant v){createdAt=v;}
    public Instant getUpdatedAt(){return updatedAt;}
    public void setUpdatedAt(Instant v){updatedAt=v;}
}
