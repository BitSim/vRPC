package com.bitsim.vrpc.entity

/**
 *@author  BitSim
 *@version  v1.0.0

 **/
data class RemoteServiceInfo(
    val serviceName: String = "",
    val serviceVersion: String = "1.0.0",
    var serviceHost: String = "localhost",
    var servicePort: Int = 8888,
){
    // 服务与版本号的组合
    val serviceKey: String
        get() = "$serviceName:$serviceVersion"
}
