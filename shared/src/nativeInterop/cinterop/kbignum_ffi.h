#ifndef KBIGNUM_FFI_H
#define KBIGNUM_FFI_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

// ==================== Memory Management ====================

void bigint_free_string(char* s);

// ==================== BigInteger Operations ====================

char* bigint_add(const char* a, const char* b);
char* bigint_subtract(const char* a, const char* b);
char* bigint_multiply(const char* a, const char* b);
char* bigint_divide(const char* a, const char* b);
char* bigint_mod(const char* a, const char* b);
char* bigint_pow(const char* base, uint32_t exponent);
char* bigint_abs(const char* a);
int32_t bigint_signum(const char* a);
int32_t bigint_compare(const char* a, const char* b);
char* bigint_gcd(const char* a, const char* b);
int64_t bigint_to_long(const char* a);

// ==================== BigDecimal Operations ====================

char* bigdecimal_add(const char* a, const char* b, int32_t scale);
char* bigdecimal_subtract(const char* a, const char* b, int32_t scale);
char* bigdecimal_multiply(const char* a, const char* b, int32_t scale);
char* bigdecimal_divide(const char* a, const char* b, int32_t scale);
char* bigdecimal_abs(const char* a);
int32_t bigdecimal_signum(const char* a);
int32_t bigdecimal_compare(const char* a, const char* b);
char* bigdecimal_set_scale(const char* a, int32_t scale, int32_t rounding_mode);
char* bigdecimal_to_biginteger(const char* a);

#ifdef __cplusplus
}
#endif

#endif // KBIGNUM_FFI_H
