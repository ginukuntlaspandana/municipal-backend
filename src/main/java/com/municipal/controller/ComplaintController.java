package com.municipal.controller;

import com.municipal.dto.request.ComplaintCreateRequest;
import com.municipal.dto.request.FeedbackRequest;
import com.municipal.dto.request.ReopenRequest;
import com.municipal.dto.response.ApiResponse;
import com.municipal.dto.response.ComplaintDetailDto;
import com.municipal.dto.response.ComplaintSummaryDto;
import com.municipal.dto.response.FeedbackDto;
import com.municipal.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ApiResponse<ComplaintDetailDto>> createComplaintMultipart(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("locality") String locality,
            @RequestParam("address") String address,
            @RequestParam(value = "landmark", required = false) String landmark,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        ComplaintCreateRequest request = new ComplaintCreateRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategoryId(categoryId);
        request.setLocality(locality);
        request.setAddress(address);
        request.setLandmark(landmark);

        ComplaintDetailDto complaint = complaintService.createComplaint(request, imageFile);
        return ResponseEntity.ok(ApiResponse.success("Complaint submitted successfully. Your Complaint ID is: " + complaint.getComplaintNumber(), complaint));
    }

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<ApiResponse<ComplaintDetailDto>> createComplaintJson(@Valid @RequestBody ComplaintCreateRequest request) {
        ComplaintDetailDto complaint = complaintService.createComplaint(request, null);
        return ResponseEntity.ok(ApiResponse.success("Complaint submitted successfully. Your Complaint ID is: " + complaint.getComplaintNumber(), complaint));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ComplaintSummaryDto>>> getMyComplaints() {
        List<ComplaintSummaryDto> complaints = complaintService.getMyComplaints();
        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved successfully", complaints));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintDetailDto>> getComplaintById(@PathVariable Long id) {
        ComplaintDetailDto complaint = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint details retrieved", complaint));
    }

    @GetMapping("/track/{complaintNumber}")
    public ResponseEntity<ApiResponse<ComplaintDetailDto>> trackComplaint(@PathVariable String complaintNumber) {
        ComplaintDetailDto complaint = complaintService.trackComplaint(complaintNumber);
        return ResponseEntity.ok(ApiResponse.success("Complaint status tracked successfully", complaint));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<ComplaintDetailDto>> reopenComplaint(
            @PathVariable Long id,
            @Valid @RequestBody ReopenRequest request
    ) {
        ComplaintDetailDto complaint = complaintService.reopenComplaint(id, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint reopened successfully", complaint));
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<ApiResponse<FeedbackDto>> submitFeedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequest request
    ) {
        FeedbackDto feedback = complaintService.submitFeedback(id, request);
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted successfully. Thank you!", feedback));
    }
}
