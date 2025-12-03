#ifndef KBIGNUM_FFI_H
#define KBIGNUM_FFI_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

// ==================== Memory Management ====================

typedef struct {
    uint8_t* data;
    uintptr_t len;
} ByteArrayResult;

void bigint_free_string(char* s);
void bigint_free_byte_result(ByteArrayResult* ptr);

// ==================== BigInteger Operations (Bytes) ====================

ByteArrayResult* bigint_from_string_bytes(const char* s);
char* bigint_to_string_bytes(const uint8_t* data, uintptr_t len);

ByteArrayResult* bigint_add_bytes(const uint8_t* a_data, uintptr_t a_len, const uint8_t* b_data, uintptr_t b_len);
ByteArrayResult* bigint_subtract_bytes(const uint8_t* a_data, uintptr_t a_len, const uint8_t* b_data, uintptr_t b_len);
ByteArrayResult* bigint_multiply_bytes(const uint8_t* a_data, uintptr_t a_len, const uint8_t* b_data, uintptr_t b_len);
ByteArrayResult* bigint_divide_bytes(const uint8_t* a_data, uintptr_t a_len, const uint8_t* b_data, uintptr_t b_len);
ByteArrayResult* bigint_mod_bytes(const uint8_t* a_data, uintptr_t a_len, const uint8_t* b_data, uintptr_t b_len);
ByteArrayResult* bigint_abs_bytes(const uint8_t* data, uintptr_t len);

int32_t bigint_signum_bytes(const uint8_t* data, uintptr_t len);
int32_t bigint_compare_bytes(const uint8_t* a_data, uintptr_t a_len, const uint8_t* b_data, uintptr_t b_len);
int64_t bigint_to_long_bytes(const uint8_t* data, uintptr_t len);

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
