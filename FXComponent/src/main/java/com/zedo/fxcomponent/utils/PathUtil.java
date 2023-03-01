package com.zedo.fxcomponent.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class PathUtil {

    /**
     * 更好的文件或目录移动
     *
     * @param source 源文件或目录 可以是文件也可以是目录
     * @param target 目标文件或目录 必须和上面对应
     * @throws IOException
     */
    public static void move(Path source, Path target) throws IOException {

        //创建文件夹
        if (Files.isDirectory(source)) {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (source.getNameCount() == dir.getNameCount()) {
                        Files.createDirectories(target);
                        //System.out.println("正在构建目录" + target);

                    }
                    else {
                        Files.createDirectories(target.resolve(dir.subpath(source.getNameCount(), dir.getNameCount())));
                        //System.out.println("正在构建目录" + target.resolve(dir.subpath(source.getNameCount(), dir.getNameCount())));
                    }
                    return FileVisitResult.CONTINUE;
                }

                /*@Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (source.getNameCount() == dir.getNameCount())
                        Files.createDirectories(target);
                    else
                        Files.createDirectories(target.resolve(dir.subpath(source.getNameCount(), dir.getNameCount())));
                    return FileVisitResult.CONTINUE;
                }*/
            });

            //System.out.println("正在移动文件");
            //移动文件
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.move(file, target.resolve(file.subpath(source.getNameCount(), file.getNameCount())));
                    return FileVisitResult.CONTINUE;
                }
            });

            //删除源目录
            PathUtil.delete(source);

        } else {
            Files.createDirectories(target.getParent());
            Files.move(source, target);
        }


    }

    /**
     * 更好的删除文件或目录
     *
     * @param path 要删除的文件或目录
     */
    public static void delete(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }


}
