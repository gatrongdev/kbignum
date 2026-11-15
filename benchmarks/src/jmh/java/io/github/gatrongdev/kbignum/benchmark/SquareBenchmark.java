package io.github.gatrongdev.kbignum.benchmark;

import io.github.gatrongdev.kbignum.math.KBigDecimal;
import io.github.gatrongdev.kbignum.math.KBigInteger;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/** Measures square and repeated-square workloads against the JDK reference. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class SquareBenchmark {
    private static final int INPUT_COUNT = 32;
    private static final int DECIMAL_SCALE = 4;
    private static final int POWER = 5;

    @Param({"512", "1024", "2048", "4096", "8192"})
    public int bits;

    private BigInteger[] javaValues;
    private BigDecimal[] javaDecimals;
    private KBigInteger[] kBigValues;
    private KBigDecimal[] kBigDecimals;
    private int cursor;

    @Setup(Level.Trial)
    public void createOperands() {
        Random random = new Random(0x4B4269674E756DL + bits);
        javaValues = new BigInteger[INPUT_COUNT];
        javaDecimals = new BigDecimal[INPUT_COUNT];
        kBigValues = new KBigInteger[INPUT_COUNT];
        kBigDecimals = new KBigDecimal[INPUT_COUNT];

        for (int i = 0; i < INPUT_COUNT; i++) {
            javaValues[i] = new BigInteger(bits, random).setBit(bits - 1).setBit(0);
            javaDecimals[i] = new BigDecimal(javaValues[i], DECIMAL_SCALE);
            kBigValues[i] = KBigInteger.Companion.fromString(javaValues[i].toString());
            kBigDecimals[i] = new KBigDecimal(kBigValues[i], DECIMAL_SCALE);
        }

        verifyEquivalentResults();
    }

    @Setup(Level.Iteration)
    public void resetOperandCursor() {
        cursor = 0;
    }

    @Benchmark
    public Object integerSquareJava() {
        BigInteger value = javaValues[nextIndex()];
        return value.multiply(value);
    }

    @Benchmark
    public Object integerSquareKBigNum() {
        KBigInteger value = kBigValues[nextIndex()];
        return value.multiply(value);
    }

    @Benchmark
    public Object integerPowJava() {
        return javaValues[nextIndex()].pow(POWER);
    }

    @Benchmark
    public Object integerPowKBigNum() {
        return kBigValues[nextIndex()].pow(POWER);
    }

    @Benchmark
    public Object decimalPowJava() {
        return javaDecimals[nextIndex()].pow(POWER);
    }

    @Benchmark
    public Object decimalPowKBigNum() {
        return kBigDecimals[nextIndex()].pow(POWER);
    }

    private int nextIndex() {
        int index = cursor;
        cursor = (cursor + 1) & (INPUT_COUNT - 1);
        return index;
    }

    private void verifyEquivalentResults() {
        for (int i = 0; i < INPUT_COUNT; i++) {
            requireSame(javaValues[i].multiply(javaValues[i]), kBigValues[i].multiply(kBigValues[i]));
            requireSame(javaValues[i].pow(POWER), kBigValues[i].pow(POWER));
            if (!javaDecimals[i].pow(POWER).toPlainString().equals(kBigDecimals[i].pow(POWER).toString())) {
                throw new IllegalStateException("KBigDecimal power result differs from java.math.BigDecimal");
            }
        }
    }

    private static void requireSame(BigInteger expected, KBigInteger actual) {
        if (!expected.toString().equals(actual.toString())) {
            throw new IllegalStateException("KBigInteger result differs from java.math.BigInteger");
        }
    }
}
