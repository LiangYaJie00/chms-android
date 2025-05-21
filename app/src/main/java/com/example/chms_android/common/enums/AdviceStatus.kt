package com.example.chms_android.common.enums

/**
 * 医生建议状态枚举
 */
enum class AdviceStatus(val code: Int, val displayName: String) {
    /**
     * 未读
     */
    UNREAD(0, "未读"),
    
    /**
     * 已读
     */
    READ(1, "已读");
    
    companion object {
        /**
         * 根据代码获取枚举值
         * @param code 代码
         * @return 枚举值，如果找不到则返回null
         */
        fun getByCode(code: Int): AdviceStatus? {
            return values().find { it.code == code }
        }
        
        /**
         * 根据代码获取名称
         * @param code 代码
         * @return 名称，如果找不到则返回"未知"
         */
        fun getNameByCode(code: Int): String {
            return getByCode(code)?.displayName ?: "未知"
        }
    }
}