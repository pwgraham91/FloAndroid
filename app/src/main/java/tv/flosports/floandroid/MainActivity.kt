package tv.flosports.floandroid

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

data class ViewModel(var message: String = "original message", var number: Int = 0) {
    var messageVisibility: Int = View.VISIBLE
}

data class Event(val id: Int, val node_id: Int, val title: String, val type: String, val image_url: String? = null)

class MainActivity : AppCompatActivity() {
    private val viewModel = ViewModel()

    private fun applyViewModel() {
        println("${viewModel.message} ${viewModel.number}")
        this.runOnUiThread {
            message.text = viewModel.message
            message.visibility = viewModel.messageVisibility
        }
    }

    fun getViewTabData(): Array<Event> {
        val url = "https://api.flosports.tv/api/mobile/watch?site_id=7"
        val obj = URL(url)

        with(obj.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "GET"

            println("\nSending 'GET' request to URL : $url")
            println("Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                val responseString = response.toString()
                val responseStringJsonified = JSONObject(responseString)
                val eventsData = responseStringJsonified["data"]
                val events: MutableList<Event> = ArrayList()

                for (i in 0 until (eventsData as JSONArray).length()) {
                    val eventData = (eventsData[i] as JSONObject)
                    val id: Int = eventData["id"] as Int
                    val nodeId: Int = eventData["node_id"] as Int
                    val title: String = eventData["title"] as String
                    val type: String = eventData["type"] as String
                    val imageUrl = (eventData["asset"] as JSONObject)["url"] as? String

                    val event = Event(id, nodeId, title, type, imageUrl)
                    events.add(event)
                }
                return events.toTypedArray()
            }
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private fun initRecyclerView(eventList: Array<Event>) {
        this.runOnUiThread {
            viewManager = LinearLayoutManager(this)
            viewAdapter = MyAdapter(eventList)

            recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = viewAdapter

                viewModel.messageVisibility = View.INVISIBLE
                applyViewModel()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        doAsync {
            viewModel.message = "on create message"
            applyViewModel()
            val events = getViewTabData()
            initRecyclerView(events)

        }
    }
}
