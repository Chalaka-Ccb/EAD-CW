module com.example.starkindustry {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.formdev.flatlaf;
    requires java.desktop;
    requires org.jfree.jfreechart;


    opens com.example.starkindustry to javafx.fxml;
    exports com.example.starkindustry;
}