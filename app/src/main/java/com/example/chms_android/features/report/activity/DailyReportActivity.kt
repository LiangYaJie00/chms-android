package com.example.chms_android.features.report.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chms_android.R
import com.example.chms_android.data.CustomLineData
import com.example.chms_android.databinding.ActivityDailyReportBinding
import com.example.chms_android.features.report.adapter.ReportAdapter
import com.github.mikephil.charting.data.Entry
import java.util.Calendar

class DailyReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDailyReportBinding
    private var chartDataList: ArrayList<CustomLineData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDailyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 设置状态栏颜色
        window.statusBarColor = ContextCompat.getColor(this, R.color.actionbar_color)

        initListData()

        val status: Int? = intent.getIntExtra("status", 0)

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerViewDailyReport.layoutManager = layoutManager
        val adapter = status?.let { ReportAdapter(this, chartDataList, it) }
        binding.recyclerViewDailyReport.adapter = adapter
        binding.recyclerViewDailyReport.setHasFixedSize(true)

        if (status == 1) {
            binding.titleBarAdr.setTitleText("月报")
        }
    }

    // 初始化数据，尚未完成对具有两种数据的图表合并
    private fun initListData() {
        // 查询数据库检测的所有数据
        val time = Calendar.getInstance()
        val date =
            "${time.get(Calendar.YEAR)}-${time.get(Calendar.MONTH) + 1}-${time.get(Calendar.DAY_OF_MONTH)}"

        val listOfheartRate = ArrayList<Entry>()

        // 使用随机数生成器
        val random = java.util.Random()

        // 补全到24小时
        for (hour in 0..24 step 2) {
            if (listOfheartRate.none { it.x == hour.toFloat() }) {
                // 生成随机心率值，假设心率在 60 到 100 之间随机
                val randomHeartRate = 60 + random.nextFloat() * 40
                listOfheartRate.add(Entry(hour.toFloat(), randomHeartRate))
            }
        }
        // 根据查询的 今日 检测次数进行对 list 扩充
        chartDataList.add(CustomLineData(listOfheartRate, "心率", 60F, 100F, date, 0F, 120F))


        // 根据查询的 今日 检测次数进行对 list 扩充
        val listOfBOA = ArrayList<Entry>()  // 动脉血氧量
        // 补全到24小时
        for (hour in 0..24 step 2) {
            if (listOfBOA.none { it.x == hour.toFloat() }) {
                // 生成随机心率值，假设心率在 60 到 100 之间随机
                val randomHeartRate = 140 + random.nextFloat() * 90
                listOfBOA.add(Entry(hour.toFloat(), randomHeartRate))
            }
        }
        chartDataList.add(
            CustomLineData(
                list = listOfBOA,
                type = "动脉血氧量",
                low = 150F,
                date = date,
                high = 230F,
                start = 70F,
                end = 300F
            )
        )

        val listOfBOS = ArrayList<Entry>()  // 静脉血氧量
        // 补全到24小时
        for (hour in 0..24 step 2) {
            if (listOfBOS.none { it.x == hour.toFloat() }) {
                // 生成随机心率值，假设心率在 60 到 100 之间随机
                val randomHeartRate = 100 + random.nextFloat() * 90
                listOfBOS.add(Entry(hour.toFloat(), randomHeartRate))
            }
        }
        chartDataList.add(
            CustomLineData(
                list = listOfBOS,
                type = "静脉血氧量",
                low = 100F,
                high = 180F,
                date = date,
                start = 50F,
                end = 250F
            )
        )

        val listOfBPS = ArrayList<Entry>()  // 收缩压
        val listOfBPD = ArrayList<Entry>()  // 舒张压
        // 根据查询的 今日 检测次数进行对 list 扩充
        chartDataList.add(
            CustomLineData(
                list = listOfBPS,
                type = "收缩压",
                date = date,
                low = 90F,
                high = 139F,
                start = 30F,
                end = 200F
            )
        )
        chartDataList.add(
            CustomLineData(
                list = listOfBPD,
                type = "舒张压",
                date = date,
                low = 60F,
                high = 89F,
                start = 0F,
                end = 150F
            )
        )

        val listOfBS = ArrayList<Entry>()
        // 根据查询的 今日 检测次数进行对 list 扩充
        chartDataList.add(CustomLineData(listOfBS, "血糖", 70F, 100F, date, 10F, 150F))

        val listOfTemperature = ArrayList<Entry>()
        // 根据查询的 今日 检测次数进行对 list 扩充
        chartDataList.add(CustomLineData(listOfTemperature, "体温", 36.3F, 37.5F, date, 33F, 43F))

    }

}