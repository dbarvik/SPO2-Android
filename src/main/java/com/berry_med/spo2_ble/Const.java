package com.berry_med.spo2_ble;

import java.util.UUID;

/**
 * Created by ZXX on 2015/8/31.
 */
public class Const {

    public static final UUID  UUID_SERVICE_DATA                 = UUID.fromString("0000f001-0000-1000-8000-00805f9b34fb");
    public static final UUID       UUID_CHARACTER_RECEIVE       = UUID.fromString("0000f101-0000-1000-8000-00805f9b34fb");
    public static final UUID       UUID_MODIFY_BT_NAME          = UUID.fromString("00005343-0000-1000-8000-00805F9B34FB");

    public static final UUID UUID_CLIENT_CHARACTER_CONFIG       = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int  MESSAGE_OXIMETER_PARAMS            = 2003;
    public static final int  MESSAGE_OXIMETER_WAVE              = 2004;

    public static final String GITHUB_SITE                      = "https://github.com/zh2x/SpO2-BLE-for-Android";
}
