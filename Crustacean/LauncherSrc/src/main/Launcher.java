package main;

import java.io.File;
import java.io.IOException;

public class Launcher {
    public static void main(String[] args) throws IOException {
        String basePath = System.getProperty("user.dir");
        String fullPath = basePath + "|bin|java.exe".replace('|', File.separatorChar);
        String[] command = new String[] {fullPath, "--enable-preview", "-Xmx2G", "--enable-native-access=crab.jnewton", "main.game.Game"};
        Runtime.getRuntime().exec(command);
    }
}
