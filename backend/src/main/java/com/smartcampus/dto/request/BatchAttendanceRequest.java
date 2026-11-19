package com.smartcampus.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAttendanceRequest {

    @NotNull(message = "Subject ID is mandatory")
    private Long subjectId;

    @NotNull(message = "Attendance date is mandatory")
    private LocalDate attendanceDate;

    @NotEmpty(message = "Attendance marks list cannot be empty")
    @Valid
    private List<AttendanceMarkItem> records;
}
