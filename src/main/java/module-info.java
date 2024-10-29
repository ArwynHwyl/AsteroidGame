module se233.asteroid {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.apache.logging.log4j;
    requires java.logging;

    exports se233.asteroid;
    exports se233.asteroid.controller;
    exports se233.asteroid.model;


    opens se233.asteroid;
    opens se233.asteroid.controller;
    opens se233.asteroid.model;

}