use num_bigint::BigInt;
use num_traits::{Zero, One, ToPrimitive, Signed};
use std::ffi::{CStr, CString};
use std::os::raw::c_char;
use std::str::FromStr;
use std::slice;

// ==================== Memory Management ====================

#[repr(C)]
pub struct ByteArrayResult {
    pub data: *mut u8,
    pub len: usize,
}

#[no_mangle]
pub extern "C" fn bigint_free_string(s: *mut c_char) {
    if !s.is_null() {
        unsafe {
            let _ = CString::from_raw(s);
        }
    }
}

#[no_mangle]
pub extern "C" fn bigint_free_byte_result(ptr: *mut ByteArrayResult) {
    if !ptr.is_null() {
        unsafe {
            let result = Box::from_raw(ptr);
            // Reconstruct Vec to drop it and free memory
            let _ = Vec::from_raw_parts(result.data, result.len, result.len);
            // result (Box) is dropped here
        }
    }
}

// ==================== Helper Functions ====================

unsafe fn c_str_to_string(c_str: *const c_char) -> Result<String, String> {
    if c_str.is_null() {
        return Err("Null pointer".to_string());
    }

    match CStr::from_ptr(c_str).to_str() {
        Ok(s) => Ok(s.to_string()),
        Err(_) => Err("Invalid UTF-8".to_string()),
    }
}

fn string_to_c_str(s: String) -> *mut c_char {
    match CString::new(s) {
        Ok(c_string) => c_string.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}

fn bigint_from_str(s: &str) -> Result<BigInt, String> {
    // Remove leading '+' sign if present (BigInt doesn't handle it)
    let s_clean = if s.starts_with('+') {
        &s[1..]
    } else {
        s
    };
    BigInt::from_str(s_clean).map_err(|e| format!("Invalid number: {}", e))
}

fn bytes_to_bigint(data: *const u8, len: usize) -> BigInt {
    let bytes = unsafe { slice::from_raw_parts(data, len) };
    BigInt::from_signed_bytes_be(bytes)
}

fn bigint_to_byte_result(n: BigInt) -> *mut ByteArrayResult {
    let bytes = n.to_signed_bytes_be();
    let mut buf = bytes.into_boxed_slice();
    let data = buf.as_mut_ptr();
    let len = buf.len();
    std::mem::forget(buf); // Prevent deallocation

    let result = Box::new(ByteArrayResult { data, len });
    Box::into_raw(result)
}

// ==================== BigInteger Operations (Bytes) ====================

#[no_mangle]
pub extern "C" fn bigint_from_string_bytes(s: *const c_char) -> *mut ByteArrayResult {
    unsafe {
        let s_str = match c_str_to_string(s) {
            Ok(val) => val,
            Err(_) => return std::ptr::null_mut(),
        };
        match bigint_from_str(&s_str) {
            Ok(n) => bigint_to_byte_result(n),
            Err(_) => std::ptr::null_mut(),
        }
    }
}

#[no_mangle]
pub extern "C" fn bigint_to_string_bytes(data: *const u8, len: usize) -> *mut c_char {
    let n = bytes_to_bigint(data, len);
    string_to_c_str(n.to_string())
}

#[no_mangle]
pub extern "C" fn bigint_add_bytes(
    a_data: *const u8, a_len: usize,
    b_data: *const u8, b_len: usize
) -> *mut ByteArrayResult {
    let a = bytes_to_bigint(a_data, a_len);
    let b = bytes_to_bigint(b_data, b_len);
    bigint_to_byte_result(a + b)
}

#[no_mangle]
pub extern "C" fn bigint_subtract_bytes(
    a_data: *const u8, a_len: usize,
    b_data: *const u8, b_len: usize
) -> *mut ByteArrayResult {
    let a = bytes_to_bigint(a_data, a_len);
    let b = bytes_to_bigint(b_data, b_len);
    bigint_to_byte_result(a - b)
}

#[no_mangle]
pub extern "C" fn bigint_multiply_bytes(
    a_data: *const u8, a_len: usize,
    b_data: *const u8, b_len: usize
) -> *mut ByteArrayResult {
    let a = bytes_to_bigint(a_data, a_len);
    let b = bytes_to_bigint(b_data, b_len);
    bigint_to_byte_result(a * b)
}

#[no_mangle]
pub extern "C" fn bigint_divide_bytes(
    a_data: *const u8, a_len: usize,
    b_data: *const u8, b_len: usize
) -> *mut ByteArrayResult {
    let a = bytes_to_bigint(a_data, a_len);
    let b = bytes_to_bigint(b_data, b_len);
    if b.is_zero() {
        return std::ptr::null_mut();
    }
    bigint_to_byte_result(a / b)
}

#[no_mangle]
pub extern "C" fn bigint_mod_bytes(
    a_data: *const u8, a_len: usize,
    b_data: *const u8, b_len: usize
) -> *mut ByteArrayResult {
    let a = bytes_to_bigint(a_data, a_len);
    let b = bytes_to_bigint(b_data, b_len);
    if b.is_zero() {
        return std::ptr::null_mut();
    }
    bigint_to_byte_result(a % b)
}

#[no_mangle]
pub extern "C" fn bigint_abs_bytes(data: *const u8, len: usize) -> *mut ByteArrayResult {
    let n = bytes_to_bigint(data, len);
    bigint_to_byte_result(n.abs())
}

#[no_mangle]
pub extern "C" fn bigint_signum_bytes(data: *const u8, len: usize) -> i32 {
    let n = bytes_to_bigint(data, len);
    if n.is_zero() { 0 } else if n > BigInt::zero() { 1 } else { -1 }
}

#[no_mangle]
pub extern "C" fn bigint_compare_bytes(
    a_data: *const u8, a_len: usize,
    b_data: *const u8, b_len: usize
) -> i32 {
    let a = bytes_to_bigint(a_data, a_len);
    let b = bytes_to_bigint(b_data, b_len);
    if a < b { -1 } else if a > b { 1 } else { 0 }
}

#[no_mangle]
pub extern "C" fn bigint_to_long_bytes(data: *const u8, len: usize) -> i64 {
    let n = bytes_to_bigint(data, len);
    n.to_i64().unwrap_or(0)
}

// ==================== BigInteger Operations (Legacy String) ====================

#[no_mangle]
pub extern "C" fn bigint_add(a: *const c_char, b: *const c_char) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let a_big = match bigint_from_str(&a_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_big = match bigint_from_str(&b_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };

        let result = a_big + b_big;
        string_to_c_str(result.to_string())
    }
}

#[no_mangle]
pub extern "C" fn bigint_subtract(a: *const c_char, b: *const c_char) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let a_big = match bigint_from_str(&a_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_big = match bigint_from_str(&b_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };

        let result = a_big - b_big;
        string_to_c_str(result.to_string())
    }
}

#[no_mangle]
pub extern "C" fn bigint_multiply(a: *const c_char, b: *const c_char) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let a_big = match bigint_from_str(&a_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_big = match bigint_from_str(&b_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };

        let result = a_big * b_big;
        string_to_c_str(result.to_string())
    }
}

#[no_mangle]
pub extern "C" fn bigint_divide(a: *const c_char, b: *const c_char) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let a_big = match bigint_from_str(&a_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_big = match bigint_from_str(&b_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };

        if b_big.is_zero() {
            return std::ptr::null_mut(); // Division by zero
        }

        let result = a_big / b_big;
        string_to_c_str(result.to_string())
    }
}

#[no_mangle]
pub extern "C" fn bigint_mod(a: *const c_char, b: *const c_char) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let a_big = match bigint_from_str(&a_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_big = match bigint_from_str(&b_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };

        if b_big.is_zero() {
            return std::ptr::null_mut(); // Division by zero
        }

        let result = a_big % b_big;
        string_to_c_str(result.to_string())
    }
}

#[no_mangle]
pub extern "C" fn bigint_pow(base: *const c_char, exponent: u32) -> *mut c_char {
    unsafe {
        let base_str = match c_str_to_string(base) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let base_big = match bigint_from_str(&base_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };

        let result = num_traits::pow::pow(base_big, exponent as usize);
        string_to_c_str(result.to_string())
    }
}

#[no_mangle]
pub extern "C" fn bigint_abs(a: *const c_char) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let a_big = match bigint_from_str(&a_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };

        let result = a_big.abs();
        string_to_c_str(result.to_string())
    }
}

#[no_mangle]
pub extern "C" fn bigint_signum(a: *const c_char) -> i32 {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return 0,
        };

        let a_big = match bigint_from_str(&a_str) {
            Ok(n) => n,
            Err(_) => return 0,
        };

        if a_big.is_zero() {
            0
        } else if a_big > BigInt::zero() {
            1
        } else {
            -1
        }
    }
}

#[no_mangle]
pub extern "C" fn bigint_compare(a: *const c_char, b: *const c_char) -> i32 {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return 0,
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return 0,
        };

        let a_big = match bigint_from_str(&a_str) {
            Ok(n) => n,
            Err(_) => return 0,
        };
        let b_big = match bigint_from_str(&b_str) {
            Ok(n) => n,
            Err(_) => return 0,
        };

        if a_big < b_big {
            -1
        } else if a_big > b_big {
            1
        } else {
            0
        }
    }
}

#[no_mangle]
pub extern "C" fn bigint_gcd(a: *const c_char, b: *const c_char) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let a_big = match bigint_from_str(&a_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_big = match bigint_from_str(&b_str) {
            Ok(n) => n,
            Err(_) => return std::ptr::null_mut(),
        };

        let result = num_integer::gcd(a_big, b_big);
        string_to_c_str(result.to_string())
    }
}

#[no_mangle]
pub extern "C" fn bigint_to_long(a: *const c_char) -> i64 {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return 0,
        };

        let a_big = match bigint_from_str(&a_str) {
            Ok(n) => n,
            Err(_) => return 0,
        };

        a_big.to_i64().unwrap_or(0)
    }
}

// ==================== BigDecimal Operations ====================
// For BigDecimal, we'll use string-based arithmetic with explicit scale handling

#[no_mangle]
pub extern "C" fn bigdecimal_add(
    a: *const c_char,
    b: *const c_char,
    scale: i32,
) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        // Convert to integers by removing decimal point and tracking scale
        let (a_int, a_scale) = parse_decimal(&a_str);
        let (b_int, b_scale) = parse_decimal(&b_str);

        // Normalize to same scale
        let max_scale = a_scale.max(b_scale);
        let a_normalized = scale_up(a_int, a_scale, max_scale);
        let b_normalized = scale_up(b_int, b_scale, max_scale);

        let result = a_normalized + b_normalized;
        let result_str = format_decimal(result, max_scale.max(scale));

        string_to_c_str(result_str)
    }
}

#[no_mangle]
pub extern "C" fn bigdecimal_subtract(
    a: *const c_char,
    b: *const c_char,
    scale: i32,
) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let (a_int, a_scale) = parse_decimal(&a_str);
        let (b_int, b_scale) = parse_decimal(&b_str);

        let max_scale = a_scale.max(b_scale);
        let a_normalized = scale_up(a_int, a_scale, max_scale);
        let b_normalized = scale_up(b_int, b_scale, max_scale);

        let result = a_normalized - b_normalized;
        let result_str = format_decimal(result, max_scale.max(scale));

        string_to_c_str(result_str)
    }
}

#[no_mangle]
pub extern "C" fn bigdecimal_multiply(
    a: *const c_char,
    b: *const c_char,
    scale: i32,
) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let (a_int, a_scale) = parse_decimal(&a_str);
        let (b_int, b_scale) = parse_decimal(&b_str);

        let result = a_int * b_int;
        let result_scale = (a_scale + b_scale).max(scale);
        let result_str = format_decimal(result, result_scale);

        string_to_c_str(result_str)
    }
}

#[no_mangle]
pub extern "C" fn bigdecimal_divide(
    a: *const c_char,
    b: *const c_char,
    scale: i32,
) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let (a_int, a_scale) = parse_decimal(&a_str);
        let (b_int, b_scale) = parse_decimal(&b_str);

        if b_int.is_zero() {
            return std::ptr::null_mut();
        }

        // Calculate target scale for division
        // We need to scale up the numerator to get enough precision
        let target_scale = scale;
        let scale_diff = target_scale + b_scale - a_scale;

        let a_scaled = if scale_diff > 0 {
            scale_up(a_int, a_scale, a_scale + scale_diff)
        } else {
            a_int
        };

        let result = a_scaled / b_int.clone();
        let result_str = format_decimal(result, target_scale);

        string_to_c_str(result_str)
    }
}

#[no_mangle]
pub extern "C" fn bigdecimal_abs(a: *const c_char) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let (a_int, a_scale) = parse_decimal(&a_str);
        let result = a_int.abs();
        let result_str = format_decimal(result, a_scale);

        string_to_c_str(result_str)
    }
}

#[no_mangle]
pub extern "C" fn bigdecimal_signum(a: *const c_char) -> i32 {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return 0,
        };

        let (a_int, _) = parse_decimal(&a_str);

        if a_int.is_zero() {
            0
        } else if a_int > BigInt::zero() {
            1
        } else {
            -1
        }
    }
}

#[no_mangle]
pub extern "C" fn bigdecimal_compare(a: *const c_char, b: *const c_char) -> i32 {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return 0,
        };
        let b_str = match c_str_to_string(b) {
            Ok(s) => s,
            Err(_) => return 0,
        };

        let (a_int, a_scale) = parse_decimal(&a_str);
        let (b_int, b_scale) = parse_decimal(&b_str);

        let max_scale = a_scale.max(b_scale);
        let a_normalized = scale_up(a_int, a_scale, max_scale);
        let b_normalized = scale_up(b_int, b_scale, max_scale);

        if a_normalized < b_normalized {
            -1
        } else if a_normalized > b_normalized {
            1
        } else {
            0
        }
    }
}

#[no_mangle]
pub extern "C" fn bigdecimal_set_scale(
    a: *const c_char,
    scale: i32,
    rounding_mode: i32,
) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let (a_int, a_scale) = parse_decimal(&a_str);

        // If same scale, return as-is
        if a_scale == scale {
            return string_to_c_str(a_str);
        }

        // If increasing scale, just add zeros
        if scale > a_scale {
            let scaled = scale_up(a_int, a_scale, scale);
            return string_to_c_str(format_decimal(scaled, scale));
        }

        // Decreasing scale - need rounding
        let divisor = BigInt::from(10).pow((a_scale - scale) as u32);
        let quotient = &a_int / &divisor;
        let remainder = &a_int % &divisor;

        let result = if remainder.is_zero() {
            quotient
        } else {
            apply_rounding_mode(a_int, quotient, remainder, &divisor, rounding_mode)
        };

        string_to_c_str(format_decimal(result, scale))
    }
}

#[no_mangle]
pub extern "C" fn bigdecimal_to_biginteger(a: *const c_char) -> *mut c_char {
    unsafe {
        let a_str = match c_str_to_string(a) {
            Ok(s) => s,
            Err(_) => return std::ptr::null_mut(),
        };

        let (a_int, a_scale) = parse_decimal(&a_str);

        if a_scale <= 0 {
            return string_to_c_str(a_int.to_string());
        }

        // Truncate fractional part
        let divisor = BigInt::from(10).pow(a_scale as u32);
        let result = a_int / divisor;

        string_to_c_str(result.to_string())
    }
}

// Helper functions for decimal arithmetic

fn apply_rounding_mode(
    original: BigInt,
    quotient: BigInt,
    remainder: BigInt,
    divisor: &BigInt,
    mode: i32,
) -> BigInt {
    let is_negative = original < BigInt::zero();
    let abs_remainder = remainder.abs();
    let half = divisor / BigInt::from(2);

    match mode {
        0 => {
            // UP - Round away from zero
            if is_negative {
                quotient - BigInt::one()
            } else {
                quotient + BigInt::one()
            }
        }
        1 => {
            // DOWN - Round towards zero (truncate)
            quotient
        }
        2 => {
            // CEILING - Round towards positive infinity
            if is_negative {
                quotient // Already truncated for negative
            } else {
                quotient + BigInt::one()
            }
        }
        3 => {
            // FLOOR - Round towards negative infinity
            if is_negative {
                quotient - BigInt::one()
            } else {
                quotient // Already truncated for positive
            }
        }
        4 => {
            // HALF_UP - Round towards nearest neighbor, ties away from zero
            if abs_remainder >= half {
                if is_negative {
                    quotient - BigInt::one()
                } else {
                    quotient + BigInt::one()
                }
            } else {
                quotient
            }
        }
        5 => {
            // HALF_DOWN - Round towards nearest neighbor, ties towards zero
            if abs_remainder > half {
                if is_negative {
                    quotient - BigInt::one()
                } else {
                    quotient + BigInt::one()
                }
            } else {
                quotient
            }
        }
        6 => {
            // HALF_EVEN (Banker's rounding)
            if abs_remainder > half {
                if is_negative {
                    quotient - BigInt::one()
                } else {
                    quotient + BigInt::one()
                }
            } else if abs_remainder == half {
                // Round to even
                if &quotient % BigInt::from(2) == BigInt::zero() {
                    quotient // Already even
                } else {
                    if is_negative {
                        quotient - BigInt::one()
                    } else {
                        quotient + BigInt::one()
                    }
                }
            } else {
                quotient
            }
        }
        _ => quotient, // Default: truncate
    }
}

fn parse_decimal(s: &str) -> (BigInt, i32) {
    let s = s.trim();

    // Remove leading '+' sign if present (BigInt doesn't handle it)
    let s_clean = if s.starts_with('+') {
        &s[1..]
    } else {
        s
    };

    if let Some(dot_pos) = s_clean.find('.') {
        let int_part = &s_clean[..dot_pos];
        let frac_part = &s_clean[dot_pos + 1..];
        let combined = format!("{}{}", int_part, frac_part);
        let scale = frac_part.len() as i32;
        (BigInt::from_str(&combined).unwrap_or_else(|_| BigInt::zero()), scale)
    } else {
        (BigInt::from_str(s_clean).unwrap_or_else(|_| BigInt::zero()), 0)
    }
}

fn scale_up(num: BigInt, current_scale: i32, target_scale: i32) -> BigInt {
    if target_scale <= current_scale {
        return num;
    }
    let scale_factor = BigInt::from(10).pow((target_scale - current_scale) as u32);
    num * scale_factor
}

fn format_decimal(num: BigInt, scale: i32) -> String {
    if scale <= 0 {
        return num.to_string();
    }

    let s = num.to_string();
    let is_negative = s.starts_with('-');
    let abs_s = if is_negative { &s[1..] } else { &s };

    if abs_s.len() <= scale as usize {
        let padding = "0".repeat(scale as usize - abs_s.len() + 1);
        let result = format!("{}{}.{}", if is_negative { "-" } else { "" }, padding, abs_s);
        return result;
    }

    let split_pos = abs_s.len() - scale as usize;
    let int_part = &abs_s[..split_pos];
    let frac_part = &abs_s[split_pos..];

    format!("{}{}.{}", if is_negative { "-" } else { "" }, int_part, frac_part)
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::ffi::CString;

    #[test]
    fn test_set_scale_ceiling() {
        let input = CString::new("123.451").unwrap();
        let result_ptr = bigdecimal_set_scale(input.as_ptr(), 2, 2); // CEILING mode
        assert!(!result_ptr.is_null());
        unsafe {
            let result = CStr::from_ptr(result_ptr).to_str().unwrap();
            println!("setScale result: {}", result);
            assert_eq!(result, "123.46");
            bigint_free_string(result_ptr);
        }
    }

    #[test]
    fn test_divide_same_scale() {
        let a = CString::new("123.45").unwrap();
        let b = CString::new("12.34").unwrap();

        // Test division with scale=2
        let result_ptr = bigdecimal_divide(a.as_ptr(), b.as_ptr(), 2);
        assert!(!result_ptr.is_null());
        unsafe {
            let result = CStr::from_ptr(result_ptr).to_str().unwrap();
            println!("Divide 123.45 / 12.34 with scale=2: '{}'", result);
            println!("Result length: {}", result.len());
            println!("Result chars: {:?}", result.chars().collect::<Vec<_>>());

            // Count decimal places
            let parts: Vec<&str> = result.split('.').collect();
            if parts.len() == 2 {
                println!("Decimal part: '{}' (length: {})", parts[1], parts[1].len());
            }

            // 123.45 / 12.34 ≈ 10.00405...
            assert_eq!(result, "10.00", "Expected exactly '10.00'");
            bigint_free_string(result_ptr);
        }
    }

    #[test]
    fn test_bigint_add() {
        let a = CString::new("12345678901234567890").unwrap();
        let b = CString::new("98765432109876543210").unwrap();

        let result = unsafe { bigint_add(a.as_ptr(), b.as_ptr()) };
        assert!(!result.is_null());

        let result_str = unsafe { CStr::from_ptr(result).to_str().unwrap() };
        assert_eq!(result_str, "111111111011111111100");

        unsafe { bigint_free_string(result); }
    }

    #[test]
    fn test_bigint_multiply() {
        let a = CString::new("123456789012345678901234567890").unwrap();
        let b = CString::new("987654321098765432109876543210").unwrap();

        let result = unsafe { bigint_multiply(a.as_ptr(), b.as_ptr()) };
        assert!(!result.is_null());

        unsafe { bigint_free_string(result); }
    }

    #[test]
    fn test_bigdecimal_add() {
        let a = CString::new("123.456").unwrap();
        let b = CString::new("789.123").unwrap();

        let result = unsafe { bigdecimal_add(a.as_ptr(), b.as_ptr(), 3) };
        assert!(!result.is_null());

        let result_str = unsafe { CStr::from_ptr(result).to_str().unwrap() };
        assert_eq!(result_str, "912.579");

        unsafe { bigint_free_string(result); }
    }
}
