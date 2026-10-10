package com.municipal.repository;

import com.municipal.entity.Complaint;
import com.municipal.entity.ComplaintAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintAssignmentRepository extends JpaRepository<ComplaintAssignment, Long> {
    List<ComplaintAssignment> findByComplaintOrderByAssignedAtDesc(Complaint complaint);
}
