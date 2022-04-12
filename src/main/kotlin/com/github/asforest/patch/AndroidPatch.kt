package com.github.asforest.patch

import com.github.asforest.data.FileObj
import com.github.asforest.data.SimpleDirectory
import com.github.asforest.data.SimpleFile
import com.github.asforest.data.SimpleFileObject
import com.github.asforest.util.Utils
import org.json.JSONObject

class AndroidPatch(val patchFile: FileObj?)
{
    val content = mutableMapOf<String, Pair<Long, Long>>()
    val isActive = patchFile != null

    /**
     * 从补丁文件读取补丁数据
     * @return 读取到的文件数量，如果文件不存在，则返回null
     */
    fun load(): Int?
    {
        if (patchFile == null || !patchFile.exists)
            return null

        // 读取
        val json = JSONObject(patchFile.content)

        for (key in json.keys())
        {
            val value = json.getString(key)
            val sp = value.split(":")
            content[key] = Pair(sp[0].toLong(), sp[1].toLong())
        }

        return content.size
    }

    /**
     * 更新补丁文件数据
     * @param local 本地目录数据
     * @param remote 远程目录数据
     */
    fun update(local: FileObj, remote: List<SimpleFileObject>)
    {
        if (patchFile == null || !patchFile.exists)
            throw IllegalStateException("未启用补丁文件时不能更新补丁文件的内容")

        val data = mutableMapOf<String, String>()
        val rFiles = SimpleDirectory("", remote)

        Utils.walkFile(local, local) { file, path ->
            var temp: SimpleDirectory = rFiles
            val sp = path.split("/")
            for (s in sp.subList(0, sp.size - 1))
                temp = (temp[s] ?: return@walkFile) as SimpleDirectory
            val r = (temp[sp.last()] ?: return@walkFile) as SimpleFile

            data[path] = "${file.modified}:${r.modified}"
        }

        patchFile.content = JSONObject(data).toString(2)
    }

    operator fun get(path: String): Pair<Long, Long>?
    {
        return content[path]
    }

    companion object {
        @JvmStatic
        fun Disabled(): AndroidPatch
        {
            return AndroidPatch(null)
        }
    }
}