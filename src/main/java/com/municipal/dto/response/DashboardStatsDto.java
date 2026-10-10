package com.municipal.dto.response;

import java.util.ArrayList;
import java.util.List;

public class DashboardStatsDto {
    private long totalComplaints;
    private long pendingComplaints; // SUBMITTED + UNDER_REVIEW
    private long assignedComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private long rejectedComplaints;
    private long reopenedComplaints;
    private double averageResolutionHours;
    private double averageFeedbackRating;
    private long totalCitizens;
    private long totalDepartments;

    private List<CategoryStatDto> categoryDistribution = new ArrayList<>();
    private List<LocalityStatDto> localityDistribution = new ArrayList<>();
    private List<MonthlyStatDto> monthlyTrends = new ArrayList<>();

    public DashboardStatsDto() {
    }

    public long getTotalComplaints() {
        return totalComplaints;
    }

    public void setTotalComplaints(long totalComplaints) {
        this.totalComplaints = totalComplaints;
    }

    public long getPendingComplaints() {
        return pendingComplaints;
    }

    public void setPendingComplaints(long pendingComplaints) {
        this.pendingComplaints = pendingComplaints;
    }

    public long getAssignedComplaints() {
        return assignedComplaints;
    }

    public void setAssignedComplaints(long assignedComplaints) {
        this.assignedComplaints = assignedComplaints;
    }

    public long getInProgressComplaints() {
        return inProgressComplaints;
    }

    public void setInProgressComplaints(long inProgressComplaints) {
        this.inProgressComplaints = inProgressComplaints;
    }

    public long getResolvedComplaints() {
        return resolvedComplaints;
    }

    public void setResolvedComplaints(long resolvedComplaints) {
        this.resolvedComplaints = resolvedComplaints;
    }

    public long getRejectedComplaints() {
        return rejectedComplaints;
    }

    public void setRejectedComplaints(long rejectedComplaints) {
        this.rejectedComplaints = rejectedComplaints;
    }

    public long getReopenedComplaints() {
        return reopenedComplaints;
    }

    public void setReopenedComplaints(long reopenedComplaints) {
        this.reopenedComplaints = reopenedComplaints;
    }

    public double getAverageResolutionHours() {
        return averageResolutionHours;
    }

    public void setAverageResolutionHours(double averageResolutionHours) {
        this.averageResolutionHours = averageResolutionHours;
    }

    public double getAverageFeedbackRating() {
        return averageFeedbackRating;
    }

    public void setAverageFeedbackRating(double averageFeedbackRating) {
        this.averageFeedbackRating = averageFeedbackRating;
    }

    public long getTotalCitizens() {
        return totalCitizens;
    }

    public void setTotalCitizens(long totalCitizens) {
        this.totalCitizens = totalCitizens;
    }

    public long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public List<CategoryStatDto> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(List<CategoryStatDto> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public List<LocalityStatDto> getLocalityDistribution() {
        return localityDistribution;
    }

    public void setLocalityDistribution(List<LocalityStatDto> localityDistribution) {
        this.localityDistribution = localityDistribution;
    }

    public List<MonthlyStatDto> getMonthlyTrends() {
        return monthlyTrends;
    }

    public void setMonthlyTrends(List<MonthlyStatDto> monthlyTrends) {
        this.monthlyTrends = monthlyTrends;
    }
}
