package com.smartcampus.dto.request;

import com.smartcampus.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceMarkItem {

    @NotNull(message = "Student ID is mandatory")
    private Long studentId;

    @NotNull(message = "Attendance status is mandatory (PRESENT, ABSENT, ON_LEAVE)")
    private AttendanceStatus status;

    private String remarks;
}
