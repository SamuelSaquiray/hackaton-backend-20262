package com.tuckersoft.branchengine.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="story_nodes", uniqueConstraints=@UniqueConstraint(columnNames="nodeCode"))
public class StoryNode {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=40)
    private String nodeCode;
    @Column(nullable=false, length=80)
    private String title;
    @Column(nullable=false, columnDefinition="TEXT")
    private String sceneText;
    @Column(nullable=false)
    private Integer branchCapacity;
    @Column(nullable=false)
    private Integer currentBranches;
    private String primaryBranchCode;
    private String glitchBranchCode;
    @Column(nullable=false)
    private Instant createdAt;

    public Long getId(){return id;}
    public String getNodeCode(){return nodeCode;}
    public void setNodeCode(String v){nodeCode=v;}
    public String getTitle(){return title;}
    public void setTitle(String v){title=v;}
    public String getSceneText(){return sceneText;}
    public void setSceneText(String v){sceneText=v;}
    public Integer getBranchCapacity(){return branchCapacity;}
    public void setBranchCapacity(Integer v){branchCapacity=v;}
    public Integer getCurrentBranches(){return currentBranches;}
    public void setCurrentBranches(Integer v){currentBranches=v;}
    public String getPrimaryBranchCode(){return primaryBranchCode;}
    public void setPrimaryBranchCode(String v){primaryBranchCode=v;}
    public String getGlitchBranchCode(){return glitchBranchCode;}
    public void setGlitchBranchCode(String v){glitchBranchCode=v;}
    public Instant getCreatedAt(){return createdAt;}
    public void setCreatedAt(Instant v){createdAt=v;}
}
