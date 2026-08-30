package com.clevstack.clevbill.service;

import com.clevstack.clevbill.dto.DailySalesResponse;
import com.clevstack.clevbill.dto.ItemSalesResponse;
import com.clevstack.clevbill.dto.SalesSummaryResponse;
import com.clevstack.clevbill.exception.ResourceNotFoundException;
import com.clevstack.clevbill.model.User;
import com.clevstack.clevbill.repository.SaleItemRepository;
import com.clevstack.clevbill.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only sales reporting, filtered by date range / cashier / category /
 * item (see the reporting skill), always scoped to one property — see
 * PropertyAccessService for who's allowed to ask about which property.
 * Reads from the same live database checkout writes to — no separate read
 * replica at this scale.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final SaleItemRepository saleItemRepository;
    private final UserRepository userRepository;
    private final PropertyAccessService propertyAccessService;

    public ReportService(
            SaleItemRepository saleItemRepository,
            UserRepository userRepository,
            PropertyAccessService propertyAccessService) {
        this.saleItemRepository = saleItemRepository;
        this.userRepository = userRepository;
        this.propertyAccessService = propertyAccessService;
    }

    public SalesSummaryResponse getSummary(
            Long propertyId, Instant from, Instant to, Long cashierId, Long categoryId, Long itemId, String username) {
        requireAccess(propertyId, username);
        Object[] row =
                saleItemRepository.summarize(propertyId, from, to, cashierId, categoryId, itemId).get(0);

        long saleCount = ((Number) row[0]).longValue();
        BigDecimal totalSubtotal = (BigDecimal) row[1];
        BigDecimal totalTax = (BigDecimal) row[2];
        BigDecimal totalRevenue = (BigDecimal) row[3];
        BigDecimal averageSaleValue = saleCount == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : totalRevenue.divide(BigDecimal.valueOf(saleCount), 2, RoundingMode.HALF_UP);

        return new SalesSummaryResponse(saleCount, totalSubtotal, totalTax, totalRevenue, averageSaleValue);
    }

    public List<ItemSalesResponse> getByItem(
            Long propertyId, Instant from, Instant to, Long cashierId, Long categoryId, Long itemId, String username) {
        requireAccess(propertyId, username);
        return saleItemRepository.summarizeByItem(propertyId, from, to, cashierId, categoryId, itemId).stream()
                .map(row -> new ItemSalesResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        (String) row[2],
                        (BigDecimal) row[3],
                        (BigDecimal) row[4]))
                .toList();
    }

    public List<DailySalesResponse> getByDay(
            Long propertyId, Instant from, Instant to, Long cashierId, Long categoryId, Long itemId, String username) {
        requireAccess(propertyId, username);
        return saleItemRepository.summarizeByDay(propertyId, from, to, cashierId, categoryId, itemId).stream()
                .map(row -> new DailySalesResponse(
                        toLocalDate(row[0]), ((Number) row[1]).longValue(), (BigDecimal) row[2]))
                .toList();
    }

    private void requireAccess(Long propertyId, String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        if (!propertyAccessService.hasAccess(user, propertyId)) {
            throw new AccessDeniedException("No access to property " + propertyId);
        }
    }

    private LocalDate toLocalDate(Object dayColumn) {
        if (dayColumn instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        }
        if (dayColumn instanceof Instant instant) {
            return instant.atZone(ZoneOffset.UTC).toLocalDate();
        }
        if (dayColumn instanceof java.time.LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (dayColumn instanceof LocalDate localDate) {
            return localDate;
        }
        throw new IllegalStateException("Unexpected day column type: " + dayColumn.getClass());
    }
}
