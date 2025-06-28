package com.licentarazu.turismapp.service;

import com.licentarazu.turismapp.model.*;
import com.licentarazu.turismapp.repository.OwnerApplicationRepository;
import com.licentarazu.turismapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class OwnerApplicationServiceTest {

    @Mock
    private OwnerApplicationRepository ownerApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OwnerApplicationService ownerApplicationService;

    private User testUser;
    private OwnerApplication testApplication;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(Role.GUEST);
        testUser.setOwnerStatus(OwnerStatus.NONE);

        testApplication = new OwnerApplication();
        testApplication.setId(1L);
        testApplication.setUser(testUser);
        testApplication.setMessage("I want to become an owner");
        testApplication.setStatus(OwnerStatus.PENDING);
        testApplication.setSubmittedAt(LocalDateTime.now());
    }

    @Test
    void testSubmitApplication_Success() {
        // Given
        when(ownerApplicationRepository.existsByUser(testUser)).thenReturn(false);
        when(ownerApplicationRepository.save(any(OwnerApplication.class))).thenReturn(testApplication);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        OwnerApplication result = ownerApplicationService.submitApplication(testUser, "I want to become an owner");

        // Then
        assertNotNull(result);
        assertEquals(OwnerStatus.PENDING, testUser.getOwnerStatus());
        verify(ownerApplicationRepository).save(any(OwnerApplication.class));
        verify(userRepository).save(testUser);
    }

    @Test
    void testSubmitApplication_UserAlreadyHasApplication() {
        // Given
        when(ownerApplicationRepository.existsByUser(testUser)).thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            ownerApplicationService.submitApplication(testUser, "I want to become an owner"));
        
        assertEquals("User already has a pending or processed application", exception.getMessage());
    }

    @Test
    void testSubmitApplication_UserAlreadyOwner() {
        // Given
        testUser.setRole(Role.OWNER);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            ownerApplicationService.submitApplication(testUser, "I want to become an owner"));
        
        assertEquals("User is already an owner", exception.getMessage());
    }

    @Test
    void testCanUserApply_UserCanApply() {
        // Given
        testUser.setRole(Role.GUEST);
        testUser.setOwnerStatus(OwnerStatus.NONE);

        // When
        boolean canApply = ownerApplicationService.canUserApply(testUser);

        // Then
        assertTrue(canApply);
    }

    @Test
    void testCanUserApply_UserIsAlreadyOwner() {
        // Given
        testUser.setRole(Role.OWNER);

        // When
        boolean canApply = ownerApplicationService.canUserApply(testUser);

        // Then
        assertFalse(canApply);
    }

    @Test
    void testCanUserApply_UserHasPendingApplication() {
        // Given
        testUser.setOwnerStatus(OwnerStatus.PENDING);

        // When
        boolean canApply = ownerApplicationService.canUserApply(testUser);

        // Then
        assertFalse(canApply);
    }

    @Test
    void testGetPendingApplications() {
        // Given
        List<OwnerApplication> pendingApplications = Arrays.asList(testApplication);
        when(ownerApplicationRepository.findByStatusOrderBySubmittedAtDesc(OwnerStatus.PENDING))
                .thenReturn(pendingApplications);

        // When
        List<OwnerApplication> result = ownerApplicationService.getPendingApplications();

        // Then
        assertEquals(1, result.size());
        assertEquals(testApplication, result.get(0));
    }

    @Test
    void testApproveApplication_Success() {
        // Given
        when(ownerApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(ownerApplicationRepository.save(testApplication)).thenReturn(testApplication);
        when(userRepository.save(testUser)).thenReturn(testUser);
        doNothing().when(emailService).sendOwnerApplicationApprovalEmail(anyString(), anyString());

        // When
        OwnerApplication result = ownerApplicationService.approveApplication(1L, "Application approved");

        // Then
        assertEquals(OwnerStatus.APPROVED, result.getStatus());
        assertEquals(Role.OWNER, testUser.getRole());
        assertEquals(OwnerStatus.APPROVED, testUser.getOwnerStatus());
        assertEquals("Application approved", result.getReviewNotes());
        assertNotNull(result.getReviewedAt());
        
        verify(emailService).sendOwnerApplicationApprovalEmail(testUser.getEmail(), "John Doe");
        verify(ownerApplicationRepository).save(testApplication);
        verify(userRepository).save(testUser);
    }

    @Test
    void testApproveApplication_ApplicationNotFound() {
        // Given
        when(ownerApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ownerApplicationService.approveApplication(1L, "Approved"));
        
        assertEquals("Application not found", exception.getMessage());
    }

    @Test
    void testApproveApplication_ApplicationNotPending() {
        // Given
        testApplication.setStatus(OwnerStatus.APPROVED);
        when(ownerApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            ownerApplicationService.approveApplication(1L, "Approved"));
        
        assertEquals("Application is not pending", exception.getMessage());
    }

    @Test
    void testRejectApplication_Success() {
        // Given
        when(ownerApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(ownerApplicationRepository.save(testApplication)).thenReturn(testApplication);
        when(userRepository.save(testUser)).thenReturn(testUser);
        doNothing().when(emailService).sendOwnerApplicationRejectionEmail(anyString(), anyString(), anyString());

        // When
        OwnerApplication result = ownerApplicationService.rejectApplication(1L, "Application rejected due to incomplete information");

        // Then
        assertEquals(OwnerStatus.REJECTED, result.getStatus());
        assertEquals(Role.GUEST, testUser.getRole()); // Should remain GUEST
        assertEquals(OwnerStatus.REJECTED, testUser.getOwnerStatus());
        assertEquals("Application rejected due to incomplete information", result.getReviewNotes());
        assertNotNull(result.getReviewedAt());
        
        verify(emailService).sendOwnerApplicationRejectionEmail(
            testUser.getEmail(), 
            "John Doe", 
            "Application rejected due to incomplete information"
        );
        verify(ownerApplicationRepository).save(testApplication);
        verify(userRepository).save(testUser);
    }

    @Test
    void testRejectApplication_ApplicationNotFound() {
        // Given
        when(ownerApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            ownerApplicationService.rejectApplication(1L, "Rejected"));
        
        assertEquals("Application not found", exception.getMessage());
    }

    @Test
    void testRejectApplication_ApplicationNotPending() {
        // Given
        testApplication.setStatus(OwnerStatus.REJECTED);
        when(ownerApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            ownerApplicationService.rejectApplication(1L, "Rejected"));
        
        assertEquals("Application is not pending", exception.getMessage());
    }

    @Test
    void testApproveApplication_EmailFailureDoesNotAffectApproval() {
        // Given
        when(ownerApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(ownerApplicationRepository.save(testApplication)).thenReturn(testApplication);
        when(userRepository.save(testUser)).thenReturn(testUser);
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendOwnerApplicationApprovalEmail(anyString(), anyString());

        // When
        OwnerApplication result = ownerApplicationService.approveApplication(1L, "Application approved");

        // Then - Application should still be approved despite email failure
        assertEquals(OwnerStatus.APPROVED, result.getStatus());
        assertEquals(Role.OWNER, testUser.getRole());
        assertEquals(OwnerStatus.APPROVED, testUser.getOwnerStatus());
        
        verify(emailService).sendOwnerApplicationApprovalEmail(testUser.getEmail(), "John Doe");
        verify(ownerApplicationRepository).save(testApplication);
        verify(userRepository).save(testUser);
    }

    @Test
    void testRejectApplication_EmailFailureDoesNotAffectRejection() {
        // Given
        when(ownerApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(ownerApplicationRepository.save(testApplication)).thenReturn(testApplication);
        when(userRepository.save(testUser)).thenReturn(testUser);
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendOwnerApplicationRejectionEmail(anyString(), anyString(), anyString());

        // When
        OwnerApplication result = ownerApplicationService.rejectApplication(1L, "Application rejected");

        // Then - Application should still be rejected despite email failure
        assertEquals(OwnerStatus.REJECTED, result.getStatus());
        assertEquals(Role.GUEST, testUser.getRole());
        assertEquals(OwnerStatus.REJECTED, testUser.getOwnerStatus());
        
        verify(emailService).sendOwnerApplicationRejectionEmail(testUser.getEmail(), "John Doe", "Application rejected");
        verify(ownerApplicationRepository).save(testApplication);
        verify(userRepository).save(testUser);
    }

    @Test
    void testGetApplicationByUser() {
        // Given
        when(ownerApplicationRepository.findByUser(testUser)).thenReturn(Optional.of(testApplication));

        // When
        Optional<OwnerApplication> result = ownerApplicationService.getApplicationByUser(testUser);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testApplication, result.get());
    }

    @Test
    void testGetAllApplications() {
        // Given
        List<OwnerApplication> allApplications = Arrays.asList(testApplication);
        when(ownerApplicationRepository.findAll()).thenReturn(allApplications);

        // When
        List<OwnerApplication> result = ownerApplicationService.getAllApplications();

        // Then
        assertEquals(1, result.size());
        assertEquals(testApplication, result.get(0));
    }
}
