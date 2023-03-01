package com.zedo.fxcomponent.utils;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FXUtil {
    public static Background getBackground(Color color) {
        return new Background(new BackgroundFill(color, new CornerRadii(0), new Insets(0)));
    }

    public static SVGPath getSVGPath(String svg) {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(svg);
        return svgPath;
    }


    /**
     * 遍历所有节点
     * 返回false 停止继续
     */
    public interface LoopNodeCallback {
        boolean loop(Node node);
    }

    public static boolean loopNode(Region region, LoopNodeCallback callback) {
        if (!callback.loop(region))
            return false;

        if (region instanceof TitledPane titledPane)
            if (!loopNode((Region) titledPane.getContent(), callback))
                return false;

        List<Node> children = region.getChildrenUnmodifiable();
        ArrayList<Node> nodes = new ArrayList<>();
        for (Node node : children) {
            nodes.clear();
            if (node instanceof TabPane tabPane)
                tabPane.getTabs().forEach(tab -> nodes.add(tab.getContent()));
            else if (node instanceof ToolBar toolBar)
                nodes.addAll(toolBar.getItems());
            else if (node instanceof SplitPane splitPane)
                nodes.addAll(splitPane.getItems());
            else if (node instanceof ButtonBar buttonBar)
                nodes.addAll(buttonBar.getButtons());
            else if (node instanceof Accordion accordion)
                nodes.addAll(accordion.getPanes());
            else
                nodes.add(node);
            for (Node node1 : nodes)
                if (!loopNode((Region) node1, callback))
                    return false;
        }

        return true;
    }

    /**
     * 设置按钮背景颜色
     *
     * @param button  要设置的按钮
     * @param exited  移出颜色
     * @param entered 进入颜色
     * @param pressed 按下颜色 可为null
     */
    public static void setButtonColor(Button button, Color exited, Color entered, Color pressed) {
        button.setStyle("-fx-background-color: #" + getStrColor(exited) + "; ");
        button.setOnMouseExited(mouseEvent -> {
            button.setStyle("-fx-background-color: #" + getStrColor(exited) + "; ");
        });
        button.setOnMouseEntered(mouseEvent -> {
            button.setStyle("-fx-background-color: #" + getStrColor(entered) + "; ");

        });
        if (pressed != null)
            button.setOnMousePressed(mouseEvent -> {
                button.setStyle("-fx-background-color: #" + getStrColor(pressed) + "; ");

            });
    }

    public static String getStrColor(Color c) {
        return c.toString().substring(2);
    }

    public static void setButtonColor(Button button, Color entered) {
        setButtonColor(button, Color.hsb(entered.getHue(), entered.getSaturation(), entered.getBrightness() - 0.2d), entered, null);
    }

    /**
     * 创建文件或是文件夹 可以创建不重复
     *
     * @param path      要创建的文件或文件夹名 如 "az"
     * @param name      文件名或是文件夹名 如果遇到重复则文件末尾计数
     * @param extension 文件夹为null 文件则是扩展名 如 "png"
     */
    public static void createFileOrDir(Path path, String name, String extension) throws IOException {
        int count = 0;
        Path newDir = null;
        //尝试创建文件夹
        do {
            count++;
            newDir = path.resolve(name + count + (extension == null ? "" : "." + extension));
        } while (newDir.toFile().exists());
        if (extension == null) {
            Files.createDirectory(newDir);
        } else {
            Files.createFile(newDir);
        }
    }

    /**
     * 获取边框
     *
     * @param c           颜色
     * @param style       样式
     * @param cornerRadii 圆角
     * @param widths      线宽
     * @return
     */
    public static Border getBorder(Color c, BorderStrokeStyle style, double cornerRadii, double widths) {
        return new Border(new BorderStroke(c, style, new CornerRadii(cornerRadii), new BorderWidths(widths)));

    }

    public static Rectangle2D getStageRect(Stage stage) {
        return new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
    }

    public static Rectangle2D getWindowsRect(Window window) {
        return new Rectangle2D(window.getX(), window.getY(), window.getWidth(), window.getHeight());
    }


    private static Screen cacheScreen;//缓存屏幕


    public static boolean isInScreen(Screen screen, double x, double y) {
        if (screen == null)
            return false;
        Rectangle2D r = screen.getBounds();
        if (x >= r.getMinX() && x <= r.getMaxX())
            if (y >= r.getMinY() && y <= r.getMaxY())
                return true;
        return false;
    }

    /**
     * 获取坐标当前屏幕
     *
     * @return 屏幕
     */
    public static Screen getMouseScreen(double x, double y) {
        if (isInScreen(cacheScreen, x, y))
            return cacheScreen;
        for (Screen screen : Screen.getScreens()) {
            if (isInScreen(screen, x, y)) {
                cacheScreen = screen;
                return screen;
            }
        }
        return null;
    }

    /**
     * 获取之前检测的屏幕
     *
     * @return 屏幕
     */
    public static Screen getLastScreen() {
        return cacheScreen;
    }
}
