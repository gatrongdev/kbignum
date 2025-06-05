package vn.com.gatrong.kbignum

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform