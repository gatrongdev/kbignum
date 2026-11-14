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

/** Compares KBigInteger and java.math.BigInteger using the same deterministic operands. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class IntegerArithmeticBenchmark {
    private static final int INPUT_COUNT = 32;

    @Param({"1024", "2048", "2560", "3072", "4096", "8192"})
    public int bits;

    private BigInteger[] javaLeft;
    private BigInteger[] javaRight;
    private BigInteger[] javaDivisor;
    private KBigInteger[] kBigLeft;
    private KBigInteger[] kBigRight;
    private KBigInteger[] kBigDivisor;
    private int cursor;

    @Setup(Level.Trial)
    public void createOperands() {
        Random random = new Random(0x4B4269674E756DL + bits);
        javaLeft = new BigInteger[INPUT_COUNT];
        javaRight = new BigInteger[INPUT_COUNT];
        javaDivisor = new BigInteger[INPUT_COUNT];
        kBigLeft = new KBigInteger[INPUT_COUNT];
        kBigRight = new KBigInteger[INPUT_COUNT];
        kBigDivisor = new KBigInteger[INPUT_COUNT];

        for (int i = 0; i < INPUT_COUNT; i++) {
            javaLeft[i] = randomPositiveInteger(bits, random);
            javaRight[i] = randomPositiveInteger(bits, random);
            javaDivisor[i] = randomPositiveInteger(bits / 2, random);
            kBigLeft[i] = KBigInteger.Companion.fromString(javaLeft[i].toString());
            kBigRight[i] = KBigInteger.Companion.fromString(javaRight[i].toString());
            kBigDivisor[i] = KBigInteger.Companion.fromString(javaDivisor[i].toString());
        }

        verifyEquivalentResults();
    }

    @Setup(Level.Iteration)
    public void resetOperandCursor() {
        cursor = 0;
    }

    @Benchmark
    public Object integerAddJava() {
        int index = nextIndex();
        return javaLeft[index].add(javaRight[index]);
    }

    @Benchmark
    public Object integerAddKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].add(kBigRight[index]);
    }

    @Benchmark
    public Object integerSubtractJava() {
        int index = nextIndex();
        return javaLeft[index].subtract(javaRight[index]);
    }

    @Benchmark
    public Object integerSubtractKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].subtract(kBigRight[index]);
    }

    @Benchmark
    public Object integerMultiplyJava() {
        int index = nextIndex();
        return javaLeft[index].multiply(javaRight[index]);
    }

    @Benchmark
    public Object integerMultiplyKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].multiply(kBigRight[index]);
    }

    @Benchmark
    public Object integerDivideJava() {
        int index = nextIndex();
        return javaLeft[index].divide(javaDivisor[index]);
    }

    @Benchmark
    public Object integerDivideKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].divide(kBigDivisor[index]);
    }

    @Benchmark
    public Object integerRemainderJava() {
        int index = nextIndex();
        return javaLeft[index].remainder(javaDivisor[index]);
    }

    @Benchmark
    public Object integerRemainderKBigNum() {
        int index = nextIndex();
        return kBigLeft[index].mod(kBigDivisor[index]);
    }

    private int nextIndex() {
        int index = cursor;
        cursor = (cursor + 1) & (INPUT_COUNT - 1);
        return index;
    }

    private void verifyEquivalentResults() {
        for (int i = 0; i < INPUT_COUNT; i++) {
            requireSame(javaLeft[i].add(javaRight[i]), kBigLeft[i].add(kBigRight[i]));
            requireSame(javaLeft[i].subtract(javaRight[i]), kBigLeft[i].subtract(kBigRight[i]));
            requireSame(javaLeft[i].multiply(javaRight[i]), kBigLeft[i].multiply(kBigRight[i]));
            requireSame(javaLeft[i].divide(javaDivisor[i]), kBigLeft[i].divide(kBigDivisor[i]));
            requireSame(javaLeft[i].remainder(javaDivisor[i]), kBigLeft[i].mod(kBigDivisor[i]));
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
