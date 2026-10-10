package com.municipal.repository;

import com.municipal.entity.Complaint;
import com.municipal.entity.ComplaintStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, Long> {
    List<ComplaintStatusHistory> findByComplaintOrderByTimestampDesc(Complaint complaint);
}
