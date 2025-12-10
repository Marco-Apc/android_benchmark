package com.example.androidbenchmark

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Debug
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.Random
import kotlin.system.measureNanoTime

data class MockPost(
    val id: Int, val title: String, val body: String, val userId: Int
)

interface JsonPlaceholderApi {
    @GET("photos")
    suspend fun getPhotos(): List<MockPhoto>
}

data class MockPhoto(val id: Int, val title: String, val url: String)


class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val gson = Gson()

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JsonPlaceholderApi::class.java)
    }

    private val textViews by lazy {
        mapOf(
            "sort10k" to findViewById<TextView>(R.id.txtResultSort10k),
            "sort100k" to findViewById<TextView>(R.id.txtResultSort100k),
            "sort1M" to findViewById<TextView>(R.id.txtResultSort1M),
            "dbWrite100" to findViewById<TextView>(R.id.txtResultDbWrite100),
            "dbWrite500" to findViewById<TextView>(R.id.txtResultDbWrite500),
            "dbWrite1k" to findViewById<TextView>(R.id.txtResultDbWrite1k),
            "dbRead100" to findViewById<TextView>(R.id.txtResultDbRead100),
            "dbRead500" to findViewById<TextView>(R.id.txtResultDbRead500),
            "dbRead1k" to findViewById<TextView>(R.id.txtResultDbRead1k),
            "fileWrite1k" to findViewById<TextView>(R.id.txtResultFileWrite1k),
            "fileWrite10k" to findViewById<TextView>(R.id.txtResultFileWrite10k),
            "fileWrite100k" to findViewById<TextView>(R.id.txtResultFileWrite100k),
            "fileRead1k" to findViewById<TextView>(R.id.txtResultFileRead1k),
            "fileRead10k" to findViewById<TextView>(R.id.txtResultFileRead10k),
            "fileRead100k" to findViewById<TextView>(R.id.txtResultFileRead100k),
            "uiTest" to findViewById<TextView>(R.id.txtResultUiTest),
            "memory" to findViewById<TextView>(R.id.txtResultMemory),
            "network" to findViewById<TextView>(R.id.txtResultNetwork),
            "json" to findViewById<TextView>(R.id.txtResultJson)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(applicationContext)

        setupClickListeners()

        findViewById<Button>(R.id.btnMemoryTest).text = "Ler Memória Atual"
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnSort10k).setOnClickListener {
            runSortTest(10_000, textViews["sort10k"]!!)
        }
        findViewById<Button>(R.id.btnSort100k).setOnClickListener {
            runSortTest(100_000, textViews["sort100k"]!!)
        }
        findViewById<Button>(R.id.btnSort1M).setOnClickListener {
            runSortTest(1_000_000, textViews["sort1M"]!!)
        }

        findViewById<Button>(R.id.btnDbWrite100).setOnClickListener {
            runDbWriteTest(100, textViews["dbWrite100"]!!)
        }
        findViewById<Button>(R.id.btnDbWrite500).setOnClickListener {
            runDbWriteTest(500, textViews["dbWrite500"]!!)
        }
        findViewById<Button>(R.id.btnDbWrite1k).setOnClickListener {
            runDbWriteTest(1_000, textViews["dbWrite1k"]!!)
        }

        findViewById<Button>(R.id.btnDbRead100).setOnClickListener {
            runDbReadTest(100, textViews["dbRead100"]!!)
        }
        findViewById<Button>(R.id.btnDbRead500).setOnClickListener {
            runDbReadTest(500, textViews["dbRead500"]!!)
        }
        findViewById<Button>(R.id.btnDbRead1k).setOnClickListener {
            runDbReadTest(1_000, textViews["dbRead1k"]!!)
        }

        findViewById<Button>(R.id.btnFileWrite1k).setOnClickListener {
            runFileWriteTest(1_000, textViews["fileWrite1k"]!!)
        }
        findViewById<Button>(R.id.btnFileWrite10k).setOnClickListener {
            runFileWriteTest(10_000, textViews["fileWrite10k"]!!)
        }
        findViewById<Button>(R.id.btnFileWrite100k).setOnClickListener {
            runFileWriteTest(100_000, textViews["fileWrite100k"]!!)
        }

        findViewById<Button>(R.id.btnFileRead1k).setOnClickListener {
            runFileReadTest(1_000, textViews["fileRead1k"]!!)
        }
        findViewById<Button>(R.id.btnFileRead10k).setOnClickListener {
            runFileReadTest(10_000, textViews["fileRead10k"]!!)
        }
        findViewById<Button>(R.id.btnFileRead100k).setOnClickListener {
            runFileReadTest(100_000, textViews["fileRead100k"]!!)
        }

        findViewById<Button>(R.id.btnUiTestList).setOnClickListener {
            startActivity(Intent(this, UiTestActivity::class.java))
        }

        findViewById<Button>(R.id.btnMemoryTest).setOnClickListener {
            readCurrentMemory(textViews["memory"]!!)
        }

        findViewById<Button>(R.id.btnNetworkTest).setOnClickListener {
            runNetworkTest(textViews["network"]!!)
        }
        findViewById<Button>(R.id.btnJsonTest).setOnClickListener {
            runJsonTest(textViews["json"]!!)
        }
    }

    private fun runSortTest(count: Int, resultView: TextView) {
        lifecycleScope.launch(Dispatchers.Main) {
            resultView.text = "Rodando..."
            val timeInMillis = withContext(Dispatchers.Default) {
                val random = Random(System.currentTimeMillis())
                val array = IntArray(count) { random.nextInt() }

                val nanoTime = measureNanoTime {
                    array.sort()
                }
                nanoTime / 1_000_000.0
            }
            resultView.text = String.format("%.3f ms", timeInMillis)
        }
    }

    private fun runDbWriteTest(count: Int, resultView: TextView): Job {
        return lifecycleScope.launch(Dispatchers.Main) {
            resultView.text = "Rodando..."
            val timeInMillis = withContext(Dispatchers.IO) {
                val db = dbHelper.writableDatabase
                dbHelper.clearTable(db)

                val nanoTime = measureNanoTime {
                    db.beginTransaction()
                    try {
                        for (i in 1..count) {
                            val values = ContentValues().apply {
                                put(
                                    DbContract.Entry.COLUMN_NAME_DATA,
                                    "Registro #$i - ${System.nanoTime()}"
                                )
                            }
                            db.insert(DbContract.Entry.TABLE_NAME, null, values)
                        }
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
                db.close()
                nanoTime / 1_000_000.0
            }
            resultView.text = String.format("%.3f ms", timeInMillis)
        }
    }

    private fun runDbReadTest(count: Int, resultView: TextView) {
        lifecycleScope.launch(Dispatchers.IO) {

            val writeResultView = textViews["dbWrite${count}"] ?: resultView
            val writeJob = runDbWriteTest(count, writeResultView)

            writeJob.join()

            withContext(Dispatchers.Main) {
                resultView.text = "Rodando..."
            }

            val timeInMillis = withContext(Dispatchers.IO) {
                val db = dbHelper.readableDatabase
                val list = mutableListOf<String>()

                val nanoTime = measureNanoTime {
                    val cursor = db.query(
                        DbContract.Entry.TABLE_NAME, null, null, null, null, null, null, "$count"
                    )
                    with(cursor) {
                        while (moveToNext()) {
                            val data =
                                getString(getColumnIndexOrThrow(DbContract.Entry.COLUMN_NAME_DATA))
                            list.add(data)
                        }
                        close()
                    }
                }
                db.close()

                if (list.size != count) {
                    withContext(Dispatchers.Main) {
                        resultView.text = "Erro: Lidos ${list.size} de $count"
                    }
                }

                nanoTime / 1_000_000.0
            }

            withContext(Dispatchers.Main) {
                if (!resultView.text.startsWith("Erro")) {
                    resultView.text = String.format("%.3f ms", timeInMillis)
                }
            }
        }
    }

    private val ALPHANUMERIC_CHARS = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private fun generateRandomString(length: Int): String {
        val random = Random()
        return (1..length).map { ALPHANUMERIC_CHARS[random.nextInt(ALPHANUMERIC_CHARS.size)] }
            .joinToString("")
    }

    private fun formatMb(bytes: Long): String {
        return String.format("%.1f", bytes / (1024.0 * 1024.0))
    }

    private fun runFileWriteTest(charCount: Int, resultView: TextView): Job {
        return lifecycleScope.launch(Dispatchers.Main) {
            resultView.text = "Rodando..."
            val timeInMillis = withContext(Dispatchers.IO) {
                val data = generateRandomString(charCount)
                val file = File(applicationContext.filesDir, "benchmark_file_$charCount.txt")

                val nanoTime = measureNanoTime {
                    file.writeText(data)
                }
                nanoTime / 1_000_000.0
            }
            resultView.text = String.format("%.3f ms", timeInMillis)
        }
    }

    private fun runFileReadTest(charCount: Int, resultView: TextView) {
        lifecycleScope.launch(Dispatchers.IO) {

            val file = File(applicationContext.filesDir, "benchmark_file_$charCount.txt")

            if (!file.exists()) {
                withContext(Dispatchers.Main) {
                    resultView.text = "Criando..."
                }
                val writeJob =
                    runFileWriteTest(charCount, textViews["fileWrite${charCount}"] ?: resultView)
                writeJob.join()
            }

            withContext(Dispatchers.Main) {
                resultView.text = "Rodando..."
            }

            var readLength = 0
            val timeInMillis = withContext(Dispatchers.IO) {
                var data: String
                val nanoTime = measureNanoTime {
                    data = file.readText()
                    readLength = data.length
                }
                nanoTime / 1_000_000.0
            }

            withContext(Dispatchers.Main) {
                if (readLength != charCount) {
                    resultView.text = "Erro: Lidos $readLength de $charCount"
                } else {
                    resultView.text = String.format("%.3f ms", timeInMillis)
                }
            }
        }
    }

    private fun runNetworkTest(resultView: TextView) {
        lifecycleScope.launch(Dispatchers.Main) {
            resultView.text = "Baixando..."
            try {
                val timeInMillis = withContext(Dispatchers.IO) {
                    val nanoTime = measureNanoTime {
                        val photos = api.getPhotos()
                        val count = photos.size
                    }
                    nanoTime / 1_000_000.0
                }
                resultView.text = String.format("%.3f ms", timeInMillis)
            } catch (e: Exception) {
                resultView.text = "Erro: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    private fun runJsonTest(resultView: TextView) {
        lifecycleScope.launch(Dispatchers.Main) {
            resultView.text = "Gerando..."

            val jsonString = withContext(Dispatchers.Default) {
                val list = ArrayList<MockPost>()
                for (i in 1..10000) {
                    list.add(
                        MockPost(
                            i,
                            "Título $i",
                            "Corpo do post $i repetido muitas vezes para dar volume.",
                            i
                        )
                    )
                }
                gson.toJson(list)
            }

            resultView.text = "Parsing..."

            val timeInMillis = withContext(Dispatchers.Default) {
                val type = object : TypeToken<List<MockPost>>() {}.type

                val nanoTime = measureNanoTime {
                    val posts: List<MockPost> = gson.fromJson(jsonString, type)
                    val size = posts.size
                }
                nanoTime / 1_000_000.0
            }
            resultView.text = String.format("%.3f ms", timeInMillis)
        }
    }

    private fun readCurrentMemory(resultView: TextView) {
        System.gc()

        lifecycleScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) { Thread.sleep(100) }

            val runtime = Runtime.getRuntime()
            val javaHeap = runtime.totalMemory() - runtime.freeMemory()
            val nativeHeap = Debug.getNativeHeapAllocatedSize()

            val totalUsed = javaHeap + nativeHeap

            resultView.text =
                "Total: ${formatMb(totalUsed)} MB" +
                        "\n(Java: ${formatMb(javaHeap)}" +
                        " + Nativo: ${
                            formatMb(nativeHeap)
                        })"
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
