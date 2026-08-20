package com.pphgreen.backend.admin.dto;

import jakarta.validation.constraints.Size;

public record AdminApprovalRequest(
        @Size(max = 500, message = "Note must be at most 500 characters")
        String note) {
}