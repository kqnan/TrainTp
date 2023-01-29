package me.kqn.traintp

import com.boydti.fawe.`object`.clipboard.FaweClipboard
import com.sk89q.jnbt.CompoundTag
import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.blocks.BaseBlock
import com.sk89q.worldedit.blocks.BlockData
import com.sk89q.worldedit.blocks.SignBlock
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.bukkit.adapter.AdapterLoadException
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter
import com.sk89q.worldedit.bukkit.adapter.BukkitImplLoader
import com.sk89q.worldedit.bukkit.adapter.impl.Spigot_v1_12_R2
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.internal.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.session.ClipboardHolder
import org.bukkit.*
import org.bukkit.block.data.type.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.SignChangeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.library.xseries.XMaterial
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min

/**
 * 执行kether脚本
 * */
fun String.eval(player: Player) {
    try {

        KetherShell.eval(this, sender = adaptPlayer(player))
    } catch (e: Throwable) {
        e.printKetherErrorMessage()
    }
}
fun getPrivateField(instance:Object,filedName:String) : Any? {
    instance.`class`.fields.forEach { print(it.type) }
    var field = instance.`class`.getField(filedName)
    field.setAccessible(true);
    return field.get(instance);
}

fun parse_schema2(world: World, schema: File,stage:State.Stage):State?{
    var weWorld=BukkitWorld(world)
    val clipboard: BlockArrayClipboard
    val file = schema
    try {
        var enter= CopyOnWriteArrayList<Location>()
        var exit=CopyOnWriteArrayList<Location>()
        var inside=CopyOnWriteArrayList<Location>()
        var leave=CopyOnWriteArrayList<Location>()

        val format = ClipboardFormat.findByFile(file)
        val reader = format!!.getReader(FileInputStream(file))
        clipboard = reader.read(weWorld.worldData) as BlockArrayClipboard

        clipboard.IMP.tileEntities.removeIf() {
            if(it.getString("id").equals("minecraft:sign")){

                try {
                    var txt1=Configuration.loadFromString(it.getString("Text1"),Type.JSON)
                    var txt2=Configuration.loadFromString(it.getString("Text2"),Type.JSON)
                    var l1=(txt1.getList("extra")!!.get(0) as Map<*, *>).get("text")
                    var l2=(txt2.getList("extra")!!.get(0) as Map<*, *>).get("text")

                    var x=it.getInt("x")+clipboard.minimumPoint.blockX
                    var y=it.getInt("y")+clipboard.minimumPoint.blockY
                    var z=it.getInt("z")+clipboard.minimumPoint.blockZ

                    if(l1?.equals("[TrainTp]") == true){
                        when(l2){
                            "[Enter]"->{enter.add(Location(world,x.toDouble(),y-1.0,z.toDouble()))
                                return@removeIf true
                            }
                            "[Exit]"->{exit.add(Location(world,x.toDouble(),y-1.0,z.toDouble()))
                                return@removeIf true}
                            "[Inside]"->{inside.add(Location(world,x.toDouble(),y-1.0,z.toDouble()))
                                return@removeIf true}
                            "[Leave]"->{leave.add(Location(world,x.toDouble(),y-1.0,z.toDouble()))
                                return@removeIf true}
                        }
                    }
                }catch (e:Exception){

                }
            }
            return@removeIf false
        }
        return State(enter,exit,inside,CopyOnWriteArrayList(),leave,world,stage)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return null
    }

}

//[TrainLoc]
// [Enter] [Exit] [Inside] [Leave]
fun parse_schema(world: World, schema: File,stage:State.Stage):State?{
    var weWorld=BukkitWorld(world)
    val clipboard: Clipboard
    val file = schema
    try {

        var enter=CopyOnWriteArrayList<Location>()
        var exit=CopyOnWriteArrayList<Location>()
        var inside=CopyOnWriteArrayList<Location>()
        var leave=CopyOnWriteArrayList<Location>()

        val format = ClipboardFormat.findByFile(file)
        val reader = format!!.getReader(FileInputStream(file))
        clipboard = reader.read(weWorld.worldData)
        var pos1=clipboard.minimumPoint
        var pos2=clipboard.maximumPoint
        for (x in min(pos1.blockX,pos2.blockX) as Int.. max(pos1.blockX,pos2.blockX) as Int){
            for (y in min(pos1.blockY,pos2.blockY) as Int.. max(pos1.blockY,pos2.blockY) as Int){
                for(z in min(pos1.blockZ,pos2.blockZ) as Int.. max(pos1.blockZ,pos2.blockZ) as Int){
                    var pos=Vector(x,y,z)



                    debug(clipboard.getBlock(pos).toString())
                    if(clipboard.getBlock(pos) is SignBlock){
                        var sign=clipboard.getBlock(pos) as SignBlock
                        var text=sign.text

                        if(text.size>=2&&text[0].equals("[TrainTp]")){
                            when(text[1]){
                                "[Enter]"->{enter.add(Location(world,x.toDouble(),y-1.0,z.toDouble()))}
                                "[Exit]"->{exit.add(Location(world,x.toDouble(),y-1.0,z.toDouble()))}
                                "[Inside]"->{inside.add(Location(world,x.toDouble(),y-1.0,z.toDouble()))}
                                "[Leave]"->{leave.add(Location(world,x.toDouble(),y-1.0,z.toDouble()))}
                            }
                        }
                    }
                }
            }
        }
        return State(enter,exit,inside,CopyOnWriteArrayList(),leave,world,stage)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        return null
    }

}
fun debug(msg:String){
    if(!TrainTP.config.getDebug())return
    print(msg)
    Bukkit.getOperators().forEach {
       it.player?.sendMessage(msg)
    }
}
var editSession:EditSession?=null
fun Vector.toBukkit(world: World):Location{
    return Location(world,this.x,this.y,this.z)
}
fun Region.containPlayer(player: Player):Boolean{
    var pos1=this.minimumPoint
    var pos2=this.maximumPoint
    var pos=Vector(player.location.x,player.location.y,player.location.z)
    return pos.containedWithin(pos1,pos2)

}
fun region_schema(world: World,schema: File):Region?{
    val weWorld = BukkitWorld(world)
    val clipboard: BlockArrayClipboard
    val file = schema
    if (!file.exists()) return null
    val format = ClipboardFormat.findByFile(file)
    try {
        val reader = format!!.getReader(FileInputStream(file))
        clipboard = reader.read(weWorld.worldData) as BlockArrayClipboard
        return clipboard.region
    }catch (e:Exception){

    }
    return null
}
fun particle_schema(world: World,schema: File){

    val weWorld = BukkitWorld(world)
    val clipboard: BlockArrayClipboard
    val file = schema
    if (!file.exists()) return
    val format = ClipboardFormat.findByFile(file)
    try {
        val reader = format!!.getReader(FileInputStream(file))
        clipboard = reader.read(weWorld.worldData) as BlockArrayClipboard
        TrainScheduler.lineEffect(clipboard.region.minimumPoint.toBukkit(world),clipboard.region.maximumPoint.toBukkit(world), Particle.SMOKE_LARGE)
    }catch (e:java.lang.Exception){

    }
    return
}
/**最好异步执行
 * */
fun place_schema(pos: Location,schema:File): Boolean {
    if(pos.world==null)return false
    val weWorld = BukkitWorld(pos.world)
    val clipboard: BlockArrayClipboard
    val file = schema
    if (!file.exists()) return false
    val format = ClipboardFormat.findByFile(file)
    try {
        val reader = format!!.getReader(FileInputStream(file))
        clipboard = reader.read(weWorld.worldData) as BlockArrayClipboard
        clipboard.IMP.tileEntities.forEach {
            if(it.getString("id").equals("minecraft:sign")){
                try {
                    var txt1=Configuration.loadFromString(it.getString("Text1"),Type.JSON)
                    var txt2=Configuration.loadFromString(it.getString("Text2"),Type.JSON)
                    var l1=(txt1.getList("extra")!!.get(0) as Map<*, *>).get("text")
                    var l2=(txt2.getList("extra")!!.get(0) as Map<*, *>).get("text")

                    var x=it.getInt("x")+clipboard.minimumPoint.blockX
                    var y=it.getInt("y")+clipboard.minimumPoint.blockY
                    var z=it.getInt("z")+clipboard.minimumPoint.blockZ

                    if(l1?.equals("[TrainTp]") == true){
                        when(l2){
                            "[Enter]"->{clipboard.setBlock(x,y,z, BaseBlock(0))}
                            "[Exit]"->{clipboard.setBlock(x,y,z,BaseBlock(0))}
                            "[Inside]"->{clipboard.setBlock(x,y,z,BaseBlock(0))}
                            "[Leave]"->{clipboard.setBlock(x,y,z,BaseBlock(0))}
                        }
                    }
                }catch (e:Exception){

                }

            }

        }

        editSession = WorldEdit.getInstance().editSessionFactory.getEditSession(weWorld, Int.MAX_VALUE)
        val operation = ClipboardHolder(clipboard, weWorld.worldData)
            .createPaste(editSession, weWorld.worldData)
            .to(clipboard.origin)
            .build()
        Operations.complete(operation)
        editSession!!.flushQueue()
        operation.cancel()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return true
}
fun undo(world: World){
    val weWorld=BukkitWorld(world)
    var es=WorldEdit.getInstance().editSessionFactory.getEditSession(weWorld,Int.MAX_VALUE)

    editSession?.undo(es)
    es.flushQueue()
    es.disableQueue()
}
fun Location.betweeen(loc1:Location,loc2:Location):Boolean{
    if(this.x>=min(loc1.x,loc2.x)&&this.x<=max(loc1.x,loc2.x)){
        if(this.y>=min(loc1.y,loc2.y)&&this.y<=max(loc1.y,loc2.y)){
            if(this.z>=min(loc1.z,loc2.z)&&this.z<=max(loc1.z,loc2.z)){
                return true;
            }
            return false;
        }
        else return false
    }else return false
}