package net.bayruby.contractnpc.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

public class BronzeCostTooltipClient
        implements ClientTooltipComponent {

    private static final ResourceLocation GOLD_COIN =
            ResourceLocation.fromNamespaceAndPath(
                    "numismaticoverhaul",
                    "textures/item/gold_coin.png"
            );

    private static final ResourceLocation SILVER_COIN =
            ResourceLocation.fromNamespaceAndPath(
                    "numismaticoverhaul",
                    "textures/item/silver_coin.png"
            );

    private static final ResourceLocation BRONZE_COIN =
            ResourceLocation.fromNamespaceAndPath(
                    "numismaticoverhaul",
                    "textures/item/bronze_coin.png"
            );

    private final long gold;
    private final long silver;
    private final long bronze;

    public BronzeCostTooltipClient(BronzeCostTooltip tooltip) {
        long value = tooltip.amount();

        this.gold = value / 10_000;
        this.silver = (value / 100) % 100;
        this.bronze = value % 100;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public int getWidth(Font font) {
        int width = 0;
        int renderedCurrencies = 0;

        if (gold > 0) {
            width += segmentWidth(font, gold);
            renderedCurrencies++;
        }

        if (silver > 0) {
            width += segmentWidth(font, silver);
            renderedCurrencies++;
        }

        if (bronze > 0) {
            width += segmentWidth(font, bronze);
            renderedCurrencies++;
        }

        if (renderedCurrencies > 1) {
            width += (renderedCurrencies - 1) * 8;
        }

        return width;
    }

    @Override
    public void renderImage(
            Font font,
            int x,
            int y,
            GuiGraphics graphics
    ) {
        boolean renderedCurrency = false;

        if (gold > 0) {
            x = drawCoin(
                    graphics, font, GOLD_COIN, gold, x, y
            );
            renderedCurrency = true;
        }

        if (silver > 0) {
            if (renderedCurrency) {
                x += 8;
            }

            x = drawCoin(
                    graphics, font, SILVER_COIN, silver, x, y
            );
            renderedCurrency = true;
        }

        if (bronze > 0) {
            if (renderedCurrency) {
                x += 8;
            }

            drawCoin(
                    graphics, font, BRONZE_COIN, bronze, x, y
            );
        }
    }

    private int segmentWidth(Font font, long amount) {
        return 16 + 3 + font.width(Long.toString(amount));
    }

    private int drawCoin(
            GuiGraphics graphics,
            Font font,
            ResourceLocation texture,
            long amount,
            int x,
            int y
    ) {
        graphics.blit(
                texture,
                x,
                y,
                0,
                0,
                16,
                16,
                16,
                16
        );

        graphics.drawString(
                font,
                Long.toString(amount),
                x + 19,
                y + 4,
                0xFFFFFFFF,
                true
        );

        return x + segmentWidth(font, amount);
    }
}