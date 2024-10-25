module com.se233.asteroid {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.se233.asteroid to javafx.fxml;
    exports com.se233.asteroid;
}