package com.zedo.fxcomponent.components;

import com.zedo.fxcomponent.utils.FXUtil;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
//import javafx.stage.


public class BetterStage {
    Stage stage;//舞台
    Scene scene;//场景
    RootNode rootNode;//根容器
    ObservableList<Node> rootChildren;
    TitleBar titleBar;//标题栏
    VBox mainNode;//主容器
    Insets boardInsets;//边框
    boolean maximized = false;//最大化
    Window sceneWindow;//当前场景


    public boolean enableEdgeLayout = true;//启用边缘布局

    public boolean cursorLock = false;
    public EventHandler cursorHandler = (EventHandler<MouseEvent>) mouseEvent -> {
        //System.out.println(mouseEvent);
        if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_RELEASED))
            cursorLock = false;
        if (mouseEvent.getTarget() instanceof RootNode) {


            if (cursorLock)
                return;
            double x = mouseEvent.getX();
            double y = mouseEvent.getY();
            boolean left = x < boardInsets.getLeft();
            boolean right = x + boardInsets.getRight() > rootNode.getWidth();
            boolean top = y < boardInsets.getTop();
            boolean bottom = y + boardInsets.getBottom() > rootNode.getHeight();

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
            }
            if (bottom) {
                if (left) rootNode.setCursor(Cursor.SW_RESIZE);
                else if (right) rootNode.setCursor(Cursor.SE_RESIZE);
            }


        } else {
            rootNode.setCursor(Cursor.DEFAULT);
        }
    };

    public class RootNode extends VBox {
        double x, y;//鼠标原始
        double w, h;//原始
        double mx, my;//鼠标偏移


        public RootNode(Insets boardInsets) {


            //移动
            setOnMouseMoved(cursorHandler);
            //退出
            setOnMouseExited(cursorHandler);
            //进入
            setOnMouseEntered(cursorHandler);
            //松开
            setOnMouseReleased(cursorHandler);

            //按下
            setOnMousePressed((MouseEvent mouseEvent) -> {
                cursorLock = true;
                mx = mouseEvent.getX() - getScene().getX();
                my = mouseEvent.getY() - getScene().getY();
                this.x = mouseEvent.getScreenX();
                this.y = mouseEvent.getScreenY();
                this.w = getScene().getWidth();
                this.h = getScene().getHeight();
            });

            //拖动
            setOnMouseDragged((MouseEvent mouseEvent) -> {
                Cursor c = getCursor();
                double newWidth = getScene().getWidth();
                double newHeight = getScene().getHeight();
                double newX = getScene().getX();
                double newY = getScene().getY();
                if (c.equals(Cursor.E_RESIZE) || c.equals(Cursor.SE_RESIZE) || c.equals(Cursor.NE_RESIZE)) {
                    setW(w + mouseEvent.getScreenX() - this.x);
                }
                if (c.equals(Cursor.S_RESIZE) || c.equals(Cursor.SE_RESIZE) || c.equals(Cursor.SW_RESIZE))
                    setH(h + mouseEvent.getScreenY() - this.y);
                if (c.equals(Cursor.W_RESIZE) || c.equals(Cursor.NW_RESIZE) || c.equals(Cursor.SW_RESIZE)) {
                    if (setW(w + (x - mouseEvent.getScreenX() - mx)))
                        stage.setX(mouseEvent.getScreenX() - mx);
                }
                if (c.equals(Cursor.N_RESIZE) || c.equals(Cursor.NE_RESIZE) || c.equals(Cursor.NW_RESIZE)) {
                    if (setH(h + (y - mouseEvent.getScreenY() - my)))
                        stage.setY(mouseEvent.getScreenY() - my);

                }

            });
        }

        private boolean setW(double w) {
            if (w > getMaxWidth() && getMaxWidth() != -1) {
                stage.setWidth(getMaxWidth());
                return false;
            }
            if (w < getMinWidth() && getMinWidth() != -1) {
                stage.setWidth(getMinWidth());
                return false;
            }
            stage.setWidth(w);
            return true;
        }

        private boolean setH(double h) {
            if (h > getMaxHeight() && getMaxHeight() != -1) {
                stage.setHeight(getMaxHeight());
                return false;
            }
            if (h < getMinHeight() && getMinHeight() != -1) {
                stage.setHeight(getMinHeight());
                return false;
            }
            stage.setHeight(h);
            return true;
        }
    }

    public class TitleBar extends HBox {
        double x, y;

        Stage sideStage = new Stage();
        public Pane sidePane = new Pane();
        public double sideSize = 26;

        int sideLeft = 0;
        int sideTop = 0;

        double toBeX = 0;//将要宽高
        double toBeY = 0;
        double toBeW = 0;
        double toBeH = 0;

        double originalW = 0;//原始宽高
        double originalH = 0;
        boolean isEdgeLayout = false;//正在布局

        public TitleBar() {

            //ImageView image = new ImageView();
            //sidePane.setBackground(FXUtils.getBackground(Color.rgb(10, 10, 10, 0.4)));
            //sidePane.setBorder(FXUtils.getBorder(Color.rgb(100, 100, 100, 0.6), BorderStrokeStyle.SOLID, 10, 4));
            sidePane.setBackground(new Background(new BackgroundFill(Color.rgb(100, 100, 100, 0.6), new CornerRadii(10), new Insets(12))));

            /*GaussianBlur g = new GaussianBlur();
            g.setRadius(10);
            sidePane.setEffect(g);*/


            Scene sideScene = new Scene(sidePane);
            sideScene.setFill(null);
            sideStage.setScene(sideScene);
            sideStage.initStyle(StageStyle.TRANSPARENT);


            setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2) {
                    //if ()
                    if (isEdgeLayout) {
                        isEdgeLayout = false;
                        setMaximized(false);
                        return;
                    }
                    setMaximized(!maximized);

                    //if (maximized)

                    //sideStage.setMaximized(!stage.isMaximized());
                }
            });

            //鼠标按下
            setOnMousePressed((MouseEvent mouseEvent) -> {


                x = sceneWindow.getX() - mouseEvent.getScreenX();
                y = sceneWindow.getY() - mouseEvent.getScreenY();


            });
            //鼠标拖动
            setOnMouseDragged((MouseEvent mouseEvent) -> {


                if (isEdgeLayout || maximized) {


                    //System.out.println("恢复边缘布局");
                    isEdgeLayout = false;

                    //System.out.println();
                    //System.out.println(mouseEvent.getSceneX() / stage.getWidth() * originalW);
                    //System.out.println(mouseEvent.getScreenX() - mouseEvent.getSceneX() / stage.getWidth() * originalW);

                    stage.setX(mouseEvent.getScreenX() - mouseEvent.getSceneX() / stage.getWidth() * originalW);
                    x = sceneWindow.getX() - mouseEvent.getScreenX();
                    //
                    //stage.setX(mouseEvent.getScreenX() + (mouseEvent.getScreenX() / stage.getWidth() * originalW));

                    stage.setWidth(originalW);
                    stage.setHeight(originalH);

                }


                double mx = mouseEvent.getScreenX();
                double my = mouseEvent.getScreenY();
                stage.setX(mx + x);
                stage.setY(my + y);

                if (enableEdgeLayout)
                //屏幕边缘检测
                {
                    Rectangle2D r = getMouseScreen(mx, my).getVisualBounds();
                    if (mx >= r.getMinX() && mx <= r.getMaxX()) if (my >= r.getMinY() && my <= r.getMaxY()) {


                        if (mx - r.getMinX() <= sideSize && mx - r.getMinX() >= 0) {//左
                            sideLeft = 1;
                        } else if (r.getMaxX() - mx <= sideSize && r.getMaxX() - mx >= 0) {//右
                            sideLeft = 2;
                        } else {
                            sideLeft = 0;
                        }
                        if (my - r.getMinY() <= sideSize && my - r.getMinY() >= 0) {//顶
                            sideTop = 1;
                        } else if (r.getMaxY() - my <= sideSize && r.getMaxY() - my >= 0) {//底
                            sideTop = 2;
                        } else {
                            sideTop = 0;
                        }

                        if (sideLeft != 0 || sideTop != 0) {

                            if (sideLeft == 0 && sideTop == 2) return;

                            if (sideThread == null) {//如果不存在则创建线程
                                sideThread = new SideThread(FXUtil.getStageRect(stage));
                            }


                            double x = r.getMinX();
                            double y = r.getMinY();
                            double w = r.getMaxX() - r.getMinX();
                            double h = r.getMaxY() - r.getMinY();
                            if (!(sideLeft == 0 && sideTop == 1)) {
                                if (sideLeft == 2) x += w / 2;
                                if (sideLeft != 0) w /= 2;
                                if (sideTop == 2) y += h / 2;
                                if (sideTop != 0) h /= 2;
                            }
                            toBeX = x;
                            toBeY = y;
                            toBeW = w;
                            toBeH = h;
                            originalW = stage.getWidth();
                            originalH = stage.getHeight();

                        } else {
                            if (sideThread != null) {//复位
                                   /* System.out.println("复位");
                                    System.out.println((sideLeft == 0 && sideTop == 0));
                                    */
                            }
                        }
                    }
                }


            });
            //鼠标松开
            setOnMouseReleased(mouseEvent -> {
                if (sideLeft != 0 || sideTop != 0) {
                    stage.setX(toBeX);
                    stage.setY(toBeY);
                    stage.setWidth(toBeW);
                    stage.setHeight(toBeH);
                    sideLeft = 0;
                    sideTop = 0;
                    isEdgeLayout = true;
                }
            });


        }

        SideThread sideThread;//负责处理边动画

        class SideThread extends Thread {
            double x = 0;
            double y = 0;
            double w = 0;
            double h = 0;
            boolean loop = true;
            Rectangle2D original;

            public SideThread(Rectangle2D r) {

                Platform.runLater(() -> {


                    sideStage.setX(r.getMinX());
                    sideStage.setY(r.getMinY());
                    sideStage.setWidth(r.getWidth());
                    sideStage.setHeight(r.getHeight());

                    sideStage.show();
                    stage.requestFocus();


                     /*robot = new Robot();
                     image = new WritableImage((int) sideStage.getWidth(), (int) sideStage.getHeight());*/
                    this.start();
                });
            }

            @Override
            public void run() {


                while (loop) {

                    Platform.runLater(() -> {//处理动画
                        if (sideLeft == 0 && sideTop != 1) {
                            toBeW = sceneWindow.getWidth();
                            toBeH = sceneWindow.getHeight();
                            toBeX = sceneWindow.getX();
                            toBeY = sceneWindow.getY();

                            if (toBeX - sideStage.getX() < 2) if (toBeY - sideStage.getY() < 2)
                                if (toBeW - sideStage.getWidth() < 2)
                                    if (toBeH - sideStage.getHeight() < 2)
                                        loop = false;


                        }
                        original = FXUtil.getStageRect(stage);
                        x = Math.ceil((toBeX - sideStage.getX()) * 0.3);
                        y = Math.ceil((toBeY - sideStage.getY()) * 0.3);
                        w = Math.ceil((toBeW - sideStage.getWidth()) * 0.3);
                        h = Math.ceil((toBeH - sideStage.getHeight()) * 0.3);


                        sideStage.setWidth(sideStage.getWidth() + w);
                        sideStage.setHeight(sideStage.getHeight() + h);
                        sideStage.setX(sideStage.getX() + x);
                        sideStage.setY(sideStage.getY() + y);



                        /*robot.getScreenCapture(image, sideStage.getX(), sideStage.getY(), sideStage.getWidth(), sideStage.getHeight());
                        sidePane.setBackground(new Background(new BackgroundImage(image,
                                null,
                                null,
                                new BackgroundPosition(Side.LEFT,0,false,Side.BOTTOM,0,false),
                                null)));*/


                    });

                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                sideThread = null;
                Platform.runLater(() -> {//结束动画
                    sideStage.close();
                    System.gc();
                });

            }
        }

        public void runSideStage(Rectangle2D r) {
            if (sideThread == null) {//如果不存在则创建线程
                sideThread = new SideThread(r);
            }
        }


    }

    public HBox getTitleBar() {
        return titleBar;
    }

    public VBox getMainNode() {
        return mainNode;
    }

    public void setMinSize(double w, double h) {
        rootNode.setMinSize(w, h);
    }

    public void setMaxSize(double w, double h) {
        rootNode.setMaxSize(w, h);
    }

    public BetterStage(Stage stage, Color bgColor, Color titleBarColor, Color mainNodeColor, double titleBarSize, double boardSize) {
        this.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);

        boardInsets = new Insets(boardSize);

        rootNode = new RootNode(boardInsets);
        rootNode.setBackground(FXUtil.getBackground(titleBarColor));


        rootNode.setMinSize(400, 300);
        rootNode.setMaxSize(600, 500);


        titleBar = new TitleBar();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setBackground(FXUtil.getBackground(mainNodeColor));
        titleBar.setPrefHeight(titleBarSize);
        titleBar.setMinHeight(titleBarSize);
        titleBar.setPadding(new Insets(-boardInsets.getTop(), 0, 0, 0));


        VBox.setMargin(titleBar, new Insets(boardInsets.getTop(), boardInsets.getRight(), 0, boardInsets.getLeft()));

        mainNode = new VBox();
        VBox.setVgrow(mainNode, Priority.ALWAYS);
        mainNode.setBackground(FXUtil.getBackground(bgColor));
        VBox.setMargin(mainNode, new Insets(0, boardInsets.getRight(), boardInsets.getBottom(), boardInsets.getLeft()));

        //处理窗口
        mainNode.setOnMouseEntered(cursorHandler);


        scene = new Scene(rootNode);
        stage.setScene(scene);
        sceneWindow = scene.getWindow();

        rootChildren = rootNode.getChildren();
        rootChildren.add(titleBar);
        rootChildren.add(mainNode);

        stage.widthProperty().addListener((observableValue, number, t1) -> {
            checkMaximized();
        });
        stage.heightProperty().addListener((observableValue, number, t1) -> {
            checkMaximized();
        });

        Platform.runLater(() -> {
            titleBar.originalW = stage.getWidth();
            titleBar.originalH = stage.getHeight();
        });

    }

    public Screen getStageScreen() {
        double x = sceneWindow.getX() + sceneWindow.getWidth() / 2;
        double y = sceneWindow.getY() + sceneWindow.getHeight() / 2;
        return getMouseScreen(x, y);
    }

    public Screen getMouseScreen(double x, double y) {
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D r = screen.getBounds();
            if (x >= r.getMinX() && x <= r.getMaxX())
                if (y >= r.getMinY() && y <= r.getMaxY())
                    return screen;
        }
        return null;
        //throw new RuntimeException("没有扎到Stage所在屏幕");
    }

    public void setMaximized(boolean maximized) {
        //System.out.println("设置最大化: " + maximized);
        Screen screen = getStageScreen();
        Rectangle2D r = screen.getVisualBounds();
        if (maximized) {
            /*originalX = stage.getX();
            originalY = stage.getY();*/
            stage.setX(r.getMinX());
            stage.setY(r.getMinY());
            stage.setWidth(r.getWidth());
            stage.setHeight(r.getHeight());
        } else {
            //System.out.println("干活");
            Rectangle2D rectangle = FXUtil.getStageRect(stage);

            stage.setX(r.getMinX() + r.getWidth() / 2 - titleBar.originalW / 2);
            stage.setY(r.getMinY() + r.getHeight() / 2 - titleBar.originalH / 2);
            stage.setWidth(titleBar.originalW);
            stage.setHeight(titleBar.originalH);

            titleBar.runSideStage(rectangle);
        }
    }

    private void checkMaximized() {
        Screen screen = getStageScreen();
        if (screen == null)
            return;
        maximized = false;
        //屏幕边缘检测
        Rectangle2D r = screen.getVisualBounds();
        if (r.getMinX() == stage.getX())
            if (r.getMaxX() == stage.getX() + stage.getWidth())
                if (r.getMinY() == stage.getY())
                    if (r.getMaxY() == stage.getY() + stage.getHeight())
                        maximized = true;


    }
}
