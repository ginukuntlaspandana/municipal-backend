package com.municipal.repository;

import com.municipal.entity.AdministrativeRemark;
import com.municipal.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdministrativeRemarkRepository extends JpaRepository<AdministrativeRemark, Long> {
    List<AdministrativeRemark> findByComplaintOrderByCreatedAtDesc(Complaint complaint);
    List<AdministrativeRemark> findByComplaintAndInternalOnlyFalseOrderByCreatedAtDesc(Complaint complaint);
}
