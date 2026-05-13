package shopeasy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Task 3 — Design by Contract tests.
 *
 * Verifies that assert-based pre/post-conditions hold for valid inputs and
 * that invalid inputs trigger AssertionError when assertions are enabled.
 */
class ContractTest {

    @BeforeAll
    static void ensureAssertionsEnabled() {
        boolean assertsEnabled = false;
        // This assert will flip assertsEnabled to true only when JVM assertions are
        // enabled (-ea).
        assert assertsEnabled = true;
        assumeTrue(assertsEnabled, "Assertions must be enabled (-ea) to run contract tests");
    }

    @Test
    void priceCalculatorValidInputsHoldContracts() {
        PriceCalculator calc = new PriceCalculator();
        double res = calc.calculate(100.0, 10.0, 5.0);
        // result must be >= 0
        assertThat(res).isGreaterThanOrEqualTo(0.0);
        // Formula sanity check
        assertThat(res).isCloseTo((100.0 * 0.9) * 1.05, within(1e-6));
    }

    @Test
    void priceCalculatorPreconditionsViolatedThrowAssertion() {
        PriceCalculator calc = new PriceCalculator();
        assertThatThrownBy(() -> calc.calculate(-1.0, 0.0, 0.0)).isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> calc.calculate(10.0, -5.0, 0.0)).isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> calc.calculate(10.0, 0.0, -1.0)).isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> calc.calculate(10.0, 150.0, 0.0)).isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> calc.calculate(10.0, 0.0, 150.0)).isInstanceOf(AssertionError.class);
    }

    @Test
    void shoppingCartAddItemContractsHoldAndViolatedCases() {
        ShoppingCart cart = new ShoppingCart();
        Product apple = new Product("P001", "Apple", 1.50, 100);

        int before = cart.itemCount();
        cart.addItem(apple, 2);
        assertThat(cart.itemCount()).isGreaterThanOrEqualTo(before);
        assertThat(cart.total()).isGreaterThanOrEqualTo(0.0);

        // Pre-condition violations
        assertThatThrownBy(() -> cart.addItem(null, 1)).isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> cart.addItem(apple, 0)).isInstanceOf(AssertionError.class);
    }

    @Test
    void shoppingCartApplyDiscountContracts() {
        ShoppingCart cart = new ShoppingCart();
        Product p = new Product("P010", "Thing", 10.0, 10);
        cart.addItem(p, 2);
        double before = cart.total();
        double after = cart.applyDiscount(10);
        assertThat(after).isLessThan(before);

        // Invalid discounts should trigger assertion
        assertThatThrownBy(() -> cart.applyDiscount(-1.0)).isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> cart.applyDiscount(150.0)).isInstanceOf(AssertionError.class);
    }

}
