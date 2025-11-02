package org.example.Amazon;

import org.example.Amazon.Cost.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AmazonUnitTest {

    private ShoppingCart cart; // mocked
    private PriceRule regular;
    private PriceRule delivery;
    private PriceRule extraElectronics;

    private Item book1;
    private Item book2;
    private Item phone;

    @BeforeEach
    void setUp() {
        cart = Mockito.mock(ShoppingCart.class);
        regular = new RegularCost();
        delivery = new DeliveryPrice();
        extraElectronics = new ExtraCostForElectronics();

        book1 = new Item(ItemType.OTHER, "Book A", 2, 10.0);   // 20.0
        book2 = new Item(ItemType.OTHER, "Book B", 1, 15.0);   // 15.0
        phone = new Item(ItemType.ELECTRONIC, "Phone", 1, 300.0); // 300.0
    }

    @Test
    @DisplayName("specification-based")
    void spec_calculates_total_with_regular_delivery_and_electronics_fee() {
        // Cart: 3 items (book1, book2, phone)
        when(cart.getItems()).thenReturn(List.of(book1, book2, phone));

        Amazon amazon = new Amazon(cart, List.of(regular, delivery, extraElectronics));
        double total = amazon.calculate();

        // Regular cost = 20 + 15 + 300 = 335
        // Delivery (3 items) = 5
        // Electronics fee (present) = 7.5
        assertThat(total).isEqualTo(335 + 5 + 7.5);
    }

    @Test
    @DisplayName("structural-based")
    void structural_delivery_price_boundaries_and_electronics_branching() {
        // Boundary checks for DeliveryPrice: 0, 1, 3, 4, 10, 11 items
        double d0 = new DeliveryPrice().priceToAggregate(List.of());
        assertThat(d0).isEqualTo(0.0);

        var one = new Item(ItemType.OTHER, "X", 1, 1.0);
        double d1 = new DeliveryPrice().priceToAggregate(List.of(one));
        assertThat(d1).isEqualTo(5.0);

        var a = new Item(ItemType.OTHER, "A", 1, 1.0);
        var b = new Item(ItemType.OTHER, "B", 1, 1.0);
        double d3 = new DeliveryPrice().priceToAggregate(List.of(one, a, b));
        assertThat(d3).isEqualTo(5.0);

        // 4 items → 12.5
        double d4 = new DeliveryPrice().priceToAggregate(List.of(one, a, b, new Item(ItemType.OTHER, "C", 1, 1.0)));
        assertThat(d4).isEqualTo(12.5);

        // 10 items → 12.5
        var ten = List.of(one, a, b,
                new Item(ItemType.OTHER, "C", 1, 1.0),
                new Item(ItemType.OTHER, "D", 1, 1.0),
                new Item(ItemType.OTHER, "E", 1, 1.0),
                new Item(ItemType.OTHER, "F", 1, 1.0),
                new Item(ItemType.OTHER, "G", 1, 1.0),
                new Item(ItemType.OTHER, "H", 1, 1.0),
                new Item(ItemType.OTHER, "I", 1, 1.0));
        double d10 = new DeliveryPrice().priceToAggregate(ten);
        assertThat(d10).isEqualTo(12.5);

        // 11 items → 20.0
        double d11 = new DeliveryPrice().priceToAggregate(
                new java.util.ArrayList<>(ten) {{ add(new Item(ItemType.OTHER, "J", 1, 1.0)); }});
        assertThat(d11).isEqualTo(20.0);

        // ExtraCostForElectronics: both branches
        double extra0 = new ExtraCostForElectronics().priceToAggregate(List.of(a, b));
        assertThat(extra0).isEqualTo(0.0);

        double extra7_5 = new ExtraCostForElectronics().priceToAggregate(List.of(a, new Item(ItemType.ELECTRONIC, "TV", 1, 100.0)));
        assertThat(extra7_5).isEqualTo(7.50);

        // RegularCost: sum of qty * unit price
        double reg = new RegularCost().priceToAggregate(
                List.of(new Item(ItemType.OTHER, "P", 3, 2.0), new Item(ItemType.OTHER, "Q", 2, 5.0)));
        assertThat(reg).isEqualTo(3 * 2.0 + 2 * 5.0);
    }

    @Test
    @DisplayName("specification-based")
    void spec_addToCart_delegates_to_cart() {
        Amazon amazon = new Amazon(cart, List.of(regular));
        amazon.addToCart(book1);
        verify(cart, times(1)).add(book1);
    }
}
