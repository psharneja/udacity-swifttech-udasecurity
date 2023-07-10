module com.penguin.security {
    requires transitive com.penguin.image;
    requires transitive java.desktop;
    requires transitive com.google.common;
    requires transitive com.google.gson;
    requires transitive java.prefs;
    requires transitive com.miglayout.swing;
    opens com.penguin.security.service;
    opens com.penguin.security.data to com.google.gson;
}