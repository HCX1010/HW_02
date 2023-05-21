package com.example.hw_02

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewParent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import okhttp3.*
import java.io.IOException
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //版面設計
        val county_spinner = findViewById<Spinner>(R.id.spinner)
        val adapter_county = ArrayAdapter.createFromResource(this, R.array.county, android.R.layout.simple_spinner_dropdown_item)
        county_spinner.adapter = adapter_county

        val time_spinner = findViewById<Spinner>(R.id.spinner2)
        val currentDate = LocalDate.now()//當下日期
        val year = currentDate.year
        val month = currentDate.monthValue
        val day = currentDate.dayOfMonth
        val tomorrow = currentDate.plusDays(1)//隔天日期
        val tomorrow_year = tomorrow.year
        val tomorrow_month = tomorrow.monthValue
        val tomorrow_day = tomorrow.dayOfMonth

        val interval_time = arrayListOf("選擇時間","$year/$month/$day 12:00 ~ 18:00","$year/$month/$day 18:00 ~ 06:00","$tomorrow_year/$tomorrow_month/$tomorrow_day 06:00 ~ 18:00")
        val adapter_time = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, interval_time)
        time_spinner.adapter = adapter_time

        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageResource(R.drawable.rain)
        val imageView2 = findViewById<ImageView>(R.id.imageView2)
        imageView2.setImageResource(R.drawable.temperature)

        //監聽所選擇的縣市和時間(POST)
        time_spinner.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                WeatherData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 在这里处理未选择任何项的逻辑
            }
        }
        county_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                WeatherData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 在这里处理未选择任何项的逻辑
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun WeatherData(){
                val county_spinner = findViewById<Spinner>(R.id.spinner)
                val select_county = county_spinner.selectedItem.toString()
                val time_spinner = findViewById<Spinner>(R.id.spinner2)
                val select_time = time_spinner.selectedItem.toString()
                val currentDate = LocalDate.now()//當下日期
                val year = currentDate.year
                val month = currentDate.monthValue
                val day = currentDate.dayOfMonth
                val tomorrow = currentDate.plusDays(1)//隔天日期
                val tomorrow_year = tomorrow.year
                val tomorrow_month = tomorrow.monthValue
                val tomorrow_day = tomorrow.dayOfMonth
                // 所选的县市加载相应的天气数据
                Log.d("Spinner", "Selected county: $select_county")
                Log.d("Spinner2", "Selected time: $select_time")

                //呼叫所選擇縣市和時段的天氣預報(GET)
                val client = OkHttpClient()
                val url =
                    "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization=CWB-C634345D-5538-4D5E-8114-BACE0174F044&locationName="
                val request = Request.Builder()
                    .url(url)
                    .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            println(responseBody)

                            val gson = Gson()
                            val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)

                            //逐步匹配縣市,找出縣市所在陣列的位置
                            val json_array_locationName = jsonObject
                                .getAsJsonObject("records")
                                .getAsJsonArray("location")

                            for (i in 0 until json_array_locationName.size()) {
                                val location_name = json_array_locationName
                                    .get(i)
                                    .asJsonObject
                                    .get("locationName")
                                    .asString
                                if (location_name == select_county) {
                                    //取得回應回來內容
                                    runOnUiThread {
                                        //將該縣市的資料掉入textview中
                                        val json_array_weatherElement = jsonObject
                                            .getAsJsonObject("records")
                                            .getAsJsonArray("location")
                                            .get(i)
                                            .asJsonObject
                                            .getAsJsonArray("weatherElement")
                                        for (j in 0 until json_array_weatherElement.size()) {
                                            val elementName = json_array_weatherElement
                                                .get(j)
                                                .asJsonObject
                                                .get("elementName")
                                                .asString

                                            //將天氣現象(Wx)放入text
                                            if (elementName == "Wx") {
                                                if (select_time == "$year/$month/$day 12:00 ~ 18:00") {
                                                    val find_time = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter = find_time
                                                        .get(0)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView).text = "天氣狀況:$find_parameter"
                                                } else if (select_time == "$year/$month/$day 18:00 ~ 06:00") {
                                                    val find_time = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter = find_time
                                                        .get(1)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView).text = "天氣狀況:$find_parameter"
                                                } else if (select_time == "$tomorrow_year/$tomorrow_month/$tomorrow_day 06:00 ~ 18:00") {
                                                    val find_time = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter = find_time
                                                        .get(2)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView).text = "天氣狀況:$find_parameter"
                                                }
                                            }
                                            if (elementName == "PoP") {
                                                if (select_time == "$year/$month/$day 12:00 ~ 18:00") {
                                                    val find_time = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter = find_time
                                                        .get(0)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView3).text = "降雨機率:$find_parameter%"
                                                } else if (select_time == "$year/$month/$day 18:00 ~ 06:00") {
                                                    val find_time = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter = find_time
                                                        .get(1)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView3).text = "降雨機率:$find_parameter%"
                                                } else if (select_time == "$tomorrow_year/$tomorrow_month/$tomorrow_day 06:00 ~ 18:00") {
                                                    val find_time = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter = find_time
                                                        .get(2)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView3).text = "降雨機率:$find_parameter%"
                                                }
                                            }
                                            if (elementName == "MinT") {
                                                if (select_time == "$year/$month/$day 12:00 ~ 18:00") {
                                                    val find_time_MinT = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter_MinT = find_time_MinT
                                                        .get(0)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    val find_time_MaxT = json_array_weatherElement
                                                        .get(j+2)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter_MaxT = find_time_MaxT
                                                        .get(0)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView2).text = "體感溫度:$find_parameter_MinT-$find_parameter_MaxT"
                                                } else if (select_time == "$year/$month/$day 18:00 ~ 06:00") {
                                                    val find_time_MinT = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter_MinT = find_time_MinT
                                                        .get(1)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    val find_time_MaxT = json_array_weatherElement
                                                        .get(j+2)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter_MaxT = find_time_MaxT
                                                        .get(1)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView2).text = "體感溫度:$find_parameter_MinT-$find_parameter_MaxT"
                                                } else if (select_time == "$tomorrow_year/$tomorrow_month/$tomorrow_day 06:00 ~ 18:00") {
                                                    val find_time_MinT = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter_MinT = find_time_MinT
                                                        .get(2)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    val find_time_MaxT = json_array_weatherElement
                                                        .get(j+2)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter_MaxT = find_time_MaxT
                                                        .get(2)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView2).text = "體感溫度:$find_parameter_MinT-$find_parameter_MaxT"
                                                }
                                            }
                                            if (elementName == "CI") {
                                                if (select_time == "$year/$month/$day 12:00 ~ 18:00") {
                                                    val find_time = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter = find_time
                                                        .get(0)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView4).text = find_parameter
                                                } else if (select_time == "$year/$month/$day 18:00 ~ 06:00") {
                                                    val find_time = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter = find_time
                                                        .get(1)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView4).text = find_parameter
                                                } else if (select_time == "$tomorrow_year/$tomorrow_month/$tomorrow_day 06:00 ~ 18:00") {
                                                    val find_time = json_array_weatherElement
                                                        .get(j)
                                                        .asJsonObject
                                                        .getAsJsonArray("time")
                                                    val find_parameter = find_time
                                                        .get(2)
                                                        .asJsonObject
                                                        .getAsJsonObject("parameter")
                                                        .get("parameterName")
                                                        .asString
                                                    findViewById<TextView>(R.id.textView4).text = find_parameter
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            println("Request failed")
                            runOnUiThread {
                                //直接將內容回傳給id名稱為TextView
                                findViewById<TextView>(R.id.textView).text = "資料錯誤"
                            }
                        }
                    }

                })
    }
}


