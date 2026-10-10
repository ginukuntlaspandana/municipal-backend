package com.municipal.service;

import com.municipal.dto.request.ComplaintCreateRequest;
import com.municipal.dto.request.FeedbackRequest;
import com.municipal.dto.request.ReopenRequest;
import com.municipal.dto.response.*;
import com.municipal.entity.*;
import com.municipal.exception.BadRequestException;
import com.municipal.exception.ResourceNotFoundException;
import com.municipal.exception.UnauthorizedException;
import com.municipal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintStatusHistoryRepository historyRepository;

    @Autowired
    private ComplaintAssignmentRepository assignmentRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private AdministrativeRemarkRepository remarkRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AuthService authService;

    @Transactional
    public ComplaintDetailDto createComplaint(ComplaintCreateRequest request, MultipartFile imageFile) {
        User citizen = authService.getCurrentAuthenticatedUser();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        // Upload image if provided
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = fileStorageService.storeFile(imageFile);
        }

        Complaint complaint = new Complaint();
        complaint.setTitle(request.getTitle().trim());
        complaint.setDescription(request.getDescription().trim());
        complaint.setCategory(category);
        complaint.setCitizen(citizen);
        complaint.setLocality(request.getLocality().trim());
        complaint.setAddress(request.getAddress().trim());
        complaint.setLandmark(request.getLandmark() != null ? request.getLandmark().trim() : null);
        complaint.setImageUrl(imageUrl);
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        complaint.setPriority(ComplaintPriority.MEDIUM);

        // Generate human-readable complaint ID: MUNIC-YYYY-XXXX
        complaint.setComplaintNumber(generateUniqueComplaintNumber());

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Record initial status history
        ComplaintStatusHistory history = new ComplaintStatusHistory(
                savedComplaint,
                null,
                ComplaintStatus.SUBMITTED,
                citizen,
                "Complaint filed successfully by citizen"
        );
        historyRepository.save(history);

        return convertToDetailDto(savedComplaint, false);
    }

    public List<ComplaintSummaryDto> getMyComplaints() {
        User citizen = authService.getCurrentAuthenticatedUser();
        List<Complaint> complaints = complaintRepository.findByCitizenOrderByCreatedAtDesc(citizen);
        return complaints.stream().map(this::convertToSummaryDto).collect(Collectors.toList());
    }

    public ComplaintDetailDto getComplaintById(Long id) {
        User user = authService.getCurrentAuthenticatedUser();
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !complaint.getCitizen().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to view this complaint");
        }

        return convertToDetailDto(complaint, isAdmin);
    }

    public ComplaintDetailDto trackComplaint(String complaintNumber) {
        Complaint complaint = complaintRepository.findByComplaintNumber(complaintNumber.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("No complaint found with ID: " + complaintNumber));

        // For public tracking, internal admin remarks are omitted
        return convertToDetailDto(complaint, false);
    }

    @Transactional
    public ComplaintDetailDto reopenComplaint(Long id, ReopenRequest request) {
        User citizen = authService.getCurrentAuthenticatedUser();
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        if (!complaint.getCitizen().getId().equals(citizen.getId())) {
            throw new UnauthorizedException("Only the citizen who filed this complaint can reopen it");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.REJECTED) {
            throw new BadRequestException("Only Resolved or Rejected complaints can be reopened. Current status: " + complaint.getStatus());
        }

        ComplaintStatus previousStatus = complaint.getStatus();
        complaint.setStatus(ComplaintStatus.REOPENED);
        complaint.setReopenReason(request.getReason().trim());

        Complaint updatedComplaint = complaintRepository.save(complaint);

        // Record history
        ComplaintStatusHistory history = new ComplaintStatusHistory(
                updatedComplaint,
                previousStatus,
                ComplaintStatus.REOPENED,
                citizen,
                "Complaint reopened by citizen: " + request.getReason().trim()
        );
        historyRepository.save(history);

        return convertToDetailDto(updatedComplaint, false);
    }

    @Transactional
    public FeedbackDto submitFeedback(Long id, FeedbackRequest request) {
        User citizen = authService.getCurrentAuthenticatedUser();
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        if (!complaint.getCitizen().getId().equals(citizen.getId())) {
            throw new UnauthorizedException("Only the citizen who filed this complaint can submit feedback");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new BadRequestException("Feedback can only be submitted for Resolved complaints");
        }

        if (feedbackRepository.existsByComplaintId(complaint.getId())) {
            throw new BadRequestException("Feedback has already been submitted for this complaint");
        }

        Feedback feedback = new Feedback(
                complaint,
                citizen,
                request.getRating(),
                request.getComments() != null ? request.getComments().trim() : null
        );

        Feedback savedFeedback = feedbackRepository.save(feedback);

        return new FeedbackDto(
                savedFeedback.getId(),
                complaint.getId(),
                complaint.getComplaintNumber(),
                complaint.getTitle(),
                citizen.getFullName(),
                savedFeedback.getRating(),
                savedFeedback.getComments(),
                savedFeedback.getCreatedAt()
        );
    }

    private synchronized String generateUniqueComplaintNumber() {
        int year = Year.now().getValue();
        long count = complaintRepository.count() + 1;
        String formatted = String.format("MUNIC-%d-%04d", year, count);

        // If collision occurs, add random offset
        while (complaintRepository.findByComplaintNumber(formatted).isPresent()) {
            count++;
            formatted = String.format("MUNIC-%d-%04d", year, count);
        }

        return formatted;
    }

    public ComplaintSummaryDto convertToSummaryDto(Complaint complaint) {
        ComplaintSummaryDto dto = new ComplaintSummaryDto();
        dto.setId(complaint.getId());
        dto.setComplaintNumber(complaint.getComplaintNumber());
        dto.setTitle(complaint.getTitle());
        dto.setCategoryName(complaint.getCategory() != null ? complaint.getCategory().getName() : "General");
        dto.setDepartmentName(complaint.getDepartment() != null ? complaint.getDepartment().getName() : "Unassigned");
        dto.setCitizenName(complaint.getCitizen() != null ? complaint.getCitizen().getFullName() : "Anonymous");
        dto.setStatus(complaint.getStatus());
        dto.setPriority(complaint.getPriority());
        dto.setLocality(complaint.getLocality());
        dto.setAddress(complaint.getAddress());
        dto.setImageUrl(complaint.getImageUrl());
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());
        dto.setResolvedAt(complaint.getResolvedAt());
        return dto;
    }

    public ComplaintDetailDto convertToDetailDto(Complaint complaint, boolean includeInternalRemarks) {
        ComplaintDetailDto dto = new ComplaintDetailDto();
        dto.setId(complaint.getId());
        dto.setComplaintNumber(complaint.getComplaintNumber());
        dto.setTitle(complaint.getTitle());
        dto.setDescription(complaint.getDescription());
        dto.setCategoryId(complaint.getCategory() != null ? complaint.getCategory().getId() : null);
        dto.setCategoryName(complaint.getCategory() != null ? complaint.getCategory().getName() : "General");
        dto.setDepartmentId(complaint.getDepartment() != null ? complaint.getDepartment().getId() : null);
        dto.setDepartmentName(complaint.getDepartment() != null ? complaint.getDepartment().getName() : "Unassigned");

        if (complaint.getCitizen() != null) {
            dto.setCitizenId(complaint.getCitizen().getId());
            dto.setCitizenName(complaint.getCitizen().getFullName());
            dto.setCitizenEmail(complaint.getCitizen().getEmail());
            dto.setCitizenPhone(complaint.getCitizen().getPhone());
        }

        dto.setStatus(complaint.getStatus());
        dto.setPriority(complaint.getPriority());
        dto.setLocality(complaint.getLocality());
        dto.setAddress(complaint.getAddress());
        dto.setLandmark(complaint.getLandmark());
        dto.setImageUrl(complaint.getImageUrl());
        dto.setResolutionDetails(complaint.getResolutionDetails());
        dto.setResolutionImageUrl(complaint.getResolutionImageUrl());
        dto.setResolvedAt(complaint.getResolvedAt());
        dto.setRejectionReason(complaint.getRejectionReason());
        dto.setReopenReason(complaint.getReopenReason());
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());

        // Latest assignment
        List<ComplaintAssignment> assignments = assignmentRepository.findByComplaintOrderByAssignedAtDesc(complaint);
        if (!assignments.isEmpty()) {
            ComplaintAssignment latestAssign = assignments.get(0);
            dto.setAssignedOfficerName(latestAssign.getAssignedOfficerName());
            dto.setAssignmentNotes(latestAssign.getNotes());
            dto.setAssignedAt(latestAssign.getAssignedAt());
        }

        // Status history
        List<ComplaintStatusHistory> historyList = historyRepository.findByComplaintOrderByTimestampDesc(complaint);
        dto.setHistory(historyList.stream().map(h -> new StatusHistoryDto(
                h.getId(),
                h.getPreviousStatus(),
                h.getNewStatus(),
                h.getChangedByUser() != null ? h.getChangedByUser().getFullName() : "System",
                h.getRemarks(),
                h.getTimestamp()
        )).collect(Collectors.toList()));

        // Feedback
        feedbackRepository.findByComplaintId(complaint.getId()).ifPresent(f -> {
            dto.setFeedback(new FeedbackDto(
                    f.getId(),
                    complaint.getId(),
                    complaint.getComplaintNumber(),
                    complaint.getTitle(),
                    f.getCitizen().getFullName(),
                    f.getRating(),
                    f.getComments(),
                    f.getCreatedAt()
            ));
        });

        // Remarks
        List<AdministrativeRemark> remarks = includeInternalRemarks
                ? remarkRepository.findByComplaintOrderByCreatedAtDesc(complaint)
                : remarkRepository.findByComplaintAndInternalOnlyFalseOrderByCreatedAtDesc(complaint);

        dto.setRemarks(remarks.stream().map(r -> new RemarkDto(
                r.getId(),
                r.getAdmin() != null ? r.getAdmin().getFullName() : "Admin",
                r.getRemark(),
                r.getInternalOnly(),
                r.getCreatedAt()
        )).collect(Collectors.toList()));

        return dto;
    }
}
