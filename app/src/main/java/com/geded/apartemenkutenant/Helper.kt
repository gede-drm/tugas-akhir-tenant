package com.geded.apartemenkutenant

import java.text.DecimalFormat

class Helper {
    companion object{
        fun formatter(n: Double): String {
            return DecimalFormat("#,###.00").format(n)
        }
    }
}