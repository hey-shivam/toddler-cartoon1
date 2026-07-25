package com.example.toddlercartoons

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class StoredVideo(
    val id: String,
    val title: String,
    val uriString: String,
    val thumbnailPath: String?
)

/**
 * Handles the parent-facing "add / remove video" workflow. Videos are NOT copied
 * into the app by default — we take a persistable permission on the Uri the parent
 * picked (from Files, Gallery, Downloads, wherever) so it keeps playing even after
 * the phone restarts, without duplicating storage space.
 *
 * A small thumbnail is generated once per video and cached to internal storage so
 * MainActivity doesn't have to re-decode the video every time it draws the grid.
 */
class VideoManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("toddler_cartoons_videos", Context.MODE_PRIVATE)
    private val thumbDir = File(context.filesDir, "thumbnails").apply { if (!exists()) mkdirs() }

    companion object {
        private const val KEY_VIDEO_LIST = "video_list_json"
    }

    fun getVideos(): List<StoredVideo> {
        val json = prefs.getString(KEY_VIDEO_LIST, "[]") ?: "[]"
        val array = JSONArray(json)
        val result = mutableListOf<StoredVideo>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(
                StoredVideo(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    uriString = obj.getString("uri"),
                    thumbnailPath = obj.optString("thumb", "").ifEmpty { null }
                )
            )
        }
        return result
    }

    private fun saveVideos(videos: List<StoredVideo>) {
        val array = JSONArray()
        for (v in videos) {
            val obj = JSONObject()
            obj.put("id", v.id)
            obj.put("title", v.title)
            obj.put("uri", v.uriString)
            obj.put("thumb", v.thumbnailPath ?: "")
            array.put(obj)
        }
        prefs.edit().putString(KEY_VIDEO_LIST, array.toString()).apply()
    }

    /**
     * Call after the parent picks a video with ACTION_OPEN_DOCUMENT.
     * Takes persistable permission, generates a thumbnail, and stores the entry.
     * Returns the new StoredVideo, or null if something about the file was unreadable.
     */
    fun addVideo(uri: Uri, title: String): StoredVideo? {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
            // Some providers don't support persistable permission; playback may still
            // work for this session even if it won't survive a reboot.
        }

        val id = "vid_${System.currentTimeMillis()}"
        val thumbPath = generateThumbnail(uri, id)

        val newVideo = StoredVideo(id, title, uri.toString(), thumbPath)
        val current = getVideos().toMutableList()
        current.add(newVideo)
        saveVideos(current)
        return newVideo
    }

    fun removeVideo(id: String) {
        val current = getVideos().toMutableList()
        val toRemove = current.find { it.id == id }
        toRemove?.thumbnailPath?.let { path ->
            val f = File(path)
            if (f.exists()) f.delete()
        }
        current.removeAll { it.id == id }
        saveVideos(current)
    }

    private fun generateThumbnail(uri: Uri, id: String): String? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val bitmap: Bitmap? = retriever.getFrameAtTime(1_000_000) // 1 second in
            retriever.release()
            if (bitmap == null) return null

            val outFile = File(thumbDir, "$id.jpg")
            FileOutputStream(outFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            outFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
