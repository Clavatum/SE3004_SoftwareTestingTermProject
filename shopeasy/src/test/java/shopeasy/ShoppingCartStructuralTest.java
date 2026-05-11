package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 2 – Structural Testing &amp; Code Coverage (Chapter 3)
 *
 * <p>
 * Target class: {@link ShoppingCart}
 *
 * <h3>Workflow</h3>
 * <ol>
 * <li>Write an initial test suite based on the specification (Javadoc of
 * ShoppingCart).</li>
 * <li>Run {@code mvn test} to generate the JaCoCo report:
 * 
 * <pre>
 * target / site / jacoco / index.html
 * </pre>
 * 
 * </li>
 * <li>Open the report, navigate to {@code ShoppingCart}, and identify uncovered
 * branches.</li>
 * <li>Add tests specifically to cover those branches until branch coverage
 * &gt;= 80%.</li>
 * <li>Take a screenshot of the final JaCoCo summary and put it in
 * {@code report/jacoco-screenshot.png}.</li>
 * </ol>
 *
 * <h3>Branches to think about</h3>
 * <ul>
 * <li>{@code addItem}: product already in cart vs. new product</li>
 * <li>{@code removeItem}: product found vs. not found in cart</li>
 * <li>{@code updateQuantity}: product found vs. not found, quantity valid vs.
 * invalid</li>
 * <li>{@code applyDiscount}: zero discount, positive discount</li>
 * <li>{@code total}: empty cart vs. non-empty cart</li>
 * </ul>
 *
 * <h3>Bonus (PIT Mutation Testing)</h3>
 * Run: {@code mvn org.pitest:pitest-maven:mutationCoverage}
 * <br>
 * Examine the HTML report in {@code target/pit-reports/}. Find two surviving
 * mutants,
 * explain why each survived, and describe a test that would kill it. Add this
 * analysis
 * to your reflection report.
 */
class ShoppingCartStructuralTest {

    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
        apple = new Product("P001", "Apple", 1.50, 100);
        banana = new Product("P002", "Banana", 0.80, 50);
    }

    // Empty cart total should be 0
    @Test
    void emptyCartHasZeroTotalAndZeroItems() {
        assertThat(cart.itemCount()).isEqualTo(0);
        assertThat(cart.total()).isEqualTo(0.0);
    }

    // New product adds a line and increases total
    @Test
    void addingNewItemIncreasesCountAndTotal() {
        cart.addItem(apple, 2);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isCloseTo(1.50 * 2, within(1e-6));
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProduct().getId()).isEqualTo("P001");
    }

    // Adding same product combines quantities
    @Test
    void addingExistingItemCombinesQuantity() {
        cart.addItem(apple, 2);
        cart.addItem(apple, 3);
        assertThat(cart.itemCount()).isEqualTo(1);
        CartItem line = cart.getItems().get(0);
        assertThat(line.getQuantity()).isEqualTo(5);
        assertThat(cart.total()).isCloseTo(1.50 * 5, within(1e-6));
    }

    // Removing an existing product actually removes it
    @Test
    void removeItemRemovesWhenPresent() {
        cart.addItem(apple, 1);
        cart.addItem(banana, 2);
        cart.removeItem("P001");
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getProduct().getId()).isEqualTo("P002");
    }

    // Removing a non-existent id is a no-op
    @Test
    void removeItemNoOpWhenNotPresent() {
        cart.addItem(apple, 1);
        cart.removeItem("NOPE");
        assertThat(cart.itemCount()).isEqualTo(1);
    }

    // updateQuantity: happy path updates qty
    @Test
    void updateQuantityChangesLineQuantity() {
        cart.addItem(banana, 2);
        cart.updateQuantity("P002", 5);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(cart.total()).isCloseTo(0.80 * 5, within(1e-6));
    }

    // Invalid quantity (<=0) throws IllegalArgumentException
    @Test
    void updateQuantityWithNonPositiveValueThrows() {
        cart.addItem(apple, 1);
        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled) {
            assertThatThrownBy(() -> cart.updateQuantity("P001", 0))
                    .isInstanceOf(AssertionError.class);
        } else {
            assertThatThrownBy(() -> cart.updateQuantity("P001", 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // Product not found throws IllegalArgumentException
    @Test
    void updateQuantityForMissingProductThrows() {
        assertThatThrownBy(() -> cart.updateQuantity("MISSING", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    // 0 percent discount returns same total; positive discount reduces it and
    // result
    @Test
    void applyDiscountZeroAndPositiveBehavior() {
        cart.addItem(apple, 4);
        double before = cart.total();
        double noDiscount = cart.applyDiscount(0);
        assertThat(noDiscount).isCloseTo(before, within(1e-6));

        double withDiscount = cart.applyDiscount(25);
        assertThat(withDiscount).isLessThan(before);
        assertThat(withDiscount).isCloseTo(before * 0.75, within(1e-6));
    }

    // getItems returns unmodifiable list
    @Test
    void getItemsReturnsUnmodifiableList() {
        cart.addItem(apple, 1);
        assertThatThrownBy(() -> cart.getItems().add(new CartItem(banana, 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // Emptying the cart
    @Test
    void clearRemovesAllItems() {
        cart.addItem(apple, 1);
        cart.addItem(banana, 1);
        cart.clear();
        assertThat(cart.itemCount()).isEqualTo(0);
        assertThat(cart.total()).isEqualTo(0.0);
    }

    // Basic sanity check that it contains item count and total formatting
    @Test
    void toStringContainsCountAndTotal() {
        cart.addItem(apple, 2);
        String s = cart.toString();
        assertThat(s).contains("ShoppingCart");
        assertThat(s).contains("items=");
        assertThat(s).contains("total=");
    }
}
