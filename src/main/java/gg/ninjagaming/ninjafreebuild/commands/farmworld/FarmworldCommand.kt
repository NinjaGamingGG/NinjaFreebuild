package gg.ninjagaming.ninjafreebuild.commands.farmworld

import gg.ninjagaming.ninjafreebuild.NinjaFreebuild
import gg.ninjagaming.ninjafreebuild.database.tables.LastPlayerWorldPosition
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.ktorm.database.iterator
import org.ktorm.dsl.*
import kotlin.random.Random

class FarmWorldCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {

        if (sender !is Player) {
            sender.sendMessage("${NinjaFreebuild.getPrefix()}§cOnly players can use this command!")
            return false
        }

        val database = NinjaFreebuild.getDatabase()

        if (database == null){
            println("${NinjaFreebuild.getPrefix()}§cError: Database is not connected!")
            return false
        }

        val config = NinjaFreebuild.getConfig()

        val worldName = config.getString("world_configuration.farmworld.world_name")

        if (worldName == null)
        {
            println("${NinjaFreebuild.getPrefix()}§cError: No Farmworld name set!")
            return false
        }


        val lastPosition = database.from(LastPlayerWorldPosition).select().where((LastPlayerWorldPosition.PlayerId eq sender.uniqueId.toString()) and (LastPlayerWorldPosition.WorldName eq worldName))

        var lastPositionX = 0.0
        var lastPositionY = 0.0
        var lastPositionZ = 0.0
        var lastPositionPitch = 0f
        var lastPositionYaw = 0f

        val farmWorld = Bukkit.getWorld(worldName)

        if (lastPosition.rowSet.size() == 0)
        {
            val spawnRadius = config.getString("world_configuration.farmworld.spawn_radius")?.toInt()?: 200

            lastPositionX =-spawnRadius + Random.nextDouble() * (spawnRadius - -spawnRadius)
            lastPositionZ =-spawnRadius + Random.nextDouble() * (spawnRadius - -spawnRadius)

            if (farmWorld == null)
                return false

            lastPositionY = farmWorld.getHighestBlockYAt(Location(farmWorld,lastPositionX,0.0,lastPositionZ)).toDouble()
        }

        for (row in lastPosition.rowSet)
        {
            lastPositionX = row[LastPlayerWorldPosition.WorldLocationX] as Double
            lastPositionY = row[LastPlayerWorldPosition.WorldLocationY] as Double
            lastPositionZ = row[LastPlayerWorldPosition.WorldLocationZ] as Double
            lastPositionPitch = row[LastPlayerWorldPosition.WorldLocationPitch] as Float
            lastPositionYaw = row[LastPlayerWorldPosition.WorldLocationYaw] as Float
        }

        sender.teleport(Location(Bukkit.getWorld(worldName),lastPositionX,lastPositionY,lastPositionZ,lastPositionYaw,lastPositionPitch))

        sender.sendMessage("${NinjaFreebuild.getPrefix()}You are now in the Farmworld! You are allowed to Remove/ Build Blocks wherever you wish! §cThis world gets reset after a few days")


        return true
    }
}