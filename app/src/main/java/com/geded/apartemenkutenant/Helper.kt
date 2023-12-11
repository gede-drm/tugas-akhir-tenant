package com.geded.apartemenkutenant

import java.text.DecimalFormat

class Helper {
    companion object{
        fun formatter(n: Double): String {
            if(n == 0.0){
                return "0.00"
            }else {
                return DecimalFormat("#,###.00").format(n)
            }
        }
    }
}