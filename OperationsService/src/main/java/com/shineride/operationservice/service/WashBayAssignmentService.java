package com.shineride.operationservice.service;

import com.shineride.operationservice.dto.WashBayAssignmentRequestDTO;
import com.shineride.operationservice.dto.WashBayStatusUpdateDTO;
import com.shineride.operationservice.entity.WashBayAssignmentEntity;

import java.util.List;

public interface WashBayAssignmentService {

    // ✅ SYSTEM/ADMIN triggers assignment (washbay picked automatically)
    WashBayAssignmentEntity assignWashBay(WashBayAssignmentRequestDTO dto);

    // ✅ ADMIN
    List<WashBayAssignmentEntity> getAllAssignments();

    // ✅ EMPLOYEE: only their assignments
    List<WashBayAssignmentEntity> getAssignmentsByEmployee(String employeeEmail);

    // ✅ EMPLOYEE: update status ONLY if assigned employee
    WashBayAssignmentEntity updateStatus(String assignmentId, WashBayStatusUpdateDTO dto, String employeeEmail);

    WashBayAssignmentEntity getAssignmentByBooking(String bookingId);
}