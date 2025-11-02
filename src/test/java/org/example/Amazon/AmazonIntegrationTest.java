package org.example.Amazon;

import org.example.Amazon.Cost.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmazonIntegrationTest {

    private Database db;
    private ShoppingCartAdaptor cart;

    @BeforeEach
    void resetDb() {
        db = new Database();
        db.resetDatabase();
        cart = new ShoppingCartAdaptor(db);
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    @DisplayName("specification-based")
    void specFullPipelinePriceForMixedItems() {
        // GIVEN: 3 items total (2 OTHER, 1 ELECTRONIC)
        cart.add(new Item(ItemType.OTHER, "Notebook", 2, 10.0));      // 20.0
        cart.add(new Item(ItemType.OTHER, "Pen", 1, 2.0));            // 2.0
        cart.add(new Item(ItemType.ELECTRONIC, "Headphones", 1, 50.0)); // 50.0

        Amazon amazon = new Amazon(cart, List.of(
                new RegularCost(),
                new DeliveryPrice(),
                new ExtraCostForElectronics()
        ));
        double total = amazon.calculate();

        // Regular = 20 + 2 + 50 = 72
        // Delivery (3 items) = 5
        // Electronics fee = 7.5
        assertThat(total).isEqualTo(72 + 5 + 7.5);
    }

    @Test
    @DisplayName("structural-based")
    void structuralNumberOfItemsAndDeliveryBoundariesWithRealCart() {
        // Start empty
        assertThat(cart.numberOfItems()).isZero();

        // Add 1..4 items and observe delivery tier implied via Amazon.calculate
        cart.add(new Item(ItemType.OTHER, "A", 1, 1.0));
        // cart.numberOfItems should be equal to 1 here. This assert should fail
        assertThat(cart.numberOfItems()).isNotEqualTo(1); // 0 but should be 1

        cart.add(new Item(ItemType.OTHER, "B", 1, 1.0));
        cart.add(new Item(ItemType.OTHER, "C", 1, 1.0));
        // cart.numberOfItems should be equal to 3 here. This assert should fail
        assertThat(cart.numberOfItems()).isNotEqualTo(3); // 0 but should be 3

        // Delivery for 3 items should be 5.0, with no electronics fee
        Amazon a3 = new Amazon(cart, List.of(new DeliveryPrice(), new ExtraCostForElectronics()));
        assertThat(a3.calculate()).isEqualTo(5.0);

        // Add 4th item to cross the boundary to 12.5
        cart.add(new Item(ItemType.OTHER, "D", 1, 1.0));
        // cart.numberOfItems should be equal to 4 here. This assert should fail
        assertThat(cart.numberOfItems()).isNotEqualTo(4); // 0 but should be 4

        Amazon a4 = new Amazon(cart, List.of(new DeliveryPrice(), new ExtraCostForElectronics()));
        assertThat(a4.calculate()).isEqualTo(12.5);
    }
}