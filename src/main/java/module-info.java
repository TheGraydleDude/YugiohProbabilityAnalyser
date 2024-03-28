module com.example.yugiohprobabilityanalyser {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.yugiohprobabilityanalyser to javafx.fxml;
    exports com.example.yugiohprobabilityanalyser;
}