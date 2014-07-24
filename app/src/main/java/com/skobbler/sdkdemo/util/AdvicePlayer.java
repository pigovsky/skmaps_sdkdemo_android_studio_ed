package com.skobbler.sdkdemo.util;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.media.AudioManager;
import android.media.MediaPlayer;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.navigation.SKAdvisorSettings;


public class AdvicePlayer {
    
    private static AdvicePlayer instance;
    
    public static AdvicePlayer getInstance() {
        if (instance == null) {
            instance = new AdvicePlayer();
        }
        return instance;
    }
    
    private MediaPlayer player;
    
    private AdvicePlayer() {
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }
    
    public void playAdvice(String[] adviceParts) {
        SKAdvisorSettings advisorSettings = SKMaps.getInstance().getMapInitSettings().getAdvisorSettings();
        String soundFilesDirPath = advisorSettings.getResourcePath() + advisorSettings.getLanguage() + "/sound_files/";
        
        String temporaryFilePath = soundFilesDirPath + "temp.mp3";
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (int i = 0; i < adviceParts.length; i++) {
            String soundFilePath = soundFilesDirPath + adviceParts[i] + ".mp3";
            try {
                InputStream is = new FileInputStream(new File(soundFilePath));
                int availableBytes = is.available();
                byte[] tmp = new byte[availableBytes];
                is.read(tmp, 0, availableBytes);
                if (stream != null) {
                    stream.write(tmp);
                }
                is.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        
        writeFile(stream.toByteArray(), temporaryFilePath);
        
        playFile(temporaryFilePath);
    }
    
    private void writeFile(byte[] data, String filePath) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(new File(filePath));
            out.write(data);
            try {
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    private void playFile(String filePath) {
        try {
            player.reset();
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            try {
                player.setDataSource(fileDescriptor);
            } catch (IllegalStateException ile) {
                player.reset();
                player.setDataSource(fileDescriptor);
            }
            fileInputStream.close();
            
            player.prepare();
            player.start();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
