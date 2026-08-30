package com.clevstack.clevbill.repository;

import com.clevstack.clevbill.model.SaleItem;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    /**
     * Native SQL throughout this reporting block, with explicit casts on
     * every parameter — Postgres can't infer a bind parameter's type when
     * its only appearance in a branch is an {@code IS NULL} check (error
     * 42P18), which JPQL's {@code :param is null or ...} filter pattern
     * triggers here. propertyId is a hard filter, never optional — a
     * report always belongs to exactly one property (see
     * PropertyAccessService for who's allowed to ask about which one).
     */
    @Query(
            value =
                    """
                    select count(distinct s.id) as sale_count,
                           coalesce(sum(si.line_subtotal), 0) as total_subtotal,
                           coalesce(sum(si.cgst_amount + si.sgst_amount + si.igst_amount), 0) as total_tax,
                           coalesce(sum(si.line_total), 0) as total_revenue
                    from sale_items si
                    join sales s on s.id = si.sale_id
                    join items i on i.id = si.item_id
                    where s.property_id = :propertyId
                      and (cast(:from as timestamp) is null or s.created_at >= cast(:from as timestamp))
                      and (cast(:to as timestamp) is null or s.created_at <= cast(:to as timestamp))
                      and (cast(:cashierId as bigint) is null or s.cashier_id = cast(:cashierId as bigint))
                      and (cast(:categoryId as bigint) is null or i.category_id = cast(:categoryId as bigint))
                      and (cast(:itemId as bigint) is null or i.id = cast(:itemId as bigint))
                    """,
            nativeQuery = true)
    List<Object[]> summarize(
            @Param("propertyId") Long propertyId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("cashierId") Long cashierId,
            @Param("categoryId") Long categoryId,
            @Param("itemId") Long itemId);

    @Query(
            value =
                    """
                    select i.id, i.sku, i.name,
                           coalesce(sum(si.quantity), 0) as quantity_sold,
                           coalesce(sum(si.line_total), 0) as revenue
                    from sale_items si
                    join sales s on s.id = si.sale_id
                    join items i on i.id = si.item_id
                    where s.property_id = :propertyId
                      and (cast(:from as timestamp) is null or s.created_at >= cast(:from as timestamp))
                      and (cast(:to as timestamp) is null or s.created_at <= cast(:to as timestamp))
                      and (cast(:cashierId as bigint) is null or s.cashier_id = cast(:cashierId as bigint))
                      and (cast(:categoryId as bigint) is null or i.category_id = cast(:categoryId as bigint))
                      and (cast(:itemId as bigint) is null or i.id = cast(:itemId as bigint))
                    group by i.id, i.sku, i.name
                    order by revenue desc
                    """,
            nativeQuery = true)
    List<Object[]> summarizeByItem(
            @Param("propertyId") Long propertyId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("cashierId") Long cashierId,
            @Param("categoryId") Long categoryId,
            @Param("itemId") Long itemId);

    @Query(
            value =
                    """
                    select date_trunc('day', s.created_at) as day,
                           count(distinct s.id) as sale_count,
                           coalesce(sum(si.line_total), 0) as revenue
                    from sale_items si
                    join sales s on s.id = si.sale_id
                    join items i on i.id = si.item_id
                    where s.property_id = :propertyId
                      and (cast(:from as timestamp) is null or s.created_at >= cast(:from as timestamp))
                      and (cast(:to as timestamp) is null or s.created_at <= cast(:to as timestamp))
                      and (cast(:cashierId as bigint) is null or s.cashier_id = cast(:cashierId as bigint))
                      and (cast(:categoryId as bigint) is null or i.category_id = cast(:categoryId as bigint))
                      and (cast(:itemId as bigint) is null or i.id = cast(:itemId as bigint))
                    group by day
                    order by day
                    """,
            nativeQuery = true)
    List<Object[]> summarizeByDay(
            @Param("propertyId") Long propertyId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("cashierId") Long cashierId,
            @Param("categoryId") Long categoryId,
            @Param("itemId") Long itemId);
}
