package com.library.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class Dialogs {
    private Dialogs() {}
    public static void info(String title, String message) { show(Alert.AlertType.INFORMATION,title,message); }
    public static void error(String title, String message) { show(Alert.AlertType.ERROR,title,message); }
    public static void warning(String title, String message) { show(Alert.AlertType.WARNING,title,message); }
    public static boolean confirm(String title, String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message);
        a.setTitle(title); a.setHeaderText(null);
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
    private static void show(Alert.AlertType type,String title,String message) {
        Alert a = new Alert(type,message); a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }
}
