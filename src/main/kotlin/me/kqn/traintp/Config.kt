package me.kqn.traintp

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.util.random
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.configuration.util.getLocation
import taboolib.module.configuration.util.getStringColored
import java.io.File

class Config {
    var  config: Configuration
    constructor(){
        releaseResourceFile("config.yml",false)

        config= Configuration.loadFromFile(File("plugins/TrainTP/config.yml"),Type.YAML)
    }
    fun save(){
        config.saveToFile(File("plugins/TrainTP/config.yml"))
    }
    fun getDebug():Boolean{
        return config.getBoolean("debug",false)
    }
    fun getAction(key:String):String{
        return config.getString("actions.$key","")!!
    }
    fun getArea(key:String): Location? {
        var loc=config.getLocation("area.$key")
        if(loc==null)return null
        else return Location(Bukkit.getWorld(loc.world!!),loc.x,loc.y,loc.z)
    }
    fun getBroadcastInterval():Int{
        return config.getInt("broadcast-interval",5)
    }
    fun addSchemaFile(filename:String){
        var list=config.getStringList("trains-schematics") .toMutableList()
        list.add(filename)
        config["trains-schematics"] = list
        save()
    }
    fun getInterval():Int{
        if(!config.getString("interval")!!.contains("-")){
            return config.getString("interval")!!.toInt()
        }
        var tmp=config.getString("interval")!!.split("-")
        return random(tmp[0].toInt(),tmp[1].toInt())
    }
    fun getBroacast_Timing():Int{
        return config.getInt("broadcast-Timing",0)
    }
    fun getTitle():String{
        return config.getString("broadcast-enter","")!!
    }
    fun getBroacast_range():Double{
        return config.getDouble("broadcast-range",50.0)
    }
    fun getBroacast_message():String{
        return config.getString("broadcast-message","")!!
    }
    fun getTrains_schemas():String{
        var name="plugins/TrainTP/schematic/"+config.getStringList("trains-schematics").random()
        debug("????????????$name")
        return name
    }
    data class Cmd(val cmd:String,val chance:Int,val message:String){
        var isCall:Boolean?=false
    }
    fun getCommands():Cmd{
        var list=ArrayList<Cmd>()
        var cfg=config.getConfigurationSection("commands")!!
        for (key in cfg.getKeys(false)) {
            list.add(Cmd(cfg.getString("${key}.cmd","")!!,cfg.getInt("${key}.chance",100),cfg.getStringColored("${key}.message")!!))
        }

        return list.random()
    }
    fun getMessage(key:String):String{

        return config.getStringColored("message.$key")!!
    }
    fun checkNonNull():Boolean{
        try {

            for (key in config.getConfigurationSection("area")!!.getKeys(false)) {
                getArea(key)?:return false
            }
            getInterval()
            getBroacast_Timing()
            getBroacast_range()
            getBroacast_message()
            if(config.getStringList("trains-schematics").isEmpty()){
                return false
            }
            config.getStringList("trains-schematics").forEach {
                if(!File("plugins/TrainTP/schematic/${it}").exists()){
                    return false
                }
            }
            getCommands()
            for (key in config.getConfigurationSection("message")!!.getKeys(false)) {
                getMessage(key)
            }
        }catch (e:Exception){
            return false
        }
        return true;
    }
}