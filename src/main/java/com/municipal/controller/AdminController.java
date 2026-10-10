package com.municipal.controller;

import com.municipal.dto.request.*;
import com.municipal.dto.response.*;
import com.municipal.entity.*;
import com.municipal.service.AdminService;
import com.municipal.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        DashboardStatsDto stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics loaded", stats));
    }

    @GetMapping("/complaints")
    public ResponseEntity<ApiResponse<Page<ComplaintSummaryDto>>> getAllComplaints(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) ComplaintPriority priority,
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ComplaintSummaryDto> complaints = adminService.getAllComplaints(
                search, categoryId, departmentId, status, priority, locality, startDate, endDate, pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Complaints loaded successfully", complaints));
    }

    @GetMapping("/complaints/{id}")
    public ResponseEntity<ApiResponse<ComplaintDetailDto>> getComplaintDetails(@PathVariable Long id) {
        ComplaintDetailDto complaint = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint details loaded", complaint));
    }

    @PutMapping(value = "/complaints/{id}/status", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ApiResponse<ComplaintDetailDto>> updateStatusMultipart(
            @PathVariable Long id,
            @RequestParam("status") ComplaintStatus status,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "resolutionDetails", required = false) String resolutionDetails,
            @RequestParam(value = "rejectionReason", required = false) String rejectionReason,
            @RequestParam(value = "resolutionPhoto", required = false) MultipartFile resolutionPhoto
    ) {
        StatusUpdateRequest request = new StatusUpdateRequest();
        request.setStatus(status);
        request.setRemarks(remarks);
        request.setResolutionDetails(resolutionDetails);
        request.setRejectionReason(rejectionReason);

        ComplaintDetailDto updated = adminService.updateStatus(id, request, resolutionPhoto);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated to " + updated.getStatus(), updated));
    }

    @PutMapping(value = "/complaints/{id}/status", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<ApiResponse<ComplaintDetailDto>> updateStatusJson(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        ComplaintDetailDto updated = adminService.updateStatus(id, request, null);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated to " + updated.getStatus(), updated));
    }

    @PutMapping("/complaints/{id}/assign")
    public ResponseEntity<ApiResponse<ComplaintDetailDto>> assignComplaint(
            @PathVariable Long id,
            @Valid @RequestBody AssignComplaintRequest request
    ) {
        ComplaintDetailDto updated = adminService.assignComplaint(id, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint assigned successfully", updated));
    }

    @PutMapping("/complaints/{id}/priority")
    public ResponseEntity<ApiResponse<ComplaintDetailDto>> updatePriority(
            @PathVariable Long id,
            @Valid @RequestBody PriorityUpdateRequest request
    ) {
        ComplaintDetailDto updated = adminService.updatePriority(id, request);
        return ResponseEntity.ok(ApiResponse.success("Priority updated to " + updated.getPriority(), updated));
    }

    @PostMapping("/complaints/{id}/remarks")
    public ResponseEntity<ApiResponse<RemarkDto>> addRemark(
            @PathVariable Long id,
            @Valid @RequestBody RemarkRequest request
    ) {
        RemarkDto remark = adminService.addRemark(id, request);
        return ResponseEntity.ok(ApiResponse.success("Administrative remark added", remark));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getReports() {
        DashboardStatsDto stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Reports generated", stats));
    }

    @GetMapping("/reports/export-csv")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] csvData = adminService.exportComplaintsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"complaints_report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments() {
        List<Department> departments = adminService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success("Departments loaded", departments));
    }

    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<Department>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        Department dept = adminService.createDepartment(request);
        return ResponseEntity.ok(ApiResponse.success("Department created successfully", dept));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getCategories() {
        List<Category> categories = adminService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories loaded", categories));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<Category>> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category cat = adminService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", cat));
    }

    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getAllFeedback() {
        List<FeedbackDto> feedback = adminService.getAllFeedback();
        return ResponseEntity.ok(ApiResponse.success("User feedback loaded", feedback));
    }
}
