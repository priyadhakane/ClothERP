package com.clotherp.backend.modules.branch;

import java.util.List;
import java.util.UUID;

public interface BranchService {
    List<Branch> getAllActiveBranches();
    Branch createBranch(Branch branch);
    Branch getBranchById(UUID id);
}
