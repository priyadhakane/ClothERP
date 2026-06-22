package com.clotherp.backend.modules.sales;

import com.clotherp.backend.modules.accounting.AccountingService;
import com.clotherp.backend.modules.branch.Branch;
import com.clotherp.backend.modules.branch.BranchRepository;
import com.clotherp.backend.modules.customer.Customer;
import com.clotherp.backend.modules.customer.CustomerRepository;
import com.clotherp.backend.modules.inventory.InventoryService;
import com.clotherp.backend.modules.inventory.InventoryTransactionType;
import com.clotherp.backend.modules.inventory.StockAdjustmentRequest;
import com.clotherp.backend.modules.product.Product;
import com.clotherp.backend.modules.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final AccountingService accountingService;

    @Override
    public SalesOrderDTO createSalesOrder(CreateSalesOrderRequest request) {
        // 1. Create order entity skeleton
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", (int)(Math.random() * 10000));
        String orderNumber = "INV-" + dateStr + "-" + randomSuffix;

        SalesOrder order = SalesOrder.builder()
                .orderNumber(orderNumber)
                .customerId(request.getCustomerId())
                .branchId(request.getBranchId())
                .status(SalesOrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CASH)
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<SaleItem> saleItems = new ArrayList<>();

        // 2. Validate products and calculate line totals
        for (CreateSaleItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemReq.getProductId()));

            // Line discount math
            BigDecimal discountDiv = itemReq.getDiscountPercent().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal discountedPrice = itemReq.getUnitPrice().multiply(BigDecimal.ONE.subtract(discountDiv));
            BigDecimal lineTotal = discountedPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            subtotal = subtotal.add(lineTotal);

            SaleItem item = SaleItem.builder()
                    .salesOrder(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .discountPercent(itemReq.getDiscountPercent())
                    .lineTotal(lineTotal)
                    .build();

            saleItems.add(item);
        }

        order.setItems(saleItems);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(request.getDiscountAmount());
        order.setTaxAmount(request.getTaxAmount());

        BigDecimal totalAmount = subtotal.subtract(request.getDiscountAmount()).add(request.getTaxAmount());
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }
        order.setTotalAmount(totalAmount);

        // Save order first to generate its ID for foreign key usage in references
        SalesOrder savedOrder = salesOrderRepository.save(order);

        // 3. Stock deduction
        for (SaleItem item : savedOrder.getItems()) {
            StockAdjustmentRequest adjReq = StockAdjustmentRequest.builder()
                    .productId(item.getProduct().getId())
                    .branchId(savedOrder.getBranchId())
                    .quantity(-item.getQuantity())
                    .type(InventoryTransactionType.STOCK_OUT)
                    .referenceId(savedOrder.getId())
                    .notes("POS sale " + savedOrder.getOrderNumber())
                    .build();
            inventoryService.adjustStock(adjReq);
        }

        // 4. Update Customer loyalty points (1 point per 100 spent)
        if (savedOrder.getCustomerId() != null) {
            customerRepository.findById(savedOrder.getCustomerId()).ifPresent(customer -> {
                int earnedPoints = totalAmount.divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).intValue();
                customer.setLoyaltyPoints(customer.getLoyaltyPoints() + earnedPoints);
                customerRepository.save(customer);
            });
        }

        // 5. Accounting Hook (Automatic double-entry Journal Posting)
        accountingService.postSalesJournalEntry(savedOrder);

        return toDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderDTO getSalesOrderById(UUID id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sales order not found: " + id));
        return toDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalesOrderDTO> getSalesOrdersByBranch(UUID branchId, Pageable pageable) {
        return salesOrderRepository.findByBranchId(branchId, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalesOrderDTO> getAllSalesOrders(Pageable pageable) {
        return salesOrderRepository.findAll(pageable).map(this::toDTO);
    }

    private SalesOrderDTO toDTO(SalesOrder order) {
        String customerName = "Walk-in Customer";
        String customerPhone = "N/A";
        if (order.getCustomerId() != null) {
            Customer customer = customerRepository.findById(order.getCustomerId()).orElse(null);
            if (customer != null) {
                customerName = customer.getFullName();
                customerPhone = customer.getPhone();
            }
        }

        String branchName = "Default Branch";
        Branch branch = branchRepository.findById(order.getBranchId()).orElse(null);
        if (branch != null) {
            branchName = branch.getName();
        }

        List<SaleItemDTO> itemDTOs = order.getItems().stream().map(item -> SaleItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountPercent(item.getDiscountPercent())
                .lineTotal(item.getLineTotal())
                .build()).collect(Collectors.toList());

        return SalesOrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .customerName(customerName)
                .customerMobile(customerPhone)
                .branchId(order.getBranchId())
                .branchName(branchName)
                .status(order.getStatus())
                .statusLabel(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus())
                .paymentStatusLabel(order.getPaymentStatus().name())
                .paymentMethod(order.getPaymentMethod())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .paidAmount(order.getTotalAmount()) // immediate fully paid for POS
                .balanceDue(BigDecimal.ZERO)
                .notes(order.getNotes())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDTOs)
                .build();
    }
}
