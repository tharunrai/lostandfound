module com.lostandfound {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires jbcrypt;

    opens com.lostandfound.controllers to javafx.fxml;
    opens com.lostandfound.models to javafx.base;
    
    exports com.lostandfound.main;
    exports com.lostandfound.controllers;
    exports com.lostandfound.models;
}
