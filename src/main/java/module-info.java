module ro.ubbcluj.map {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;

    opens ro.ubbcluj.map to javafx.fxml;
    opens ro.ubbcluj.map.domain to javafx.base;
    opens ro.ubbcluj.map.controller to javafx.fxml;
    exports ro.ubbcluj.map.controller;
    exports ro.ubbcluj.map;
}