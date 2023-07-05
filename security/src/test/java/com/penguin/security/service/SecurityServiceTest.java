package com.penguin.security.service;

import com.penguin.image.service.ImageService;
import com.penguin.security.application.StatusListener;
import com.penguin.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.lang.reflect.Executable;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    private SecurityService securityService;

    @Mock
    private StatusListener statusListener;

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    ImageService imageService;

    private Sensor sensor;

    private final String uuid = UUID.randomUUID().toString();

    private Sensor getSensor(){
        return new Sensor(uuid, SensorType.DOOR);
    }

    private Set<Sensor> getSensors(int count, boolean status) {
        Set<Sensor> sensors = new HashSet<>();
        for(int i =0; i<count; i++){
            sensors.add(new Sensor(uuid, SensorType.DOOR));
        }
        sensors.forEach(sensor -> sensor.setActive(status));
        return sensors;
    }

    static Stream<Arguments> methodSource(){
        return Stream.of(
                Arguments.of(false, true)
        );
    }


    @BeforeEach
    void run() {
        securityService = new SecurityService(securityRepository, imageService);
        sensor = getSensor();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false})
    void  setAlarmStatus_whenSensorUpdate_NoAlarmChange(boolean status) {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, status);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class, names = {"ALARM", "PENDING_ALARM", "NO_ALARM"})
    void setAlarmStatus_whenSensorDeactivated_AlarmStatus(AlarmStatus alarmStatus){
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);

        sensor.setActive(false);
        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));

    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names= {"ARMED_HOME", "ARMED_AWAY"})
    void setAlarmStatus_whenAlarmActivatedAndSensorActivates_setAlarmStatusPending(ArmingStatus armingStatus){
        when(securityService.getArmingStatus()).thenReturn(armingStatus);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        ArgumentCaptor<AlarmStatus> argumentCaptor = ArgumentCaptor.forClass(AlarmStatus.class);

        verify(securityRepository, atMostOnce()).setAlarmStatus(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue(), AlarmStatus.PENDING_ALARM);
    }

    @MethodSource("methodSource")
    @ParameterizedTest
    void setAlarmStatus_whenSystemDisarmed_setStatusNoChange(Boolean a, Boolean b){
        sensor.setActive(a);

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.changeSensorActivationStatus(sensor, b);

        verify(securityRepository, atMostOnce()).updateSensor(sensor);
    }

    @Test
    void setAlarmStatus_whenSensorActivatedAndSystemArmed_setStatusPending(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void setAlarmStatus_whenSensorPendingAndSystemArmed_setStatusAlarm(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }


    @Test
    void setAlarmStatus_whenSensorNotActiveAndSystemArmed_setSensorDeactivated(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, times(1)).updateSensor(sensor);
    }


    @Test
    void changeSensorActivationStatus_whenAlarmActiveAndArmingDisarmed_setPendingAlarm(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor);

        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void setAlarmStatus_whenSensorInActiveAndAlarmPending_setStatusNoAlarm(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        sensor.setActive(false);
        securityService.changeSensorActivationStatus(sensor);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void setAlarmStatus_whenSensorActiveAndAlarmPending_setStatusAlarm(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }


    @Test
    void setAlarmStatus_whenImageServiceFindsCatAndAlarmArmedHome_setStatusAlarm(){
        BufferedImage image = new BufferedImage(256,256, BufferedImage.TYPE_INT_RGB);

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);

        securityService.processImage(image);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);

    }


    @Test
    void  setAlarmStatus_whenImageServiceFindsNoCat_setStatusNoAlarm() {
        Set<Sensor> sensors = getSensors(4, false);

        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(false);

        securityService.processImage(mock(BufferedImage.class));

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);

    }

    @Test
    void setAlarmStatus_whenSystemDisarmed_setStatusNoAlarm(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

}
