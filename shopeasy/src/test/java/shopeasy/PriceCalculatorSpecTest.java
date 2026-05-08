package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 1 – Specification-Based Testing (Chapter 2)
 *
 * <p>
 * Target class: {@link PriceCalculator}
 *
 * <p>
 * Your goal is to test
 * {@code PriceCalculator.calculate(basePrice, discountRate, taxRate)}
 * using the domain testing technique from Chapter 2:
 * <ol>
 * <li>Identify equivalence partitions for each input dimension.</li>
 * <li>Identify boundary values between partitions (on-point / off-point).</li>
 * <li>Write at least 10 meaningful test cases that cover both partitions and
 * boundaries.</li>
 * <li>Use {@code @ParameterizedTest} with {@code @CsvSource} for tests that
 * share structure.</li>
 * <li>Add a comment above each test method explaining which partition or
 * boundary it covers.</li>
 * </ol>
 *
 * <h3>Input dimensions to consider</h3>
 * <ul>
 * <li><b>basePrice</b> – zero, positive, very large</li>
 * <li><b>discountRate</b> – 0 (no discount), (0,100) typical, 100 (full
 * discount)</li>
 * <li><b>taxRate</b> – 0 (no tax), (0,100) typical, 100 (100% tax)</li>
 * </ul>
 */
class PriceCalculatorSpecTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    /**
     * Zero base price should always be 0, no matter the discount or tax
     */
    @Test
    void zeroBasePriceAlwaysReturnsZero() {
        assertThat(calculator.calculate(0.0, 0.0, 0.0)).isEqualTo(0.0);
        assertThat(calculator.calculate(0.0, 50.0, 20.0)).isEqualTo(0.0);
    }

    /** 0 percent discount does nothing to price, only tax should apply */
    @ParameterizedTest(name = "base={0},tax={1}")
    @CsvSource({ "100.0, 0.0", "0.0, 0.0", "250.5, 5.0" })
    void discountRateZeroMeansNoDiscount(double base, double tax) {
        double result = calculator.calculate(base, 0.0, tax);
        double expected = base * (1.0 + tax / 100.0);
        assertThat(result).isCloseTo(expected, within(1e-6));
    }

    /**
     * 100 percent discount should zero the price even if tax is present
     */
    @Test
    void discountRateHundredMeansFullDiscount() {
        assertThat(calculator.calculate(100.0, 100.0, 0.0)).isCloseTo(0.0, within(1e-6));
        assertThat(calculator.calculate(50.0, 100.0, 25.0)).isCloseTo(0.0, within(1e-6));
    }

    /** 0 percent tax means only discount affects the price */
    @ParameterizedTest(name = "base={0},disc={1}")
    @CsvSource({ "100.0, 10.0", "200.0, 0.0", "123.45, 50.0" })
    void zeroTaxAppliesOnlyDiscount(double base, double discount) {
        double result = calculator.calculate(base, discount, 0.0);
        double expected = base * (1.0 - discount / 100.0);
        assertThat(result).isCloseTo(expected, within(1e-6));
    }

    /** Checking the results whether they are expected or not */
    @ParameterizedTest(name = "base={0},disc={1}%,tax={2}% => expected={3}")
    @CsvSource({
            "100.0, 10.0, 20.0, 108.0",
            "200.0, 0.0, 10.0, 220.0",
            "50.0, 50.0, 10.0, 27.5"
    })
    void typicalValues(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(1e-6));
    }

    /**
     * Checking for negative discount
     */
    @Test
    void negativeDiscountProducesHigherPrice() {
        double result = calculator.calculate(100.0, -10.0, 0.0);
        double expected = 100.0 * (1.0 - (-10.0) / 100.0);
        assertThat(result).isCloseTo(expected, within(1e-6));
    }

    /**
     * Checking for discount greater than 100 percent
     */
    @Test
    void discountGreaterThanHundredMayBeNegative() {
        double result = calculator.calculate(100.0, 150.0, 0.0);
        double expected = 100.0 * (1.0 - 150.0 / 100.0);
        assertThat(result).isCloseTo(expected, within(1e-6));
    }

    /**
     * Handle the result for large base price to check for potential overflow issues
     */
    @Test
    void handleLargeBasePriceCorrectly() {
        double base = 1_000_000_000.0;
        double result = calculator.calculate(base, 25.0, 10.0);
        double expected = (base * (1.0 - 25.0 / 100.0)) * (1.0 + 10.0 / 100.0);
        assertThat(result).isCloseTo(expected, within(1e-3));
    }
}
