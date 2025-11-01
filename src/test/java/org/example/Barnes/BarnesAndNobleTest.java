package org.example.Barnes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for BarnesAndNoble:
 * - Specification-based: assert externally visible behavior (return values, calls)
 * - Structural-based: drive paths/branches (null/empty/multiple items/zero qty)
 */
class BarnesAndNobleTest {

    private BookDatabase bookDatabase;
    private BuyBookProcess process;
    private BarnesAndNoble sut;

    private static Book book(String isbn, int price, int qty) {
        return new Book(isbn, price, qty);
    }

    @BeforeEach
    void setUp() {
        bookDatabase = mock(BookDatabase.class);
        process = mock(BuyBookProcess.class);
        sut = new BarnesAndNoble(bookDatabase, process);
    }

    // ---------- SPECIFICATION-BASED TESTS ----------

    @Test
    @DisplayName("specification-based: returns null when order is null")
    void returnsNullWhenOrderIsNull() {
        assertNull(sut.getPriceForCart(null));
        verifyNoInteractions(bookDatabase, process);
    }

    @Test
    @DisplayName("specification-based: single ISBN → total = qty * price and buyBook called with same qty")
    void singleIsbnTotalsAndBuysCorrectly() {
        // Arrange
        final String isbn = "111";
        final int price = 25;
        final int qty = 3;
        when(bookDatabase.findByISBN(isbn)).thenReturn(book(isbn, price, /*stock*/ 10));

        Map<String,Integer> order = new HashMap<>();
        order.put(isbn, qty);

        // Act
        PurchaseSummary summary = sut.getPriceForCart(order);

        // Assert
        assertNotNull(summary);
        assertEquals(qty * price, summary.getTotalPrice());
        assertTrue(summary.getUnavailable().isEmpty(), "No unavailability expected for this happy path");

        // Verify buyBook call
        ArgumentCaptor<Book> bookCap = ArgumentCaptor.forClass(Book.class);
        ArgumentCaptor<Integer> qtyCap = ArgumentCaptor.forClass(Integer.class);
        verify(process, times(1)).buyBook(bookCap.capture(), qtyCap.capture());
        assertEquals(isbn, bookCap.getValue().equals(book(isbn, price, 10)) ? isbn : bookCap.getValue().equals(null));
        assertEquals(qty, qtyCap.getValue());
    }

    @Test
    @DisplayName("specification-based: multiple ISBNs → total is sum of each (qty * price)")
    void multipleIsbnsTotalsCorrectly() {
        // Arrange
        when(bookDatabase.findByISBN("A")).thenReturn(book("A", 10, 5));
        when(bookDatabase.findByISBN("B")).thenReturn(book("B", 40, 5));

        Map<String,Integer> order = new HashMap<>();
        order.put("A", 2); // 2 * 10
        order.put("B", 1); // 1 * 40

        // Act
        PurchaseSummary summary = sut.getPriceForCart(order);

        // Assert
        assertEquals(60, summary.getTotalPrice()); // 20 + 40
        verify(process, times(1)).buyBook(any(Book.class), eq(2));
        verify(process, times(1)).buyBook(any(Book.class), eq(1));
        verify(process, times(2)).buyBook(any(Book.class), anyInt());
    }

    // ---------- STRUCTURAL-BASED TESTS ----------

    @Test
    @DisplayName("structural-based: empty order → total = 0 and no buyBook calls")
    void emptyOrderTotalsZeroAndNoPurchases() {
        PurchaseSummary summary = sut.getPriceForCart(new HashMap<>());
        assertNotNull(summary);
        assertEquals(0, summary.getTotalPrice());
        verifyNoInteractions(process);
    }

    @Test
    @DisplayName("structural-based: zero-quantity line item still exercises path without changing total")
    void zeroQuantityItemDoesNotChangeTotal() {
        // Arrange
        when(bookDatabase.findByISBN("Z")).thenReturn(book("Z", 99, 100));
        Map<String,Integer> order = new HashMap<>();
        order.put("Z", 0);

        // Act
        PurchaseSummary summary = sut.getPriceForCart(order);

        // Assert
        assertEquals(0, summary.getTotalPrice());
        // Depending on implementation, buyBook(…,0) may or may not be called;
        // assert zero or one call safely by allowing either:
        verify(process, atMostOnce()).buyBook(any(Book.class), eq(0));
    }

    @Test
    @DisplayName("structural-based: iterates over >1 keys and calls process per key")
    void iteratesOverMultipleIsbnsAndCallsProcessPerKey() {
        when(bookDatabase.findByISBN("X")).thenReturn(book("X", 5, 10));
        when(bookDatabase.findByISBN("Y")).thenReturn(book("Y", 7, 10));
        when(bookDatabase.findByISBN("W")).thenReturn(book("W", 0, 10));

        Map<String,Integer> order = new HashMap<>();
        order.put("X", 1);
        order.put("Y", 2);
        order.put("W", 3);

        PurchaseSummary summary = sut.getPriceForCart(order);

        assertEquals((1*5) + (2*7) + (3*0), summary.getTotalPrice());
        verify(process, times(1)).buyBook(argThat(b -> b.equals(book("X", 5, 10))), eq(1));
        verify(process, times(1)).buyBook(argThat(b -> b.equals(book("Y", 7, 10))), eq(2));
        verify(process, times(1)).buyBook(argThat(b -> b.equals(book("W", 0, 10))), eq(3));
        verify(process, times(3)).buyBook(any(Book.class), anyInt());
    }
}