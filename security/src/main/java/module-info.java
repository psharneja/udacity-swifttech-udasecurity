module com.penguin.security {
    requires com.penguin.image;
    requires java.desktop;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    requires com.miglayout.swing;
    exports com.penguin.security.service;
    exports com.penguin.security.data;
    exports com.penguin.security.application;
    opens com.penguin.security.service;
    opens com.penguin.security.data to com.google.gson;
}