package lol.sylvie.overture.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.function.Supplier;

public abstract class GuideScreen extends Screen {
    protected ArrayList<Step> steps = new ArrayList<>();
    private int stepIndex = 0;

    private Screen parent;

    protected GuideScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
        this.initSteps();
    }

    protected void addStep(Step step) {
        this.steps.add(step);
    }

    protected abstract void initSteps();

    protected void page(int change) {
        if (this.stepIndex == this.steps.size()) {
            this.onClose();
        }

        stepIndex += change;
        this.clearWidgets();
        this.init();
    }

    protected Component link(String uri) {
        return Component.literal(uri).withStyle(Style.EMPTY
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.OpenUrl(URI.create(uri))));
    }

    protected MultiLineTextWidget text(Component component) {
        MultiLineTextWidget widget = new MultiLineTextWidget(component, this.font);
        widget.active = true;
        widget.setComponentClickHandler(style -> {
            ClickEvent event = style.getClickEvent();
            if (event == null) return;

            if (event.action() == ClickEvent.Action.OPEN_URL) {
                clickUrlAction(minecraft, this, ((ClickEvent.OpenUrl) event).uri());
            }
        });
        return widget;
    }

    @Override
    protected void init() {
        super.init();

        Step step = this.steps.get(this.stepIndex);

        HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
        layout.addTitleHeader(this.title, this.font);

        LinearLayout footer = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        Button backButton = Button.builder(CommonComponents.GUI_BACK, _ -> page(-1)).build();
        if (this.stepIndex == 0) backButton.active = false;
        footer.addChild(backButton);

        Button nextButton = Button.builder(this.stepIndex == this.steps.size() - 1 ? CommonComponents.GUI_DONE : CommonComponents.GUI_CONTINUE, _ -> {
            Component error = step.beforeNext().get();
            if (error == null) page(1);
            else {
                minecraft.getToastManager().addToast(new SystemToast(SystemToast.SystemToastId.LOW_DISK_SPACE, Component.literal("Error!"), error));
            }
        }).build();
        footer.addChild(nextButton);

        LinearLayout body = LinearLayout.vertical().spacing(10);

        for (AbstractWidget widget : step.components()) {
            body.addChild(widget);
            int maxWidth = Math.min(256, this.width - 16);
            if (widget instanceof MultiLineTextWidget textWidget) {
                textWidget.setMaxWidth(maxWidth);
            } else if (widget instanceof AbstractWidget abstractWidget) {
                abstractWidget.setWidth(maxWidth);
            }
        }

        layout.addToContents(body);

        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(this.parent);
    }

    /**
     * A step in this guide
     * @param beforeNext Runs before going to the next step, returns null if ready or returns an error message
     * @param components Text or inputs
     */
    public record Step(Supplier<@Nullable Component> beforeNext, AbstractWidget... components) {};
}
