package com.clotherp.backend.modules.accounting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryDTO {
    private UUID id;
    private String entryNumber;
    private LocalDateTime transactionDate;
    private UUID referenceId;
    private String description;
    private String status;
    private List<JournalItemDTO> items;
}
