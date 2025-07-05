import java.math.BigDecimal;

public class TestMultiply {
    public static void main(String[] args) {
        BigDecimal a = new BigDecimal("123.45");
        BigDecimal b = new BigDecimal("67.89");
        BigDecimal result = a.multiply(b);
        System.out.println("Result: " + result.toString());
        System.out.println("Expected: 8381.0505");
        
        BigDecimal c = new BigDecimal("123.4");
        BigDecimal d = new BigDecimal("67.895");
        BigDecimal result2 = c.multiply(d);
        System.out.println("Result2: " + result2.toString());
        System.out.println("Expected2: 8378.643");
    }
}