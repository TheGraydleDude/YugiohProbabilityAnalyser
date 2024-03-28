module com.example.yugiohprobabilityanalyser {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;


    opens com.example.yugiohprobabilityanalyser to javafx.fxml;
    exports com.example.yugiohprobabilityanalyser;
}