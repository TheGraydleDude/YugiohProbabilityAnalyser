module com.example.yugiohprobabilityanalyser {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;
    requires java.desktop;
    requires javafx.swing;

    opens com.example.yugiohprobabilityanalyser to javafx.fxml;
    exports com.example.yugiohprobabilityanalyser;
}