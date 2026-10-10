package com.municipal.service;

import com.municipal.dto.request.*;
import com.municipal.dto.response.*;
import com.municipal.entity.*;
import com.municipal.exception.BadRequestException;
import com.municipal.exception.ResourceNotFoundException;
import com.municipal.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintStatusHistoryRepository historyRepository;

    @Autowired
    private ComplaintAssignmentRepository assignmentRepository;

    @Autowired
    private AdministrativeRemarkRepository remarkRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ComplaintService complaintService;

    public Page<ComplaintSummaryDto> getAllComplaints(
            String search,
            Long categoryId,
            Long departmentId,
            ComplaintStatus status,
            ComplaintPriority priority,
            String locality,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        Specification<Complaint> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate numberPred = cb.like(cb.lower(root.get("complaintNumber")), searchPattern);
                Predicate titlePred = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate localityPred = cb.like(cb.lower(root.get("locality")), searchPattern);
                Predicate descPred = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate citizenPred = cb.like(cb.lower(root.get("citizen").get("fullName")), searchPattern);
                predicates.add(cb.or(numberPred, titlePred, localityPred, descPred, citizenPred));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            if (locality != null && !locality.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("locality")), "%" + locality.trim().toLowerCase() + "%"));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(LocalTime.MAX)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Complaint> page = complaintRepository.findAll(spec, pageable);
        return page.map(complaintService::convertToSummaryDto);
    }

    @Transactional
    public ComplaintDetailDto updateStatus(Long complaintId, StatusUpdateRequest request, MultipartFile resolutionPhoto) {
        User admin = authService.getCurrentAuthenticatedUser();
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        ComplaintStatus oldStatus = complaint.getStatus();
        ComplaintStatus newStatus = request.getStatus();

        // Business Rule: A complaint cannot be marked Resolved without appropriate resolution details
        if (newStatus == ComplaintStatus.RESOLVED) {
            if (request.getResolutionDetails() == null || request.getResolutionDetails().trim().isEmpty()) {
                throw new BadRequestException("Resolution details are mandatory when marking a complaint as Resolved");
            }
            complaint.setResolutionDetails(request.getResolutionDetails().trim());
            complaint.setResolvedAt(LocalDateTime.now());

            if (resolutionPhoto != null && !resolutionPhoto.isEmpty()) {
                complaint.setResolutionImageUrl(fileStorageService.storeFile(resolutionPhoto));
            }
        }

        // Business Rule: A complaint marked Rejected must have a reason
        if (newStatus == ComplaintStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
                throw new BadRequestException("Rejection reason is mandatory when rejecting a complaint");
            }
            complaint.setRejectionReason(request.getRejectionReason().trim());
        }

        complaint.setStatus(newStatus);
        Complaint updated = complaintRepository.save(complaint);

        // Record History
        String remarks = request.getRemarks();
        if (remarks == null || remarks.trim().isEmpty()) {
            remarks = "Status updated from " + oldStatus + " to " + newStatus;
        }

        ComplaintStatusHistory history = new ComplaintStatusHistory(
                updated,
                oldStatus,
                newStatus,
                admin,
                remarks
        );
        historyRepository.save(history);

        return complaintService.convertToDetailDto(updated, true);
    }

    @Transactional
    public ComplaintDetailDto assignComplaint(Long complaintId, AssignComplaintRequest request) {
        User admin = authService.getCurrentAuthenticatedUser();
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        complaint.setDepartment(department);

        ComplaintStatus oldStatus = complaint.getStatus();
        ComplaintStatus newStatus = ComplaintStatus.ASSIGNED;

        // Automatically move to ASSIGNED if submitted or under review
        if (oldStatus == ComplaintStatus.SUBMITTED || oldStatus == ComplaintStatus.UNDER_REVIEW) {
            complaint.setStatus(newStatus);
        } else {
            newStatus = oldStatus;
        }

        Complaint updated = complaintRepository.save(complaint);

        // Record Assignment
        ComplaintAssignment assignment = new ComplaintAssignment(
                updated,
                department,
                request.getAssignedOfficerName() != null ? request.getAssignedOfficerName().trim() : null,
                request.getNotes() != null ? request.getNotes().trim() : null,
                admin
        );
        assignmentRepository.save(assignment);

        // Record History
        String remarks = "Assigned to department: " + department.getName() +
                (request.getAssignedOfficerName() != null ? " (Officer: " + request.getAssignedOfficerName() + ")" : "");

        ComplaintStatusHistory history = new ComplaintStatusHistory(
                updated,
                oldStatus,
                updated.getStatus(),
                admin,
                remarks
        );
        historyRepository.save(history);

        return complaintService.convertToDetailDto(updated, true);
    }

    @Transactional
    public ComplaintDetailDto updatePriority(Long complaintId, PriorityUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        complaint.setPriority(request.getPriority());
        Complaint updated = complaintRepository.save(complaint);
        return complaintService.convertToDetailDto(updated, true);
    }

    @Transactional
    public RemarkDto addRemark(Long complaintId, RemarkRequest request) {
        User admin = authService.getCurrentAuthenticatedUser();
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        AdministrativeRemark remark = new AdministrativeRemark(
                complaint,
                admin,
                request.getRemark().trim(),
                request.getInternalOnly()
        );

        AdministrativeRemark saved = remarkRepository.save(remark);
        return new RemarkDto(
                saved.getId(),
                admin.getFullName(),
                saved.getRemark(),
                saved.getInternalOnly(),
                saved.getCreatedAt()
        );
    }

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        long total = complaintRepository.count();
        long submitted = complaintRepository.countByStatus(ComplaintStatus.SUBMITTED);
        long underReview = complaintRepository.countByStatus(ComplaintStatus.UNDER_REVIEW);
        long assigned = complaintRepository.countByStatus(ComplaintStatus.ASSIGNED);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long rejected = complaintRepository.countByStatus(ComplaintStatus.REJECTED);
        long reopened = complaintRepository.countByStatus(ComplaintStatus.REOPENED);

        stats.setTotalComplaints(total);
        stats.setPendingComplaints(submitted + underReview);
        stats.setAssignedComplaints(assigned);
        stats.setInProgressComplaints(inProgress);
        stats.setResolvedComplaints(resolved);
        stats.setRejectedComplaints(rejected);
        stats.setReopenedComplaints(reopened);

        stats.setTotalCitizens(userRepository.countByRole(Role.ROLE_CITIZEN));
        stats.setTotalDepartments(departmentRepository.count());

        Double avgHours = complaintRepository.getAverageResolutionHours();
        stats.setAverageResolutionHours(avgHours != null ? Math.round(avgHours * 10.0) / 10.0 : 0.0);

        Double avgRating = feedbackRepository.getAverageRating();
        stats.setAverageFeedbackRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

        // Group by category
        List<Object[]> catRows = complaintRepository.countComplaintsByCategory();
        List<CategoryStatDto> categoryStats = new ArrayList<>();
        for (Object[] row : catRows) {
            String catName = (String) row[0];
            long count = ((Number) row[1]).longValue();
            categoryStats.add(new CategoryStatDto(catName != null ? catName : "Other", count));
        }
        stats.setCategoryDistribution(categoryStats);

        // Group by locality
        List<Object[]> locRows = complaintRepository.countComplaintsByLocality();
        List<LocalityStatDto> localityStats = new ArrayList<>();
        for (Object[] row : locRows) {
            String locality = (String) row[0];
            long count = ((Number) row[1]).longValue();
            localityStats.add(new LocalityStatDto(locality != null ? locality : "General", count));
        }
        stats.setLocalityDistribution(localityStats);

        // Monthly trends
        List<Object[]> monthRows = complaintRepository.countComplaintsByMonth();
        List<MonthlyStatDto> monthlyStats = new ArrayList<>();
        for (Object[] row : monthRows) {
            String month = (String) row[0];
            long count = ((Number) row[1]).longValue();
            monthlyStats.add(new MonthlyStatDto(month != null ? month : "Current", count));
        }
        stats.setMonthlyTrends(monthlyStats);

        return stats;
    }

    public byte[] exportComplaintsCsv() {
        List<Complaint> complaints = complaintRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            // Write CSV Header
            writer.println("Complaint ID,Title,Category,Department,Citizen Name,Citizen Phone,Locality,Address,Status,Priority,Created At,Resolved At,Resolution Details");

            for (Complaint c : complaints) {
                String id = escapeCsv(c.getComplaintNumber());
                String title = escapeCsv(c.getTitle());
                String category = escapeCsv(c.getCategory() != null ? c.getCategory().getName() : "");
                String department = escapeCsv(c.getDepartment() != null ? c.getDepartment().getName() : "Unassigned");
                String citizen = escapeCsv(c.getCitizen() != null ? c.getCitizen().getFullName() : "");
                String phone = escapeCsv(c.getCitizen() != null ? c.getCitizen().getPhone() : "");
                String locality = escapeCsv(c.getLocality());
                String address = escapeCsv(c.getAddress());
                String status = escapeCsv(c.getStatus().name());
                String priority = escapeCsv(c.getPriority().name());
                String createdAt = escapeCsv(c.getCreatedAt() != null ? c.getCreatedAt().toString() : "");
                String resolvedAt = escapeCsv(c.getResolvedAt() != null ? c.getResolvedAt().toString() : "");
                String resolution = escapeCsv(c.getResolutionDetails() != null ? c.getResolutionDetails() : "");

                writer.println(String.join(",", id, title, category, department, citizen, phone, locality, address, status, priority, createdAt, resolvedAt, resolution));
            }
        }

        return out.toByteArray();
    }

    private String escapeCsv(String val) {
        if (val == null) return "\"\"";
        String escaped = val.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    // Departments Management
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Department with name '" + request.getName() + "' already exists");
        }
        if (departmentRepository.existsByCodeIgnoreCase(request.getCode().trim())) {
            throw new BadRequestException("Department code '" + request.getCode() + "' is already in use");
        }

        Department dept = new Department(
                request.getName().trim(),
                request.getCode().trim().toUpperCase(),
                request.getContactEmail() != null ? request.getContactEmail().trim() : null,
                request.getContactPhone() != null ? request.getContactPhone().trim() : null
        );
        if (request.getActive() != null) {
            dept.setActive(request.getActive());
        }

        return departmentRepository.save(dept);
    }

    // Categories Management
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        Category cat = new Category(
                request.getName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getIcon() != null ? request.getIcon().trim() : "AlertCircle"
        );
        if (request.getActive() != null) {
            cat.setActive(request.getActive());
        }

        return categoryRepository.save(cat);
    }

    // Feedback Management
    public List<FeedbackDto> getAllFeedback() {
        List<Feedback> feedbackList = feedbackRepository.findAllByOrderByCreatedAtDesc();
        return feedbackList.stream().map(f -> new FeedbackDto(
                f.getId(),
                f.getComplaint().getId(),
                f.getComplaint().getComplaintNumber(),
                f.getComplaint().getTitle(),
                f.getCitizen().getFullName(),
                f.getRating(),
                f.getComments(),
                f.getCreatedAt()
        )).collect(Collectors.toList());
    }
}
