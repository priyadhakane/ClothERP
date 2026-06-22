package com.clotherp.backend.modules.accounting;

import com.clotherp.backend.modules.sales.SalesOrder;

import java.util.List;

public interface AccountingService {
    void postSalesJournalEntry(SalesOrder salesOrder);
    List<AccountDTO> getChartOfAccounts();
    List<JournalEntryDTO> getJournalEntries();
    IncomeStatementDTO getIncomeStatement();
}
