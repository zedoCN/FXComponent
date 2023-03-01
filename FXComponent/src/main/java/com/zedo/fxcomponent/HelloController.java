package com.zedo.fxcomponent;

import com.zedo.fxcomponent.components.fileTreeView.FileTreeView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    TreeItem<String> root = new TreeItem<>();
    TreeItem<String> treeItem = new TreeItem<>();

    @FXML
    private VBox mainBox;


    FileTreeView fileTreeView = new FileTreeView();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {



        /*mainBox.getChildren().add(fileTreeView);

        //mainBox.getChildren().add(new FileTreeHbox(new File("D:\\新666\\newDirec555\\新建 BMP 图像.bmp")));
        try {
            fileTreeView.bindDirFile(Paths.get("files"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileTreeView.addCreateNewFileGuide(ShapeResources.file_note, Color.WHITE, "谱面文件", fileItem -> {
            System.out.println("创建:" + fileItem);
            try {
                FXUtils.createFileOrDir(fileItem.getPath(), "谱面", "zxn");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });*/

    }

    public void bu1() {

        fileTreeView.release();
        /*Label label = new Label("asdfasfgdsf");
        PopupControl popupControl = new PopupControl();
        popupControl.setSkin(new Skin<>() {
            @Override
            public Skinnable getSkinnable() {
                return null;
            }

            @Override
            public Node getNode() {
                return label;
            }

            @Override
            public void dispose() {
                System.out.println(1111);
            }
        });
        popupControl.show(mainBox, 400, 400);
        popupControl.setX(500);*/

        //fileTreeView.release();
        /*try {
            fileTreeView.bindDirFile(Paths.get("files"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }
}