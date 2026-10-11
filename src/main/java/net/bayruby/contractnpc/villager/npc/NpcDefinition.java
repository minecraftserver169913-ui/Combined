
package net.bayruby.contractnpc.villager.npc;

import net.minecraft.resources.ResourceLocation;

public record NpcDefinition(
        String id,
        String displayName,
        ResourceLocation texture,
        double maxHealth,
        double movementSpeed
) {
    public NpcDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("NPC id cannot be blank");
        }

        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("NPC display name cannot be blank");
        }

        if (texture == null) {
            throw new IllegalArgumentException("NPC texture cannot be null");
        }

        if (maxHealth <= 0.0D) {
            throw new IllegalArgumentException("NPC max health must be positive");
        }

        if (movementSpeed < 0.0D) {
            throw new IllegalArgumentException("NPC movement speed cannot be negative");
        }
    }

    public String contractItemId() {
        return id + "_contract";
    }

    public String texturePath() {
        return texture.toString();
    }
}
