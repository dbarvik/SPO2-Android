package com.berry_med.OximeterData;

import android.bluetooth.BluetoothGattCharacteristic;

import com.berry_med.spo2_ble.BluetoothLeService;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by ZXX on 2015/8/31.
 *
 * Add all data from oximeter into a Queue, and then Parsing the data as the protocol manual.
 * If you want more details about the protocol, click the link below.
 *
 *     https://github.com/zh2x/BCI_Protocol_Demo/tree/master/protocol_manual
 */
public class PackageParser
{
    int[] RedData = new int[1001];
    //int[] IrData = new int[2000];
    int redSum, irSum, redDC,irDC,redAC_sq,irAC_sq,offset,period=0,peak1=0,peak2=0,peak3=0,index1=0,index2=0,index3=0,pr1,pr2;
    double redAC,irAC,spo2=0,pulseRate=0;
    private int i =0;
    private OxiParams mOxiParams;
    private OnDataChangeListener mOnDataChangeListener;


    public PackageParser(OnDataChangeListener onDataChangeListener)
    {
        this.mOnDataChangeListener = onDataChangeListener;

        mOxiParams = new OxiParams();
    }

    public void parse(int[] packageDat) {

        int  pi,red,ir;

        red= 256*256*packageDat[1]+256*packageDat[2]+packageDat[3];
        ir = 256*256*packageDat[4]+256*packageDat[5]+packageDat[6];

        RedData[i] = ir;
        redSum=redSum+red;
        irSum=irSum+ir;
        if(i>20) {
            if (RedData[i - 10] >= RedData[i - 20] && RedData[i - 10] > RedData[i] && peak1 == 0) {
                peak1 = RedData[i - 10];
                index1 = i - 10;
            }
            if (peak1 > 0 && i > (index1 + 25) && peak2 == 0) {
                if (RedData[i - 10] >= RedData[i - 20] && RedData[i - 10] > RedData[i]) {
                    peak2 = RedData[i - 10];
                    index2 = i - 10;
                }
            }
            if (peak2 > 0 && i > (index1 + 25) && peak3 == 0) {
                if (RedData[i - 10] >= RedData[i - 20] && RedData[i - 10] > RedData[i]) {
                    peak3 = RedData[i - 10];
                    index3 = i - 10;
                }
            }
            pr1 = (index2 - index1) * 10;
            pr2 = (index3 - index2) * 10;
            if (pr1 > 0 ) {//&& Math.abs(pr1 - pr2) < 100) {
                pulseRate = 60000 / (pr1 + pr2);
            }
        }
        if (i>200)
        {
            redDC=redSum/i;
            irDC=irSum/i;
            offset=irDC+25000;
            redAC_sq+=Math.pow((red-redDC),2);
            irAC_sq+=Math.pow((ir-irDC),2);
            redAC=Math.sqrt(redAC_sq/(i-200));
            irAC=Math.sqrt(irAC_sq/(i-200));

            if(irAC!=0 || redDC!=0)
            {
                //spo2 =110-25*((redAC * irDC) / (irAC * redDC));
                spo2=106.5-4.1*((redAC * irDC) / (irAC * redDC))-17*Math.pow(((redAC * irDC) / (irAC * redDC)),2);//101.5-4.15-17.69
            }
            pi        = packageDat[0] & 0x0f;

            if(spo2 != mOxiParams.spo2 || (int)pulseRate != mOxiParams.pulseRate || pi != mOxiParams.pi)
            {
                mOxiParams.update((int)spo2,(int)pulseRate,pi);
                mOnDataChangeListener.onSpO2ParamsChanged();
            }
        }

        mOnDataChangeListener.onSpO2WaveChanged(-ir+offset);
        if(i<1000)
        {
            i++;
        }
        else
        {
            i=0;
            redAC=0;
            redSum=0;
            redAC_sq=0;
            irAC=0;
            irSum=0;
            irAC_sq=0;
            RedData=new int[1001];
        }
    }

    /**
     * interface for parameters changed.
     */
    public interface OnDataChangeListener
    {
        void onSpO2ParamsChanged();
        void onSpO2WaveChanged(int wave);
    }


    /**
     * a small collection of Oximeter parameters.
     * you can add more parameters as the manual.
     *
     * spo2          Pulse Oxygen Saturation
     * pulseRate     pulse rate
     * pi            perfusion index
     *
     */
    public class OxiParams
    {
        private int spo2;
        private int pulseRate;
        private int pi;             //perfusion index

        private void update(int spo2, int pulseRate, int pi) {
            this.spo2 = spo2;
            this.pulseRate = pulseRate;
            this.pi = pi;
        }

        public int getSpo2() {
            return spo2;
        }

        public int getPulseRate() {
            return pulseRate;
        }

        public int getPi() {
            return pi;
        }
    }

    public OxiParams getOxiParams()
    {
        return mOxiParams;
    }

    /**
     *
     * Modify the Bluetooth Name On the Air.
     *
     * @param service service of BluetoothLeService
     *
     * @param ch      characteristic of Modify Bluetooth Name
     *                if this characteristic not found, the function
     *                of modify not support.
     *
     * @param btName  length of btName should not more than 26 bytes.
     *                the bytes more then 26 bytes will be ignored.
     */
    public static void modifyBluetoothName(BluetoothLeService service,
                                           BluetoothGattCharacteristic ch,
                                           String                      btName)
    {
        if(service == null || ch == null)
            return;

        byte[] b = btName.getBytes();
        byte[] bytes = new byte[b.length+2];
        bytes[0] = 0x00;
        bytes[1] = (byte) b.length;
        System.arraycopy(b,0,bytes,2,b.length);

        service.write(ch,bytes);
    }
}
