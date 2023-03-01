package com.zedo.fxcomponent;

import com.zedo.fxcomponent.components.CustomStage;
import javafx.animation.*;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("DefaultStage.fxml"));
        CustomStage customStage = new CustomStage(null, stage, null);
        customStage.registerDragMove(null);

        customStage.setMinSize(300, 200);
        //customStage.setMaxSize(1200, 800);
        customStage.setSize(600, 400);
        customStage.registerResize();
        customStage.registerLayoutManagement(null);
        customStage.registerControlButton();


        stage.show();


    }

    public static void main(String[] args) {
        launch();
    }
}