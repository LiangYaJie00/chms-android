package com.example.chms_android.common.enums

/**
 * 医生建议类型枚举
 */
enum class AdviceType(val code: Int, val displayName: String) {
    /**
     * 一般建议
     */
    GENERAL(0, "一般建议"),
    
    /**
     * 饮食建议
     */
    DIET(1, "饮食建议"),
    
    /**
     * 运动建议
     */
    EXERCISE(2, "运动建议"),
    
    /**
     * 用药建议
     */
    MEDICATION(3, "用药建议"),
    
    /**
     * 其他
     */
    OTHER(4, "其他");
    
    companion object {
        /**
         * 根据代码获取枚举值
         * @param code 代码
         * @return 枚举值，如果找不到则返回null
         */
        fun getByCode(code: Int): AdviceType? {
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