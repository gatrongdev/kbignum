package io.github.gatrongdev.kbignum.benchmark;

import io.github.gatrongdev.kbignum.math.KBigInteger;
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

/**
 * Compares multi-word division and remainder against java.math.BigInteger using identical seeded
 * operands. {@code divisorWords} and {@code dividendExtraWords} target the recursive-division
 * dispatch boundary; the JVM fork and operand pair stay fixed between baseline and candidate runs.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DivisionCrossoverBenchmark {
    private static final int INPUT_COUNT = 8;

    @Param({"64", "80", "96", "112", "128"})
    public int divisorWords;

    @Param({"24", "40", "64", "128"})
    public int dividendExtraWords;

    private BigInteger[] javaDividend;
    private BigInteger[] javaDivisor;
    private KBigInteger[] kBigDividend;
    private KBigInteger[] kBigDivisor;
    private int cursor;

    @Setup(Level.Trial)
    public void createOperandsAndVerify() {
        Random random = new Random(0x4B4269674469764CL + divisorWords * 31L + dividendExtraWords);
        int dividendWords = divisorWords + dividendExtraWords;

        javaDividend = new BigInteger[INPUT_COUNT];
        javaDivisor = new BigInteger[INPUT_COUNT];
        kBigDividend = new KBigInteger[INPUT_COUNT];
        kBigDivisor = new KBigInteger[INPUT_COUNT];

        for (int i = 0; i < INPUT_COUNT; i++) {
            javaDividend[i] = randomPositiveInteger(dividendWords * 32, random);
            javaDivisor[i] = randomPositiveInteger(divisorWords * 32, random);
            kBigDividend[i] = KBigInteger.Companion.fromString(javaDividend[i].toString());
            kBigDivisor[i] = KBigInteger.Companion.fromString(javaDivisor[i].toString());
        }

        verifyEquivalentResults();
    }

    @Setup(Level.Iteration)
    public void resetOperandCursor() {
        cursor = 0;
    }

    @Benchmark
    public Object divideJava() {
        int index = nextIndex();
        return javaDividend[index].divide(javaDivisor[index]);
    }

    @Benchmark
    public Object divideKBigNum() {
        int index = nextIndex();
        return kBigDividend[index].divide(kBigDivisor[index]);
    }

    @Benchmark
    public Object remainderJava() {
        int index = nextIndex();
        return javaDividend[index].remainder(javaDivisor[index]);
    }

    @Benchmark
    public Object remainderKBigNum() {
        int index = nextIndex();
        return kBigDividend[index].mod(kBigDivisor[index]);
    }

    private int nextIndex() {
        int index = cursor;
        cursor = (cursor + 1) & (INPUT_COUNT - 1);
        return index;
    }

    private void verifyEquivalentResults() {
        for (int i = 0; i < INPUT_COUNT; i++) {
            requireSame(
                    javaDividend[i].divide(javaDivisor[i]),
                    kBigDividend[i].divide(kBigDivisor[i]));
            requireSame(
                    javaDividend[i].remainder(javaDivisor[i]),
                    kBigDividend[i].mod(kBigDivisor[i]));
        }
    }

    private static BigInteger randomPositiveInteger(int bits, Random random) {
        return new BigInteger(bits, random).setBit(bits - 1).setBit(0);
    }

    private static void requireSame(BigInteger expected, KBigInteger actual) {
        if (!expected.toString().equals(actual.toString())) {
            throw new IllegalStateException("KBigInteger result differs from java.math.BigInteger");
        }
    }
}
