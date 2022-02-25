module aasim.ris {
    requires javafx.controls;
    requires java.sql;
    requires javafx.graphics;
    opens datastorage;
    exports aasim.ris;
}
