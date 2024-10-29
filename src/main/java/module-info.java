module com.se233.asteroid {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.apache.logging.log4j;
    requires java.logging;

    opens com.se233.asteroid to javafx.fxml;
    opens com.se233.asteroid.controller to javafx.fxml; // เพิ่ม package ที่ต้องการเปิด
    opens com.se233.asteroid.model to javafx.fxml;      // เพิ่ม package ที่ต้องการเปิด

    exports com.se233.asteroid;
    exports com.se233.asteroid.controller; // เพิ่ม package ที่ต้องการ export
    exports com.se233.asteroid.model;      // เพิ่ม package ที่ต้องการ export
}