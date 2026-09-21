/*
 * VanishNoPacket
 * Copyright (C) 2011-2022 Matt Baxter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.kitteh.vanish.hooks.plugins;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.hooks.Hook;

/**
 * Lets vanished players be heard through Simple Voice Chat.
 */
public final class VoiceChatHook extends Hook implements VoicechatPlugin {

  private static final String LISTEN_PERMISSION = "voicechat.listen";

  public VoiceChatHook(final @NonNull VanishPlugin plugin) {
    super(plugin);
  }

  @Override
  public void onEnable() {
    final @Nullable BukkitVoicechatService service = this.plugin.getServer().getServicesManager()
        .load(BukkitVoicechatService.class);
    if (service != null) {
      service.registerPlugin(this);
      this.plugin.getLogger().info("Now hooking into Simple Voice Chat");
    } else {
      this.plugin.getLogger()
          .info("Can't find Simple Voice Chat!");
    }
  }

  @Override
  public String getPluginId() {
    return "vanishnopacket";
  }

  @Override
  public void registerEvents(final @NonNull EventRegistration registration) {
    registration.registerEvent(EntitySoundPacketEvent.class, this::onEntitySound);
  }

  private void onEntitySound(final @NonNull EntitySoundPacketEvent event) {
    final @Nullable Player speaker = VoiceChatHook.bukkitPlayer(event.getSenderConnection());
    final @Nullable VoicechatConnection receiverConnection = event.getReceiverConnection();
    final @Nullable Player listener = VoiceChatHook.bukkitPlayer(receiverConnection);
    if (speaker == null || listener == null) {
      return;
    }
    final EntitySoundPacket packet = event.getPacket();
    if (!speaker.getUniqueId().equals(packet.getEntityUuid())
        || !this.plugin.getManager().isVanished(speaker) || listener.canSee(speaker)) {
      return;
    }
    event.cancel();
    if (!listener.hasPermission(VoiceChatHook.LISTEN_PERMISSION)) {
      return;
    }
    final Location mouth = speaker.getEyeLocation();
    final LocationalSoundPacket positional = packet.locationalSoundPacketBuilder()
        .position(event.getVoicechat().createPosition(mouth.getX(), mouth.getY(), mouth.getZ()))
        .distance(packet.getDistance())
        .build();
    event.getVoicechat().sendLocationalSoundPacketTo(receiverConnection, positional);
  }

  private static @Nullable Player bukkitPlayer(final @Nullable VoicechatConnection connection) {
    return connection == null ? null : (Player) connection.getPlayer().getPlayer();
  }
}
