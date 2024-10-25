module com.se233.asteroid {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;


    opens com.se233.asteroid to javafx.fxml;

}