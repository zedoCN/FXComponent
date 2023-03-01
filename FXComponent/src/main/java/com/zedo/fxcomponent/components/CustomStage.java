package com.zedo.fxcomponent.components;

import com.zedo.fxcomponent.utils.FXUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;

public class CustomStage {
    public final Pane rootNode;//根节点
    public final Container rootContainer;//根容器
    public final Stage stage;//舞台
    private final static double UPDATE_FPS = 60;//更新帧率
    private boolean isMinimize = false;//最小化状态
    private double minimizeMinW, minimizeMinH;//记录最小化之前的窗口最小尺寸

    public double maxWidth = -1, maxHeight = -1;//最大 为-1则无视
    public double minWidth = -1, minHeight = -1;//最小 同上

    private static final double rootContainerPadding = 8;//根容器外层阴影

    /**
     * 设置窗口最小尺寸
     */
    public void setMinSize(double minWidth, double minHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
    }

    /**
     * 设置窗口最大尺寸
     */
    public void setMaxSize(double maxWidth, double maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }


    private double originalWidth, originalHeight, originalX, originalY;//原始宽高XY
    private double mousePressedScreenX, mousePressedScreenY;//记录鼠标按下时鼠标的位置
    private double mouseScreenX, mouseScreenY;//实时鼠标位置

    private String cssStyle;

    public CustomStage(Pane rootNode, Stage stage, String cssStyle) {
        if (cssStyle == null)
            cssStyle = this.getClass().getResource("DefaultStage.css").toExternalForm();
        this.cssStyle = cssStyle;
        if (rootNode == null) {
            FXMLLoader fxmlLoader = new FXMLLoader(CustomStage.class.getResource("DefaultStage.fxml"));
            try {
                rootNode = fxmlLoader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.rootNode = rootNode;
        rootContainer = new Container(rootNode);//根容器
        rootContainer.setPadding(new Insets(rootContainerPadding));
        rootContainer.setBackground(FXUtil.getBackground(Color.TRANSPARENT));

        Scene scene = new Scene(rootContainer);
        scene.getStylesheets().add(cssStyle);
        scene.setFill(Color.TRANSPARENT);
        this.stage = stage;
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        setWidth(maxWidth);
        setHeight(maxHeight);
        //窗口焦点处理
        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) rootContainer.getStyleClass().add("window-focused");
            else rootContainer.getStyleClass().remove("window-focused");
        });

    }


    private boolean dragMoveLock = false;//拖动移动锁
    private double dragMoveXProportion = 0;//拖动时记录x的比例

    private final EventHandler<MouseEvent> mousePressedHandler = mouseEvent -> {
        cursorLock = true;
        mousePressedScreenX = mouseEvent.getScreenX();
        mousePressedScreenY = mouseEvent.getScreenY();
        originalWidth = getWidth();
        originalHeight = getHeight();
        originalX = getX();
        originalY = getY();
        dragMoveXProportion = (mousePressedScreenX - originalX) / originalWidth;
    };
    private boolean cursorLock;//光标锁

    /**
     * 注册拖动移动 (一般用于标题栏)
     *
     * @param node 需要监听的节点
     */
    public void registerDragMove(Node node) {
        if (node == null)
            node = rootContainer.findNode(".window-titleBar");
        if (node == null) throw new RuntimeException("注册拖动移动 注册失败!");

        //鼠标按下
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);

        //鼠标停止拖动
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> dragMoveLock = false);

        //鼠标拖动
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
            mouseScreenX = mouseEvent.getScreenX();
            mouseScreenY = mouseEvent.getScreenY();
            if (executeLayout) {//如果是还原布局

            } else if (dragMoveLock) {
                setX(originalX + (mouseScreenX - mousePressedScreenX));
                setY(originalY + (mouseScreenY - mousePressedScreenY));
            } else {
                if (Math.abs(mouseScreenX - mousePressedScreenX)//判断是否达到拖动阈值
                        + Math.abs(mouseScreenY - mousePressedScreenY) > 11)
                    dragMoveLock = true;
            }


        });

    }


    /**
     * 注册调整大小 (用于调整窗口大小)
     */
    public void registerResize() {

        Insets boardInsets = rootContainer.getPadding();

        EventHandler<MouseEvent> cursorHandler = mouseEvent -> {

            if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_RELEASED))
                cursorLock = false;
            if (cursorLock)
                return;
            if (mouseEvent.getTarget().equals(rootNode)) {


                double x = mouseEvent.getX();
                double y = mouseEvent.getY();
                boolean left = x <= boardInsets.getLeft();
                boolean right = x + boardInsets.getRight() >= rootContainer.getWidth();
                boolean top = y <= boardInsets.getTop();
                boolean bottom = y + boardInsets.getBottom() >= rootContainer.getHeight();


                if (!top && !bottom) {
                    if (left) rootNode.setCursor(Cursor.W_RESIZE);
                    else rootNode.setCursor(Cursor.E_RESIZE);
                } else if (!left && !right) {
                    if (top) rootNode.setCursor(Cursor.N_RESIZE);
                    else rootNode.setCursor(Cursor.S_RESIZE);
                }

                if (top) {
                    if (left) rootNode.setCursor(Cursor.NW_RESIZE);
                    else if (right) rootNode.setCursor(Cursor.NE_RESIZE);
                } else if (bottom) {
                    if (left) rootNode.setCursor(Cursor.SW_RESIZE);
                    else if (right) rootNode.setCursor(Cursor.SE_RESIZE);
                }

            } else {
                rootNode.setCursor(Cursor.DEFAULT);
            }


        };
        //移动
        rootContainer.addEventHandler(MouseEvent.MOUSE_MOVED, cursorHandler);
        //退出
        rootContainer.addEventHandler(MouseEvent.MOUSE_EXITED, cursorHandler);
        //进入
        rootContainer.addEventHandler(MouseEvent.MOUSE_ENTERED, cursorHandler);
        //松开
        rootContainer.addEventHandler(MouseEvent.MOUSE_RELEASED, cursorHandler);

        //按下
        rootContainer.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);

        //拖动
        rootContainer.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent mouseEvent) -> {
            Cursor c = rootNode.getCursor();
            if (c.equals(Cursor.E_RESIZE) || c.equals(Cursor.SE_RESIZE) || c.equals(Cursor.NE_RESIZE))
                setWidth(originalWidth + mouseEvent.getScreenX() - mousePressedScreenX);
            else if (c.equals(Cursor.W_RESIZE) || c.equals(Cursor.NW_RESIZE) || c.equals(Cursor.SW_RESIZE)) {
                setWidth(originalWidth + (mousePressedScreenX - mouseEvent.getScreenX()));
                setX(originalX + originalWidth - getWidth());
            }
            if (c.equals(Cursor.S_RESIZE) || c.equals(Cursor.SE_RESIZE) || c.equals(Cursor.SW_RESIZE))
                setHeight(originalHeight + mouseEvent.getScreenY() - mousePressedScreenY);
            else if (c.equals(Cursor.N_RESIZE) || c.equals(Cursor.NE_RESIZE) || c.equals(Cursor.NW_RESIZE)) {
                setHeight(originalHeight + (mousePressedScreenY - mouseEvent.getScreenY()));
                setY(originalY + originalHeight - getHeight());
            }
        });


    }

    /**
     * 注册控制按钮
     */
    public void registerControlButton() {
        rootContainer.findNode(".window-button-minimize").setOnMouseClicked(event -> {
            setMinimize(!isMinimize());
            // stage.setIconified(true);
        });
        rootContainer.findNode(".window-button-maximize").setOnMouseClicked(event -> setMaximize(!isMaximize(), null, false));
        rootContainer.findNode(".window-button-close").setOnMouseClicked(event -> stage.close());
    }

    private boolean showLayout = false;//用于判断展示布局
    private boolean executeLayout = false;//执行布局
    private static final double LAYOUT_SPEED = 16 / (UPDATE_FPS * 2);//布局动画速度
    private double layoutShowSpeed = 0;//布局动画速度
    private double layoutX, layoutY;//布局位置
    private double layoutW, layoutH;//布局大小
    private boolean layoutAutoXY = false;//自动XY

    private double layoutOriginalX, layoutOriginalY;//布局原始位置
    private double layoutOriginalW, layoutOriginalH;//布局原始大小
    private final Timeline layoutTimeline = new Timeline();

    /**
     * 负数向下取整 正数向上取整
     */
    private double mathFloorCeil(double d, double s) {
        return (d < 0 ? Math.floor(d * s) : Math.ceil(d * s));
    }

    /**
     * 注册布局管理
     */
    public void registerLayoutManagement(Node node) {

        if (node == null)
            node = rootContainer.findNode(".window-titleBar");
        if (node == null) throw new RuntimeException("注册拖动移动 注册失败!");

        Stage layoutStage = new Stage();


        Pane pane = new Pane();
        pane.getStyleClass().add("window-layout-background");

        Container layoutRootContainer = new Container(pane);//根容器
        layoutRootContainer.setPadding(new Insets(rootContainerPadding));
        layoutRootContainer.setBackground(FXUtil.getBackground(Color.TRANSPARENT));

        Scene layoutScene = new Scene(layoutRootContainer);
        layoutScene.getStylesheets().add(cssStyle);
        layoutScene.setFill(Color.TRANSPARENT);
        layoutScene.getStylesheets().add(cssStyle);
        layoutStage.setScene(layoutScene);
        layoutStage.initStyle(StageStyle.TRANSPARENT);
        layoutStage.setResizable(false);


        layoutTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000 / UPDATE_FPS), event1 -> {

            if (layoutShowSpeed < 1)
                layoutShowSpeed += 0.01;
            if (executeLayout) {


                double stageX = getX();
                double stageY = getY();
                double stageW = getWidth();
                double stageH = getHeight();
                double calculateX;
                double calculateY;
                if (!showLayout && layoutAutoXY) {
                    layoutX = mouseScreenX - dragMoveXProportion * stageW;
                    layoutY = mouseScreenY - 8;


                    originalX = stageX;
                    originalY = stageY;
                    mousePressedScreenX = mouseScreenX;
                    mousePressedScreenY = mouseScreenY;
                    calculateX = mathFloorCeil(layoutX - stageX, LAYOUT_SPEED * 4 + layoutShowSpeed);
                    calculateY = mathFloorCeil(layoutY - stageY, LAYOUT_SPEED * 4 + layoutShowSpeed);
                } else {
                    calculateX = mathFloorCeil(layoutX - stageX, LAYOUT_SPEED + layoutShowSpeed);
                    calculateY = mathFloorCeil(layoutY - stageY, LAYOUT_SPEED + layoutShowSpeed);
                }


                double calculateW = mathFloorCeil(layoutW - stageW, LAYOUT_SPEED + layoutShowSpeed);
                double calculateH = mathFloorCeil(layoutH - stageH, LAYOUT_SPEED + layoutShowSpeed);

                if (isMinimize) {
                    //layoutShowSpeed
                }

                if (Math.abs(calculateX) < 2 && Math.abs(calculateY) < 2 && Math.abs(calculateW) < 2 && Math.abs(calculateH) < 2) {

                    if (isMinimize) {
                        System.out.println("最小化🌶");
                        stage.setIconified(true);
                    }


                    layoutTimeline.stop();
                    executeLayout = false;
                    showLayout = false;
                    layoutStage.close();
                    if (isLayoutStyle()) {
                        calculateX = layoutX - stageX;
                        calculateY = layoutY - stageY;
                        calculateW = layoutW - stageW;
                        calculateH = layoutH - stageH;
                    }
                    System.out.println("布局执行完毕");
                }

                setX(stageX + calculateX);
                setY(stageY + calculateY);
                setWidth(stageW + calculateW);
                setHeight(stageH + calculateH);
            } else {
                if (!layoutStage.isShowing()) {

                    layoutStage.setX(getX());
                    layoutStage.setY(getY());
                    layoutStage.setWidth(getWidth());
                    layoutStage.setHeight(getHeight());
                    layoutStage.show();
                    stage.requestFocus();//保证主窗口最前
                    layoutShowSpeed = 0;

                }

                {

                    double stageX = layoutStage.getX();
                    double stageY = layoutStage.getY();
                    double stageW = layoutStage.getWidth();
                    double stageH = layoutStage.getHeight();
                    double calculateX = mathFloorCeil(layoutX - stageX, LAYOUT_SPEED + layoutShowSpeed);
                    double calculateY = mathFloorCeil(layoutY - stageY, LAYOUT_SPEED + layoutShowSpeed);
                    double calculateW = mathFloorCeil(layoutW - stageW, LAYOUT_SPEED + layoutShowSpeed);
                    double calculateH = mathFloorCeil(layoutH - stageH, LAYOUT_SPEED + layoutShowSpeed);


                    if (Math.abs(calculateX) < 8 && Math.abs(calculateY) < 8 && Math.abs(calculateW) < 8 && Math.abs(calculateH) < 8) {
                        calculateX = layoutX - stageX;
                        calculateY = layoutY - stageY;
                        calculateW = layoutW - stageW;
                        calculateH = layoutH - stageH;
                        if (!executeLayout) {
                            layoutTimeline.stop();
                        }
                        if (!showLayout) {
                            layoutStage.close();
                            System.out.println("结束展示");
                        }
                    }


               /* if (calculateX == 0 && calculateY == 0 && calculateW == 0 && calculateH == 0) {
                    if (!executeLayout) {
                        layoutTimeline.stop();
                    }
                    if (!showLayout)
                        layoutStage.close();
                }*/
                    layoutStage.setX(stageX + calculateX);
                    layoutStage.setY(stageY + calculateY);
                    layoutStage.setWidth(stageW + calculateW);
                    layoutStage.setHeight(stageH + calculateH);
                }
            }


        }));
        layoutTimeline.setCycleCount(Timeline.INDEFINITE);


        //鼠标点击
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2) {
                /*if (isLayoutStyle()) {
                    layoutX = layoutOriginalX;
                    layoutY = layoutOriginalY;
                    layoutW = layoutOriginalW;
                    layoutH = layoutOriginalH;
                    setLayoutStyle(false);
                } else {
                    layoutOriginalX = getX();
                    layoutOriginalY = getY();
                    layoutOriginalW = getWidth();
                    layoutOriginalH = getHeight();
                    Rectangle2D r = FXUtil.getMouseScreen(event.getScreenX(), event.getScreenY()).getVisualBounds();
                    layoutX = r.getMinX();
                    layoutY = r.getMinY();
                    layoutW = r.getWidth();
                    layoutH = r.getHeight();
                    setLayoutStyle(true);
                }
                layoutAutoXY = false;
                executeLayout = true;
                layoutShowSpeed = 0;
                layoutTimeline.play();*/


                setMaximize(!isMaximize(), null, false);
            }
        });
        //鼠标松开
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (showLayout) {
                layoutOriginalX = getX();
                layoutOriginalY = getY();
                layoutOriginalW = getWidth();
                layoutOriginalH = getHeight();
                executeLayout = true;
                setLayoutStyle(true);
                layoutShowSpeed = 0;
                layoutTimeline.play();
            }
        });
        //鼠标拖动
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (executeLayout)
                return;
            double mouseX = event.getScreenX();
            double mouseY = event.getScreenY();

            Rectangle2D r = FXUtil.getMouseScreen(mouseX, mouseY).getVisualBounds();
            byte sideLeft, sideTop;
            if (mouseX >= r.getMinX() && mouseX <= r.getMaxX())
                if (mouseY >= r.getMinY() && mouseY <= r.getMaxY()) {


                    if (mouseX - r.getMinX() <= rootContainerPadding && mouseX - r.getMinX() >= 0) {//左
                        sideLeft = 1;
                    } else if (r.getMaxX() - mouseX <= rootContainerPadding && r.getMaxX() - mouseX >= 0) {//右
                        sideLeft = 2;
                    } else {
                        sideLeft = 0;
                    }
                    if (mouseY - r.getMinY() <= rootContainerPadding && mouseY - r.getMinY() >= 0) {//顶
                        sideTop = 1;
                    } else if (r.getMaxY() - mouseY <= rootContainerPadding && r.getMaxY() - mouseY >= 0) {//底
                        sideTop = 2;
                    } else {
                        sideTop = 0;
                    }

                    if (sideLeft != 0 || sideTop != 0) {//触发布局
                        if (sideLeft == 0 && sideTop == 2) return;//如果在底部无视


                        double x = r.getMinX();
                        double y = r.getMinY();
                        double w = r.getMaxX() - r.getMinX();
                        double h = r.getMaxY() - r.getMinY();

                        if (w > h) {//横屏布局
                            if (!(sideLeft == 0)) {
                                if (sideLeft == 2) x += w / 2;
                                w /= 2;
                                if (sideTop == 2) y += h / 2;
                                if (sideTop != 0) h /= 2;
                            }
                        } else if (h > w) {//竖屏布局
                            if (sideTop == 2) y += h / 2;
                            if (sideTop != 0 && sideLeft != 0) h /= 2;
                        }
                        layoutX = x;
                        layoutY = y;
                        layoutW = w;
                        layoutH = h;

                        /*
                        originalX = getX();
                        originalY = getX();
                        originalWidth = getWidth();
                        originalHeight = getHeight();*/

                        if (!showLayout && !isLayoutStyle()) {
                            layoutOriginalX = getX();
                            layoutOriginalY = getY();
                            layoutOriginalW = getWidth();
                            layoutOriginalH = getHeight();
                            showLayout = true;
                        }
                        layoutShowSpeed = 0;
                        layoutTimeline.play();



                        /*if (sideThread == null) {//如果不存在则创建线程
                            sideThread = new BetterStage.TitleBar.SideThread(FXUtil.getStageRect(stage));
                        }*/

                    } else {


                        if (showLayout) {
                            showLayout = false;
                            /*layoutX = layoutOriginalX;
                            layoutY = layoutOriginalY;*/
                            if (isLayoutStyle()) {//恢复
                                /*layoutX = layoutOriginalX;
                                layoutY = layoutOriginalY;*/
                                layoutW = layoutOriginalW;
                                layoutH = layoutOriginalH;
                                executeLayout = true;
                                setLayoutStyle(false);
                                layoutAutoXY = true;
                                System.out.println("恢复原");
                            }
                            layoutShowSpeed = 0;
                            layoutTimeline.play();
                            System.out.println("结束展示布局");

                        } else if (!executeLayout) {
                            layoutX = getX();
                            layoutY = getY();
                            layoutW = getWidth();
                            layoutH = getHeight();
                        }


                        /*if (sideThread != null) {//复位
                         *//* System.out.println("复位");
                                System.out.println((sideLeft == 0 && sideTop == 0));
                                *//*
                        }*/
                    }
                }

            if (isLayoutStyle()) {
                setMaximize(false, FXUtil.getLastScreen(), true);
            }


        });


        stage.iconifiedProperty().addListener((observable, oldValue, newValue) -> {
            if (isMinimize != newValue) {
                //stage.setIconified(isMinimize);
                setMinimize(newValue);
                System.out.println("拦截 保持:" + isMinimize);
            }
        });
    }

    public boolean isMinimize() {
        return isMinimize;
    }


    private boolean minimizeLayout = false;
    private double minimizeLayoutX, minimizeLayoutY;
    private double minimizeLayoutW, minimizeLayoutH;

    public void setMinimize(boolean minimize) {
        stage.setIconified(false);
        if (minimize != isMinimize) {
            isMinimize = minimize;
            if (minimize) {
                //setSize(0,0);
                //stage.setIconified(true);
                minimizeMinW = minWidth;
                minimizeMinH = minHeight;//记录之前的最小尺寸
                setMinSize(0, 0);
                //setMinSize(0, 0);


                //if (isLayoutStyle())
                minimizeLayout = isLayoutStyle();
                if (minimizeLayout) {
                    System.out.println(layoutOriginalW);
                    minimizeLayoutX = layoutOriginalX;
                    minimizeLayoutY = layoutOriginalY;
                    minimizeLayoutW = layoutOriginalW;
                    minimizeLayoutH = layoutOriginalH;
                    setLayoutStyle(false);
                }
                layoutOriginalX = getX();
                layoutOriginalY = getY();
                layoutOriginalW = getWidth();
                layoutOriginalH = getHeight();
                layoutX = getX() + getWidth() / 2;
                layoutY = getY() + getHeight() / 2;
                layoutW = 0;
                layoutH = 0;
                layoutAutoXY = false;
                executeLayout = true;
                layoutShowSpeed = 0;
                layoutTimeline.play();
            } else {

                //setMaximize(true, null, false);
                if (minimizeLayout) {
                    setLayoutStyle(true);
                    /*layoutX = layoutOriginalX;
                    layoutY = layoutOriginalY;
                    layoutW = layoutOriginalW;
                    layoutH = layoutOriginalH;*/

                    layoutX = layoutOriginalX;
                    layoutY = layoutOriginalY;
                    layoutW = layoutOriginalW;
                    layoutH = layoutOriginalH;


                    layoutOriginalX = minimizeLayoutX;
                    layoutOriginalY = minimizeLayoutY;
                    layoutOriginalW = minimizeLayoutW;
                    layoutOriginalH = minimizeLayoutH;

                } else {
                    layoutX = layoutOriginalX;
                    layoutY = layoutOriginalY;
                    layoutW = layoutOriginalW;
                    layoutH = layoutOriginalH;
                }


                layoutAutoXY = false;
                executeLayout = true;
                layoutShowSpeed = 0;
                layoutTimeline.play();
                setMinSize(minimizeMinW, minimizeMinH);
            }
        }

    }

    public void setMaximize(boolean maximize, Screen screen, boolean autoXY) {
        if (screen == null)
            screen = FXUtil.getMouseScreen(getX() + getWidth() / 2, getY() + getHeight() / 2);
        if (maximize != isLayoutStyle())
            if (!maximize) {
                Rectangle2D r = FXUtil.getLastScreen().getVisualBounds();
                if (FXUtil.getMouseScreen(layoutOriginalX + layoutOriginalW / 2, layoutOriginalY + layoutOriginalH / 2) == null) {
                    layoutX = r.getMinX() + r.getWidth() / 2 - layoutOriginalW / 2;
                    layoutY = r.getMinY() + r.getHeight() / 2 - layoutOriginalH / 2;
                } else {
                    layoutX = layoutOriginalX;
                    layoutY = layoutOriginalY;
                }


                layoutW = layoutOriginalW;
                layoutH = layoutOriginalH;
                setLayoutStyle(false);
            } else {
                layoutOriginalX = getX();
                layoutOriginalY = getY();
                layoutOriginalW = getWidth();
                layoutOriginalH = getHeight();
                Rectangle2D r = screen.getVisualBounds();
                layoutX = r.getMinX();
                layoutY = r.getMinY();
                layoutW = r.getWidth();
                layoutH = r.getHeight();
                setLayoutStyle(true);
            }
        layoutAutoXY = autoXY;
        executeLayout = true;
        layoutShowSpeed = 0;
        layoutTimeline.play();
    }

    public boolean isMaximize() {
        return isLayoutStyle();
    }

    /**
     * 设置窗口布局样式
     */
    private void setLayoutStyle(boolean isLayout) {
        ObservableList<String> styleClass = rootContainer.getStyleClass();
        if (isLayout != styleClass.contains("window-layout"))
            if (isLayout)
                styleClass.add("window-layout");
            else
                styleClass.remove("window-layout");
    }

    private boolean isLayoutStyle() {
        return rootContainer.getStyleClass().contains("window-layout");
    }

    public double getX() {
        return stage.getX() + rootContainerPadding;
    }

    public double getY() {
        return stage.getY() + rootContainerPadding;
    }

    public double getWidth() {
        return stage.getWidth() - rootContainerPadding * 2;
    }

    public double getHeight() {
        return stage.getHeight() - rootContainerPadding * 2;
    }

    public void setX(double x) {
        stage.setX(x - rootContainerPadding);
    }

    public void setY(double y) {
        stage.setY(y - rootContainerPadding);
    }


    public void setWidth(double w) {
        double padding = rootContainerPadding * 2;
        if (w > maxWidth && maxWidth != -1) {
            stage.setWidth(maxWidth + padding);
            return;
        }
        if (w < minWidth && minWidth != -1) {
            stage.setWidth(minWidth + padding);
            return;
        }
        stage.setWidth(w + padding);
    }

    public void setHeight(double h) {
        double padding = rootContainerPadding * 2;
        if (h > maxHeight && maxHeight != -1) {
            stage.setHeight(maxHeight + padding);
            return;
        }
        if (h < minHeight && minHeight != -1) {
            stage.setHeight(minHeight + padding);
            return;
        }
        stage.setHeight(h + padding);
    }

    public void setSize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    /**
     * 注册过渡 (用于按钮)
     */
    public void registerTransition() {
/*
        Timeline timeline = new Timeline();


        //customStage.rootContainer.getChildrenUnmodifiable()
        CssParser cssParser = new CssParser();
        Stylesheet parse = cssParser.parse(Files.readString(Path.of("D:/Projects/FXComponent/target/classes/com/zedo/fxcomponent/DefaultStage.css")));
        List<Rule> rules = parse.getRules();


        FXUtil.loopNode(customStage.rootContainer, node -> {
            if (node.getStyleClass().contains("button")) {

                if (node instanceof Button button) {

                    *//*System.out.println(button.getStyle());
                    System.out.println(button.getStyleClass());
                    System.out.println(button.getCssMetaData().get(0));*//*
         *//* button.getCssMetaData().forEach(cssMetaData -> {
                        if (cssMetaData.getProperty().equals("-fx-region-background"))
                            cssMetaData.getSubProperties().forEach(cssMetaData1 -> {
                                if (cssMetaData1.getProperty().equals("-fx-background-color")) {
                                    HashMap<CssMetaData<? extends Styleable, ?>, Object> hash = new HashMap<>();
                                    System.out.println(cssMetaData1.getConverter().convert(hash));
                                    System.out.println(hash);
                                }

                            });

                    });*//*

                    for (String styleClass : button.getStyleClass()) {

                    }

                    System.out.println(button);
                    *//*button.setOnMouseEntered(event -> {
                        System.out.println(button.getBackground().getFills().get(0).getFill());
                    });
                    button.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
                    System.out.println(node);*//*
                }
            }
            return true;
        });
        //System.out.println(customStage.stage.getScene().getStylesheets());

        *//*KeyFrame one = new KeyFrame(Duration.ZERO, new KeyValue(customStage.rootNode.backgroundProperty(), FXUtil.getBackground(Color.RED)));
        KeyFrame two = new KeyFrame(Duration.millis(500), new KeyValue(customStage.rootNode.backgroundProperty(), FXUtil.getBackground(Color.YELLOW)));
        KeyFrame three = new KeyFrame(Duration.millis(1000), new KeyValue(customStage.rootNode.backgroundProperty(), FXUtil.getBackground(Color.ORANGE)));*//*
        WritableDoubleValue wv = new WritableDoubleValue() {

            @Override
            public double get() {
                return 0;
            }

            @Override
            public void set(double value) {


                BackgroundFill backgroundFill = customStage.rootNode.getBackground().getFills().get(0);
                Paint paint = backgroundFill.getFill();
                customStage.rootNode.setBackground(new Background(new BackgroundFill(paint, backgroundFill.getRadii(), backgroundFill.getInsets())));
                //System.out.println("kf2 " + value);
            }

            @Override
            public void setValue(Number value) {

            }

            @Override
            public Number getValue() {
                return null;
            }
        };

        KeyFrame kf1 = new KeyFrame(Duration.ZERO, "az", new KeyValue(wv, 0));
        KeyFrame kf2 = new KeyFrame(Duration.millis(500), "az", new KeyValue(wv, 1));
        timeline.getKeyFrames().addAll(kf1, kf2);
        *//*timeline.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            //customStage.rootNode.setBackground(FXUtil.getBackground(Color.hsb()));
        });*//*
        //timeline.setAutoReverse(true);
        timeline.setCycleCount(1);
        timeline.play();*/
    }
}
