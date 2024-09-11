package galvin.playerfinder;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlayerFinder implements ModInitializer {
//	public static final String MOD_ID = "player-finder";

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("findplayer")
					.then(CommandManager.argument("player", StringArgumentType.string())
							.suggests(new PlayerSuggestionProvider())
							.executes(context -> {
								String playerName = StringArgumentType.getString(context, "player");
								PlayerEntity playerEntity = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
								BlockPos blockPos = playerEntity.getBlockPos();
								RegistryKey<World> playerDimension = playerEntity.getWorld().getRegistryKey();

								context.getSource().sendFeedback(() -> Text.literal(playerName + " ")
										.append(Text.literal(blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ()).formatted(Formatting.GREEN))
										.append(Text.literal(" [").append(Text.literal(playerDimension.getValue().toString().split(":")[1])
												.formatted(getDimensionColor(playerDimension))).append(Text.literal("]"))), false);
								return 1;
							})
					)
			);
		});
	}

	private Formatting getDimensionColor(RegistryKey<World> playerDimension) {
		if (playerDimension.equals(World.OVERWORLD)) {
			return Formatting.GREEN;
		} else if (playerDimension.equals(World.NETHER)) {
			return Formatting.RED;
		} else if (playerDimension.equals(World.END)) {
			return Formatting.LIGHT_PURPLE;
		} else {
			return Formatting.GRAY; // Fallback color for custom or unknown dimensions
		}
	}
}
