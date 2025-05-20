package com.example.chms_android.dto

data class PublicationDTO(
    val title: String? = null,     // 论文/著作标题
    val journal: String? = null,   // 期刊/出版社名称
    val publishDate: String? = null, // 发表/出版日期
    val url: String? = null        // 链接地址
)