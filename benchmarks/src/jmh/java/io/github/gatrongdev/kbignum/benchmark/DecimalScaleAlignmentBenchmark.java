package io.github.gatrongdev.kbignum.benchmark;

import io.github.gatrongdev.kbignum.math.KBigDecimal;
import java.math.BigDecimal;
import java.math.BigInteger;
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

/** Measures scale-aligned addition and subtraction over controlled scale gaps. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DecimalScaleAlignmentBenchmark {
    private static final int INPUT_COUNT = 32;
    private static final int LEFT_SCALE = 8;

    @Param({"34", "100", "500", "1000"})
    public int digits;

    @Param({"1", "8", "32", "128"})
    public int scaleGap;

    private BigDecimal[] javaLeft;
    private BigDecimal[] javaRight;
    private KBigDecimal[] kBigLeft;
    private KBigDecimal[] kBigRight;
    private BigDecimal javaZeroLeft;
    private BigDecimal javaZeroRight;
    private KBigDecimal kBigZeroLeft;
    private KBigDecimal kBigZeroRight;
    private int cursor;

    @Setup(Level.Trial)
    public void createOperands() {
        Random random = new Random(0x4B4269675363616cL + digits * 31L + scaleGap);
        javaLeft = new BigDecimal[INPUT_COUNT];
        javaRight = new BigDecimal[INPUT_COUNT];
        kBigLeft = new KBigDecimal[INPUT_COUNT];
        kBigRight = new KBigDecimal[INPUT_COUNT];
        javaZeroLeft = BigDecimal.ZERO.setScale(LEFT_SCALE);
        javaZeroRight = BigDecimal.ZERO.setScale(LEFT_SCALE + scaleGap);
        kBigZeroLeft = new KBigDecimal(KBigDecimal.Companion.getZERO().getUnscaledValue(), LEFT_SCALE);
        kBigZeroRight = new KBigDecimal(KBigDecimal.Companion.getZERO().getUnscaledValue(), LEFT_SCALE + scaleGap);

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
            javaRight[i] = new BigDecimal(rightUnscaled, LEFT_SCALE + scaleGap);
            kBigLeft[i] = fromJavaDecimal(javaLeft[i]);
            kBigRight[i] = fromJavaDecimal(javaRight[i]);
        }

        verifyEquivalentResultsWithoutWarmingMeasuredOperands();
    }

    @Setup(Level.Iteration)
    public void resetOperandCursor() {
        cursor = 0;
    }

    @Benchmark
    public Object scaleAlignmentAddJava() {
        int index = nextIndex();
        return javaLeft[index].add(javaRight[index]);
    }

    @Benchmark
    public Object scaleAlignmentAddKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].add(kBigRight[index]);
    }

    @Benchmark
    public Object scaleAlignmentSubtractJava() {
        int index = nextIndex();
        return javaLeft[index].subtract(javaRight[index]);
    }

    @Benchmark
    public Object scaleAlignmentSubtractKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].subtract(kBigRight[index]);
    }

    @Benchmark
    public Object scaleAlignmentCompareJava() {
        int index = nextIndex();
        return javaLeft[index].compareTo(javaRight[index]);
    }

    @Benchmark
    public Object scaleAlignmentCompareKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].compareTo(kBigRight[index]);
    }

    @Benchmark
    public Object scaleAlignmentAddZeroJava() {
        int index = nextIndex();
        return javaZeroLeft.add(javaRight[index]);
    }

    @Benchmark
    public Object scaleAlignmentAddZeroKBigNum() {
        int index = nextIndex();
        return kBigZeroLeft.add(kBigRight[index]);
    }

    @Benchmark
    public Object scaleAlignmentSubtractZeroJava() {
        int index = nextIndex();
        return javaLeft[index].subtract(javaZeroRight);
    }

    @Benchmark
    public Object scaleAlignmentSubtractZeroKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].subtract(kBigZeroRight);
    }

    @Benchmark
    public Object scaleAlignmentCompareZeroJava() {
        int index = nextIndex();
        return javaZeroLeft.compareTo(javaRight[index]);
    }

    @Benchmark
    public Object scaleAlignmentCompareZeroKBigNum() {
        int index = nextIndex();
        return kBigZeroLeft.compareTo(kBigRight[index]);
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

    private void verifyEquivalentResultsWithoutWarmingMeasuredOperands() {
        for (int i = 0; i < INPUT_COUNT; i++) {
            KBigDecimal left = fromJavaDecimal(javaLeft[i]);
            KBigDecimal right = fromJavaDecimal(javaRight[i]);
            requireSame(javaLeft[i].add(javaRight[i]), left.add(right));
            requireSame(javaLeft[i].subtract(javaRight[i]), left.subtract(right));
        }
        for (int i = 0; i < INPUT_COUNT; i++) {
            KBigDecimal right = kBigRight[i];
            requireSame(javaZeroLeft.add(javaRight[i]), kBigZeroLeft.add(right));
            requireSame(javaLeft[i].subtract(javaZeroRight), kBigLeft[i].subtract(kBigZeroRight));
            int javaComparison = javaLeft[i].compareTo(javaRight[i]);
            int kBigComparison = kBigLeft[i].compareTo(right);
            if (Integer.signum(javaComparison) != Integer.signum(kBigComparison)) {
                throw new IllegalStateException("KBigDecimal comparison differs from java.math.BigDecimal");
            }
            int javaZeroComparison = javaZeroLeft.compareTo(javaRight[i]);
            int kBigZeroComparison = kBigZeroLeft.compareTo(right);
            if (Integer.signum(javaZeroComparison) != Integer.signum(kBigZeroComparison)) {
                throw new IllegalStateException("KBigDecimal zero comparison differs from java.math.BigDecimal");
            }
        }
    }

    private static void requireSame(BigDecimal expected, KBigDecimal actual) {
        if (!expected.toPlainString().equals(actual.toString())) {
            throw new IllegalStateException("KBigDecimal result differs from java.math.BigDecimal");
        }
    }
}
