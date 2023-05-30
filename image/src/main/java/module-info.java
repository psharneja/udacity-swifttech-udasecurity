module com.penguin.image {
    requires java.desktop;
    requires org.slf4j;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.rekognition;

    exports com.penguin.image.service;
    opens com.penguin.image.service;

}