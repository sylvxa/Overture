package lol.sylvie.overture.util;

import net.minecraft.network.chat.Component;

public enum Anchor {
    TOP(Component.translatable("overture.position.top.horizontal"), Component.translatable("overture.position.top.vertical")) {
        @Override
        public int value(int size, int space) {
            return 0;
        }
    },
    CENTER(Component.translatable("overture.position.center"), Component.translatable("overture.position.center")) {
        @Override
        public int value(int size, int space) {
            return (space / 2) - (size / 2);
        }
    },
    BOTTOM(Component.translatable("overture.position.bottom.horizontal"), Component.translatable("overture.position.bottom.vertical")) {
        @Override
        public int value(int size, int space) {
            return space - size;
        }
    };

    public abstract int value(int size, int space);

    private final Component horizontal;
    private final Component vertical;

    Anchor(Component horizontal, Component vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public Component getHorizontalName() {
        return horizontal;
    }

    public Component getVerticalName() {
        return vertical;
    }
}