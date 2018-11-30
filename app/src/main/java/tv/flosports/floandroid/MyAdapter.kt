package tv.flosports.floandroid

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MyAdapter(private val myDataset: Array<Event>, private val myAdapterContext: Context) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val textView: TextView, var event: Event? = null) : RecyclerView.ViewHolder(textView)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_text_view, parent, false) as TextView
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(textView)
    }

    private fun getPreviewPlaylist(streamId: String): String {
        val url = "https://live-api-3.flosports.tv/streams/${streamId}/tokens/preview"
        val obj = URL(url)

        with(obj.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "POST"

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

                val playlist = ((responseStringJsonified["data"] as JSONObject)["playlist"] as JSONObject)
                val edge: String = playlist["edge"] as String

                val previewUrl: String = playlist["path"] as String
                val wholeUrl = "https://${edge}${previewUrl}"
                return wholeUrl
            }
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.text = myDataset[position].title
        holder.event = myDataset[position]

        holder.textView.setOnClickListener {
            holder.textView.setTextColor(Color.GREEN)

            doAsync {
                val watchVideoIntent = Intent(myAdapterContext, WatchVideoActivity::class.java)

                holder.event?.let {

                    nonNullEvent ->
                    val streamIdSafe = nonNullEvent.streamId
                    val streamUrl = getPreviewPlaylist(streamIdSafe)
                    watchVideoIntent.putExtra("streamUrl", streamUrl)
                    myAdapterContext.runOnUiThread {
                        myAdapterContext.startActivity(watchVideoIntent)
                    }
                }

            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}