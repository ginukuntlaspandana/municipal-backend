package com.municipal.service;

import com.municipal.dto.request.ComplaintCreateRequest;
import com.municipal.dto.request.FeedbackRequest;
import com.municipal.dto.request.ReopenRequest;
import com.municipal.dto.response.ComplaintDetailDto;
import com.municipal.dto.response.FeedbackDto;
import com.municipal.entity.*;
import com.municipal.exception.BadRequestException;
import com.municipal.exception.UnauthorizedException;
import com.municipal.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ComplaintStatusHistoryRepository historyRepository;

    @Mock
    private ComplaintAssignmentRepository assignmentRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private AdministrativeRemarkRepository remarkRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ComplaintService complaintService;

    private User sampleCitizen;
    private User otherCitizen;
    private Category sampleCategory;
    private Complaint sampleComplaint;

    @BeforeEach
    void setUp() {
        sampleCitizen = new User("Rajesh Kumar", "rajesh@example.com", "pass", "9876543210", "Indiranagar", Role.ROLE_CITIZEN);
        sampleCitizen.setId(1L);

        otherCitizen = new User("Other Person", "other@example.com", "pass", "9999999999", "Koramangala", Role.ROLE_CITIZEN);
        otherCitizen.setId(2L);

        sampleCategory = new Category("Roads & Potholes", "Road maintenance", "Car");
        sampleCategory.setId(1L);

        sampleComplaint = new Complaint();
        sampleComplaint.setId(100L);
        sampleComplaint.setComplaintNumber("MUNIC-2026-0001");
        sampleComplaint.setTitle("Pothole on Main Road");
        sampleComplaint.setDescription("Deep pothole causing accidents");
        sampleComplaint.setCategory(sampleCategory);
        sampleComplaint.setCitizen(sampleCitizen);
        sampleComplaint.setStatus(ComplaintStatus.SUBMITTED);
        sampleComplaint.setPriority(ComplaintPriority.MEDIUM);
        sampleComplaint.setLocality("Indiranagar");
        sampleComplaint.setAddress("12th Main Road");
    }

    @Test
    void testCreateComplaintSuccess() {
        ComplaintCreateRequest req = new ComplaintCreateRequest();
        req.setTitle("Pothole on Main Road");
        req.setDescription("Deep pothole causing accidents");
        req.setCategoryId(1L);
        req.setLocality("Indiranagar");
        req.setAddress("12th Main Road");

        when(authService.getCurrentAuthenticatedUser()).thenReturn(sampleCitizen);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(complaintRepository.count()).thenReturn(0L);
        when(complaintRepository.findByComplaintNumber(anyString())).thenReturn(Optional.empty());
        when(complaintRepository.save(any(Complaint.class))).thenReturn(sampleComplaint);

        ComplaintDetailDto result = complaintService.createComplaint(req, null);

        assertNotNull(result);
        assertEquals("MUNIC-2026-0001", result.getComplaintNumber());
        assertEquals("Pothole on Main Road", result.getTitle());
        verify(historyRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    void testReopenComplaintSuccess() {
        sampleComplaint.setStatus(ComplaintStatus.RESOLVED);
        ReopenRequest req = new ReopenRequest();
        req.setReason("The pothole was not filled completely and has re-appeared.");

        when(authService.getCurrentAuthenticatedUser()).thenReturn(sampleCitizen);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(sampleComplaint);

        ComplaintDetailDto result = complaintService.reopenComplaint(100L, req);

        assertNotNull(result);
        assertEquals(ComplaintStatus.REOPENED, result.getStatus());
        verify(historyRepository, times(1)).save(any(ComplaintStatusHistory.class));
    }

    @Test
    void testReopenComplaintByNonOwnerFails() {
        sampleComplaint.setStatus(ComplaintStatus.RESOLVED);
        ReopenRequest req = new ReopenRequest();
        req.setReason("Some reason");

        when(authService.getCurrentAuthenticatedUser()).thenReturn(otherCitizen);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));

        assertThrows(UnauthorizedException.class, () -> complaintService.reopenComplaint(100L, req));
    }

    @Test
    void testReopenNonResolvedComplaintFails() {
        sampleComplaint.setStatus(ComplaintStatus.IN_PROGRESS);
        ReopenRequest req = new ReopenRequest();
        req.setReason("Some reason");

        when(authService.getCurrentAuthenticatedUser()).thenReturn(sampleCitizen);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));

        assertThrows(BadRequestException.class, () -> complaintService.reopenComplaint(100L, req));
    }

    @Test
    void testSubmitFeedbackSuccess() {
        sampleComplaint.setStatus(ComplaintStatus.RESOLVED);
        FeedbackRequest req = new FeedbackRequest();
        req.setRating(5);
        req.setComments("Prompt and effective resolution!");

        Feedback feedback = new Feedback(sampleComplaint, sampleCitizen, 5, "Prompt and effective resolution!");
        feedback.setId(10L);

        when(authService.getCurrentAuthenticatedUser()).thenReturn(sampleCitizen);
        when(complaintRepository.findById(100L)).thenReturn(Optional.of(sampleComplaint));
        when(feedbackRepository.existsByComplaintId(100L)).thenReturn(false);
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

        FeedbackDto result = complaintService.submitFeedback(100L, req);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Prompt and effective resolution!", result.getComments());
    }
}
