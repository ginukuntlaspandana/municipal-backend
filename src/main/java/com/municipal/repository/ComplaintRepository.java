package com.municipal.repository;

import com.municipal.entity.Complaint;
import com.municipal.entity.ComplaintStatus;
import com.municipal.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {

    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    List<Complaint> findByCitizenOrderByCreatedAtDesc(User citizen);

    Page<Complaint> findByCitizen(User citizen, Pageable pageable);

    long countByStatus(ComplaintStatus status);

    @Query("SELECT c.category.name, COUNT(c) FROM Complaint c GROUP BY c.category.name")
    List<Object[]> countComplaintsByCategory();

    @Query("SELECT c.locality, COUNT(c) FROM Complaint c GROUP BY c.locality ORDER BY COUNT(c) DESC")
    List<Object[]> countComplaintsByLocality();

    @Query(value = "SELECT DATE_FORMAT(c.created_at, '%Y-%m') as month, COUNT(*) as cnt FROM complaints c GROUP BY DATE_FORMAT(c.created_at, '%Y-%m') ORDER BY month ASC", nativeQuery = true)
    List<Object[]> countComplaintsByMonth();

    @Query(value = "SELECT AVG(TIMESTAMPDIFF(HOUR, c.created_at, c.resolved_at)) FROM complaints c WHERE c.status = 'RESOLVED' AND c.resolved_at IS NOT NULL", nativeQuery = true)
    Double getAverageResolutionHours();
}
