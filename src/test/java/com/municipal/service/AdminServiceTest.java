package com.municipal.service;

import com.municipal.dto.request.AssignComplaintRequest;
import com.municipal.dto.request.StatusUpdateRequest;
import com.municipal.dto.response.ComplaintDetailDto;
import com.municipal.dto.response.DashboardStatsDto;
import com.municipal.entity.*;
import com.municipal.exception.BadRequestException;
import com.municipal.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ComplaintStatusHistoryRepository historyRepository;

    @Mock
    private ComplaintAssignmentRepository assignmentRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private ComplaintService complaintService;

    @InjectMocks
    private AdminService adminService;

    private User sampleAdmin;
    private Complaint sampleComplaint;
    private Department sampleDepartment;

    @BeforeEach
    void setUp() {
        sampleAdmin = new User("Commissioner", "admin@municipal.gov", "pass", "9876500001", "HQ", Role.ROLE_ADMIN);
        sampleAdmin.setId(99L);

        sampleDepartment = new Department("Roads Dept", "RPI", "roads@municipal.gov", "1234567890");
        sampleDepartment.setId(5L);

        sampleComplaint = new Complaint();
        sampleComplaint.setId(50L);
        sampleComplaint.setComplaintNumber("MUNIC-2026-0050");
        sampleComplaint.setTitle("Broken pavement");
        sampleComplaint.setDescription("Needs urgent fix");
        sampleComplaint.setStatus(ComplaintStatus.SUBMITTED);
        sampleComplaint.setPriority(ComplaintPriority.MEDIUM);
        sampleComplaint.setLocality("Indiranagar");
        sampleComplaint.setAddress("4th cross");
    }

    @Test
    void testResolveComplaintWithoutDetailsThrowsBadRequest() {
        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(ComplaintStatus.RESOLVED);
        req.setResolutionDetails(null); // Missing required details!

        when(authService.getCurrentAuthenticatedUser()).thenReturn(sampleAdmin);
        when(complaintRepository.findById(50L)).thenReturn(Optional.of(sampleComplaint));

        assertThrows(BadRequestException.class, () -> adminService.updateStatus(50L, req, null));
    }

    @Test
    void testRejectComplaintWithoutReasonThrowsBadRequest() {
        StatusUpdateRequest req = new StatusUpdateRequest();
        req.setStatus(ComplaintStatus.REJECTED);
        req.setRejectionReason(""); // Missing required rejection reason!

        when(authService.getCurrentAuthenticatedUser()).thenReturn(sampleAdmin);
        when(complaintRepository.findById(50L)).thenReturn(Optional.of(sampleComplaint));

        assertThrows(BadRequestException.class, () -> adminService.updateStatus(50L, req, null));
    }

    @Test
    void testAssignComplaintSuccessMovesStatusToAssigned() {
        AssignComplaintRequest req = new AssignComplaintRequest();
        req.setDepartmentId(5L);
        req.setAssignedOfficerName("Officer Roy");
        req.setNotes("High priority action");

        when(authService.getCurrentAuthenticatedUser()).thenReturn(sampleAdmin);
        when(complaintRepository.findById(50L)).thenReturn(Optional.of(sampleComplaint));
        when(departmentRepository.findById(5L)).thenReturn(Optional.of(sampleDepartment));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(sampleComplaint);
        when(complaintService.convertToDetailDto(any(Complaint.class), eq(true))).thenReturn(new ComplaintDetailDto());

        ComplaintDetailDto result = adminService.assignComplaint(50L, req);

        assertNotNull(result);
        assertEquals(ComplaintStatus.ASSIGNED, sampleComplaint.getStatus());
        assertEquals(sampleDepartment, sampleComplaint.getDepartment());
        verify(assignmentRepository, times(1)).save(any(ComplaintAssignment.class));
        verify(historyRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    void testGetDashboardStatsReturnsRealCounts() {
        when(complaintRepository.count()).thenReturn(10L);
        when(complaintRepository.countByStatus(ComplaintStatus.SUBMITTED)).thenReturn(3L);
        when(complaintRepository.countByStatus(ComplaintStatus.UNDER_REVIEW)).thenReturn(1L);
        when(complaintRepository.countByStatus(ComplaintStatus.ASSIGNED)).thenReturn(2L);
        when(complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS)).thenReturn(2L);
        when(complaintRepository.countByStatus(ComplaintStatus.RESOLVED)).thenReturn(2L);
        when(complaintRepository.countByStatus(ComplaintStatus.REJECTED)).thenReturn(0L);
        when(complaintRepository.countByStatus(ComplaintStatus.REOPENED)).thenReturn(0L);
        when(userRepository.countByRole(Role.ROLE_CITIZEN)).thenReturn(25L);
        when(departmentRepository.count()).thenReturn(6L);
        when(complaintRepository.getAverageResolutionHours()).thenReturn(24.5);
        when(feedbackRepository.getAverageRating()).thenReturn(4.8);
        when(complaintRepository.countComplaintsByCategory()).thenReturn(new ArrayList<>());
        when(complaintRepository.countComplaintsByLocality()).thenReturn(new ArrayList<>());
        when(complaintRepository.countComplaintsByMonth()).thenReturn(new ArrayList<>());

        DashboardStatsDto stats = adminService.getDashboardStats();

        assertNotNull(stats);
        assertEquals(10L, stats.getTotalComplaints());
        assertEquals(4L, stats.getPendingComplaints()); // 3 + 1
        assertEquals(2L, stats.getAssignedComplaints());
        assertEquals(2L, stats.getInProgressComplaints());
        assertEquals(2L, stats.getResolvedComplaints());
        assertEquals(24.5, stats.getAverageResolutionHours());
        assertEquals(4.8, stats.getAverageFeedbackRating());
        assertEquals(25L, stats.getTotalCitizens());
    }
}
