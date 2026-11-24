package com.smartcampus.repository;

import com.smartcampus.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    @Query("SELECT a FROM Announcement a WHERE " +
           "(:deptId IS NULL OR a.targetDepartment.id IS NULL OR a.targetDepartment.id = :deptId) AND " +
           "(:year IS NULL OR a.targetYear IS NULL OR a.targetYear = :year) AND " +
           "(:sem IS NULL OR a.targetSemester IS NULL OR a.targetSemester = :sem) " +
           "ORDER BY a.createdAt DESC")
    Page<Announcement> findTargetedAnnouncements(
            @Param("deptId") Long deptId,
            @Param("year") Integer year,
            @Param("sem") Integer sem,
            Pageable pageable);
}
