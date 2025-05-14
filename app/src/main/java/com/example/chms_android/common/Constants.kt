package com.example.chms_android.common

object Constants {
//    const val IP = "10.0.2.2"
    const val IP = "172.21.70.101"
//    const val IP = "192.168.80.91"

    const val BASE_URL = "http://${this.IP}:8080/chms"

    const val TEST_URL = "http://${this.IP}:8080/chms/test/hello"

    const val DATABASE_NAME = "chms.db"
    const val DATABASE_VERSION = 1

    const val IS_TESTING = false // 是否在测试环境

//    const val TENCENT_TRTC_SDKAPPID = 1600078530
//    const val TENCENT_TRTC_SECRETKEY = "7187547f2eff3f4b06f3c93196ba2c678a9b5771d65749f57e0d7ff099bb9e78"
}