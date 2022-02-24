module aasim.ris {
    requires javafx.controls;
    requires java.sql;
    opens datastorage;
    exports aasim.ris;
}
