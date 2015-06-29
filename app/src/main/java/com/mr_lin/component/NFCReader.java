package com.mr_lin.component;

import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by root on 15-6-21.
 */
public class NFCReader{
    private Intent intent;
    private ArrayList<String> mReadData;

    public NFCReader(Intent intent){
        this.intent = intent;
        this.mReadData = new ArrayList<String>(){};
    }
    public NFCReader(){
        this.mReadData = new ArrayList<String>(){};
    }

    public ArrayList<String> resolveIntent(Intent intent){
        NdefMessage[] messages = null;
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if(rawMessages != null){
            messages = new NdefMessage[rawMessages.length];
            for(int i=0;i<rawMessages.length;i++){
                messages[i] = (NdefMessage) rawMessages[i];
            }
        }else {
            byte[] empty = new byte[]{};
            NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,empty,empty,empty);
            NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
            messages = new NdefMessage[]{msg};

        }
        mReadData.clear();
        processNDEF(messages);
        return this.mReadData;
    }

    private void processNDEF(NdefMessage[] messages){
        if(messages == null||messages.length == 0){
            return;
        }
        for(int i = 0;i<messages.length;i++){
            int lenght = messages[i].getRecords().length;
            NdefRecord[] records = messages[i].getRecords();
            for(NdefRecord record:records){
                if(isText(record)){
                    parseTextRecord(record);
                }
            }
        }

    }

    private boolean isText(NdefRecord record){
        if(record.getTnf() == NdefRecord.TNF_WELL_KNOWN){
            if(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    private void parseTextRecord(NdefRecord record){
        if(record.getTnf() != NdefRecord.TNF_WELL_KNOWN){
            return;
        }
        if(!Arrays.equals(record.getType(),NdefRecord.RTD_TEXT)){
            return;
        }
        String payLoadStr = "";
        byte[] payLoad = record.getPayload();
        byte statusByte = payLoad[0];
        String textEncodeing = "";
        textEncodeing = ((statusByte & 0200) == 0) ? "UTF-8":"UTF-16";
        int languageCodeLength = 0;
        languageCodeLength = statusByte & 0077;
        String languageCode = "";
        languageCode = new String(payLoad,1,languageCodeLength, Charset.forName("UTF-8"));

        try{
            payLoadStr = new String(payLoad,languageCodeLength+1,payLoad.length-languageCodeLength-1,textEncodeing);

        }catch (UnsupportedEncodingException e1){
            e1.printStackTrace();
        }
        mReadData.add(payLoadStr);
    }

}
