module com.penguin.image {
    requires java.desktop;
    requires org.slf4j;
    requires transitive software.amazon.awssdk.auth;
    requires transitive software.amazon.awssdk.core;
    requires transitive software.amazon.awssdk.regions;
    requires transitive software.amazon.awssdk.services.rekognition;
    opens com.penguin.image.service;
    exports com.penguin.image.service;
}