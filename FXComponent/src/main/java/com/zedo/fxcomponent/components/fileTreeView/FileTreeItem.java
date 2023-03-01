package com.zedo.fxcomponent.components.fileTreeView;

import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileTreeItem extends TreeItem<FileTreeHbox> {
    private HashMap<String, FileTreeItem> fileList = new HashMap<>();//用于记录子项目成员 方便快速查找

    @Override
    public String toString() {
        return "FileTreeItem: " + getValue().path.getFileName();
    }

    public FileTreeItem parent;//父项目 如果为null 则为Root
    private WatchService fileStateWatcher;//主文件目录状态监听器
    private WatchKey watchKey;//当前监听
    private boolean isDirectory;//是否是文件
    private FileTreeView fileTreeView;


    public FileTreeItem(Path thisFile, FileTreeItem parent, WatchService fileStateWatcher, FileTreeView fileTreeView) {
        this.fileStateWatcher = fileStateWatcher;
        this.fileTreeView = fileTreeView;
        setValue(new FileTreeHbox(thisFile, this, fileTreeView));
        this.parent = parent;
        isDirectory = Files.isDirectory(thisFile);
        if (isDirectory)//如果是文件目录 则继续向下枚举
        {
            register();

        }


    }

    /**
     * 释放自己 和 子Item
     */
    public void release() {
        if (isDirectory) {
            //System.out.println("释放: " + getPath());
            watchKey.cancel();
            for (FileTreeItem fileTreeItem : getFTIChildrenDir())
                fileTreeItem.release();
        }
    }

    public void register() {
        //注册目录状态监听器
        try {
            //System.out.println("注册: " + getPath());


            watchKey = getPath().register(fileStateWatcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //继续创建子目录
        try {
            Files.list(getPath()).forEach(path -> {
                addSubItem(path);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public void removeSubItem(String name) {
        getChildren().remove(fileList.remove(name));
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void addSubItem(Path path) {
        FileTreeItem fileTreeItem = new FileTreeItem(path, this, fileStateWatcher, fileTreeView);
        fileList.put(path.getFileName().toString(), fileTreeItem);
        getChildren().add(fileTreeItem);
    }

    public FileTreeItem getSubItem(String name) {
        //System.out.println(fileList);
        return fileList.get(name);
    }

    public FileTreeItem[] getFTIChildren() {
        return getChildren().toArray(new FileTreeItem[0]);
    }

    public FileTreeItem[] getFTIChildrenDir() {
        ArrayList<FileTreeItem> dirs = new ArrayList<>();
        for (FileTreeItem fileTreeItem : getFTIChildren())
            if (Files.isDirectory(fileTreeItem.getPath()))
                dirs.add(fileTreeItem);
        return dirs.toArray(new FileTreeItem[0]);
    }

    public FileTreeItem[] getFTIChildrenFile() {
        ArrayList<FileTreeItem> files = new ArrayList<>();
        for (FileTreeItem fileTreeItem : getFTIChildren())
            if (!Files.isDirectory(fileTreeItem.getPath()))
                files.add(fileTreeItem);
        return files.toArray(new FileTreeItem[0]);
    }

    public FileTreeItem pathFindItem(Path path) {
        //System.out.println("反找");
        //System.out.println(path.toAbsolutePath());
        //System.out.println(getPath().toAbsolutePath());
        if (path.toAbsolutePath().equals(getPath().toAbsolutePath()))
            return this;
        for (FileTreeItem fileTreeItem : getFTIChildren()) {
            FileTreeItem findFileTreeItem = fileTreeItem.pathFindItem(path);
            if (findFileTreeItem != null)
                return findFileTreeItem;
        }
        return null;
    }

    public Path getPath() {
        return getValue().path;
    }

    public FileTreeItem getParentItem() {
        return parent;
    }


}
