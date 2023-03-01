module com.zedo.fxcomponent {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    opens com.zedo.fxcomponent to javafx.fxml;
    exports com.zedo.fxcomponent;
    exports com.zedo.fxcomponent.components;
    exports com.zedo.fxcomponent.components.fileTreeView;
    exports com.zedo.fxcomponent.utils;
}