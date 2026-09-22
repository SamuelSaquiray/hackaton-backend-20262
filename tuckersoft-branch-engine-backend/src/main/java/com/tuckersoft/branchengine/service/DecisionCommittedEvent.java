package com.tuckersoft.branchengine.service;
public record DecisionCommittedEvent(Long decisionId, boolean simulateMailFailure) {}
