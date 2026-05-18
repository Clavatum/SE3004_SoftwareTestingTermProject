package shopeasy;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Task 4 – Property-Based Testing (Chapter 5)
 *
 * <p>
 * Target classes: {@link PriceCalculator}, {@link ShoppingCart}
 *
 * <p>
 * Using jqwik, define and test at least <strong>3 distinct properties</strong>.
 * You must use at least one custom {@code @Provide} method.
 *
 * <h3>Suggested properties (you may use these or design your own)</h3>
 * <ul>
 * <li><b>Monotonicity</b> – For any fixed base and tax, increasing the discount
 * rate never increases the final price.</li>
 * <li><b>Identity</b> – A 0% discount and 0% tax returns exactly the base
 * price.</li>
 * <li><b>Boundedness</b> – The result is always &gt;= 0.</li>
 * <li><b>Cart commutativity</b> – Adding product A then B yields the same total
 * as adding B then A.</li>
 * <li><b>Discount transitivity</b> – Applying a 10% then another 10% discount
 * via
 * {@code applyDiscount} is equivalent to a single call with the compounded rate
 * (think carefully: is this actually true for this implementation?).</li>
 * </ul>
 *
 * <h3>For each property, include a comment that answers:</h3>
 * <ol>
 * <li>What does this property mean in plain English?</li>
 * <li>What class of bugs would this property catch?</li>
 * </ol>
 *
 * <h3>If jqwik finds a failing case</h3>
 * Do not just fix the test. Investigate the root cause and explain it in your
 * reflection report (include the counterexample jqwik printed).
 */
class ShopEasyPropertyTest {

    // -----------------------------------------------------------------------
    /**
     * Property: final price is never negative.
     * What it means: For any valid base, discount and tax, the calculated final
     * price should be >= 0.
     * Bug class caught: any implementation path that produces negative values
     */
    @Property
    void finalPriceIsNeverNegative(
            @ForAll @DoubleRange(min = 0, max = 1_000_000) double base,
            @ForAll @DoubleRange(min = 0, max = 100) double discount,
            @ForAll @DoubleRange(min = 0, max = 100) double tax) {

        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(base, discount, tax);
        assertThat(result).isGreaterThanOrEqualTo(0.0);
    }

    /**
     * Property: identity for 0% discount and 0% tax.
     * What it means: If you apply no discount and no tax, the final price
     * should equal the base price exactly.
     * Bug class caught: off-by-one or incorrect application order bugs.
     */
    @Property
    void zeroDiscountZeroTaxReturnsBase(
            @ForAll @DoubleRange(min = 0, max = 1_000_000) double base) {

        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(base, 0.0, 0.0);
        assertThat(result).isCloseTo(base, within(1e-9));
    }

    /**
     * Property: monotonicity of discount.
     * What it means: For fixed base and tax, increasing the discount rate
     * should never increase the final price (it should stay the same or go down).
     * Bug class caught: wrong discount application order or sign errors.
     */
    @Property
    void increasingDiscountDoesNotIncreasePrice(
            @ForAll @DoubleRange(min = 0, max = 100_000) double base,
            @ForAll @DoubleRange(min = 0, max = 100) double tax,
            @ForAll @DoubleRange(min = 0, max = 100) double d1,
            @ForAll @DoubleRange(min = 0, max = 100) double d2) {

        // ensure we compare a lower discount to a higher discount
        double low = Math.min(d1, d2);
        double high = Math.max(d1, d2);

        PriceCalculator calc = new PriceCalculator();
        double rLow = calc.calculate(base, low, tax);
        double rHigh = calc.calculate(base, high, tax);

        // allow a tiny epsilon for floating point noise
        assertThat(rHigh).isLessThanOrEqualTo(rLow + 1e-9);
    }

    /**
     * Property: ShoppingCart commutativity.
     * What it means: Adding product A then B should give the same cart total as
     * adding B then A (order of independent additions shouldn't matter).
     * Bug class caught: stateful side-effects, order-dependent computation bugs.
     */
    @Property
    void cartAdditionOrderDoesNotAffectTotal(
            @ForAll("validProducts") Product a,
            @ForAll("validProducts") Product b,
            @ForAll @IntRange(min = 1, max = 5) int q1,
            @ForAll @IntRange(min = 1, max = 5) int q2) {

        // skip degenerate case where provider generated the same product twice
        assumeTrue(!a.getId().equals(b.getId()));

        ShoppingCart c1 = new ShoppingCart();
        c1.addItem(a, q1);
        c1.addItem(b, q2);

        ShoppingCart c2 = new ShoppingCart();
        c2.addItem(b, q2);
        c2.addItem(a, q1);

        assertThat(c1.total()).isCloseTo(c2.total(), within(1e-9));
    }

    // Custom provider for valid Product instances
    @Provide
    Arbitrary<Product> validProducts() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(6),
                Arbitraries.doubles().between(0.01, 1000.0),
                Arbitraries.integers().between(0, 1000))
                .as((name, price, stock) -> new Product("P-" + name + "-" + Math.abs(name.hashCode()), name, price,
                        stock));
    }
}
