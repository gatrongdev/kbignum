package io.github.gatrongdev.kbignum.benchmark;

import io.github.gatrongdev.kbignum.math.KBigDecimal;
import io.github.gatrongdev.kbignum.math.KBRoundingMode;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/** Compares KBigDecimal and java.math.BigDecimal with deterministic, equivalent inputs. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DecimalArithmeticBenchmark {
    private static final int INPUT_COUNT = 32;
    private static final int LEFT_SCALE = 8;
    private static final int MISMATCHED_SCALE = 16;
    private static final int DIVISION_SCALE = 16;
    private static final int RESCALED_SCALE = 4;

    @Param({"34", "100", "500", "1000"})
    public int digits;

    private BigDecimal[] javaLeft;
    private BigDecimal[] javaRightSameScale;
    private BigDecimal[] javaRightMismatchedScale;
    private KBigDecimal[] kBigLeft;
    private KBigDecimal[] kBigRightSameScale;
    private KBigDecimal[] kBigRightMismatchedScale;
    private String[] decimalText;
    private int cursor;

    @Setup(Level.Trial)
    public void createOperands() {
        Random random = new Random(0x4B426967446563L + digits);
        javaLeft = new BigDecimal[INPUT_COUNT];
        javaRightSameScale = new BigDecimal[INPUT_COUNT];
        javaRightMismatchedScale = new BigDecimal[INPUT_COUNT];
        kBigLeft = new KBigDecimal[INPUT_COUNT];
        kBigRightSameScale = new KBigDecimal[INPUT_COUNT];
        kBigRightMismatchedScale = new KBigDecimal[INPUT_COUNT];
        decimalText = new String[INPUT_COUNT];

        for (int i = 0; i < INPUT_COUNT; i++) {
            BigInteger leftUnscaled = randomIntegerWithDigits(digits, random);
            BigInteger rightUnscaled = randomIntegerWithDigits(digits, random);
            if ((i & 1) != 0) {
                leftUnscaled = leftUnscaled.negate();
            }
            if ((i & 2) != 0) {
                rightUnscaled = rightUnscaled.negate();
            }

            javaLeft[i] = new BigDecimal(leftUnscaled, LEFT_SCALE);
            javaRightSameScale[i] = new BigDecimal(rightUnscaled, LEFT_SCALE);
            javaRightMismatchedScale[i] = new BigDecimal(rightUnscaled, MISMATCHED_SCALE);
            kBigLeft[i] = fromJavaDecimal(javaLeft[i]);
            kBigRightSameScale[i] = fromJavaDecimal(javaRightSameScale[i]);
            kBigRightMismatchedScale[i] = fromJavaDecimal(javaRightMismatchedScale[i]);
            decimalText[i] = javaLeft[i].toPlainString();
        }

        verifyEquivalentResults();
    }

    @Setup(Level.Iteration)
    public void resetOperandCursor() {
        cursor = 0;
    }

    @Benchmark
    public Object decimalAddJava() {
        int index = nextIndex();
        return javaLeft[index].add(javaRightSameScale[index]);
    }

    @Benchmark
    public Object decimalAddKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].add(kBigRightSameScale[index]);
    }

    @Benchmark
    public Object decimalAddMismatchedJava() {
        int index = nextIndex();
        return javaLeft[index].add(javaRightMismatchedScale[index]);
    }

    @Benchmark
    public Object decimalAddMismatchedKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].add(kBigRightMismatchedScale[index]);
    }

    @Benchmark
    public Object decimalSubtractJava() {
        int index = nextIndex();
        return javaLeft[index].subtract(javaRightSameScale[index]);
    }

    @Benchmark
    public Object decimalSubtractKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].subtract(kBigRightSameScale[index]);
    }

    @Benchmark
    public Object decimalSubtractMismatchedJava() {
        int index = nextIndex();
        return javaLeft[index].subtract(javaRightMismatchedScale[index]);
    }

    @Benchmark
    public Object decimalSubtractMismatchedKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].subtract(kBigRightMismatchedScale[index]);
    }

    @Benchmark
    public Object decimalMultiplyJava() {
        int index = nextIndex();
        return javaLeft[index].multiply(javaRightMismatchedScale[index]);
    }

    @Benchmark
    public Object decimalMultiplyKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].multiply(kBigRightMismatchedScale[index]);
    }

    @Benchmark
    public Object decimalDivideJava() {
        int index = nextIndex();
        return javaLeft[index].divide(javaRightMismatchedScale[index], DIVISION_SCALE, RoundingMode.HALF_UP);
    }

    @Benchmark
    public Object decimalDivideKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].divide(kBigRightMismatchedScale[index], DIVISION_SCALE, KBRoundingMode.HalfUp);
    }

    @Benchmark
    public Object decimalSetScaleJava() {
        int index = nextIndex();
        return javaLeft[index].setScale(RESCALED_SCALE, RoundingMode.HALF_UP);
    }

    @Benchmark
    public Object decimalSetScaleKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].setScale(RESCALED_SCALE, KBRoundingMode.HalfUp);
    }

    @Benchmark
    public Object decimalParseJava() {
        int index = nextIndex();
        return new BigDecimal(decimalText[index]);
    }

    @Benchmark
    public Object decimalParseKBigNum() {
        int index = nextIndex();
        return KBigDecimal.Companion.fromString(decimalText[index]);
    }

    @Benchmark
    public Object decimalFormatJava() {
        int index = nextIndex();
        return javaLeft[index].toPlainString();
    }

    @Benchmark
    public Object decimalFormatKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].toString();
    }

    private int nextIndex() {
        int index = cursor;
        cursor = (cursor + 1) & (INPUT_COUNT - 1);
        return index;
    }

    private static BigInteger randomIntegerWithDigits(int digits, Random random) {
        StringBuilder value = new StringBuilder(digits);
        value.append((char) ('1' + random.nextInt(9)));
        for (int i = 1; i < digits; i++) {
            value.append((char) ('0' + random.nextInt(10)));
        }
        return new BigInteger(value.toString());
    }

    private static KBigDecimal fromJavaDecimal(BigDecimal value) {
        return KBigDecimal.Companion.fromString(value.toPlainString());
    }

    private void verifyEquivalentResults() {
        for (int i = 0; i < INPUT_COUNT; i++) {
            requireSame(javaLeft[i].add(javaRightSameScale[i]), kBigLeft[i].add(kBigRightSameScale[i]));
            requireSame(javaLeft[i].add(javaRightMismatchedScale[i]), kBigLeft[i].add(kBigRightMismatchedScale[i]));
            requireSame(javaLeft[i].subtract(javaRightSameScale[i]), kBigLeft[i].subtract(kBigRightSameScale[i]));
            requireSame(javaLeft[i].subtract(javaRightMismatchedScale[i]), kBigLeft[i].subtract(kBigRightMismatchedScale[i]));
            requireSame(javaLeft[i].multiply(javaRightMismatchedScale[i]), kBigLeft[i].multiply(kBigRightMismatchedScale[i]));
            requireSame(
                javaLeft[i].divide(javaRightMismatchedScale[i], DIVISION_SCALE, RoundingMode.HALF_UP),
                kBigLeft[i].divide(kBigRightMismatchedScale[i], DIVISION_SCALE, KBRoundingMode.HalfUp)
            );
            requireSame(
                javaLeft[i].setScale(RESCALED_SCALE, RoundingMode.HALF_UP),
                kBigLeft[i].setScale(RESCALED_SCALE, KBRoundingMode.HalfUp)
            );
            requireSame(new BigDecimal(decimalText[i]), KBigDecimal.Companion.fromString(decimalText[i]));
            if (!javaLeft[i].toPlainString().equals(kBigLeft[i].toString())) {
                throw new IllegalStateException("KBigDecimal plain-string output differs from java.math.BigDecimal");
            }
        }
    }

    private static void requireSame(BigDecimal expected, KBigDecimal actual) {
        if (!expected.toPlainString().equals(actual.toString())) {
            throw new IllegalStateException("KBigDecimal result differs from java.math.BigDecimal");
        }
    }
}
