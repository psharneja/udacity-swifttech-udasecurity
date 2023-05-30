package com.penguin.image.service;

import java.awt.image.BufferedImage;

public interface ImageService {
    boolean imageContainCat(BufferedImage image, float confidenceThreshhold);
}
