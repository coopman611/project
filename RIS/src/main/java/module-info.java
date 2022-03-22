module aasim.ris {
    requires javafx.controls;
    requires java.sql;
    requires javafx.graphics;
    requires org.apache.commons.io;
    opens datastorage;
    exports aasim.ris;
}
