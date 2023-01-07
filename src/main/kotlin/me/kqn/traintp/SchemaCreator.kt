package me.kqn.traintp

import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submitAsync
import taboolib.module.chat.colored
import taboolib.platform.util.ItemBuilder
import java.io.File
import java.io.FileOutputStream


object SchemaCreator {
    val tool=ItemBuilder(Material.STICK).also {
        it.name="&a圈地工具".colored()
        it.lore.add("&f使用本工具圈出一个列车，包括以下部分：".colored())
        it.lore.add("&f - 列车的本体".colored())
        it.lore.add("&f - 所有的位点的告示牌".colored())
        it.lore.add("&a左键设置第一角点".colored())
        it.lore.add("&a右键设置第二角点".colored())
        it.lore.add("&a两个角点设置完后输入/ttp save [文件名] 保存原理图".colored())
    }.build()
    var pos1:Location?=null
    var pos2:Location?=null
    @SubscribeEvent
    fun click(e:PlayerInteractEvent){
        if(e.player.inventory.itemInMainHand.itemMeta?.lore?.equals(tool.itemMeta!!.lore)==true){
            if(e.action==Action.LEFT_CLICK_BLOCK){
                pos1=e.clickedBlock?.location
                e.player.sendMessage("&a第一角点设置完毕".colored())

            }
            else if(e.action==Action.RIGHT_CLICK_BLOCK) {
                pos2=e.clickedBlock?.location
                e.player.sendMessage("&a第二角点设置完毕".colored())
            }
            e.isCancelled=true
        }
    }
    fun save(filename:String):Boolean{
        if(pos1==null|| pos2==null)return false
        submitAsync {
            var min=pos1!!
            var max=pos2!!
            if(min.y>max.y){
                min=pos2!!
                max=pos1!!
            }
           try{
               val region = CuboidRegion(Vector(min.x,min.y,min.z), Vector(max.x,max.y,max.z))
               val clipboard = BlockArrayClipboard(region)

               val forwardExtentCopy = ForwardExtentCopy(
                   BukkitWorld(min.world), region, clipboard, region.minimumPoint
               )
               Operations.complete(forwardExtentCopy)
               var name=filename
               var dir=File("plugins/TrainTP/schematic")
               if(!dir.exists())dir.mkdir()
               var file = File("plugins/TrainTP/schematic/${name}.schematic")
               file.deleteOnExit()

               ClipboardFormat.SCHEMATIC.getWriter(FileOutputStream(file))
               var writer=ClipboardFormat.SCHEMATIC.getWriter(FileOutputStream(file))
               writer.write(clipboard,BukkitWorld(min.world).worldData)
               writer.close()
               clipboard.IMP.flush()
               clipboard.IMP.close()
               TrainTP.config.addSchemaFile("${name}.schematic")

           }catch (e: Exception){
               e.printStackTrace()

           }

        }
        return true
    }
}