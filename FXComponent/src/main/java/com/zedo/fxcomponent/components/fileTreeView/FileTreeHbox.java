package com.zedo.fxcomponent.components.fileTreeView;


import com.zedo.fxcomponent.ColorResources;
import com.zedo.fxcomponent.ShapeResources;
import com.zedo.fxcomponent.utils.FXUtil;
import com.zedo.fxcomponent.utils.PathUtil;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileTreeHbox extends HBox {
    public Path path;//当前文件
    public Pane icon;//图标
    public Label label = new Label();//用于显示
    public FileTreeItem fileTreeItem;//自身项目
    public FileTreeView fileTreeView;//
    public Tooltip tooltip = new Tooltip();

    /**
     * 设置项目图标
     *
     * @param sVGPath 要设置的图标
     * @param c       图标的颜色 为null不修改
     */
    public void setIcon(SVGPath sVGPath, Color c) {
        icon.setShape(sVGPath);
        if (c != null)
            icon.setBackground(FXUtil.getBackground(c));
    }

    public FileTreeHbox(Path path, FileTreeItem fileTreeItem, FileTreeView fileTreeView) {
        this.fileTreeItem = fileTreeItem;
        this.path = path;
        this.fileTreeView = fileTreeView;
        //setBackground(FXUtils.getBG(Color.BEIGE));
        //设置布局
        setAlignment(Pos.CENTER_LEFT);
        label.setPrefWidth(100);

        icon = fileGetIcon(path);
        label.setText(path.getFileName().toString());


        label.setTooltip(tooltip);
        tooltip.setAnchorX(-50);
        try {
            tooltip.setText(path.getFileName().toString() + " " + Files.size(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //textField.setVisible(false);

        //加入控件
        getChildren().add(icon);
        getChildren().add(label);
        //getChildren().add(textField);


        //注册事件


       /* //鼠标右键事件
        addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (mouseEvent.isSecondaryButtonDown()) {
                FileTreeHbox fileTreeHbox=(FileTreeHbox) mouseEvent.getSource();
                fileTreeView.dirContextMenu.show(fileTreeHbox, fileTreeHbox.getTranslateX(), fileTreeHbox.getTranslateY());
            }
        });*/

        //鼠标进入事件
        addEventFilter(MouseEvent.MOUSE_ENTERED, mouseEvent -> {
            if (fileTreeView.getFocusCreateFileItem() != null)//防止新建文件菜单丢失焦点
                return;
            if (fileTreeItem.isDirectory()) {
                fileTreeView.dirButtons.upDate(fileTreeItem);
            } else
                getChildren().add(fileTreeView.fileButtons);
        });
        //鼠标移出事件
        addEventFilter(MouseEvent.MOUSE_EXITED, mouseEvent -> {
            if (fileTreeView.getFocusCreateFileItem() != null)//防止新建文件菜单丢失焦点
                return;
            if (fileTreeItem.isDirectory())
                getChildren().remove(fileTreeView.dirButtons);
            else
                getChildren().remove(fileTreeView.fileButtons);
        });
        //设置拖拽事件
        setOnDragEntered(dragEvent -> {
            FileTreeItem selection = (fileTreeItem.isDirectory() ? fileTreeItem : fileTreeItem.getParentItem());
            selection.getValue().setBackground(FXUtil.getBackground(ColorResources.green));
            fileTreeView.getSelectionModel().select(selection);

        });
        //设置拖拽事件
        setOnDragExited(dragEvent -> {
            FileTreeItem selection = (fileTreeItem.isDirectory() ? fileTreeItem : fileTreeItem.getParentItem());
            selection.getValue().setBackground(FXUtil.getBackground(Color.rgb(0, 0, 0, 0)));


        });
        //经过
        setOnDragOver(dragEvent -> {
            dragEvent.acceptTransferModes(TransferMode.MOVE);
        });

        //拖放
        setOnDragDropped(dragEvent -> {
            FileTreeItem selection = (fileTreeItem.isDirectory() ? fileTreeItem : fileTreeItem.getParentItem());
            for (File f : dragEvent.getDragboard().getFiles()) {
                Path source = f.toPath();
                /*FileTreeItem fileItem = fileTreeView.pathFindItem(source);
                //System.out.println("正在反找:");
                //System.out.println(fileItem);
                if (fileItem != null)
                    fileItem.release();*/
                try {
                    PathUtil.move(source, selection.getPath().resolve(source.getFileName()));
                    tryDeleteMoveTemp();//尝试删除缓存文件夹
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });

        //拖放完成
        setOnDragDone(dragEvent -> {
            setBackground(FXUtil.getBackground(Color.rgb(0, 0, 0, 0)));
            Path moveTempPath = fileTreeView.getMoveTempPath();
            Path movePath = moveTempPath.resolve(path.getFileName());
            if (movePath.toFile().exists())//判断文件是否存在 复原位置
            {
                //System.out.println("复原");
                try {
                    PathUtil.move(movePath, fileTreeItem.getPath());
                    tryDeleteMoveTemp();//尝试删除缓存文件夹
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        //被拖放
        setOnDragDetected(mouseEvent -> {
            if (fileTreeItem.parent == null)
                return;
            setBackground(FXUtil.getBackground(ColorResources.dark_red));
            fileTreeItem.release();//释放
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent clipboardContent = new ClipboardContent();
            ArrayList<File> list = new ArrayList<>();

            //System.out.println("被拖放");

            Path moveTempPath = fileTreeView.getMoveTempPath();
            try {
                if (!moveTempPath.toFile().exists())
                    Files.createDirectory(moveTempPath);
                PathUtil.move(path, moveTempPath.resolve(path.getFileName()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            list.add(moveTempPath.resolve(path.getFileName()).toFile());
            clipboardContent.putFiles(list);
            db.setContent(clipboardContent);

            WritableImage wi = new WritableImage((int) getWidth(), (int) getHeight());
            snapshot(new SnapshotParameters(), wi);
            db.setDragView(wi);
        });

    }

    private void tryDeleteMoveTemp() throws IOException {
        Path moveTempPath = fileTreeView.getMoveTempPath();
        if (moveTempPath.toFile().exists())//删除缓存文件夹
            if (Files.list(moveTempPath).toArray().length == 0)
                Files.delete(moveTempPath);
    }

    private static Pane fileGetIcon(Path file) {
        Pane pane = new Pane();
        pane.setBackground(FXUtil.getBackground(Color.rgb(153, 153, 153)));
        pane.setPrefSize(18, 18);
        pane.setMaxSize(18, 18);
        if (Files.isDirectory(file)) {//返回文件夹图标
            pane.setBackground(FXUtil.getBackground(Color.rgb(153, 63, 49)));
            pane.setShape(ShapeResources.folder);
            return pane;
        }
        String extension = String.valueOf(file.getFileName());

        if (extension.endsWith(".zxp")) {
            //工程文件
            pane.setBackground(FXUtil.getBackground(Color.rgb(166, 33, 141)));
            pane.setShape(ShapeResources.file_zxp);

        } else if (extension.endsWith(".png") || extension.endsWith(".jpg") || extension.endsWith(".bmp") || extension.endsWith(".svg")) {
            //图片文件
            pane.setBackground(FXUtil.getBackground(Color.rgb(33, 117, 166)));
            pane.setShape(ShapeResources.file_picture);
        } else if (extension.endsWith(".txt") || extension.endsWith(".ini")) {
            //文本文件//
            pane.setShape(ShapeResources.file_text);
        } else if (extension.endsWith(".imd") || extension.endsWith(".osu") || extension.endsWith(".mc") || extension.endsWith(".zxn")) {
            pane.setBackground(FXUtil.getBackground(Color.rgb(153, 142, 51)));
            //谱面文件
            pane.setShape(ShapeResources.file_note);
        } else if (extension.endsWith(".mp3") || extension.endsWith(".ogg") || extension.endsWith(".wav")) {
            pane.setBackground(FXUtil.getBackground(Color.rgb(51, 153, 68)));
            //音频文件
            pane.setShape(ShapeResources.file_audio);
        } else {
            //未知文件
            pane.setShape(ShapeResources.file_unknown);
        }

        return pane;
    }
}
