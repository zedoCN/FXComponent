package com.zedo.fxcomponent.components.fileTreeView;


import com.zedo.fxcomponent.ColorResources;
import com.zedo.fxcomponent.ShapeResources;
import com.zedo.fxcomponent.utils.FXUtil;
import com.zedo.fxcomponent.utils.PathUtil;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileTreeView extends TreeView {


    public ContextMenu CreateFileContextMenu = new ContextMenu();//文件创建向导菜单

    protected DirButtons dirButtons = new DirButtons(); //目录项目按钮
    protected FileButtons fileButtons = new FileButtons();//文件项目按钮
    private FileTreeItem focusCreateFileItem;//焦点创建文件项目
    private FileTreeItem focusRenameFileTreeItem;//焦点重命名文件项目

    public boolean sureDeleteTip = false;//启用确认删除提示


    private TextField renameTextField = initRenameTextField();//重命名输入框

    private ConfirmDeletionPopupControl confirmDeletionPopupControl = new ConfirmDeletionPopupControl();//输入确认框

    private class ConfirmDeletionPopupControl extends PopupControl {
        public HBox hBox = new HBox();

        public ConfirmDeletionPopupControl() {

            hBox.setBackground(FXUtil.getBackground(ColorResources.dark_gray_side));
            hBox.setFillHeight(false);
            hBox.setAlignment(Pos.CENTER);
            hBox.setPadding(new Insets(2));
            hBox.setBorder(new Border(new BorderStroke(ColorResources.light_gray_control, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(2))));
            ObservableList<Node> list = hBox.getChildren();

            Pane pane = new Pane();
            pane.setShape(ShapeResources.warning);
            pane.setBackground(FXUtil.getBackground(ColorResources.yellow));
            pane.setPrefSize(18, 18);
            list.add(pane);

            Label label = new Label("真的要删除吗?");
            label.setTextFill(Color.WHITE);
            label.setFont(Font.font(16));
            list.add(label);
            setSkin(new Skin<>() {
                @Override
                public Skinnable getSkinnable() {
                    return null;
                }

                @Override
                public Node getNode() {
                    return hBox;
                }

                @Override
                public void dispose() {

                }
            });
        }


    }

    /**
     * 初始化重命名文本输入框
     *
     * @return
     */
    private TextField initRenameTextField() {
        TextField textField = new TextField();
        textField.setPrefWidth(100);
        HBox.setMargin(textField, new Insets(-3, 0, -3, 0));
        //setMargin(textField, new Insets(0));
        textField.setPadding(new Insets(3, 0, 3, 0));
        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER))
                focusRenameFileTreeItem.getValue().requestFocus();
        });
        textField.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                FileTreeHbox fileTreeHbox = focusRenameFileTreeItem.getValue();
                FileTreeItem fileTreeItem = fileTreeHbox.fileTreeItem;

                //System.out.println("执行重命名" + fileTreeItem.getPath());
                if (!renameTextField.getText().equals(fileTreeItem.getPath().getFileName().toString()))//如果一样则不修改

                    if (fileTreeItem.isDirectory()) {
                        Path newDir = fileTreeItem.getPath().resolveSibling(renameTextField.getText());
                        try {
                            Files.walkFileTree(fileTreeItem.getPath(), new SimpleFileVisitor<>() {
                                @Override
                                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                    if (newDir.getNameCount() == dir.getNameCount())
                                        Files.createDirectories(newDir);
                                    else
                                        Files.createDirectories(newDir.resolve(dir.subpath(newDir.getNameCount(), dir.getNameCount())));
                                    return FileVisitResult.CONTINUE;
                                }
                            });


                            Files.walkFileTree(fileTreeItem.getPath(), new SimpleFileVisitor<>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    Files.move(file, newDir.resolve(file.subpath(newDir.getNameCount(), file.getNameCount())));
                                    return FileVisitResult.CONTINUE;
                                }
                            });

                            deleteFolder(fileTreeItem);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                    } else {
                        try {
                            Files.move(fileTreeItem.getPath(), fileTreeItem.getPath().getParent().resolve(renameTextField.getText()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }


                fileTreeItem.getValue().label.setVisible(true);
                fileTreeHbox.label.setPrefWidth(100);

                fileTreeHbox.getChildren().remove(renameTextField);

                //renameTextField.setText(fileTreeItem.getPath().getFileName().toString());
                focusRenameFileTreeItem = null;
            }
        });

        //textField.setText();
        return textField;
    }

    /**
     * 初始化文件项目列表
     *
     * @return
     */
    public class FileButtons extends HBox {
        public FileButtons() {
            HBox.setHgrow(this, Priority.ALWAYS);
            setPadding(new Insets(0, 4, 0, 0));
            setAlignment(Pos.CENTER_RIGHT);
            setSpacing(4);


            //重命名按钮
            Button renameButton = new Button();
            renameButton.setShape(ShapeResources.edit);
            renameButton.setPrefSize(18, 18);
            renameButton.setPadding(new Insets(0, 0, 0, 0));
            renameButton.setOnMouseClicked(mouseEvent -> {
                FileTreeItem fileTreeItem = mouseEventToItem(mouseEvent);
                FileTreeHbox fileTreeHbox = fileTreeItem.getValue();
                fileTreeHbox.label.setVisible(false);
                fileTreeHbox.label.setPrefWidth(0);
                fileTreeHbox.getChildren().add(1, renameTextField);
                renameTextField.setText(fileTreeItem.getPath().getFileName().toString());
                focusRenameFileTreeItem = fileTreeItem;
                renameTextField.requestFocus();
                renameTextField.positionCaret(0);
                renameTextField.selectPositionCaret(renameTextField.getText().lastIndexOf("."));
            });
            FXUtil.setButtonColor(renameButton, ColorResources.orange);

            //删除文件
            Button deleteButton = new Button();
            deleteButton.setShape(ShapeResources.delete);
            deleteButton.setPrefSize(18, 18);
            deleteButton.setPadding(new Insets(0, 0, 0, 0));
            FXUtil.setButtonColor(deleteButton, ColorResources.red);
            deleteButton.setOnMouseClicked(mouseEvent -> {

                if (!dirButtons.confirmDeletion && sureDeleteTip) {
                    confirmDeletionPopupControl.show(deleteButton, mouseEvent.getScreenX() - 80, mouseEvent.getScreenY() - 40);
                    dirButtons.confirmDeletion = true;
                    deleteButton.setShape(ShapeResources.delete2);
                    return;
                }
                FileTreeItem fileItem = mouseEventToItem(mouseEvent);


                try {
                    Files.delete(fileItem.getPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });


            //焦点更新 清除确认删除
            deleteButton.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
                dirButtons.confirmDeletion = false;
                deleteButton.setShape(ShapeResources.delete);
            });


            getChildren().add(renameButton);
            getChildren().add(deleteButton);
        }
    }

    /**
     * 通过路径反找项目
     *
     * @param path 需要查找的路径
     * @return 被查找到的项目 没找到返回null
     */
    public FileTreeItem pathFindItem(Path path) {
        return rootFileTreeItem.pathFindItem(path);
    }

    /**
     * 获取焦点创建文件项目
     *
     * @return
     */
    public FileTreeItem getFocusCreateFileItem() {
        return focusCreateFileItem;
    }

    /**
     * 获取焦点重命名项目
     *
     * @return
     */
    public FileTreeItem getFocusRenameFileTreeItem() {
        return focusRenameFileTreeItem;
    }

    /**
     * 录项目按钮
     *
     * @return
     */
    public class DirButtons extends HBox {
        public Button addFolderButton = new Button();
        public Button addFileButton = new Button();
        public Button renameButton = new Button();
        public Button deleteButton = new Button();
        boolean confirmDeletion;//确认删除

        public void upDate(FileTreeItem fileTreeItem) {
            if (fileTreeItem.getParentItem() == null) {//如果是根目录
                getChildren().remove(renameButton);
                getChildren().remove(deleteButton);
            } else {
                if (!getChildren().contains(renameButton)) {
                    getChildren().add(renameButton);
                    getChildren().add(deleteButton);
                }
            }
            fileTreeItem.getValue().getChildren().add(dirButtons);
        }

        public DirButtons() {
            HBox.setHgrow(this, Priority.ALWAYS);
            setPadding(new Insets(0, 4, 0, 0));
            setAlignment(Pos.CENTER_RIGHT);
            setSpacing(4);

            //新建文件夹

            addFolderButton.setShape(ShapeResources.folder_add);
            addFolderButton.setPrefSize(18, 18);
            addFolderButton.setPadding(new Insets(0, 0, 0, 0));
            addFolderButton.setOnMouseClicked(mouseEvent -> {
                FileTreeItem fileTree = mouseEventToItem(mouseEvent);


                try {
                    FXUtil.createFileOrDir(fileTree.getPath(), "新建文件夹", null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            FXUtil.setButtonColor(addFolderButton, ColorResources.yellow);

            //添加文件

            addFileButton.setShape(ShapeResources.file_add);
            addFileButton.setPrefSize(18, 18);
            addFileButton.setPadding(new Insets(0, 0, 0, 0));
            addFileButton.setOnMouseClicked(mouseEvent -> {
                focusCreateFileItem = mouseEventToItem(mouseEvent);

                if (CreateFileContextMenu.getItems().size()==0)
                {
                    addCreateNewFileGuide(null, Color.TRANSPARENT, "无可创建文件", fileItem -> {

                    });
                }

                CreateFileContextMenu.show((Button) mouseEvent.getSource(), Side.BOTTOM, 0, 0);
            });
            FXUtil.setButtonColor(addFileButton, ColorResources.green2);

            //重命名按钮

            renameButton.setShape(ShapeResources.edit);
            renameButton.setPrefSize(18, 18);
            renameButton.setPadding(new Insets(0, 0, 0, 0));
            renameButton.setOnMouseClicked(mouseEvent -> {
                FileTreeItem fileTreeItem = mouseEventToItem(mouseEvent);
                FileTreeHbox fileTreeHbox = fileTreeItem.getValue();
                fileTreeHbox.label.setVisible(false);
                fileTreeHbox.label.setPrefWidth(0);
                fileTreeHbox.getChildren().add(1, renameTextField);
                renameTextField.setText(fileTreeItem.getPath().getFileName().toString());
                focusRenameFileTreeItem = fileTreeItem;
                renameTextField.requestFocus();
                renameTextField.positionCaret(renameTextField.getText().length());
            });
            FXUtil.setButtonColor(renameButton, ColorResources.orange);

            //删除文件夹

            deleteButton.setShape(ShapeResources.delete);
            deleteButton.setPrefSize(18, 18);
            deleteButton.setPadding(new Insets(0, 0, 0, 0));
            deleteButton.setOnMouseClicked(mouseEvent -> {
                if (!confirmDeletion && sureDeleteTip) {
                    confirmDeletionPopupControl.show(deleteButton, mouseEvent.getScreenX() - 80, mouseEvent.getScreenY() - 40);
                    confirmDeletion = true;
                    deleteButton.setShape(ShapeResources.delete2);
                    return;
                }

                FileTreeItem fileItem = mouseEventToItem(mouseEvent);
                deleteFolder(fileItem);
            });
            FXUtil.setButtonColor(deleteButton, ColorResources.red);


            //焦点更新 清除确认删除
            deleteButton.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
                confirmDeletion = false;
                deleteButton.setShape(ShapeResources.delete);
            });


            getChildren().add(addFolderButton);
            getChildren().add(addFileButton);
            getChildren().add(renameButton);
            getChildren().add(deleteButton);
        }
    }

    /**
     * 删除项目
     *
     * @param fileItem 要被删除的项目
     */
    public void deleteFolder(FileTreeItem fileItem) {
        fileItem.release();
        try {
            PathUtil.delete(fileItem.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    interface CreateNewFileGuide {
        void CreateNewFileGuide(FileTreeItem fileItem);
    }

    /**
     * 添加文件创建向导
     *
     * @param icon                文件图标
     * @param color               图标颜色
     * @param str                 标题
     * @param createNewFileGuider 向导
     */
    public void addCreateNewFileGuide(SVGPath icon, Color color, String str, CreateNewFileGuide createNewFileGuider) {
        Pane pane = new Pane();
        pane.setShape(icon);
        pane.setBackground(FXUtil.getBackground(color));
        pane.setPrefSize(16, 16);
        MenuItem menuItem = new MenuItem(str, pane);
        CreateFileContextMenu.getItems().add(menuItem);
        menuItem.setOnAction(actionEvent -> createNewFileGuider.CreateNewFileGuide(focusCreateFileItem));
    }

    /**
     * 鼠标事件获取到来源文件项目
     *
     * @param mouseEvent
     * @return
     */
    private static FileTreeItem mouseEventToItem(MouseEvent mouseEvent) {
        return ((FileTreeHbox) (((Button) mouseEvent.getSource()).getParent().getParent())).fileTreeItem;
    }

    //            watchKey = getPath().register(fileStateWatcher,new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}, ExtendedWatchEventModifier.FILE_TREE);
    public FileTreeView() {
        CreateFileContextMenu.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                focusCreateFileItem.getValue().getChildren().remove(dirButtons);
                focusCreateFileItem = null;
            }
        });

    }

    public Path getRootPath() {
        return rootPath;
    }

    private Path rootPath;//绑定的根目录

    public Path getMoveTempPath() {
        return moveTempPath;
    }

    private Path moveTempPath;//移动缓存目录


    private FileTreeItem rootFileTreeItem;//根目录项目

    private boolean fileStateListenerEnable;//文件状态监听器
    private WatchService fileStateWatcher;//文件状态检查服务
    private Thread fileStateListener;//文件状态监听线程

    /**
     * 文件状态事件监听线程
     */
    private Thread initThread() {
        return new Thread(() -> {
            while (fileStateListenerEnable) {
                WatchKey watchKey;
                try {
                    watchKey = fileStateWatcher.take();
                } catch (Exception e) {
                    continue;
                }
                if (watchKey == null)
                    continue;
                List<WatchEvent<?>> events = new ArrayList<>(watchKey.pollEvents());
                for (WatchEvent<?> event : events) {
                    if (event.kind() == OVERFLOW) {
                        continue;
                    }
                    fileStateEvent(event.kind(), ((Path) watchKey.watchable()).resolve((Path) event.context()), watchKey);

                }
                watchKey.reset();
            }
            //System.out.println("结束监听");
        });
    }

    /**
     * 释放
     */
    public void release() {
        if (fileStateListenerEnable)//如果是启用状态 则注销
        {

            try {
                fileStateWatcher.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fileStateWatcher = null;
            fileStateListenerEnable = false;
            rootFileTreeItem.release();
            rootFileTreeItem = null;
            rootPath = null;
            setRoot(null);
        }
    }

    /**
     * 绑定文件目录
     *
     * @param dirPath 要绑定的目录
     * @throws IOException
     */
    public void bindDirFile(Path dirPath) throws IOException {

        if (!Files.isDirectory(dirPath))
            throw new RuntimeException("绑定异常:不是一个一个目录");

        //System.out.println("调试: " + dirPath);

        release();
        fileStateWatcher = FileSystems.getDefault().newWatchService();
        //fileStateWatcher
        rootFileTreeItem = new FileTreeItem(dirPath, null, fileStateWatcher, this);
        setRoot(rootFileTreeItem);
        rootPath = dirPath;
        moveTempPath = rootPath.toAbsolutePath().getParent().resolve("MoveTemp");
        fileStateListenerEnable = true;
        fileStateListener = initThread();
        fileStateListener.start();


    }


    /**
     * 处理文件状态事件
     */
    private void fileStateEvent(WatchEvent.Kind<?> kind, Path path, WatchKey watchKey) {
        //找到FileTreeItem对象
        FileTreeItem fileTreeItem = rootFileTreeItem;
        for (int index = rootPath.getNameCount(); index < path.getNameCount(); index++) {
            FileTreeItem later = fileTreeItem.getSubItem(String.valueOf(path.getName(index)));
            fileTreeItem = (later == null ? fileTreeItem : later);
        }

        if (kind == ENTRY_CREATE) {//创建事件
            fileTreeItem.addSubItem(path);
        } else if (kind == ENTRY_DELETE) {//删除事件
            if (fileTreeItem.getParentItem().getSubItem(String.valueOf(path.getFileName())).isDirectory()) {
                fileTreeItem.release();//释放
            }
            fileTreeItem.getParentItem().removeSubItem(String.valueOf(path.getFileName()));
        } else if (kind == ENTRY_MODIFY) {//编辑事件

        }
    }


}
