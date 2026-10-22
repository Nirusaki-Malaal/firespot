package com.fireemu.data.catalog

import org.json.JSONObject

data class DownloadAsset(val file:String,val url:String,val sha256:String,val bytes:Long,val abi:String?=null)
data class CatalogCore(val id:String,val title:String,val consoleIds:List<String>,val license:String,val sourceUrl:String,val sourceArchiveUrl:String,val experimental:Boolean,val artifacts:List<DownloadAsset>)
data class CatalogGame(val id:String,val title:String,val consoleId:String,val description:String,val license:String,val author:String,val asset:DownloadAsset,val genre:String="Homebrew",val year:Int=2026,val players:Int=1,val sourceUrl:String="")
data class Catalog(val revision:Int,val cores:List<CatalogCore>,val games:List<CatalogGame>) {
    companion object {
        fun parse(payload:ByteArray):Catalog {
            val json=JSONObject(String(payload,Charsets.UTF_8));require(json.getInt("schema")==1)
            fun asset(o:JSONObject):DownloadAsset {
                val file=o.getString("file");require(file.matches(Regex("[a-zA-Z0-9_.-]+")) && file!="." && file!="..")
                val hash=o.getString("sha256");require(hash.matches(Regex("[a-f0-9]{64}")))
                val size=o.getLong("bytes");require(size in 1..64L*1024*1024)
                return DownloadAsset(file,o.getString("url"),hash,size,o.optString("abi").takeIf {it.isNotEmpty()})
            }
            val cs=json.getJSONArray("cores");val gs=json.getJSONArray("games")
            require(cs.length()<=64 && gs.length()<=500)
            val cores=(0 until cs.length()).map {i->val c=cs.getJSONObject(i);val ids=c.getJSONArray("consoleIds");val aa=c.getJSONArray("artifacts")
                CatalogCore(c.getString("id"),c.getString("title"),(0 until ids.length()).map {ids.getString(it)},c.getString("license"),c.getString("sourceUrl"),c.getString("sourceArchiveUrl"),c.optBoolean("experimental",true),(0 until aa.length()).map {asset(aa.getJSONObject(it))})}
            val games=(0 until gs.length()).map {i->val g=gs.getJSONObject(i);CatalogGame(g.getString("id"),g.getString("title"),g.getString("consoleId"),g.getString("description"),g.getString("license"),g.getString("author"),asset(g.getJSONObject("artifact")),g.optString("genre","Homebrew"),g.optInt("year",2026),g.optInt("players",1),g.optString("sourceUrl"))}
            require((cores.map {it.id}+games.map {it.id}).all {it.matches(Regex("[a-z0-9-]{1,60}"))})
            require(cores.map {it.id}.distinct().size==cores.size && games.map {it.id}.distinct().size==games.size)
            return Catalog(json.getInt("revision"),cores,games)
        }
    }
}
