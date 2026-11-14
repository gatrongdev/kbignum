package io.github.gatrongdev.kbignum.benchmark;

import io.github.gatrongdev.kbignum.math.KBigInteger;
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

/** Measures the production strategy against a forced schoolbook reference. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MultiplicationCrossoverBenchmark {
    @Param({"64", "72", "80", "88", "96", "112", "128", "160", "256"})
    public int baseWords;

    @Param({"1", "2", "4"})
    public int operandRatio;

    private KBigInteger left;
    private KBigInteger right;
    private int[] leftMagnitude;
    private int[] rightMagnitude;

    @Setup(Level.Trial)
    public void createOperandsAndVerify() {
        Random random = new Random(0x4B61726174737562L + baseWords * 31L + operandRatio);
        leftMagnitude = randomMagnitude(baseWords * operandRatio, random);
        rightMagnitude = randomMagnitude(baseWords, random);
        left = new KBigInteger(1, leftMagnitude);
        right = new KBigInteger(1, rightMagnitude);

        String productionResult = left.multiply(right).toString();
        String schoolbookResult = new KBigInteger(1, schoolbookMultiply(leftMagnitude, rightMagnitude)).toString();
        if (!productionResult.equals(schoolbookResult)) {
            throw new IllegalStateException("Production and schoolbook multiplication results differ");
        }
    }

    @Benchmark
    public KBigInteger multiplyProduction() {
        return left.multiply(right);
    }

    @Benchmark
    public KBigInteger multiplySchoolbookReference() {
        return new KBigInteger(1, schoolbookMultiply(leftMagnitude, rightMagnitude));
    }

    private static int[] randomMagnitude(int words, Random random) {
        int[] magnitude = new int[words];
        for (int i = 0; i < words; i++) {
            magnitude[i] = random.nextInt();
        }
        magnitude[0] |= 1;
        magnitude[words - 1] |= 0x80000000;
        return magnitude;
    }

    /** Mirrors KBigInteger's optimized schoolbook limb loop so it can be forced in JMH. */
    private static int[] schoolbookMultiply(int[] x, int[] y) {
        int[] outer = x.length <= y.length ? x : y;
        int[] inner = x.length <= y.length ? y : x;
        if (outer.length == 0 || inner.length == 0) {
            return new int[0];
        }

        int[] result = new int[outer.length + inner.length];
        long firstOuterValue = outer[0] & 0xFFFFFFFFL;
        long carry = 0;
        for (int j = 0; j < inner.length; j++) {
            long product = firstOuterValue * (inner[j] & 0xFFFFFFFFL) + carry;
            result[j] = (int) product;
            carry = product >>> 32;
        }
        result[inner.length] = (int) carry;

        for (int i = 1; i < outer.length; i++) {
            long outerValue = outer[i] & 0xFFFFFFFFL;
            carry = 0;
            int resultIndex = i;
            for (int j = 0; j < inner.length; j++) {
                long product = outerValue * (inner[j] & 0xFFFFFFFFL)
                        + (result[resultIndex] & 0xFFFFFFFFL) + carry;
                result[resultIndex] = (int) product;
                carry = product >>> 32;
                resultIndex++;
            }
            result[resultIndex] = (int) carry;
        }

        return stripLeadingZeros(result);
    }

    private static int[] stripLeadingZeros(int[] magnitude) {
        int size = magnitude.length;
        while (size > 0 && magnitude[size - 1] == 0) {
            size--;
        }
        if (size == magnitude.length) {
            return magnitude;
        }
        int[] normalized = new int[size];
        System.arraycopy(magnitude, 0, normalized, 0, size);
        return normalized;
    }
}
