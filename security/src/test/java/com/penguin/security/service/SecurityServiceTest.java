package com.penguin.security.service;

import com.penguin.image.service.FakeImageService;
import com.penguin.security.data.SecurityRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    FakeImageService fakeImageService;

    @Mock
    SecurityRepository securityRepository;

    private SecurityService securityService;





}
